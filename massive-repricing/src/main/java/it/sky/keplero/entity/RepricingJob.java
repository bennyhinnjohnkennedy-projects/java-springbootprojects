package it.sky.keplero.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "repricing_job", schema = "keplero")
public class RepricingJob {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String transactionId;
    private String inputFileName;
    private String jobStatus;
    private Instant startTime;
    private Instant endTime;
    private int totalRows = 0;
    private int processed = 0;
    private int succeeded = 0;
    private int failed = 0;

    public RepricingJob(String inputFileName, String jobStatus, Instant startTime, int totalRows) {
        this.transactionId = UUID.randomUUID().toString();
        this.inputFileName = inputFileName;
        this.jobStatus = jobStatus;
        this.startTime = startTime;
        this.totalRows = totalRows;
    }

    public RepricingJob(int id, int totalRows, int processed, int succeeded, int failed) {
        this.id = id;
        this.processed = processed;
        this.succeeded = succeeded;
        this.failed = failed;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public void setInputFileName(String inputFileName) {
        this.inputFileName = inputFileName;
    }

    public void setProcessed(int processed) {
        this.processed = processed;
    }

    public void setJobStatus(String jobStatus) {
        this.jobStatus = jobStatus;
    }

    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(Instant endTime) {
        this.endTime = endTime;
    }

    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
    }

    public void setSucceeded(int succeeded) {
        this.succeeded = succeeded;
    }

    public void setFailed(int failed) {
        this.failed = failed;
    }

    public int getId() {
        return id;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getInputFileName() {
        return inputFileName;
    }

    public String getJobStatus() {
        return jobStatus;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public int getProcessed() {
        return processed;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public int getTotalRows() {
        return totalRows;
    }

    public int getSucceeded() {
        return succeeded;
    }

    public int getFailed() {
        return failed;
    }
}
