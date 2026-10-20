package com.fraudengine.core;

import java.io.Serializable;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * ============================================================================
 * JAVA CONCEPT 3: PACKAGES (Modular structure - com.fraudengine.core)
 * ============================================================================
 * Production-ready domain model representing a financial transaction.
 * Encapsulates transaction attributes, verification status, and analytical telemetry.
 * 
 * Supports serialization for distributed pipeline caching and logging.
 * ============================================================================
 */
public class Transaction implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_FLAGGED = "FLAGGED";

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy, h:mm:ss a");

    private String txId;
    private String userId;
    private double amount;
    private String location;
    private String timestamp;
    private String status;
    private int riskScore;
    private String breachReason;

    /**
     * Default no-arg constructor required for standard enterprise JavaBeans.
     */
    public Transaction() {
        this.timestamp = LocalDateTime.now().format(FORMATTER);
        this.status = STATUS_PENDING;
        this.riskScore = 0;
        this.breachReason = "None";
    }

    /**
     * Full parameterized constructor for new transaction initialization.
     *
     * @param txId      unique identifier (e.g. TX-42F8YND2)
     * @param userId    user account reference (e.g. 24BBS0048 or U-1001)
     * @param amount    transaction value in USD
     * @param location  originating 2-letter country code (e.g. US, NG, RU)
     */
    public Transaction(String txId, String userId, double amount, String location) {
        this.txId = txId;
        this.userId = userId;
        this.amount = amount;
        this.location = location != null ? location.trim().toUpperCase() : "UNKNOWN";
        this.timestamp = LocalDateTime.now().format(FORMATTER);
        this.status = STATUS_PENDING;
        this.riskScore = 0;
        this.breachReason = "None";
    }

    /**
     * Full constructor for reconstructing persisted database entities.
     */
    public Transaction(String txId, String userId, double amount, String location, String timestamp, String status, int riskScore, String breachReason) {
        this.txId = txId;
        this.userId = userId;
        this.amount = amount;
        this.location = location != null ? location.trim().toUpperCase() : "UNKNOWN";
        this.timestamp = timestamp;
        this.status = status;
        this.riskScore = riskScore;
        this.breachReason = breachReason;
    }

    // ========================================================================
    // GETTERS AND SETTERS
    // ========================================================================

    public String getTxId() {
        return txId;
    }

    public void setTxId(String txId) {
        this.txId = txId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location != null ? location.trim().toUpperCase() : "UNKNOWN";
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(int riskScore) {
        this.riskScore = riskScore;
    }

    public String getBreachReason() {
        return breachReason;
    }

    public void setBreachReason(String breachReason) {
        this.breachReason = breachReason;
    }

    /**
     * Helper method to render currency formatted as in the UI (e.g., "$10,000.00").
     */
    public String getFormattedAmount() {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
        return currencyFormat.format(this.amount);
    }

    public boolean isFlagged() {
        return STATUS_FLAGGED.equalsIgnoreCase(this.status);
    }

    public boolean isApproved() {
        return STATUS_APPROVED.equalsIgnoreCase(this.status);
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "txId='" + txId + '\'' +
                ", userId='" + userId + '\'' +
                ", amount=" + getFormattedAmount() +
                ", location='" + location + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", status='" + status + '\'' +
                ", riskScore=" + riskScore +
                ", breachReason='" + breachReason + '\'' +
                '}';
    }
}
