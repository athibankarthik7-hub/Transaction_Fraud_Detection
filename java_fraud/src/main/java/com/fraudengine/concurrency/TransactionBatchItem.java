package com.fraudengine.concurrency;

/**
 * Encapsulates parsed user input parameters for a single transaction
 * within a concurrent bulk screening batch.
 */
public class TransactionBatchItem {

    private final String userId;
    private final double amount;
    private final String location;

    public TransactionBatchItem(String userId, double amount, String location) {
        this.userId = userId != null ? userId.trim() : "UNKNOWN";
        this.amount = amount;
        this.location = location != null ? location.trim().toUpperCase() : "US";
    }

    public String getUserId() {
        return userId;
    }

    public double getAmount() {
        return amount;
    }

    public String getLocation() {
        return location;
    }

    @Override
    public String toString() {
        return "TransactionBatchItem{" +
                "userId='" + userId + '\'' +
                ", amount=" + amount +
                ", location='" + location + '\'' +
                '}';
    }
}
