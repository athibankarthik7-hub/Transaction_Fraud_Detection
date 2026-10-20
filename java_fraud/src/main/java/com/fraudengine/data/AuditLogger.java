package com.fraudengine.data;

import com.fraudengine.core.Transaction;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================================
 * JAVA CONCEPT 6: FILE HANDLING (I/O)
 * ============================================================================
 * Enterprise Synchronous File I/O Audit Logger.
 * 
 * Key Characteristics:
 *  1. Thread-safe file append mechanism utilizing synchronized monitors.
 *  2. Utilizes BufferedWriter and FileWriter for high-throughput disk writes.
 *  3. Appends security telemetry to 'logs/fraud_alerts.log'.
 *  4. Provides companion read capabilities with BufferedReader and FileReader
 *     to feed the interactive Admin Log Console and JavaFX TextArea.
 * ============================================================================
 */
public class AuditLogger {

    private static final String LOG_DIR = "logs";
    private static final String LOG_FILE = "logs/fraud_alerts.log";

    // Concurrency Lock: Prevents interleaved log lines during simultaneous servlet requests
    private static final Object FILE_WRITE_LOCK = new Object();

    static {
        ensureLogDirectoryExists();
        seedInitialLogEntryIfEmpty();
    }

    /**
     * Verifies that the logs directory and log file exist.
     */
    private static void ensureLogDirectoryExists() {
        File dir = new File(LOG_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * Seeds initial audit log entry matching screenshot reference if file is empty.
     */
    private static void seedInitialLogEntryIfEmpty() {
        File file = new File(LOG_FILE);
        if (!file.exists() || file.length() == 0) {
            String initialEntry = "[10/12/2026, 3:36:21 PM] FLAGGED tx=TX-42F8YND2 user=24BBS0048 amount=$10,000.00 location=NG riskScore=90";
            synchronized (FILE_WRITE_LOCK) {
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8, false))) {
                    writer.write(initialEntry);
                    writer.newLine();
                } catch (IOException e) {
                    System.err.println("[AuditLogger] Failed to write seed log entry: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Synchronously appends a flagged transaction alert to logs/fraud_alerts.log.
     *
     * @param transaction the flagged Transaction entity
     * @param riskScore    calculated risk score
     */
    public static void logFraudAlert(Transaction transaction, int riskScore) {
        if (transaction == null) {
            return;
        }

        ensureLogDirectoryExists();

        // Format exactly matching screenshot console output:
        // [10/12/2026, 3:36:21 PM] FLAGGED tx=TX-42F8YND2 user=24BBS0048 amount=$10,000.00 location=NG riskScore=90
        String logEntry = String.format(
            "[%s] FLAGGED tx=%s user=%s amount=%s location=%s riskScore=%d",
            transaction.getTimestamp(),
            transaction.getTxId(),
            transaction.getUserId(),
            transaction.getFormattedAmount(),
            transaction.getLocation(),
            riskScore
        );

        // THREAD SAFETY: Synchronized block guarantees atomic, sequential writes across servlet worker threads
        synchronized (FILE_WRITE_LOCK) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FILE, StandardCharsets.UTF_8, true))) {
                writer.write(logEntry);
                writer.newLine();
                writer.flush();
                System.out.println("[AuditLogger] Written to " + LOG_FILE + ": " + logEntry);
            } catch (IOException e) {
                System.err.println("[AuditLogger] Error writing to audit log file: " + e.getMessage());
            }
        }
    }

    /**
     * Reads all recorded audit log lines from disk using BufferedReader.
     *
     * @return List of log entry lines in chronological order
     */
    public static List<String> readAuditLogs() {
        List<String> logLines = new ArrayList<>();
        File file = new File(LOG_FILE);

        if (!file.exists()) {
            return logLines;
        }

        // Synchronized read ensures reading does not clash with active writes
        synchronized (FILE_WRITE_LOCK) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.trim().isEmpty()) {
                        logLines.add(line);
                    }
                }
            } catch (IOException e) {
                System.err.println("[AuditLogger] Error reading from audit log file: " + e.getMessage());
            }
        }

        return logLines;
    }

    /**
     * Retrieves the complete log content as a single newline-separated text block.
     */
    public static String readAuditLogsAsString() {
        List<String> lines = readAuditLogs();
        if (lines.isEmpty()) {
            return "Audit log lines appear here after loading.";
        }
        return String.join(System.lineSeparator(), lines);
    }
}
