package it.sky.keplero.service;

import it.sky.keplero.aws.S3Utils;
import it.sky.keplero.config.ConfigUtils;
import it.sky.keplero.repository.RepricingJobRepository;

import java.util.*;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ContractProcessingService {
	private static final org.slf4j.Logger log = LoggerFactory.getLogger(ContractProcessingService.class);

    private final CleanupAndRepriceService cleanupAndRepriceService;
    private final RepricingJobRepository repricingJobRepository;
    private final S3Utils s3Utils;
    private final ConfigUtils configUtils;

    public ContractProcessingService(CleanupAndRepriceService cleanupAndRepriceService,
                                     S3Utils s3Utils,
                                     RepricingJobRepository repricingJobRepository,
                                     ConfigUtils configUtils) {
        this.cleanupAndRepriceService = cleanupAndRepriceService;
        this.s3Utils = s3Utils;
        this.repricingJobRepository = repricingJobRepository;
        this.configUtils = configUtils;
    }

    public void processContracts() {
        Map<Integer, String> fileList = new LinkedHashMap<>();
        // Get the list of files to be processed
        getFileToBeProcessed(fileList);

        // Iterate until the list is empty and also check if new files are added
        while(!fileList.isEmpty()) {
            for (Map.Entry<Integer, String> entry : fileList.entrySet()) {
                cleanupAndRepriceService.processStartTransaction(entry.getKey(), "STARTED");
                String fileName = entry.getValue();
                try {
                    // Get value from S3 bucket
                    String contractIds = this.s3Utils.fetchCSVFile(fileName);
//                    String contractIdList = this.s3Utils.getInputString();

                    // Parse the CSV file and get the contracts
                    List<String> extractedContractIds = parseCsvString(contractIds, true);

                    // Perform cleanup and reprice
                    cleanupAndRepriceService.cleanUpAndReprice(extractedContractIds, fileName, entry.getKey())
                            .doOnError((error) -> {
                                log.error("Error while processing the file: {}", fileName, error);
                            })
                            .blockLast();
                } catch (Exception e) {
                    log.error("Error while processing file: {}", fileName, e);
                    // Added to prevent multiple fetches of the same file in case of any error
                    cleanupAndRepriceService.processStatusTransaction(entry.getKey(), "ERROR - " + e.getMessage().substring(0, 40) + "..");
                }
            }
            fileList.clear();

            // check again if any new file is added to be processed
            getFileToBeProcessed(fileList);
        }
    }

    public void getFileToBeProcessed(Map<Integer, String> fileList) {
        // Get the file from database
        this.repricingJobRepository.findByJobStatusOrderByStartTime("UPLOADED").forEach((job) -> {
            fileList.put((Integer) job[0], String.valueOf(job[1]));
        });
        log.info("List of files to be processed from database: {}", fileList);
    }

    public List<String> parseCsvString(String contractIds, boolean ignoreFirstElement) {
        String delimiter = this.configUtils.getConfigValue("csv.delimiter") != "" ? this.configUtils.getConfigValue("csv.delimiter") : "\n";
        return Arrays.stream(contractIds.split(delimiter))
                     .map(String::trim)
                     .filter(value -> !value.isEmpty())
                     .skip(ignoreFirstElement ? 1 : 0)
                     .toList();
    }
}

