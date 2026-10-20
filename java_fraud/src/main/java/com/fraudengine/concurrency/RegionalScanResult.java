package com.fraudengine.concurrency;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Encapsulates the complete result of a parallel regional threat scan,
 * including individual regional statistics and formatted console telemetry.
 */
public class RegionalScanResult {

    private final int totalTransactions;
    private final int totalRegions;
    private final List<RegionalStat> stats;
    private final String logOutput;
    private final long executionDurationMs;

    public RegionalScanResult(int totalTransactions, int totalRegions,
                              List<RegionalStat> stats, String logOutput,
                              long executionDurationMs) {
        this.totalTransactions = totalTransactions;
        this.totalRegions = totalRegions;
        this.stats = stats != null ? Collections.unmodifiableList(new ArrayList<>(stats)) : Collections.emptyList();
        this.logOutput = logOutput != null ? logOutput : "";
        this.executionDurationMs = executionDurationMs;
    }

    public int getTotalTransactions() {
        return totalTransactions;
    }

    public int getTotalRegions() {
        return totalRegions;
    }

    public List<RegionalStat> getStats() {
        return stats;
    }

    public String getLogOutput() {
        return logOutput;
    }

    public long getExecutionDurationMs() {
        return executionDurationMs;
    }
}
