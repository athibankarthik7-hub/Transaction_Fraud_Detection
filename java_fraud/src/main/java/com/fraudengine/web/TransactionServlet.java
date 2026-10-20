package com.fraudengine.web;

import com.fraudengine.core.EngineEvaluator;
import com.fraudengine.core.Transaction;
import com.fraudengine.data.AuditLogger;
import com.fraudengine.data.DatabaseManager;

import com.fraudengine.concurrency.BulkScreeningItemResult;
import com.fraudengine.concurrency.BulkScreeningResult;
import com.fraudengine.concurrency.ParallelScreeningService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

/**
 * ============================================================================
 * JAVA CONCEPT 10: SERVLETS (Java EE HttpServlet)
 * ============================================================================
 * Production HTTP Controller responsible for handling transaction screening requests.
 * ============================================================================
 */
@WebServlet(name = "TransactionServlet", urlPatterns = {"/screen-transaction", "/api/screen", "/api/screen-bulk", "/screen-bulk"})
public class TransactionServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private EngineEvaluator evaluator;
    private DatabaseManager dbManager;

    @Override
    public void init() throws ServletException {
        super.init();
        // Initialize core backend singletons
        this.evaluator = EngineEvaluator.getInstance();
        this.dbManager = DatabaseManager.getInstance();
        System.out.println("[TransactionServlet] Initialized and ready to process screening requests.");
    }

    /**
     * Handles HTTP GET requests - routes user to the Client Portal form.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        request.getRequestDispatcher("/client-portal.jsp").forward(request, response);
    }

    /**
     * Handles HTTP POST requests - screens a submitted transaction against deterministic rules.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        // Check if bulk screening was requested
        String servletPath = request.getServletPath();
        String bulkInput = request.getParameter("bulkTransactions");
        if (bulkInput == null) {
            bulkInput = request.getParameter("bulkData");
        }

        if (bulkInput != null || (servletPath != null && servletPath.contains("bulk"))) {
            if (bulkInput == null) bulkInput = "";
            BulkScreeningResult bulkResult = ParallelScreeningService.getInstance().screenBatch(bulkInput);
            response.setContentType("application/json;charset=UTF-8");
            StringBuilder json = new StringBuilder();
            json.append("{\"success\":true,");
            json.append("\"total\":").append(bulkResult.getTotal()).append(",");
            json.append("\"approved\":").append(bulkResult.getApproved()).append(",");
            json.append("\"flagged\":").append(bulkResult.getFlagged()).append(",");
            json.append("\"totalDurationMs\":").append(bulkResult.getTotalDurationMs()).append(",");
            json.append("\"threadsUsed\":[");
            for (int i = 0; i < bulkResult.getThreadsUsed().size(); i++) {
                json.append("\"").append(bulkResult.getThreadsUsed().get(i)).append("\"");
                if (i < bulkResult.getThreadsUsed().size() - 1) json.append(",");
            }
            json.append("],\"results\":[");
            for (int i = 0; i < bulkResult.getResults().size(); i++) {
                BulkScreeningItemResult item = bulkResult.getResults().get(i);
                json.append("{");
                json.append("\"txId\":\"").append(item.getTxId()).append("\",");
                json.append("\"userId\":\"").append(item.getUserId()).append("\",");
                json.append("\"amount\":").append(item.getAmount()).append(",");
                json.append("\"formattedAmount\":\"").append(item.getFormattedAmount()).append("\",");
                json.append("\"location\":\"").append(item.getLocation()).append("\",");
                json.append("\"status\":\"").append(item.getStatus()).append("\",");
                json.append("\"riskScore\":").append(item.getRiskScore()).append(",");
                json.append("\"processedByThread\":\"").append(item.getProcessedByThread()).append("\",");
                json.append("\"durationMs\":").append(item.getDurationMs()).append(",");
                json.append("\"breachReason\":\"").append(item.getBreachReason() != null ? item.getBreachReason().replace("\"", "\\\"") : "").append("\"");
                json.append("}");
                if (i < bulkResult.getResults().size() - 1) json.append(",");
            }
            json.append("]}");
            response.getWriter().write(json.toString());
            return;
        }

        // 1. Parameter extraction with defensive sanitization
        String userId = request.getParameter("userId");
        String amountStr = request.getParameter("amount");
        String location = request.getParameter("location");

        // Default or sanitize inputs
        if (userId == null || userId.trim().isEmpty()) {
            userId = "U-" + (int)(Math.random() * 9000 + 1000);
        } else {
            userId = userId.trim();
        }

        double amount = 0.0;
        try {
            if (amountStr != null && !amountStr.trim().isEmpty()) {
                amount = Double.parseDouble(amountStr.replace("$", "").replace(",", "").trim());
            }
        } catch (NumberFormatException e) {
            amount = 0.0;
        }

        if (location == null || location.trim().isEmpty()) {
            location = "US";
        } else {
            location = location.trim().toUpperCase();
        }

        // 2. Generate unique enterprise transaction identifier
        String shortId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String txId = "TX-" + shortId;

        Transaction transaction = new Transaction(txId, userId, amount, location);

        // 3. Execute deterministic evaluation via EngineEvaluator (POLYMORPHISM & COLLECTIONS)
        Map<String, Object> outcome = evaluator.processTransaction(transaction);

        int riskScore = (int) outcome.get("riskScore");
        String status = (String) outcome.get("status");

        // 4. Trigger FILE HANDLING (AuditLogger) if flagged
        if (Transaction.STATUS_FLAGGED.equalsIgnoreCase(status)) {
            AuditLogger.logFraudAlert(transaction, riskScore);
        }

        // 5. Trigger JDBC PERSISTENCE (DatabaseManager with PreparedStatement)
        dbManager.saveTransaction(transaction);

        // 6. Check if caller requested JSON response (e.g. AJAX or API clients)
        String acceptHeader = request.getHeader("Accept");
        String requestedWith = request.getHeader("X-Requested-With");
        boolean wantsJson = (acceptHeader != null && acceptHeader.contains("application/json")) ||
                            "XMLHttpRequest".equalsIgnoreCase(requestedWith) ||
                            request.getServletPath().contains("/api/");

        if (wantsJson) {
            response.setContentType("application/json");
            StringBuilder json = new StringBuilder();
            json.append("{");
            json.append("\"txId\":\"").append(transaction.getTxId()).append("\",");
            json.append("\"userId\":\"").append(transaction.getUserId()).append("\",");
            json.append("\"amount\":").append(transaction.getAmount()).append(",");
            json.append("\"formattedAmount\":\"").append(transaction.getFormattedAmount()).append("\",");
            json.append("\"location\":\"").append(transaction.getLocation()).append("\",");
            json.append("\"status\":\"").append(transaction.getStatus()).append("\",");
            json.append("\"riskScore\":").append(riskScore).append(",");
            json.append("\"breachReason\":\"").append(transaction.getBreachReason().replace("\"", "\\\"")).append("\",");
            json.append("\"timestamp\":\"").append(transaction.getTimestamp()).append("\"");
            json.append("}");
            response.getWriter().write(json.toString());
            return;
        }

        // 7. Attach attributes and forward to JSP View (JAVA CONCEPT 11)
        request.setAttribute("transaction", transaction);
        request.setAttribute("outcome", outcome);
        request.setAttribute("isFlagged", Transaction.STATUS_FLAGGED.equalsIgnoreCase(status));

        request.getRequestDispatcher("/result.jsp").forward(request, response);
    }
}
