package com.fraudengine.concurrency;

import java.text.NumberFormat;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Domain model representing the screening decision and worker thread execution
 * telemetry for a single transaction processed within a multithreaded batch.
 */
public class BulkScreeningItemResult {

    private final String txId;
    private final String userId;
    private final double amount;
    private final String formattedAmount;
    private final String location;
    private final String status;
    private final int riskScore;
    private final List<String> violations;
    private final String breachReason;
    private final String processedByThread;
    private final long durationMs;

    public BulkScreeningItemResult(String txId, String userId, double amount, String location,
                                   String status, int riskScore, List<String> violations,
                                   String breachReason, String processedByThread, long durationMs) {
        this.txId = txId;
        this.userId = userId;
        this.amount = amount;
        this.formattedAmount = NumberFormat.getCurrencyInstance(Locale.US).format(amount);
        this.location = location;
        this.status = status;
        this.riskScore = riskScore;
        this.violations = violations != null ? violations : Collections.emptyList();
        this.breachReason = breachReason;
        this.processedByThread = processedByThread;
        this.durationMs = durationMs;
    }

    public String getTxId() {
        return txId;
    }

    public String getUserId() {
        return userId;
    }

    public double getAmount() {
        return amount;
    }

    public String getFormattedAmount() {
        return formattedAmount;
    }

    public String getLocation() {
        return location;
    }

    public String getStatus() {
        return status;
    }

    public boolean isFlagged() {
        return "FLAGGED".equalsIgnoreCase(status);
    }

    public int getRiskScore() {
        return riskScore;
    }

    public List<String> getViolations() {
        return violations;
    }

    public String getBreachReason() {
        return breachReason;
    }

    public String getProcessedByThread() {
        return processedByThread;
    }

    public long getDurationMs() {
        return durationMs;
    }
}
