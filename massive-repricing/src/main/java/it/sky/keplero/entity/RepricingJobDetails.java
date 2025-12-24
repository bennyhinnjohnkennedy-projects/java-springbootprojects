package it.sky.keplero.entity;

import jakarta.persistence.*;

import java.sql.Timestamp;
import java.time.Instant;

@Entity
@Table(name = "repricing_job_details", schema = "keplero")
public class RepricingJobDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String fileName;
    private int repricingJobId;
    private String contractId;
    private String processName;
    private String status;
    private String stackTrace;
    private Instant startTime;
    private Instant endTime;

    public RepricingJobDetails(String fileName, int repricingJobId, String contractId, String processName, String status, Instant startTime, Instant endTime, String stackTrace) {
        this.fileName = fileName;
        this.repricingJobId = repricingJobId;
        this.contractId = contractId;
        this.processName = processName;
        this.status = status;
        this.startTime = startTime;
        this.endTime = endTime;
        this.stackTrace = stackTrace;
    }

    public int getRepricingJobId() {
        return repricingJobId;
    }

    public void setRepricingJobId(int repricingJobId) {
        this.repricingJobId = repricingJobId;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setContractId(String contractId) {
        this.contractId = contractId;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(Instant endTime) {
        this.endTime = endTime;
    }

    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }

    public int getId() {
        return id;
    }

    public String getFileName() {
        return fileName;
    }

    public String getContractId() {
        return contractId;
    }

    public String getProcessName() {
        return processName;
    }

    public String getStatus() {
        return status;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public String getStackTrace() {
        return stackTrace;
    }
}
