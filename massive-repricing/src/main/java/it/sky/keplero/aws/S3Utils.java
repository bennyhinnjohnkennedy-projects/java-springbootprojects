package it.sky.keplero.aws;

import it.sky.keplero.exception.RepricingGenericException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class S3Utils {

    private static final Logger log = LoggerFactory.getLogger(S3Utils.class);
    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.s3.input-file-prefix}")
    private String inputFileFolder;

    @Value("${aws.s3.region}")
    private String region;

    @Value("${aws.s3.processed-file-prefix}")
    private String processedFile;

    @Value("${aws.s3.error-file-prefix}")
    private String errorFile;

    ConcurrentHashMap<String, String> templateRepo = new ConcurrentHashMap<>();

    public S3Utils(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public String fetchCSVFile(String fileName) {
        String key = inputFileFolder + "/" + fileName;

        try {
            // Create GetObjectRequest
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            // Fetch the object file from S3
            ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(getObjectRequest);

            return objectBytes.asUtf8String();

        } catch (SdkClientException e) {
            log.error("Error in fetching file from S3 bucket:: {}", e.getMessage(), e);
            throw new RepricingGenericException("Error in fetching file from S3 bucket:: " + e.getMessage());
        } catch (NoSuchKeyException e) {
            log.error("The specified key {} does not exist: {}", key, e.getMessage(), e);
            throw new RepricingGenericException("The specified key " + key + " does not exist: " + e.getMessage());
        }
    }

    public void uploadFileToS3(String path, File file) {
        String s3Key;
        try {
            String fileName = file.getName();
            if(path.equalsIgnoreCase("error")) {
                s3Key = errorFile + "/" + fileName;
            } else {
                s3Key = processedFile + "/" + fileName;
            }
            log.info("s3 key:: {}", s3Key);

            // Create PutObjectRequest
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            // Upload the file to S3
            PutObjectResponse response = s3Client.putObject(putObjectRequest, file.toPath());

            if (response.sdkHttpResponse().isSuccessful()) {
                log.info("File uploaded to S3: {}/{}", bucketName, s3Key);
            } else {
                log.info("S3 upload failed with statusCode: {} and statusText: {} for file: {}",
                        response.sdkHttpResponse().statusCode(), response.sdkHttpResponse().statusText(), fileName);
                throw new RepricingGenericException(
                        "S3 upload failed with statusCode: " + response.sdkHttpResponse().statusCode()
                                + " and statusText: " + response.sdkHttpResponse().statusText());
            }

        } catch (SdkClientException e) {
            log.error("Error while uploading to S3: {}", e.getMessage(), e);
            throw new RepricingGenericException("Error uploading file to S3::" + e.getMessage(), e);
        }
    }

    public void deleteFileFromS3(String fileName) {
        try {
            String key = inputFileFolder + "/" + fileName;

            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            log.info("File deleted from S3: {}/{}", bucketName, key);

        } catch (SdkClientException e) {
            log.error("Error in deleting file from S3 bucket:: {}", e.getMessage(), e);
            throw new RepricingGenericException("Error in deleting file from S3 bucket:: " + e.getMessage());
        }
    }
}
