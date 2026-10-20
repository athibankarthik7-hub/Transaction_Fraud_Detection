package com.fraudengine.web;

import com.fraudengine.core.AbstractRule;
import com.fraudengine.core.EngineEvaluator;
import com.fraudengine.core.Transaction;
import com.fraudengine.data.AuditLogger;
import com.fraudengine.data.DatabaseManager;
import com.fraudengine.concurrency.RegionalScanService;
import com.fraudengine.concurrency.RegionalScanResult;
import com.fraudengine.concurrency.RegionalStat;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * ============================================================================
 * JAVA CONCEPT 10: SERVLETS (Admin Controller)
 * ============================================================================
 * Administrative controller supporting runtime configuration, live database
 * refreshes, and log inspection as featured in the Admin Dashboard UI.
 * ============================================================================
 */
@WebServlet(name = "AdminServlet", urlPatterns = {"/admin", "/admin-dashboard", "/api/admin/*"})
public class AdminServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private EngineEvaluator evaluator;
    private DatabaseManager dbManager;

    @Override
    public void init() throws ServletException {
        super.init();
        this.evaluator = EngineEvaluator.getInstance();
        this.dbManager = DatabaseManager.getInstance();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {

        String pathInfo = request.getPathInfo();
        String action = request.getParameter("action");

        // API Endpoint for Audit Log inspection
        if ("loadLog".equalsIgnoreCase(action) || (pathInfo != null && pathInfo.contains("/logs"))) {
            response.setContentType("text/plain;charset=UTF-8");
            String logContent = AuditLogger.readAuditLogsAsString();
            response.getWriter().write(logContent);
            return;
        }

        // API Endpoint for resetting to 3 benchmark transactions
        if ("resetDb".equalsIgnoreCase(action) || (pathInfo != null && pathInfo.contains("/reset"))) {
            dbManager.resetToDemoState();
            evaluator.setMetrics(3, 1);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"success\":true}");
            return;
        }

        // API Endpoint for live DB refresh in JSON
        if ("refreshDb".equalsIgnoreCase(action) || (pathInfo != null && pathInfo.contains("/flagged"))) {
            response.setContentType("application/json;charset=UTF-8");
            List<Transaction> flagged = dbManager.getFlaggedTransactions();
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < flagged.size(); i++) {
                Transaction t = flagged.get(i);
                json.append("{")
                    .append("\"txId\":\"").append(t.getTxId()).append("\",")
                    .append("\"userId\":\"").append(t.getUserId()).append("\",")
                    .append("\"amount\":").append(t.getAmount()).append(",")
                    .append("\"formattedAmount\":\"").append(t.getFormattedAmount()).append("\",")
                    .append("\"location\":\"").append(t.getLocation()).append("\",")
                    .append("\"riskScore\":").append(t.getRiskScore()).append(",")
                    .append("\"status\":\"").append(t.getStatus()).append("\"")
                    .append("}");
                if (i < flagged.size() - 1) json.append(",");
            }
            json.append("]");
            response.getWriter().write(json.toString());
            return;
        }

        // API Endpoint for parallel regional threat scan (JAVA MULTITHREADING)
        if ("regionalScan".equalsIgnoreCase(action) || (pathInfo != null && pathInfo.contains("/regional-scan"))) {
            response.setContentType("application/json;charset=UTF-8");
            RegionalScanResult scanResult = RegionalScanService.getInstance().runParallelRegionalScan();
            StringBuilder json = new StringBuilder();
            json.append("{");
            json.append("\"success\":true,");
            json.append("\"totalTransactions\":").append(scanResult.getTotalTransactions()).append(",");
            json.append("\"totalRegions\":").append(scanResult.getTotalRegions()).append(",");
            json.append("\"executionDurationMs\":").append(scanResult.getExecutionDurationMs()).append(",");
            json.append("\"logOutput\":\"").append(scanResult.getLogOutput().replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r")).append("\",");
            json.append("\"stats\":[");
            for (int i = 0; i < scanResult.getStats().size(); i++) {
                RegionalStat stat = scanResult.getStats().get(i);
                json.append("{");
                json.append("\"region\":\"").append(stat.getRegion()).append("\",");
                json.append("\"total\":").append(stat.getTotal()).append(",");
                json.append("\"flagged\":").append(stat.getFlagged()).append(",");
                json.append("\"failureRate\":").append(stat.getFailureRate()).append(",");
                json.append("\"formattedFailureRate\":\"").append(stat.getFormattedFailureRate()).append("\",");
                json.append("\"flaggedVolume\":").append(stat.getFlaggedVolume()).append(",");
                json.append("\"formattedFlaggedVolume\":\"").append(stat.getFormattedFlaggedVolume()).append("\",");
                json.append("\"velocitySpike\":").append(stat.isVelocitySpike()).append(",");
                json.append("\"velocitySpikeText\":\"").append(stat.getVelocitySpikeText()).append("\",");
                json.append("\"maxInWindow\":").append(stat.getMaxInWindow()).append(",");
                json.append("\"durationMs\":").append(stat.getDurationMs()).append(",");
                json.append("\"workerThread\":\"").append(stat.getWorkerThread()).append("\"");
                json.append("}");
                if (i < scanResult.getStats().size() - 1) json.append(",");
            }
            json.append("]}");
            response.getWriter().write(json.toString());
            return;
        }

        // Standard page view: prepare model attributes for admin-dashboard.jsp
        List<AbstractRule> rules = evaluator.getRules();
        List<Transaction> flaggedRecords = dbManager.getFlaggedTransactions();
        Map<String, Object> metrics = dbManager.getLiveMetrics();
        RegionalScanResult initialScan = RegionalScanService.getInstance().runParallelRegionalScan();

        request.setAttribute("rules", rules);
        request.setAttribute("flaggedRecords", flaggedRecords);
        request.setAttribute("regionalStats", initialScan.getStats());
        request.setAttribute("regionalTotalTxs", initialScan.getTotalTransactions());
        request.setAttribute("regionalTotalRegions", initialScan.getTotalRegions());
        request.setAttribute("initialScanLog", initialScan.getLogOutput());

        request.setAttribute("activeRulesCount", evaluator.getActiveRuleCount());
        request.setAttribute("screenedCount", metrics.getOrDefault("screened", 0));
        request.setAttribute("flaggedCount", metrics.getOrDefault("flagged", 0));
        request.setAttribute("avgRisk", metrics.getOrDefault("avgRisk", 0));
        request.setAttribute("approvalRate", metrics.getOrDefault("approvalRate", 100));

        request.getRequestDispatcher("/admin-dashboard.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");

        // Action: Toggle rule state at runtime
        if ("toggleRule".equalsIgnoreCase(action)) {
            String ruleId = request.getParameter("ruleId");
            boolean enabled = Boolean.parseBoolean(request.getParameter("enabled"));
            boolean updated = evaluator.setRuleEnabled(ruleId, enabled);

            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"success\":" + updated + ",\"ruleId\":\"" + ruleId + "\",\"enabled\":" + enabled + "}");
            return;
        }

        doGet(request, response);
    }
}
