package it.sky.keplero.DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OAuthResponse {
    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("instance_url")
    private String instanceUrl;

    @JsonProperty("id")
    private String id;

    @JsonProperty("token_type")
    private String tokenType;

    @JsonProperty("issued_at")
    private long issuedAt;

    @JsonProperty("scope")
    private String scope;

    @JsonProperty("signature")
    private String signature;

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void setInstanceUrl(String instanceUrl) {
        this.instanceUrl = instanceUrl;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public void setIssuedAt(long issuedAt) {
        this.issuedAt = issuedAt;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getInstanceUrl() {
        return instanceUrl;
    }

    public String getId() {
        return id;
    }

    public String getTokenType() {
        return tokenType;
    }

    public long getIssuedAt() {
        return issuedAt;
    }

    public String getScope() {
        return scope;
    }

    public String getSignature() {
        return signature;
    }
}
