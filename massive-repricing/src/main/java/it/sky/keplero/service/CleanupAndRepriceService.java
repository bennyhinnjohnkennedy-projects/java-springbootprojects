package it.sky.keplero.service;
import it.sky.keplero.DTO.CleanupRequest;
import it.sky.keplero.DTO.OAuthResponse;
import it.sky.keplero.DTO.RepriceRequest;
import it.sky.keplero.aws.S3Utils;
import it.sky.keplero.config.ConfigUtils;
import it.sky.keplero.entity.RepricingJobDetails;
import it.sky.keplero.repository.RepricingJobDetailsRepository;
import it.sky.keplero.repository.RepricingJobRepository;
import it.sky.keplero.utils.JobMetrics;
import it.sky.keplero.utils.TimeUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
public class CleanupAndRepriceService {

    private static final Logger log = LoggerFactory.getLogger(CleanupAndRepriceService.class);

    private final Queue<RepricingJobDetails> jobDetailsBatch = new ConcurrentLinkedQueue<>();
    private final WebClient webClient;
    private final OauthServiceWebClient oauthService;
    private final RepricingJobDetailsRepository repricingJobDetailsRepository;
    private final RepricingJobRepository repricingJobRepository;
    private final TransactionalService transactionalService;
    private final ObjectMapper objectMapper;
    private final S3Utils s3Utils;
    private final JobMetrics jobMetrics = new JobMetrics();
    private final List<String[]> errorResponses = new ArrayList<>();
    private final ConfigUtils configUtils;
    private String salesforceCleanupUrl;
    private String salesforceRepriceUrl;


    public CleanupAndRepriceService(WebClient webClient, OauthServiceWebClient oauthService,
                                    RepricingJobDetailsRepository repricingJobDetailsRepository, RepricingJobRepository repricingJobRepository,
                                    TransactionalService transactionalService, ConfigUtils configUtils, S3Utils s3Utils) {
        this.webClient = webClient;
        this.oauthService = oauthService;
        this.transactionalService = transactionalService;
        this.configUtils = configUtils;
        this.repricingJobDetailsRepository = repricingJobDetailsRepository;
        this.repricingJobRepository = repricingJobRepository;
        this.objectMapper = new ObjectMapper();
        this.s3Utils = s3Utils;
        loadConfig();
    }

    private void loadConfig() {
        String BASE_URL = this.configUtils.getConfigValue("repricing.salesforce.base-url");
        this.salesforceCleanupUrl = BASE_URL + this.configUtils.getConfigValue("repricing.salesforce.cleanup-url");
        this.salesforceRepriceUrl = BASE_URL + this.configUtils.getConfigValue("repricing.salesforce.reprice-url");
    }

    public Flux<List<String>> cleanUpAndReprice(List<String> result, String fileName, Integer repricingJobId) {
        int batchSize = Integer.parseInt(this.configUtils.getConfigValue("repricing.batch-size"));
        Instant startTime = TimeUtils.getInstant();

        log.info("Starting the processing for the file: {}", fileName);
        Flux<List<String>> response = Flux.fromIterable(result)
                .flatMap(contractId -> {
                    CleanupRequest cleanupRequest = new CleanupRequest(contractId);
                    Instant cleanupStartTime = TimeUtils.getInstant();

                    return invokeWithToken(oauthService, this.salesforceCleanupUrl, cleanupRequest, "Cleanup")
                            .flatMap(res -> {
                                boolean isCleanupSuccess = processCleanupResponse(res, contractId, fileName, repricingJobId, cleanupStartTime);

                                if (isCleanupSuccess) {
                                    RepriceRequest repriceRequest = new RepriceRequest(contractId);
                                    Instant repriceStartTime = TimeUtils.getInstant();

                                    return invokeWithToken(oauthService, this.salesforceRepriceUrl, repriceRequest, "Reprice")
                                            .flatMap(repriceResponse -> {
                                                processRepriceResponse(repriceResponse, contractId, fileName, repricingJobId, repriceStartTime);
                                                return Mono.just(res);
                                            })
                                            .onErrorResume(err -> {
                                                String errorMessage = (err instanceof WebClientResponseException) ?
                                                        ((WebClientResponseException) err).getResponseBodyAsString() : err.getMessage();
                                                log.error("Error during reprice for contractId: {} and error response: {}", contractId, errorMessage, err);
                                                handleError("Reprice", contractId, fileName, repricingJobId, repriceStartTime, jobDetailsBatch, errorMessage + "|" + err.getMessage());
                                                return Mono.just(errorMessage);
                                            });
                                }
                                else {
                                    return Mono.just("isCleanupSuccess flag is false and hence skipping reprice step " + contractId);
                                }
                            })
                            .onErrorResume(WebClientResponseException.BadRequest.class, e -> {
                                log.error("400 Error during token / cleanup for contractId: {}, response: {}", contractId, e.getResponseBodyAsString(), e);
                                handleError("Cleanup", contractId, fileName, repricingJobId, cleanupStartTime, jobDetailsBatch, e.getMessage());
                                return Mono.just(e.getMessage());
                            })
                            .onErrorResume(error -> {
                                String errorMessage = (error instanceof WebClientResponseException) ?
                                        ((WebClientResponseException) error).getResponseBodyAsString() : error.getMessage();
                                log.error("Error during token / cleanup for contractId: {} and error response: {}", contractId, errorMessage, error);
                                handleError("Cleanup", contractId, fileName, repricingJobId, cleanupStartTime, jobDetailsBatch, errorMessage + "|" + error.getMessage());
                                return Mono.just(errorMessage);
                            });
                }, batchSize)
                .doOnNext((element) -> jobMetrics.incrementProcessed())
                .buffer(batchSize)
                .concatMap(batch -> processTransaction(batch, repricingJobId))
                .doOnError(err -> log.error("Batch transaction failed: {}", err.getMessage(), err))
                .doFinally(signal -> { finalizeProcessing(fileName, repricingJobId, startTime, result); });

            return response;
    }

