package it.sky.keplero.aws;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

import java.util.concurrent.atomic.AtomicReference;

@Service
public class SecretManagerUtils {
    private static final Logger log = LoggerFactory.getLogger(SecretManagerUtils.class);

    private final SecretsManagerClient secretsManagerClient;
    private final ObjectMapper objectMapper;
    AtomicReference<JsonNode> cache = new AtomicReference<>(null);

    private final String secretName;

    public SecretManagerUtils(SecretsManagerClient secretsManagerClient,
                              @Value("${aws.secrets-manager.name}") String secretName) {
        this.secretsManagerClient=secretsManagerClient;
        this.objectMapper = new ObjectMapper();
        this.secretName = secretName;
    }

    public JsonNode getSecret() {
        // Checks if the secrets are cached
        JsonNode cachedSecret = cache.get();
        if(cachedSecret != null) {
            return cachedSecret;
        }

        // If not cached, get the secrets from Secrets Manager
        try {
            GetSecretValueRequest valueRequest = GetSecretValueRequest.builder()
                    .secretId(secretName)
                    .build();

            //Get all the keys and values present in the secrets as JSON
            log.info("Fetching from the secrets manager...");
            GetSecretValueResponse valueResponse = secretsManagerClient.getSecretValue(valueRequest);

            String secretJson = valueResponse.secretString();
            JsonNode secret = objectMapper.readTree(secretJson);
            cache.set(secret);
            return secret;
        } catch (Exception e) {
            log.error("Failed to retrieve the secrets from AWS Secrets Manager: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve and parse secret from AWS Secrets Manager", e);
        }
    }
}
