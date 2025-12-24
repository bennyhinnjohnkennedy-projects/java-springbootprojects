package it.sky.keplero.DTO;

public class CleanupRequest {
    public String includeGetAsset = "false";
    private String contractId;
    public String onlyCleanUpAsset = "true";
    public String Reason = "UPDATE_CONTRACT_OUTCOME";

    public CleanupRequest(String contractId) {
        this.contractId = contractId;
    }

    public String getContractId() { return contractId; }

    @Override
    public String toString() {
        return "CleanupRequest{" +
                "includeGetAsset='" + includeGetAsset + '\'' +
                ", contractId='" + contractId + '\'' +
                ", onlyCleanUpAsset='" + onlyCleanUpAsset + '\'' +
                ", Reason='" + Reason + '\'' +
                '}';
    }
}