    private Mono<String> invokeWithToken(OauthServiceWebClient oauthServiceWebClient,
                                         String url,
                                         Object body,
                                         String actionName) {
        return oauthServiceWebClient.getToken()
                .flatMap(token ->
                        invokeApexEndpoint(token, url, body, actionName)
                                .onErrorResume(WebClientResponseException.Unauthorized.class, e -> {
                                    String response = e.getResponseBodyAsString();
                                    if (response != null && response.contains("INVALID_SESSION_ID")) {
                                        log.warn("Token expired. Refreshing...");
                                        return oauthService.refreshToken()
                                                .flatMap(newToken -> invokeApexEndpoint(newToken, url, body, actionName));
                                    }
                                    return Mono.error(e);
                                })
                );
    }

    private Mono<String> invokeApexEndpoint(OAuthResponse token, String url, Object body, String actionName) {
        return webClient.post()
                .uri(url)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .doOnSubscribe(sub -> log.info("{} request sent to Salesforce: {}", actionName, body));
//                .doOnNext(res -> log.info("{} response: {}", actionName, res))
//                .doOnError(err -> log.error("Error during {} call to Salesforce", actionName, err));
    }

    private Mono<List<String>> processTransaction(List<String> list, Integer repricingJobId) {
        return transactionalService.executeTransaction(() -> {
            log.info("Commiting to the database..");
            if (!jobDetailsBatch.isEmpty()) {
                repricingJobDetailsRepository.saveAll(jobDetailsBatch);
                jobDetailsBatch.clear();
            }
            repricingJobRepository.updateJobDetails(repricingJobId, jobMetrics.getProcessed(), jobMetrics.getSucceeded(), jobMetrics.getFailed(), "IN_PROGRESS");

            return Mono.just(list);
        });
    }


    private boolean processCleanupResponse(String res, String contractId, String fileName,
                                        Integer repricingJobId, Instant startTime) {
        boolean isSuccess = false;

        try {
            JsonNode jsonNode = this.objectMapper.readTree(res);
            if ("GetAsset Ext Success - Only AssetEnrichment".equalsIgnoreCase(jsonNode.get("Description").asText())
                    && "0".equals(jsonNode.get("Outcome").asText())) {
                handleSuccess("Cleanup", contractId, fileName, repricingJobId, startTime, jobDetailsBatch, res);
                isSuccess = true;
            } else {
                handleError("Cleanup", contractId, fileName, repricingJobId, startTime, jobDetailsBatch, res);
            }
        } catch (JsonProcessingException e) {
            log.info("Error while parsing cleanup response for contractId={}", contractId, e);
            handleError("Cleanup", contractId, fileName, repricingJobId, startTime, jobDetailsBatch, e.getMessage());
        }
        return isSuccess;
    }

    private void processRepriceResponse(String repriceResponse, String contractId, String fileName,
                                        Integer repricingJobId, Instant startTime) {
        try {
            JsonNode jsonNode = this.objectMapper.readTree(repriceResponse);
            if ("Repricing Success - Contract".equalsIgnoreCase(jsonNode.get("Description").asText())
                    && "0".equals(jsonNode.get("Outcome").asText())) {
                handleSuccess("Reprice", contractId, fileName, repricingJobId, startTime, jobDetailsBatch, repriceResponse);
            } else {
                handleError("Reprice", contractId, fileName, repricingJobId, startTime, jobDetailsBatch, repriceResponse);
            }
        } catch (JsonProcessingException e) {
            log.info("Error while parsing reprice response for contractId={}", contractId, e);
            handleError("Reprice", contractId, fileName, repricingJobId, startTime, jobDetailsBatch, e.getMessage());
        }

    }

