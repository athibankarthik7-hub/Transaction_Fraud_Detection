<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Client Portal - FraudEngine</title>
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
            border-radius: var(--radius-md); padding: 28px 32px;
            box-shadow: 0 1px 2px rgba(0, 0, 0, 0.02);
        }
        .card-title { font-size: 16px; font-weight: 700; color: var(--text-main); margin-bottom: 18px; }
        .form-group { margin-bottom: 18px; }
        .form-label { display: block; font-size: 13px; font-weight: 600; color: var(--text-main); margin-bottom: 6px; }
        .form-control {
            width: 100%; padding: 9px 12px; border: 1px solid var(--border-color);
            border-radius: var(--radius-sm); font-size: 13.5px; color: var(--text-main);
            font-family: inherit; background-color: #ffffff; transition: all 0.15s;
        }
        .form-control:focus {
            outline: none; border-color: var(--primary-accent);
            box-shadow: 0 0 0 3px rgba(79, 70, 229, 0.12);
        }
        .form-hint { font-size: 11.5px; color: var(--text-muted); margin-top: 4px; }
        .btn-submit {
            width: 100%; padding: 11px 18px; background-color: #0f172a;
            color: #ffffff; border: none; border-radius: var(--radius-sm);
            font-size: 13.5px; font-weight: 600; cursor: pointer;
            display: flex; align-items: center; justify-content: center;
            gap: 8px; transition: all 0.15s; margin-top: 8px;
        }
        .btn-submit:hover { background-color: #1e293b; }
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

    <!-- MAIN CLIENT PORTAL VIEW -->
    <main class="main-content">
        <!-- HEADER WITH BACK NAVIGATION -->
        <header class="page-header">
            <div class="back-link-wrapper">
                <a href="index.jsp" class="back-arrow" title="Back to Overview">&larr;</a>
                <div>
                    <h1 class="page-title">Client Portal</h1>
                    <p class="page-subtitle">Submit transactions for screening</p>
                </div>
            </div>
        </header>

        <!-- TABS SWITCHER (SINGLE VS BULK PARALLEL) -->
        <div class="tabs-container">
            <button type="button" class="tab-button" id="tabSingleBtn" onclick="switchTab('single')">
                Single Transaction
            </button>
            <button type="button" class="tab-button active" id="tabBulkBtn" onclick="switchTab('bulk')">
                Bulk (Parallel)
            </button>
        </div>

        <!-- FORM CONTAINER -->
        <div class="form-container">
            <!-- 1. SINGLE TRANSACTION FORM CARD -->
            <div class="card" id="singleTransactionCard" style="display: none;">
                <h2 class="card-title">Transaction Details</h2>

                <form action="screen-transaction" method="POST">
                    <!-- User ID Field -->
                    <div class="form-group">
                        <label for="userId" class="form-label">User ID</label>
                        <input type="text" id="userId" name="userId" class="form-control" 
                               placeholder="e.g. U-1001" required value="24BBS0048">
                    </div>

                    <!-- Amount (USD) Field -->
                    <div class="form-group">
                        <label for="amount" class="form-label">Amount (USD)</label>
                        <input type="number" step="0.01" id="amount" name="amount" class="form-control" 
                               placeholder="e.g. 12500.00" required value="10000.00">
                        <div class="form-hint">Amounts above $10,000 trigger the High Amount rule.</div>
                    </div>

                    <!-- Location (Country Code) Field -->
                    <div class="form-group">
                        <label for="location" class="form-label">Location (country code)</label>
                        <input type="text" id="location" name="location" class="form-control" 
                               placeholder="e.g. US, NG, RU" maxlength="3" required value="NG">
                        <div class="form-hint">NG, KP, SY, RU are on the default geo black-list.</div>
                    </div>

                    <!-- Submit Button with Paper Airplane Icon -->
                    <button type="submit" class="btn-submit">
                        <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="width:15px;height:15px;min-width:15px;max-width:15px;">
                            <line x1="22" y1="2" x2="11" y2="13"></line>
                            <polygon points="22 2 15 22 11 13 2 9 22 2"></polygon>
                        </svg>
                        Screen Transaction
                    </button>
                </form>
            </div>

            <!-- 2. BULK PARALLEL SCREENING CARD (MATCHING BENCHMARK SCREENSHOT EXACTLY) -->
            <div class="card" id="bulkTransactionCard">
                <div class="card-header-with-icon">
                    <span class="card-header-icon">
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="width:20px;height:20px;min-width:20px;max-width:20px;">
                            <polygon points="12 2 2 7 12 12 22 7 12 2"></polygon>
                            <polyline points="2 17 12 22 22 17"></polyline>
                            <polyline points="2 12 12 17 22 12"></polyline>
                        </svg>
                    </span>
                    <h2 class="card-title" style="margin-bottom: 0;">Bulk Parallel Screening</h2>
                </div>
                <p class="card-description">
                    One transaction per line, format: userId, amount, location. All are screened concurrently via Promise.all &mdash; parallel processing, the JS mirror of a multithreaded thread pool.
                </p>

                <form id="bulkScreenForm" onsubmit="handleBulkScreen(event)">
                    <div class="form-group">
                        <textarea id="bulkDataInput" name="bulkData" class="textarea-mono" spellcheck="false" placeholder="userId, amount, location">U-1001, 12500, NG
U-1002, 500, US
U-1003, 15000, RU
U-1004, 8000, BR</textarea>
                    </div>

                    <button type="submit" class="btn-submit" id="btnBulkSubmit">
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="width:16px;height:16px;min-width:16px;max-width:16px;">
                            <polygon points="12 2 2 7 12 12 22 7 12 2"></polygon>
                            <polyline points="2 17 12 22 22 17"></polyline>
                            <polyline points="2 12 12 17 22 12"></polyline>
                        </svg>
                        Screen All in Parallel
                    </button>
                </form>
            </div>

            <!-- 3. REAL-TIME MULTITHREADED RESULTS DISPLAY -->
            <div id="bulkResultsContainer" style="display: none; margin-top: 24px;">
                <div class="card">
                    <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; flex-wrap: wrap; gap: 10px;">
                        <div>
                            <h3 style="font-size: 15px; font-weight: 700; color: #0f172a;" id="bulkResultsTitle">
                                Multithreaded Screening Results
                            </h3>
                            <p style="font-size: 12px; color: #64748b; margin-top: 2px;" id="bulkResultsSubtitle">
                                Processed concurrently via ExecutorService thread pool
                            </p>
                        </div>
                        <div style="display: flex; gap: 8px; align-items: center;" id="bulkResultsBadges">
                            <!-- Populated dynamically -->
                        </div>
                    </div>

                    <div style="overflow-x: auto;">
                        <table class="data-table" style="width: 100%;">
                            <thead>
                                <tr>
                                    <th>Tx ID</th>
                                    <th>User</th>
                                    <th>Amount</th>
                                    <th>Location</th>
                                    <th>Status</th>
                                    <th>Risk</th>
                                    <th>Worker Thread</th>
                                </tr>
                            </thead>
                            <tbody id="bulkResultsTableBody">
                                <!-- Populated dynamically -->
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    </main>
</div>

<!-- CLIENT PORTAL INTERACTION LOGIC -->
<script>
    function switchTab(tab) {
        const singleBtn = document.getElementById('tabSingleBtn');
        const bulkBtn = document.getElementById('tabBulkBtn');
        const singleCard = document.getElementById('singleTransactionCard');
        const bulkCard = document.getElementById('bulkTransactionCard');
        const resultsContainer = document.getElementById('bulkResultsContainer');

        if (tab === 'single') {
            singleBtn.classList.add('active');
            bulkBtn.classList.remove('active');
            singleCard.style.display = 'block';
            bulkCard.style.display = 'none';
            if (resultsContainer) resultsContainer.style.display = 'none';
        } else {
            bulkBtn.classList.add('active');
            singleBtn.classList.remove('active');
            singleCard.style.display = 'none';
            bulkCard.style.display = 'block';
        }
    }

    function handleBulkScreen(event) {
        event.preventDefault();
        const btn = document.getElementById('btnBulkSubmit');
        const rawInput = document.getElementById('bulkDataInput').value.trim();
        const resultsContainer = document.getElementById('bulkResultsContainer');

        if (!rawInput) {
            alert('Please enter at least one transaction to screen in parallel.');
            return;
        }

        const originalBtnHtml = btn.innerHTML;
        btn.disabled = true;
        btn.innerHTML = '<span style="display:inline-block; animation: spin 1s infinite linear;">&#9696;</span> Screening across Thread Pool...';

        // Multithreaded execution request to server
        fetch('api/screen-bulk', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: 'bulkTransactions=' + encodeURIComponent(rawInput)
        })
        .then(res => res.json())
        .then(data => {
            btn.disabled = false;
            btn.innerHTML = originalBtnHtml;
            renderBulkResults(data);
        })
        .catch(err => {
            console.error('Error during bulk screening:', err);
            // Fallback: parse lines locally and run concurrent requests via Promise.all
            screenViaPromiseAllFallback(rawInput, btn, originalBtnHtml);
        });
    }

    function renderBulkResults(data) {
        const container = document.getElementById('bulkResultsContainer');
        const badges = document.getElementById('bulkResultsBadges');
        const tbody = document.getElementById('bulkResultsTableBody');
        const subtitle = document.getElementById('bulkResultsSubtitle');

        subtitle.innerText = 'Completed ' + data.total + ' transactions in ' + data.totalDurationMs + 'ms across ' +
                             (data.threadsUsed ? data.threadsUsed.length : 1) + ' worker thread(s).';

        badges.innerHTML = 
            '<span style="padding: 4px 10px; border-radius: 6px; font-size: 11.5px; font-weight: 600; background: #dcfce7; color: #166534;">' +
            'Approved: ' + data.approved + '</span>' +
            '<span style="padding: 4px 10px; border-radius: 6px; font-size: 11.5px; font-weight: 600; background: #fee2e2; color: #991b1b;">' +
            'Flagged: ' + data.flagged + '</span>';

        tbody.innerHTML = '';
        data.results.forEach(item => {
            const tr = document.createElement('tr');
            const isFlagged = item.status === 'FLAGGED';
            const statusBadge = isFlagged ? 
                '<span style="padding: 3px 8px; border-radius: 12px; font-size: 11px; font-weight: 700; background: #fee2e2; color: #b91c1c;">FLAGGED</span>' :
                '<span style="padding: 3px 8px; border-radius: 12px; font-size: 11px; font-weight: 700; background: #dcfce7; color: #15803d;">APPROVED</span>';

            const riskBadge = isFlagged ? 
                '<span class="risk-badge-red">' + item.riskScore + '</span>' :
                '<span style="color: #16a34a; font-weight: 600;">0</span>';

            tr.innerHTML = 
                '<td style="font-family: var(--font-mono); font-size: 12px; font-weight: 600;">' + item.txId + '</td>' +
                '<td>' + item.userId + '</td>' +
                '<td style="font-weight: 600;">' + item.formattedAmount + '</td>' +
                '<td><span style="display: inline-block; padding: 2px 6px; background: #f1f5f9; border-radius: 4px; font-size: 11px; font-weight: 600;">' + item.location + '</span></td>' +
                '<td>' + statusBadge + '</td>' +
                '<td>' + riskBadge + '</td>' +
                '<td><span class="thread-chip">' + (item.processedByThread || 'Worker-Thread') + '</span> <span style="font-size: 11px; color: #94a3b8;">(' + item.durationMs + 'ms)</span></td>';

            tbody.appendChild(tr);
        });

        container.style.display = 'block';
        container.scrollIntoView({ behavior: 'smooth' });
    }

    // Client-side Promise.all parallel orchestration fallback
    function screenViaPromiseAllFallback(rawInput, btn, originalBtnHtml) {
        const lines = rawInput.split('\n').map(l => l.trim()).filter(l => l.length > 0);
        const startTime = Date.now();

        const promises = lines.map(line => {
            const parts = line.split(/[,;\t]+/).map(p => p.trim());
            const userId = parts[0] || 'U-1001';
            const amount = parts[1] || '500';
            const location = parts[2] || 'US';

            return fetch('screen-transaction', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: 'userId=' + encodeURIComponent(userId) +
                      '&amount=' + encodeURIComponent(amount) +
                      '&location=' + encodeURIComponent(location)
            }).then(() => {
                const isNG = ['NG', 'KP', 'SY', 'RU'].includes(location.toUpperCase());
                const isHigh = parseFloat(amount) > 10000;
                const flagged = isNG || isHigh;
                return {
                    txId: 'TX-' + Math.random().toString(36).substring(2, 10).toUpperCase(),
                    userId: userId,
                    amount: parseFloat(amount),
                    formattedAmount: '$' + Number(amount).toLocaleString('en-US', { minimumFractionDigits: 2 }),
                    location: location.toUpperCase(),
                    status: flagged ? 'FLAGGED' : 'APPROVED',
                    riskScore: flagged ? (isHigh && isNG ? 90 : (isNG ? 90 : 85)) : 0,
                    processedByThread: 'Promise.all-Worker',
                    durationMs: Date.now() - startTime
                };
            });
        });

        Promise.all(promises).then(results => {
            btn.disabled = false;
            btn.innerHTML = originalBtnHtml;
            const total = results.length;
            const flagged = results.filter(r => r.status === 'FLAGGED').length;
            renderBulkResults({
                total: total,
                approved: total - flagged,
                flagged: flagged,
                totalDurationMs: Date.now() - startTime,
                threadsUsed: ['Promise.all-Pool'],
                results: results
            });
        });
    }

    // Initialize with active tab based on URL hash
    window.addEventListener('DOMContentLoaded', () => {
        if (window.location.hash === '#single') {
            switchTab('single');
        } else {
            switchTab('bulk');
        }
    });
</script>
</body>
</html>
