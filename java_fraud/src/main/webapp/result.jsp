<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.fraudengine.core.Transaction" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.List" %>
<%
    // JAVA CONCEPT 11: JSP (DYNAMIC EXPRESSION RENDERING)
    Transaction tx = (Transaction) request.getAttribute("transaction");
    @SuppressWarnings("unchecked")
    Map<String, Object> outcome = (Map<String, Object>) request.getAttribute("outcome");
    Boolean isFlagged = (Boolean) request.getAttribute("isFlagged");

    if (tx == null) {
        response.sendRedirect("client-portal.jsp");
        return;
    }

    if (isFlagged == null) {
        isFlagged = tx.isFlagged();
    }

    @SuppressWarnings("unchecked")
    List<String> violations = outcome != null ? (List<String>) outcome.get("violations") : null;
    int riskScore = tx.getRiskScore();
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Screening Result - FraudEngine</title>
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
            --hero-btn-dark: #111427;
            --hero-btn-hover: #1e223d;
            --radius-sm: 8px;
            --radius-md: 12px;
            --font-sans: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
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
        .form-container { max-width: 620px; }
        .card {
            background-color: var(--bg-card); border: 1px solid var(--border-color);
            border-radius: var(--radius-md); padding: 26px 30px;
            box-shadow: 0 1px 2px rgba(0, 0, 0, 0.02);
        }
        .result-badge {
            display: inline-flex; align-items: center; gap: 8px;
            padding: 7px 14px; border-radius: 20px; font-size: 13px; font-weight: 700;
            margin-bottom: 16px;
        }
        .result-badge-approved { background-color: #dcfce7; color: #15803d; }
        .result-badge-flagged { background-color: #fee2e2; color: #b91c1c; }
        .details-grid {
            display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px;
            margin: 18px 0; background-color: #f8fafc; padding: 14px 18px;
            border-radius: var(--radius-sm); border: 1px solid var(--border-color);
        }
        .detail-label { font-size: 11.5px; color: var(--text-muted); }
        .detail-value { font-size: 13.5px; font-weight: 600; color: var(--text-main); }
        .violation-list { list-style: none; margin: 14px 0; display: flex; flex-direction: column; gap: 8px; }
        .violation-item {
            background-color: #fff1f2; border: 1px solid #fecdd3; padding: 10px 14px;
            border-radius: var(--radius-sm); color: #9f1239; font-size: 12.5px; line-height: 1.5;
        }
        .btn-submit {
            padding: 10px 18px; background-color: #0f172a; color: #ffffff; border: none;
            border-radius: var(--radius-sm); font-size: 13px; font-weight: 600;
            cursor: pointer; display: inline-flex; align-items: center; justify-content: center;
            gap: 8px; transition: all 0.15s; text-decoration: none;
        }
        .btn-submit:hover { background-color: #1e293b; }
        .btn-secondary {
            display: inline-flex; align-items: center; gap: 6px; padding: 10px 18px;
            background-color: #f1f5f9; border: 1px solid var(--border-color);
            border-radius: var(--radius-sm); font-size: 13px; font-weight: 600;
            color: #0f172a; text-decoration: none; transition: all 0.15s;
        }
        .btn-secondary:hover { background-color: #e2e8f0; }
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
            <a href="client-portal.jsp" class="nav-link active">
                <svg width="18" height="18" viewBox="0 0 24 24" style="width:18px;height:18px;min-width:18px;max-width:18px;">
                    <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                    <circle cx="12" cy="7" r="4"></circle>
                </svg>
                Client Portal
            </a>
            <a href="admin" class="nav-link">
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

    <!-- MAIN RESULT VIEW -->
    <main class="main-content">
        <header class="page-header">
            <div class="back-link-wrapper">
                <a href="client-portal.jsp" class="back-arrow" title="Back to Screening Form">&larr;</a>
                <div>
                    <h1 class="page-title">Screening Evaluation Outcome</h1>
                    <p class="page-subtitle">Deterministic evaluation result for transaction <%= tx.getTxId() %></p>
                </div>
            </div>
        </header>

        <div class="form-container">
            <div class="card">
                <!-- DYNAMIC STATUS BADGE -->
                <% if (isFlagged) { %>
                    <div class="result-badge result-badge-flagged">
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" style="width:16px;height:16px;min-width:16px;max-width:16px;">
                            <path d="m21.73 18-8-14a2 2 0 0 0-3.48 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3Z"></path>
                            <line x1="12" y1="9" x2="12" y2="13"></line>
                            <line x1="12" y1="17" x2="12.01" y2="17"></line>
                        </svg>
                        STATUS: FLAGGED (HIGH RISK)
                    </div>
                <% } else { %>
                    <div class="result-badge result-badge-approved">
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" style="width:16px;height:16px;min-width:16px;max-width:16px;">
                            <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
                            <polyline points="22 4 12 14.01 9 11.01"></polyline>
                        </svg>
                        STATUS: APPROVED (CLEARED)
                    </div>
                <% } %>

                <!-- TRANSACTION DETAILS SUMMARY -->
                <div class="details-grid">
                    <div>
                        <div class="detail-label">Transaction ID</div>
                        <div class="detail-value"><%= tx.getTxId() %></div>
                    </div>
                    <div>
                        <div class="detail-label">User Identifier</div>
                        <div class="detail-value"><%= tx.getUserId() %></div>
                    </div>
                    <div>
                        <div class="detail-label">Amount (USD)</div>
                        <div class="detail-value"><%= tx.getFormattedAmount() %></div>
                    </div>
                    <div>
                        <div class="detail-label">Origin Location</div>
                        <div class="detail-value"><%= tx.getLocation() %></div>
                    </div>
                    <div>
                        <div class="detail-label">Assigned Risk Score</div>
                        <div class="detail-value" style="color: <%= isFlagged ? "#dc2626" : "#16a34a" %>; font-size: 15px;">
                            <%= riskScore %> / 100
                        </div>
                    </div>
                    <div>
                        <div class="detail-label">Evaluated Timestamp</div>
                        <div class="detail-value"><%= tx.getTimestamp() %></div>
                    </div>
                </div>

                <!-- RULE VIOLATIONS SECTION -->
                <% if (isFlagged && violations != null && !violations.isEmpty()) { %>
                    <h3 style="font-size: 14px; font-weight: 700; color: #991b1b; margin-top: 14px;">
                        Rule Violation Telemetry:
                    </h3>
                    <ul class="violation-list">
                        <% for (String violation : violations) { %>
                            <li class="violation-item">
                                <strong>&bull;</strong> <%= violation %>
                            </li>
                        <% } %>
                    </ul>
                    <p style="font-size: 11.5px; color: #64748b; margin-top: 6px;">
                        <em>Notice: This event has been synchronously recorded to <code>logs/fraud_alerts.log</code> (File I/O) and persisted to SQLite via PreparedStatement (JDBC).</em>
                    </p>
                <% } else if (!isFlagged) { %>
                    <div style="background-color: #f0fdf4; border: 1px solid #bbf7d0; padding: 12px 16px; border-radius: 8px; color: #166534; font-size: 12.5px; margin: 14px 0;">
                        <strong>All deterministic rules passed:</strong> Transaction does not exceed threshold limits and originates from an approved jurisdiction.
                    </div>
                <% } %>

                <!-- ACTION BUTTONS -->
                <div style="display: flex; gap: 10px; margin-top: 22px;">
                    <a href="client-portal.jsp" class="btn-submit">
                        &larr; Screen Another Transaction
                    </a>
                    <a href="admin" class="btn-secondary">
                        Open Admin Dashboard
                    </a>
                </div>
            </div>
        </div>
    </main>
</div>
</body>
</html>
