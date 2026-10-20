<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.fraudengine.core.AbstractRule" %>
<%@ page import="com.fraudengine.core.Transaction" %>
<%@ page import="com.fraudengine.core.EngineEvaluator" %>
<%@ page import="com.fraudengine.data.DatabaseManager" %>
<%@ page import="com.fraudengine.data.AuditLogger" %>
<%@ page import="com.fraudengine.concurrency.RegionalScanService" %>
<%@ page import="com.fraudengine.concurrency.RegionalScanResult" %>
<%@ page import="com.fraudengine.concurrency.RegionalStat" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%
    // JAVA CONCEPT 11: JSP (DYNAMIC SCRIPTLET & MODEL POPULATION)
    EngineEvaluator evaluator = EngineEvaluator.getInstance();
    DatabaseManager dbManager = DatabaseManager.getInstance();

    @SuppressWarnings("unchecked")
    List<AbstractRule> rules = (List<AbstractRule>) request.getAttribute("rules");
    if (rules == null) {
        rules = evaluator.getRules();
    }

    @SuppressWarnings("unchecked")
    List<Transaction> flaggedRecords = (List<Transaction>) request.getAttribute("flaggedRecords");
    if (flaggedRecords == null) {
        flaggedRecords = dbManager.getFlaggedTransactions();
    }

    Map<String, Object> metrics = dbManager.getLiveMetrics();
    int activeRules = evaluator.getActiveRuleCount();
    int screened = (int) metrics.getOrDefault("screened", 2);
    int flagged = (int) metrics.getOrDefault("flagged", 1);
    long avgRisk = Math.round(((Number) metrics.getOrDefault("avgRisk", 90)).doubleValue());
    if (avgRisk == 0 && flagged > 0) avgRisk = 90;

    RegionalScanResult initialRegionalResult = RegionalScanService.getInstance().runParallelRegionalScan();
    int regionalTotalTxs = initialRegionalResult.getTotalTransactions();
    int regionalTotalRegions = initialRegionalResult.getTotalRegions();
    List<RegionalStat> regionalStats = initialRegionalResult.getStats();
    String initialScanLog = initialRegionalResult.getLogOutput();
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin Dashboard - FraudEngine</title>
    <link rel="stylesheet" href="css/style.css">
    <style>
        :root {
            --bg-sidebar: #0b0f19;
            --bg-sidebar-hover: #161e2e;
            --bg-primary: #f8fafc;
            --bg-card: #ffffff;
            --border-color: #e2e8f0;
            --text-main: #0f172a;
            --text-muted: #64748b;
            --text-light: #94a3b8;
            --primary-accent: #4f46e5;
            --primary-accent-hover: #4338ca;
            --log-console-bg: #090e17;
            --radius-sm: 8px;
            --radius-md: 12px;
            --font-sans: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
            --font-mono: 'Consolas', 'SFMono-Regular', Menlo, Monaco, monospace;
        }
        * { box-sizing: border-box; margin: 0; padding: 0; }
        html, body {
            width: 100%; min-height: 100vh; margin: 0; padding: 0;
            background-color: var(--bg-primary); font-family: var(--font-sans);
            color: var(--text-main); -webkit-font-smoothing: antialiased;
        }
        svg {
            display: inline-block; vertical-align: middle; flex-shrink: 0;
            max-width: 24px; max-height: 24px;
        }
        .app-layout { display: flex; width: 100%; min-height: 100vh; }
        .sidebar {
            width: 240px; min-width: 240px; max-width: 240px;
            background-color: var(--bg-sidebar); display: flex; flex-direction: column;
            padding: 24px 16px; flex-shrink: 0; border-right: 1px solid rgba(255, 255, 255, 0.05);
            position: sticky; top: 0; height: 100vh;
        }
        .sidebar-brand {
            display: flex; align-items: center; gap: 12px; margin-bottom: 28px;
            padding: 4px 8px; text-decoration: none;
        }
        .brand-icon {
            width: 34px; height: 34px; min-width: 34px; background-color: #6366f1;
            border-radius: 9px; display: flex; align-items: center; justify-content: center;
            color: #ffffff; box-shadow: 0 2px 8px rgba(99, 102, 241, 0.4);
        }
        .brand-name { font-size: 17px; font-weight: 700; color: #ffffff; letter-spacing: -0.01em; }
        .sidebar-nav { display: flex; flex-direction: column; gap: 6px; flex-grow: 1; }
        .nav-link {
            display: flex; align-items: center; gap: 12px; padding: 10px 14px;
            border-radius: var(--radius-sm); color: var(--text-light); text-decoration: none;
            font-size: 13.5px; font-weight: 500; transition: all 0.15s ease-in-out;
        }
        .nav-link:hover { background-color: var(--bg-sidebar-hover); color: #ffffff; }
        .nav-link.active {
            background-color: var(--primary-accent); color: #ffffff; font-weight: 600;
            box-shadow: 0 2px 8px rgba(79, 70, 229, 0.35);
        }
        .sidebar-footer {
            padding-top: 16px; border-top: 1px solid rgba(255, 255, 255, 0.08);
            font-size: 11px; color: #64748b; padding-left: 8px; margin-top: auto;
        }
        .main-content {
            flex-grow: 1; padding: 32px 40px; background-color: var(--bg-primary);
            overflow-y: auto; max-width: 1350px;
        }
        .page-header { margin-bottom: 22px; }
        .back-link-wrapper { display: flex; align-items: center; gap: 12px; }
        .back-arrow {
            display: inline-flex; align-items: center; justify-content: center;
            width: 32px; height: 32px; min-width: 32px; border-radius: 8px;
            background: #ffffff; border: 1px solid var(--border-color); color: var(--text-main);
            text-decoration: none; font-size: 15px; cursor: pointer; transition: all 0.15s;
        }
        .back-arrow:hover { background: #f1f5f9; }
        .page-title { font-size: 22px; font-weight: 700; color: var(--text-main); line-height: 1.2; }
        .page-subtitle { font-size: 13px; color: var(--text-muted); margin-top: 2px; }
        .stats-grid {
            display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 20px;
        }
        .stat-card {
            background-color: var(--bg-card); border: 1px solid var(--border-color);
            border-radius: var(--radius-md); padding: 18px 22px; display: flex;
            justify-content: space-between; align-items: flex-start;
            box-shadow: 0 1px 2px rgba(0, 0, 0, 0.02);
        }
        .stat-info { display: flex; flex-direction: column; }
        .stat-label { font-size: 12.5px; color: var(--text-muted); font-weight: 500; margin-bottom: 6px; }
        .stat-value { font-size: 24px; font-weight: 700; color: var(--text-main); line-height: 1; }
        .stat-icon-wrapper {
            width: 36px; height: 36px; min-width: 36px; border-radius: 9px;
            display: flex; align-items: center; justify-content: center; flex-shrink: 0;
        }
        .stat-icon-purple { background-color: #ede9fe; color: #7c3aed; }
        .stat-icon-blue { background-color: #e0f2fe; color: #0284c7; }
        .stat-icon-red { background-color: #fee2e2; color: #ef4444; }
        .stat-icon-yellow { background-color: #fef3c7; color: #d97706; }
        .admin-grid { display: grid; grid-template-columns: 320px 1fr; gap: 18px; }
        .rule-controls-card {
            background-color: var(--bg-card); border: 1px solid var(--border-color);
            border-radius: var(--radius-md); padding: 22px;
        }
        .rule-box {
            background-color: #ffffff; border: 1px solid var(--border-color);
            border-radius: var(--radius-sm); padding: 12px 14px; margin-top: 12px;
            transition: all 0.15s;
        }
        .rule-box:hover { border-color: #cbd5e1; }
        .rule-header { display: flex; align-items: flex-start; gap: 10px; }
        .rule-checkbox {
            width: 16px; height: 16px; min-width: 16px; margin-top: 2px;
            cursor: pointer; accent-color: #0f172a;
        }
        .rule-label { font-size: 13.5px; font-weight: 600; color: var(--text-main); cursor: pointer; }
        .rule-desc {
            font-size: 11.5px; color: var(--text-muted); margin-left: 26px;
            margin-top: 3px; line-height: 1.45;
        }
        .analytics-column { display: flex; flex-direction: column; gap: 18px; }
        .card {
            background-color: var(--bg-card); border: 1px solid var(--border-color);
            border-radius: var(--radius-md); padding: 22px;
            box-shadow: 0 1px 2px rgba(0, 0, 0, 0.02);
        }
        .card-header-flex { display: flex; align-items: center; justify-content: space-between; margin-bottom: 14px; }
        .btn-group { display: flex; gap: 8px; }
        .btn-secondary {
            display: inline-flex; align-items: center; gap: 6px; padding: 6px 12px;
            background-color: #ffffff; border: 1px solid var(--border-color);
            border-radius: var(--radius-sm); font-size: 12px; font-weight: 600;
            color: var(--text-main); cursor: pointer; transition: all 0.15s;
        }
        .btn-secondary:hover { background-color: #f1f5f9; }
        .data-table { width: 100%; border-collapse: collapse; font-size: 13px; text-align: left; }
        .data-table th {
            padding: 9px 12px; color: var(--text-muted); font-weight: 600;
            border-bottom: 1px solid var(--border-color); font-size: 12px;
        }
        .data-table td { padding: 11px 12px; border-bottom: 1px solid #f1f5f9; color: var(--text-main); }
        .data-table tr:last-child td { border-bottom: none; }
        .risk-badge-red { color: #dc2626; font-weight: 700; }
        .log-terminal {
            background-color: var(--log-console-bg); border-radius: var(--radius-sm);
            padding: 14px 16px; font-family: var(--font-mono); font-size: 11.5px;
            color: #94a3b8; height: 160px; overflow-y: auto; white-space: pre-wrap;
            line-height: 1.6; border: 1px solid rgba(255, 255, 255, 0.05);
        }
    </style>
</head>
<body>
<div class="app-layout">
    <!-- LEFT SIDEBAR -->
    <aside class="sidebar">
        <a href="index.jsp" class="sidebar-brand">
            <div class="brand-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round" style="width:18px;height:18px;min-width:18px;max-width:18px;">
                    <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
                </svg>
            </div>
            <span class="brand-name">FraudEngine</span>
        </a>

        <nav class="sidebar-nav">
            <a href="index.jsp" class="nav-link">
                <svg width="18" height="18" viewBox="0 0 24 24" style="width:18px;height:18px;min-width:18px;max-width:18px;">
                    <rect x="3" y="3" width="7" height="7"></rect>
                    <rect x="14" y="3" width="7" height="7"></rect>
                    <rect x="14" y="14" width="7" height="7"></rect>
                    <rect x="3" y="14" width="7" height="7"></rect>
                </svg>
                Overview
            </a>
            <a href="client-portal.jsp" class="nav-link">
                <svg width="18" height="18" viewBox="0 0 24 24" style="width:18px;height:18px;min-width:18px;max-width:18px;">
                    <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                    <circle cx="12" cy="7" r="4"></circle>
                </svg>
                Client Portal
            </a>
            <a href="admin" class="nav-link active">
                <svg width="18" height="18" viewBox="0 0 24 24" style="width:18px;height:18px;min-width:18px;max-width:18px;">
                    <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"></path>
                    <path d="m9 12 2 2 4-4"></path>
                </svg>
                Admin Dashboard
            </a>
        </nav>

        <div class="sidebar-footer">
            Deterministic Rule-Based Engine v1.0
        </div>
    </aside>

    <!-- MAIN ADMIN DASHBOARD VIEW -->
    <main class="main-content">
        <!-- HEADER -->
        <header class="page-header">
            <div class="back-link-wrapper">
                <a href="index.jsp" class="back-arrow" title="Back to Overview">&larr;</a>
                <div>
                    <h1 class="page-title">Admin Dashboard</h1>
                    <p class="page-subtitle">Fraud analyst control center</p>
                </div>
            </div>
        </header>

        <!-- TOP STAT CARDS -->
        <section class="stats-grid">
            <div class="stat-card">
                <div class="stat-info">
                    <span class="stat-label">Active Rules</span>
                    <span class="stat-value" id="statActiveRules"><%= activeRules %></span>
                </div>
                <div class="stat-icon-wrapper stat-icon-purple">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="width:18px;height:18px;min-width:18px;max-width:18px;">
                        <circle cx="12" cy="12" r="10"></circle>
                        <circle cx="12" cy="6" r="6"></circle>
                        <circle cx="12" cy="2" r="2"></circle>
                    </svg>
                </div>
            </div>

            <div class="stat-card">
                <div class="stat-info">
                    <span class="stat-label">Screened</span>
                    <span class="stat-value" id="statScreened"><%= screened %></span>
                </div>
                <div class="stat-icon-wrapper stat-icon-blue">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="width:18px;height:18px;min-width:18px;max-width:18px;">
                        <polyline points="22 12 18 12 15 21 9 3 6 12 2 12"></polyline>
                    </svg>
                </div>
            </div>

            <div class="stat-card">
                <div class="stat-info">
                    <span class="stat-label">Flagged</span>
                    <span class="stat-value" id="statFlagged"><%= flagged %></span>
                </div>
                <div class="stat-icon-wrapper stat-icon-red">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="width:18px;height:18px;min-width:18px;max-width:18px;">
                        <path d="m21.73 18-8-14a2 2 0 0 0-3.48 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3Z"></path>
                        <line x1="12" y1="9" x2="12" y2="13"></line>
                        <line x1="12" y1="17" x2="12.01" y2="17"></line>
                    </svg>
                </div>
            </div>

            <div class="stat-card">
                <div class="stat-info">
                    <span class="stat-label">Avg Risk</span>
                    <span class="stat-value" id="statAvgRisk"><%= avgRisk %></span>
                </div>
                <div class="stat-icon-wrapper stat-icon-yellow">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="width:18px;height:18px;min-width:18px;max-width:18px;">
                        <path d="M12 2v4"></path>
                        <path d="m4.93 4.93 2.83 2.83"></path>
                        <path d="M2 12h4"></path>
                        <path d="m4.93 19.07 2.83-2.83"></path>
                        <path d="M12 22v-4"></path>
                        <path d="m19.07 19.07-2.83-2.83"></path>
                        <path d="M22 12h-4"></path>
                        <path d="m19.07 4.93-2.83 2.83"></path>
                    </svg>
                </div>
            </div>
        </section>

        <!-- DASHBOARD TWO-COLUMN CONTENT GRID -->
        <section class="admin-grid">
            <!-- LEFT COLUMN: RULE CONTROLS -->
            <div class="rule-controls-card">
                <h2 style="font-size: 15px; font-weight: 700; color: #0f172a; margin-bottom: 2px;">
                    Rule Controls
                </h2>
                <p style="font-size: 12px; color: #64748b; margin-bottom: 14px;">
                    Active Rules &mdash; toggle ON/OFF at runtime
                </p>

                <% for (AbstractRule rule : rules) { %>
                    <div class="rule-box">
                        <div class="rule-header">
                            <input type="checkbox" id="rule_<%= rule.getRuleId() %>" 
                                   class="rule-checkbox"
                                   <%= rule.isEnabled() ? "checked" : "" %>
                                   onchange="toggleRule('<%= rule.getRuleId() %>', this.checked)">
                            <label for="rule_<%= rule.getRuleId() %>" class="rule-label">
                                <%= rule.getRuleName() %>
                            </label>
                        </div>
                        <div class="rule-desc">
                            <%= rule.getDescription() %>
                        </div>
                    </div>
                <% } %>
            </div>

            <!-- RIGHT COLUMN: FLAGGED RECORDS TABLE & LOG CONSOLE -->
            <div class="analytics-column">
                <!-- Flagged Records Card -->
                <div class="card">
                    <div class="card-header-flex">
                        <h2 style="font-size: 15px; font-weight: 700; color: #0f172a;">
                            Flagged Records
                        </h2>
                        <div class="btn-group">
                            <button type="button" class="btn-secondary" onclick="refreshDatabase()">
                                <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="width:13px;height:13px;min-width:13px;max-width:13px;">
                                    <polyline points="23 4 23 10 17 10"></polyline>
                                    <path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"></path>
                                </svg>
                                Refresh DB
                            </button>
                            <button type="button" class="btn-secondary" onclick="loadAuditLog()">
                                <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="width:13px;height:13px;min-width:13px;max-width:13px;">
                                    <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
                                    <polyline points="14 2 14 8 20 8"></polyline>
                                    <line x1="16" y1="13" x2="8" y2="13"></line>
                                    <line x1="16" y1="17" x2="8" y2="17"></line>
                                    <polyline points="10 9 9 9 8 9"></polyline>
                                </svg>
                                Load Audit Log
                            </button>
                            <button type="button" class="btn-secondary" onclick="resetDatabase()">
                                <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="width:13px;height:13px;min-width:13px;max-width:13px;">
                                    <path d="M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8"></path>
                                    <path d="M3 3v5h5"></path>
                                </svg>
                                Reset Demo Data
                            </button>
                        </div>
                    </div>

                    <!-- Table -->
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th>Tx ID</th>
                                <th>User</th>
                                <th>Amount</th>
                                <th>Location</th>
                                <th>Risk</th>
                            </tr>
                        </thead>
                        <tbody id="flaggedTableBody">
                            <% if (flaggedRecords.isEmpty()) { %>
                                <tr>
                                    <td colspan="5" style="text-align: center; color: #94a3b8; padding: 20px;">
                                        No flagged transactions recorded yet.
                                    </td>
                                </tr>
                            <% } else { %>
                                <% for (Transaction t : flaggedRecords) { %>
                                    <tr>
                                        <td><%= t.getTxId() %></td>
                                        <td><%= t.getUserId() %></td>
                                        <td><%= t.getFormattedAmount() %></td>
                                        <td><%= t.getLocation() %></td>
                                        <td class="risk-badge-red"><%= t.getRiskScore() %></td>
                                    </tr>
                                <% } %>
                            <% } %>
                        </tbody>
                    </table>
                </div>

                <!-- Log Console Card -->
                <div class="card">
                    <h2 style="font-size: 15px; font-weight: 700; color: #0f172a; margin-bottom: 10px;">
                        Log Console
                    </h2>
                    <div id="logTerminal" class="log-terminal"><%= initialScanLog != null && !initialScanLog.isEmpty() ? initialScanLog : "Audit log lines appear here after loading." %></div>
                </div>
            </div>
        </section>

        <!-- REGIONAL THREAT ANALYTICS CARD (MULTITHREADED CONCURRENT BATCH SCAN) -->
        <section class="threat-analytics-card">
            <div class="threat-card-header">
                <div class="threat-card-title">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="color: #6366f1; width:18px; height:18px;">
                        <circle cx="12" cy="12" r="10"></circle>
                        <line x1="2" y1="12" x2="22" y2="12"></line>
                        <path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"></path>
                    </svg>
                    Regional Threat Analytics
                </div>
                <button type="button" class="btn-scan-parallel" id="btnRunRegionalScan" onclick="runParallelRegionalScan()">
                    <svg width="13" height="13" viewBox="0 0 24 24" fill="currentColor" stroke="none" style="width:13px;height:13px;">
                        <polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2"></polygon>
                    </svg>
                    Run Parallel Regional Scan
                </button>
            </div>
            <p class="card-description" id="regionalScanDesc" style="margin-top: 8px; margin-bottom: 16px;">
                Partitions <%= regionalTotalTxs %> transactions across <%= regionalTotalRegions %> regions and analyses them concurrently via Promise.all &mdash; the JS mirror of the Java ExecutorService + CompletableFuture.allOf. Re-screens each batch through the live rule engine.
            </p>

            <table class="data-table">
                <thead>
                    <tr>
                        <th>Region</th>
                        <th>Total</th>
                        <th>Flagged</th>
                        <th>Failure Rate</th>
                        <th>Flagged Volume</th>
                        <th>Velocity Spike</th>
                    </tr>
                </thead>
                <tbody id="regionalTableBody">
                    <% for (RegionalStat stat : regionalStats) { %>
                        <tr>
                            <td style="font-weight: 600;"><%= stat.getRegion() %></td>
                            <td><%= stat.getTotal() %></td>
                            <td class="<%= stat.getFlagged() > 0 ? "risk-badge-red" : "" %>"><%= stat.getFlagged() %></td>
                            <td><%= stat.getFormattedFailureRate() %></td>
                            <td style="font-weight: 500;"><%= stat.getFormattedFlaggedVolume() %></td>
                            <td><%= stat.getVelocitySpikeText() %></td>
                        </tr>
                    <% } %>
                </tbody>
            </table>
        </section>
    </main>
</div>

<!-- JAVASCRIPT FOR DYNAMIC AJAX INTERACTIONS -->
<script>
    function toggleRule(ruleId, enabled) {
        fetch('admin', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: 'action=toggleRule&ruleId=' + encodeURIComponent(ruleId) + '&enabled=' + enabled
        })
        .then(res => res.json())
        .then(data => {
            const activeBoxes = document.querySelectorAll('.rule-checkbox:checked').length;
            document.getElementById('statActiveRules').innerText = activeBoxes;
        })
        .catch(err => console.error('Error toggling rule:', err));
    }

    function refreshDatabase() {
        fetch('admin?action=refreshDb')
        .then(res => res.json())
        .then(data => {
            const tbody = document.getElementById('flaggedTableBody');
            tbody.innerHTML = '';
            if (data.length === 0) {
                tbody.innerHTML = '<tr><td colspan="5" style="text-align: center; color: #94a3b8; padding: 20px;">No flagged transactions recorded yet.</td></tr>';
                return;
            }
            data.forEach(t => {
                const tr = document.createElement('tr');
                tr.innerHTML = '<td>' + t.txId + '</td>' +
                               '<td>' + t.userId + '</td>' +
                               '<td>' + t.formattedAmount + '</td>' +
                               '<td>' + t.location + '</td>' +
                               '<td class="risk-badge-red">' + t.riskScore + '</td>';
                tbody.appendChild(tr);
            });
            document.getElementById('statFlagged').innerText = data.length;
        })
        .catch(err => console.error('Error refreshing DB:', err));
    }

    function loadAuditLog() {
        fetch('admin?action=loadLog')
        .then(res => res.text())
        .then(text => {
            const terminal = document.getElementById('logTerminal');
            terminal.innerText = text.trim() ? text : 'Audit log is currently empty.';
        })
        .catch(err => {
            console.error('Error loading audit log:', err);
            document.getElementById('logTerminal').innerText = 'Failed to load audit logs from server.';
        });
    }

    function runParallelRegionalScan() {
        const btn = document.getElementById('btnRunRegionalScan');
        const originalHtml = btn.innerHTML;
        btn.disabled = true;
        btn.innerHTML = '<span style="display:inline-block; animation: spin 1s infinite linear;">&#9696;</span> Scanning Regions...';

        fetch('admin?action=regionalScan')
        .then(res => res.json())
        .then(data => {
            btn.disabled = false;
            btn.innerHTML = originalHtml;

            // 1. Update Log Console
            document.getElementById('logTerminal').innerText = data.logOutput;

            // 2. Update description text
            const desc = document.getElementById('regionalScanDesc');
            if (desc) {
                desc.innerHTML = 'Partitions ' + data.totalTransactions + ' transactions across ' + data.totalRegions +
                    ' regions and analyses them concurrently via Promise.all &mdash; the JS mirror of the Java ExecutorService + CompletableFuture.allOf. Re-screens each batch through the live rule engine.';
            }

            // 3. Update table rows
            const tbody = document.getElementById('regionalTableBody');
            tbody.innerHTML = '';
            data.stats.forEach(stat => {
                const tr = document.createElement('tr');
                const flaggedCell = stat.flagged > 0 ?
                    '<td class="risk-badge-red">' + stat.flagged + '</td>' :
                    '<td>' + stat.flagged + '</td>';

                tr.innerHTML = 
                    '<td style="font-weight: 600;">' + stat.region + '</td>' +
                    '<td>' + stat.total + '</td>' +
                    flaggedCell +
                    '<td>' + stat.formattedFailureRate + '</td>' +
                    '<td style="font-weight: 500;">' + stat.formattedFlaggedVolume + '</td>' +
                    '<td>' + stat.velocitySpikeText + '</td>';
                tbody.appendChild(tr);
            });
        })
        .catch(err => {
            btn.disabled = false;
            btn.innerHTML = originalHtml;
            console.error('Error running regional scan:', err);
        });
    }

    function resetDatabase() {
        if (!confirm('Reset database to the 3 reference transactions (1 EMEA flagged, 2 North America approved)?')) return;
        fetch('admin?action=resetDb')
        .then(res => res.json())
        .then(() => {
            refreshDatabase();
            runParallelRegionalScan();
        })
        .catch(err => console.error('Error resetting DB:', err));
    }
</script>
</body>
</html>
