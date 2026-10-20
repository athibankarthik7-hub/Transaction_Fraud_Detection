package com.fraudengine.concurrency;

import com.fraudengine.core.EngineEvaluator;
import com.fraudengine.core.Transaction;
import com.fraudengine.data.AuditLogger;
import com.fraudengine.data.DatabaseManager;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * ============================================================================
 * JAVA CONCEPT 12: MULTITHREADING & CONCURRENCY (ThreadPool & CompletableFuture)
 * ============================================================================
 * Enterprise Parallel Screening Service.
 * 
 * Screens batches of incoming transactions concurrently across an optimized
 * ExecutorService thread pool, mirroring production high-throughput compliance
 * pipelines.
 * 
 * Concurrency Highlights:
 *  - Fixed thread pool managed by NamedThreadFactory for clear worker tracing.
 *  - Asynchronous dispatch using CompletableFuture.supplyAsync() per transaction.
 *  - Barrier synchronization using CompletableFuture.allOf().
 *  - Thread-safe coordination with SQLite persistence and synchronous audit logging.
 * ============================================================================
 */
public class ParallelScreeningService {

    private static volatile ParallelScreeningService instance;

    private final ExecutorService executorService;
    private final EngineEvaluator evaluator;
    private final DatabaseManager dbManager;

    /**
     * Singleton instance provider.
     */
    public static ParallelScreeningService getInstance() {
        if (instance == null) {
            synchronized (ParallelScreeningService.class) {
                if (instance == null) {
                    instance = new ParallelScreeningService();
                }
            }
        }
        return instance;
    }

    public ParallelScreeningService() {
        int corePoolSize = Math.max(4, Runtime.getRuntime().availableProcessors());
        this.executorService = Executors.newFixedThreadPool(
                corePoolSize,
                new NamedThreadFactory("ParallelScreening-Worker", true)
        );
        this.evaluator = EngineEvaluator.getInstance();
        this.dbManager = DatabaseManager.getInstance();
    }

    /**
     * Parses raw multiline transaction input and dispatches all lines concurrently.
     * Expected format per line: "userId, amount, location"
     * Example:
     *   U-1001, 12500, NG
     *   U-1002, 500, US
     *
     * @param rawText Multiline text input
     * @return Aggregated BulkScreeningResult
     */
    public BulkScreeningResult screenBatch(String rawText) {
        List<TransactionBatchItem> items = parseRawInput(rawText);
        return screenTransactionsConcurrently(items);
    }

    /**
     * Dispatches a list of TransactionBatchItem domain models concurrently
     * across worker threads using CompletableFuture.
     */
    public BulkScreeningResult screenTransactionsConcurrently(List<TransactionBatchItem> items) {
        if (items == null || items.isEmpty()) {
            return new BulkScreeningResult(0, 0, 0, 0, Collections.emptyList(), Collections.emptyList());
        }

        long batchStartTime = System.currentTimeMillis();
        Set<String> threadsEngaged = ConcurrentHashMap.newKeySet();

        // 1. Asynchronously submit each transaction screening task to the thread pool
        List<CompletableFuture<BulkScreeningItemResult>> futures = items.stream()
                .map(item -> CompletableFuture.supplyAsync(() -> processSingleTransaction(item, threadsEngaged), executorService))
                .collect(Collectors.toList());

        // 2. Multithreaded barrier: await completion of all parallel screening tasks
        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        allOf.join();

        // 3. Collect individual results in preservation order
        List<BulkScreeningItemResult> results = futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

        long batchDuration = System.currentTimeMillis() - batchStartTime;

        int total = results.size();
        int flagged = 0;
        int approved = 0;
        for (BulkScreeningItemResult r : results) {
            if (r.isFlagged()) {
                flagged++;
            } else {
                approved++;
            }
        }

        return new BulkScreeningResult(
                total,
                approved,
                flagged,
                batchDuration,
                new ArrayList<>(threadsEngaged),
                results
        );
    }

