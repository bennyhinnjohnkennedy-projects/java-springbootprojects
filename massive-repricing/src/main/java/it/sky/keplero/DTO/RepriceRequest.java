package it.sky.keplero.DTO;

import java.util.Arrays;

public class RepriceRequest {
    public String[] idsList;
    public String Reason = "UPDATE_CONTRACT_OUTCOME";

    public RepriceRequest(String contractId) {
        this.idsList = new String[] {contractId};
    }

    public String[] getIdsList() { return idsList; }

    @Override
    public String toString() {
        return "RepriceRequest{" +
                "idsList=" + Arrays.toString(idsList) +
                ", Reason='" + Reason + '\'' +
                '}';
    }
}
