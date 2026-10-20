<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.fraudengine.core.EngineEvaluator" %>
<%@ page import="com.fraudengine.data.DatabaseManager" %>
<%@ page import="java.util.Map" %>
<%
    // JAVA CONCEPT 11: JSP (Server-Side Dynamic Rendering)
    EngineEvaluator evaluator = EngineEvaluator.getInstance();
    DatabaseManager dbManager = DatabaseManager.getInstance();
    Map<String, Object> metrics = dbManager.getLiveMetrics();

    int activeRules = evaluator.getActiveRuleCount();
    int screened = (int) metrics.getOrDefault("screened", evaluator.getScreenedCount());
    int flagged = (int) metrics.getOrDefault("flagged", evaluator.getFlaggedCount());
    long approvalRate = Math.round(((Number) metrics.getOrDefault("approvalRate", evaluator.getApprovalRate())).doubleValue());
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>FraudEngine - Deterministic Rule-Based Engine</title>
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
            --hero-bg: #1e1b4b;
            --hero-btn-dark: #111427;
            --hero-btn-hover: #1e223d;
            --radius-sm: 8px;
            --radius-md: 12px;
            --radius-lg: 16px;
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
        .hero-banner {
            background-color: var(--hero-bg); border-radius: var(--radius-lg);
            padding: 32px 36px; color: #ffffff; margin-bottom: 20px;
            box-shadow: 0 4px 20px rgba(15, 23, 42, 0.06);
        }
        .hero-pill {
            display: inline-flex; align-items: center; gap: 8px;
            font-size: 12px; font-weight: 600; color: #a5b4fc; margin-bottom: 12px;
        }
        .hero-title { font-size: 26px; font-weight: 700; margin-bottom: 8px; letter-spacing: -0.02em; }
        .hero-desc {
            font-size: 13.5px; color: #cbd5e1; max-width: 650px; line-height: 1.55; margin-bottom: 22px;
        }
        .hero-buttons { display: flex; gap: 12px; align-items: center; }
        .btn-hero-primary {
            background-color: var(--hero-btn-dark); border: 1px solid rgba(255, 255, 255, 0.15);
            color: #ffffff; padding: 9px 18px; border-radius: var(--radius-sm);
            font-size: 13px; font-weight: 600; text-decoration: none; display: inline-flex;
            align-items: center; gap: 8px; transition: all 0.2s;
        }
        .btn-hero-primary:hover { background-color: var(--hero-btn-hover); border-color: rgba(255, 255, 255, 0.3); }
        .btn-hero-secondary {
            background-color: rgba(255, 255, 255, 0.1); border: 1px solid transparent;
            color: #ffffff; padding: 9px 18px; border-radius: var(--radius-sm);
            font-size: 13px; font-weight: 600; text-decoration: none; transition: all 0.2s;
        }
        .btn-hero-secondary:hover { background-color: rgba(255, 255, 255, 0.18); }
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
        .stat-icon-green { background-color: #dcfce7; color: #16a34a; }
        .action-cards-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 18px; }
        .action-card {
            background-color: var(--bg-card); border: 1px solid var(--border-color);
            border-radius: var(--radius-md); padding: 24px 26px; display: flex;
            flex-direction: column; justify-content: space-between;
            box-shadow: 0 1px 2px rgba(0, 0, 0, 0.02);
        }
        .action-card-header { display: flex; align-items: center; gap: 12px; margin-bottom: 10px; }
        .action-card-icon {
            width: 38px; height: 38px; min-width: 38px; border-radius: 9px;
            display: flex; align-items: center; justify-content: center;
        }
        .action-card-title { font-size: 16px; font-weight: 700; color: var(--text-main); }
        .action-card-desc { font-size: 13px; color: var(--text-muted); line-height: 1.55; margin-bottom: 20px; }
        .action-card-btn {
            display: inline-flex; align-items: center; gap: 6px; padding: 7px 14px;
            background-color: #ffffff; border: 1px solid var(--border-color);
            border-radius: var(--radius-sm); color: var(--text-main); text-decoration: none;
            font-size: 12.5px; font-weight: 600; width: fit-content; transition: all 0.15s;
        }
        .action-card-btn:hover { background-color: #f1f5f9; }
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
            <a href="index.jsp" class="nav-link active">
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

    <!-- MAIN OVERVIEW VIEW -->
    <main class="main-content">
        <!-- HERO BANNER -->
        <section class="hero-banner">
            <div class="hero-pill">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="width:14px;height:14px;min-width:14px;max-width:14px;">
                    <circle cx="12" cy="12" r="10"></circle>
                    <path d="m4.93 4.93 4.24 4.24"></path>
                    <path d="m14.83 9.17 4.24-4.24"></path>
                    <path d="m14.83 14.83 4.24 4.24"></path>
                    <path d="m9.17 14.83-4.24 4.24"></path>
                </svg>
                Deterministic Rule-Based Engine
            </div>
            <h1 class="hero-title">Fraud Detection Engine</h1>
            <p class="hero-desc">
                A production-grade, rule-driven screening system that evaluates transactions
                against configurable rules in real time — flagging high amounts and black-listed
                locations.
            </p>
            <div class="hero-buttons">
                <a href="client-portal.jsp" class="btn-hero-primary">
                    Open Client Portal &rarr;
                </a>
                <a href="admin" class="btn-hero-secondary">
                    Admin Dashboard
                </a>
            </div>
        </section>

        <!-- 4 STAT METRIC CARDS -->
        <section class="stats-grid">
            <!-- Active Rules -->
            <div class="stat-card">
                <div class="stat-info">
                    <span class="stat-label">Active Rules</span>
                    <span class="stat-value"><%= activeRules %></span>
                </div>
                <div class="stat-icon-wrapper stat-icon-purple">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="width:18px;height:18px;min-width:18px;max-width:18px;">
                        <circle cx="12" cy="12" r="10"></circle>
                        <circle cx="12" cy="12" r="6"></circle>
                        <circle cx="12" cy="12" r="2"></circle>
                    </svg>
                </div>
            </div>

            <!-- Screened -->
            <div class="stat-card">
                <div class="stat-info">
                    <span class="stat-label">Screened</span>
                    <span class="stat-value"><%= screened %></span>
                </div>
                <div class="stat-icon-wrapper stat-icon-blue">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="width:18px;height:18px;min-width:18px;max-width:18px;">
                        <polyline points="22 12 18 12 15 21 9 3 6 12 2 12"></polyline>
                    </svg>
                </div>
            </div>

            <!-- Flagged -->
            <div class="stat-card">
                <div class="stat-info">
                    <span class="stat-label">Flagged</span>
                    <span class="stat-value"><%= flagged %></span>
                </div>
                <div class="stat-icon-wrapper stat-icon-red">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="width:18px;height:18px;min-width:18px;max-width:18px;">
                        <path d="m21.73 18-8-14a2 2 0 0 0-3.48 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3Z"></path>
                        <line x1="12" y1="9" x2="12" y2="13"></line>
                        <line x1="12" y1="17" x2="12.01" y2="17"></line>
                    </svg>
                </div>
            </div>

            <!-- Approval Rate -->
            <div class="stat-card">
                <div class="stat-info">
                    <span class="stat-label">Approval Rate</span>
                    <span class="stat-value"><%= approvalRate %>%</span>
                </div>
                <div class="stat-icon-wrapper stat-icon-green">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="width:18px;height:18px;min-width:18px;max-width:18px;">
                        <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
                        <polyline points="22 4 12 14.01 9 11.01"></polyline>
                    </svg>
                </div>
            </div>
        </section>

        <!-- 2 ACTION CARDS -->
        <section class="action-cards-grid">
            <div class="action-card">
                <div>
                    <div class="action-card-header">
                        <div class="action-card-icon stat-icon-purple">
                            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="width:20px;height:20px;min-width:20px;max-width:20px;">
                                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                                <circle cx="12" cy="7" r="4"></circle>
                            </svg>
                        </div>
                        <h2 class="action-card-title">Client Portal</h2>
                    </div>
                    <p class="action-card-desc">
                        Submit transactions for screening and view APPROVED / FLAGGED results with rule-violation details.
                    </p>
                </div>
                <a href="client-portal.jsp" class="action-card-btn">
                    Open portal &rarr;
                </a>
            </div>

            <div class="action-card">
                <div>
                    <div class="action-card-header">
                        <div class="action-card-icon" style="background-color: #f1f5f9; color: #334155;">
                            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="width:20px;height:20px;min-width:20px;max-width:20px;">
                                <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"></path>
                            </svg>
                        </div>
                        <h2 class="action-card-title">Admin Dashboard</h2>
                    </div>
                    <p class="action-card-desc">
                        Toggle rules at runtime, review flagged records in a table, and inspect the audit log console.
                    </p>
                </div>
                <a href="admin" class="action-card-btn">
                    Open dashboard &rarr;
                </a>
            </div>
        </section>
    </main>
</div>
</body>
</html>