    /**
     * Unit of work executed within an individual worker thread.
     */
    @SuppressWarnings("unchecked")
    private BulkScreeningItemResult processSingleTransaction(TransactionBatchItem item, Set<String> threadsEngaged) {
        long itemStartTime = System.currentTimeMillis();
        String currentThreadName = Thread.currentThread().getName();
        threadsEngaged.add(currentThreadName);

        String shortId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String txId = "TX-" + shortId;

        Transaction tx = new Transaction(txId, item.getUserId(), item.getAmount(), item.getLocation());

        // Dynamic rule evaluation dispatch
        Map<String, Object> outcome = evaluator.processTransaction(tx);
        int riskScore = (int) outcome.getOrDefault("riskScore", tx.getRiskScore());
        List<String> violations = (List<String>) outcome.getOrDefault("violations", Collections.emptyList());
        boolean isFlagged = tx.isFlagged();

        // Synchronous audit logging for compliance auditability if flagged
        if (isFlagged) {
            AuditLogger.logFraudAlert(tx, riskScore);
        }

        // Thread-safe JDBC persistence
        dbManager.saveTransaction(tx);

        long itemDuration = System.currentTimeMillis() - itemStartTime;

        return new BulkScreeningItemResult(
                tx.getTxId(),
                tx.getUserId(),
                tx.getAmount(),
                tx.getLocation(),
                tx.getStatus(),
                riskScore,
                violations,
                tx.getBreachReason(),
                currentThreadName,
                itemDuration
        );
    }

    /**
     * Parses raw textarea input into TransactionBatchItem instances defensively.
     */
    public List<TransactionBatchItem> parseRawInput(String rawText) {
        List<TransactionBatchItem> list = new ArrayList<>();
        if (rawText == null || rawText.trim().isEmpty()) {
            return list;
        }

        String[] lines = rawText.split("\\r?\\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith("//")) {
                continue; // Ignore blank lines and comments
            }

            String[] tokens = trimmed.split("[,;\\t]+");
            String userId = "UNKNOWN";
            double amount = 0.0;
            String location = "US";

            if (tokens.length >= 4) {
                // Format: txId, userId, amount, location
                userId = tokens[1].trim();
                try {
                    amount = Double.parseDouble(tokens[2].replaceAll("[^0-9.]", "").trim());
                } catch (NumberFormatException ignored) {}
                location = tokens[3].trim().toUpperCase();
            } else if (tokens.length == 3) {
                // Format: userId, amount, location
                userId = tokens[0].trim();
                try {
                    amount = Double.parseDouble(tokens[1].replaceAll("[^0-9.]", "").trim());
                } catch (NumberFormatException ignored) {}
                location = tokens[2].trim().toUpperCase();
            } else if (tokens.length == 2) {
                // Format: userId, amount
                userId = tokens[0].trim();
                try {
                    amount = Double.parseDouble(tokens[1].replaceAll("[^0-9.]", "").trim());
                } catch (NumberFormatException ignored) {}
            } else if (tokens.length == 1) {
                userId = tokens[0].trim();
            }

            list.add(new TransactionBatchItem(userId, amount, location));
        }
        return list;
    }

    public String screenBatchAndGetJson(String rawText) {
        BulkScreeningResult result = screenBatch(rawText);
        return toJson(result);
    }

    public static String toJson(BulkScreeningResult result) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"success\":true,");
        sb.append("\"total\":").append(result.getTotal()).append(",");
        sb.append("\"approved\":").append(result.getApproved()).append(",");
        sb.append("\"flagged\":").append(result.getFlagged()).append(",");
        sb.append("\"totalDurationMs\":").append(result.getTotalDurationMs()).append(",");
        sb.append("\"threadsUsed\":[");
        for (int i = 0; i < result.getThreadsUsed().size(); i++) {
            sb.append("\"").append(escapeJson(result.getThreadsUsed().get(i))).append("\"");
            if (i < result.getThreadsUsed().size() - 1) sb.append(",");
        }
        sb.append("],");
        sb.append("\"results\":[");
        for (int i = 0; i < result.getResults().size(); i++) {
            BulkScreeningItemResult item = result.getResults().get(i);
            sb.append("{");
            sb.append("\"txId\":\"").append(escapeJson(item.getTxId())).append("\",");
            sb.append("\"userId\":\"").append(escapeJson(item.getUserId())).append("\",");
            sb.append("\"amount\":").append(item.getAmount()).append(",");
            sb.append("\"formattedAmount\":\"").append(escapeJson(item.getFormattedAmount())).append("\",");
            sb.append("\"location\":\"").append(escapeJson(item.getLocation())).append("\",");
            sb.append("\"status\":\"").append(escapeJson(item.getStatus())).append("\",");
            sb.append("\"riskScore\":").append(item.getRiskScore()).append(",");
            sb.append("\"processedByThread\":\"").append(escapeJson(item.getProcessedByThread())).append("\",");
            sb.append("\"durationMs\":").append(item.getDurationMs()).append(",");
            sb.append("\"breachReason\":\"").append(escapeJson(item.getBreachReason())).append("\"");
            sb.append("}");
            if (i < result.getResults().size() - 1) sb.append(",");
        }
        sb.append("]");
        sb.append("}");
        return sb.toString();
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    public void shutdown() {
        executorService.shutdown();
    }
}
