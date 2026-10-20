package com.fraudengine.concurrency;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Aggregates overall multithreaded screening statistics, including total
 * processed, decisions breakdown, execution latency, and worker threads engaged.
 */
public class BulkScreeningResult {

    private final int total;
    private final int approved;
    private final int flagged;
    private final long totalDurationMs;
    private final List<String> threadsUsed;
    private final List<BulkScreeningItemResult> results;

    public BulkScreeningResult(int total, int approved, int flagged, long totalDurationMs,
                               List<String> threadsUsed, List<BulkScreeningItemResult> results) {
        this.total = total;
        this.approved = approved;
        this.flagged = flagged;
        this.totalDurationMs = totalDurationMs;
        this.threadsUsed = threadsUsed != null ? Collections.unmodifiableList(new ArrayList<>(threadsUsed)) : Collections.emptyList();
        this.results = results != null ? Collections.unmodifiableList(new ArrayList<>(results)) : Collections.emptyList();
    }

    public int getTotal() {
        return total;
    }

    public int getApproved() {
        return approved;
    }

    public int getFlagged() {
        return flagged;
    }

    public long getTotalDurationMs() {
        return totalDurationMs;
    }

    public List<String> getThreadsUsed() {
        return threadsUsed;
    }

    public List<BulkScreeningItemResult> getResults() {
        return results;
    }
}