    private void handleError(String action, String contractId, String fileName, Integer repricingJobId,
                                   Instant startTime, Queue<RepricingJobDetails> jobDetailsBatch, String errorMessage) {
        this.jobMetrics.incrementFailed();
        jobDetailsBatch.add(new RepricingJobDetails(
                fileName, repricingJobId, contractId, action, "ERROR", startTime, TimeUtils.getInstant(), errorMessage
        ));
        errorResponses.add(new String[]{contractId, "\"" + errorMessage.replace("\"", "\"\"") + "\"", action, TimeUtils.getInstant().toString()});
    }

    private void handleSuccess(String action, String contractId, String fileName, Integer repricingJobId,
                                   Instant startTime, Queue<RepricingJobDetails> jobDetailsBatch, String errorMessage) {
        if(action.equals("Reprice")) { this.jobMetrics.incrementSucceeded(); }
        jobDetailsBatch.add(new RepricingJobDetails(
                fileName, repricingJobId, contractId, action, "SUCCESS", startTime, TimeUtils.getInstant(), errorMessage
        ));
    }

    private void finalizeProcessing(String fileName, Integer repricingJobId, Instant startTime, List<String> inputs) {

        // Determine final status based on failures
        String finalStatus = jobMetrics.getFailed() > 0 ? "COMPLETED_WITH_ERRORS" : "COMPLETED";

        // Clear the token after completion of file being processed
        this.oauthService.clearToken();

        // Update the job details as completed
        processStatusTransaction(repricingJobId, finalStatus);

        // Move the processed file to processed folder in S3 bucket
        moveFileToProcessedFolder(fileName, inputs);

        // Generate the error CSV file in S3 bucket
        generateErrorReport(fileName, errorResponses);

        log.info("File {} processing finished. processed={}, succeeded={}, failed={}, status={}, duration={}s",
                fileName,
                jobMetrics.getProcessed(),
                jobMetrics.getSucceeded(),
                jobMetrics.getFailed(),
                finalStatus,
                TimeUtils.getInstant().getEpochSecond() - startTime.getEpochSecond()
        );

        this.jobMetrics.reset();
    }

    public void processStatusTransaction(int repricingJobId, String status) {
        transactionalService.executeTransaction(() -> {
            this.repricingJobRepository.updateEndJobStatus(repricingJobId, status, TimeUtils.getInstant());
            return repricingJobId;
        });
    }

    public void processStartTransaction(int repricingJobId, String status) {
        transactionalService.executeTransaction(() -> {
            this.repricingJobRepository.updateStartJobStatus(repricingJobId, status, TimeUtils.getInstant());
            return repricingJobId;
        });
    }

    private void moveFileToProcessedFolder(String fileName, List<String> inputs) {
        File processedFile = new File("/tmp/"+fileName.replace(".csv", "") + "_processed.csv");
        try (FileWriter csvWriter = new FileWriter(processedFile)) {
            writeToFile("contractid\n", csvWriter, inputs);
            this.s3Utils.uploadFileToS3("processed", processedFile);
        } catch (IOException e) {
            log.error("Error writing processed file {}: {}", processedFile.getName(), e.getMessage(), e);
        } finally {
            if (processedFile.exists() && !processedFile.delete()) {
                log.warn("Unable to delete temp file {}", processedFile.getName());
            }
        }

        // Delete the original file from S3 bucket
        try {
            this.s3Utils.deleteFileFromS3(fileName);
        } catch (Exception e) {
            log.warn("Failed to delete original S3 object {}: {}", fileName, e.getMessage());
        }

    }

    private void generateErrorReport(String fileName, List<String[]> errorResponses) {
        File errorFile = new File("/tmp/" + fileName.replace(".csv", "") + "_failed.csv");
        if (!errorResponses.isEmpty()) {
            try (FileWriter csvWriter = new FileWriter(errorFile)) {
                String headers = "ContractId,Response,Action,Timestamp\n";
                List<String> errorRows = errorResponses.stream()
                        .map(arr -> String.join(",", arr))
                        .toList();
                writeToFile(headers, csvWriter, errorRows);
                this.s3Utils.uploadFileToS3("error", errorFile);
            } catch (IOException e) {
                log.error("Error writing error responses to CSV: {}", e.getMessage(), e);
            } finally {
                if (errorFile.exists() && !errorFile.delete()) {
                    log.warn("Unable to delete temp file: {}", errorFile.getName());
                }
                errorResponses.clear();
            }
        }
    }

    private void writeToFile(String headers, FileWriter csvWriter, List<String> rows) throws IOException {
        csvWriter.append(headers);
        for (String element : rows) {
            csvWriter.append(element).append("\n");
        }
        csvWriter.flush();
    }
}
