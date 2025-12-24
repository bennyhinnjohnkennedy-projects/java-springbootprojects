package it.sky.keplero.service;

import com.fasterxml.jackson.databind.JsonNode;
import it.sky.keplero.DTO.OAuthResponse;
import it.sky.keplero.aws.SecretManagerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class OauthServiceWebClient {

    private static final Logger log = LoggerFactory.getLogger(OauthServiceWebClient.class);

    private final WebClient webClient;
    private final String clientId;
    private final String clientSecret;
    private final String tokenUrl;
    private final String username;
    private final String password;

    // Cached token
    private volatile OAuthResponse cachedToken;
    private Mono<OAuthResponse> ongoingFetchMono;

    public OauthServiceWebClient(WebClient webClient, SecretManagerUtils secretManagerUtils) {
        this.webClient = webClient;
        JsonNode secretJson = secretManagerUtils.getSecret();
        this.clientId = secretJson.get("salesforce.oauth2.client-id").asText();
        this.clientSecret = secretJson.get("salesforce.oauth2.client-secret").asText();
        this.username = secretJson.get("salesforce.oauth2.username").asText();
        this.password = secretJson.get("salesforce.oauth2.password").asText();
        this.tokenUrl = secretJson.get("salesforce.oauth2.token-url").asText();
    }

    public Mono<OAuthResponse> getToken() {

        if (cachedToken != null) {
            return Mono.just(cachedToken);
        }

        if (ongoingFetchMono != null) {
            return ongoingFetchMono;
        }

        synchronized (this) {
            if (cachedToken != null) {
                return Mono.just(cachedToken);
            }

            if (ongoingFetchMono != null) {
                return ongoingFetchMono;
            }

            // Fetch new token
            ongoingFetchMono = webClient.post()
                    .uri(tokenUrl)
                    .body(BodyInserters.fromFormData("grant_type", "password")
                            .with("username", username)
                            .with("password", password)
                            .with("client_id", clientId)
                            .with("client_secret", clientSecret))
                    .headers(headers -> headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED))
                    .retrieve()
                    .bodyToMono(OAuthResponse.class)
                    .doOnNext(token -> {
                        log.info("Access token fetched successfully");
                        cachedToken = token;
                    })
                    .doOnError(error -> log.error("Error fetching access token: {}", error.getMessage()))
                    .doFinally(sig -> ongoingFetchMono = null)
                    .cache();

            return ongoingFetchMono;
        }
    }

    public Mono<OAuthResponse> refreshToken() {
        synchronized(this) {
            cachedToken = null;
            ongoingFetchMono = null;
        }

        return getToken();
    }

    public void clearToken() {
        synchronized (this) {
            cachedToken = null;
            ongoingFetchMono = null;
        }
    }
}
