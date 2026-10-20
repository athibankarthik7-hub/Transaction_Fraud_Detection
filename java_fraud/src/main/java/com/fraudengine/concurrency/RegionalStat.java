package com.fraudengine.concurrency;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Domain model representing security threat intelligence and velocity metrics
 * for a specific geographic region analyzed in parallel.
 */
public class RegionalStat {

    private final String region;
    private final int total;
    private final int flagged;
    private final double failureRate;
    private final double flaggedVolume;
    private final boolean velocitySpike;
    private final int maxInWindow;
    private final long durationMs;
    private final String workerThread;

    public RegionalStat(String region, int total, int flagged, double failureRate,
                        double flaggedVolume, boolean velocitySpike, int maxInWindow,
                        long durationMs, String workerThread) {
        this.region = region;
        this.total = total;
        this.flagged = flagged;
        this.failureRate = failureRate;
        this.flaggedVolume = flaggedVolume;
        this.velocitySpike = velocitySpike;
        this.maxInWindow = maxInWindow;
        this.durationMs = durationMs;
        this.workerThread = workerThread;
    }

    public String getRegion() {
        return region;
    }

    public int getTotal() {
        return total;
    }

    public int getFlagged() {
        return flagged;
    }

    public double getFailureRate() {
        return failureRate;
    }

    public String getFormattedFailureRate() {
        return String.format(Locale.US, "%.1f%%", failureRate);
    }

    public double getFlaggedVolume() {
        return flaggedVolume;
    }

    public String getFormattedFlaggedVolume() {
        return NumberFormat.getCurrencyInstance(Locale.US).format(flaggedVolume);
    }

    public boolean isVelocitySpike() {
        return velocitySpike;
    }

    public String getVelocitySpikeText() {
        return velocitySpike ? "Yes" : "No";
    }

    public int getMaxInWindow() {
        return maxInWindow;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public String getWorkerThread() {
        return workerThread;
    }
}
