package com.fraudengine.data;

import com.fraudengine.core.Transaction;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ============================================================================
 * JAVA CONCEPT 9: JDBC CONNECTIVITY (DriverManager, PreparedStatement, ResultSet)
 * ============================================================================
 * Enterprise Persistence Service utilizing pure JDBC standards.
 * 
 * Key Characteristics:
 *  1. Uses PreparedStatement for all DML operations to strictly prevent SQL Injection attacks.
 *  2. Implements Try-With-Resources (AutoCloseable) for automatic connection and statement closure.
 *  3. Connects to embedded SQLite (zero external installation required), with full table initialization.
 *  4. Provides atomic queries for transactional records, flagged incidents, and aggregate metrics.
 * ============================================================================
 */
public class DatabaseManager {

    private static volatile DatabaseManager instance;
    private static final String DEFAULT_DB_URL = "jdbc:sqlite:fraud_engine.db";

    private final String dbUrl;

    /**
     * Singleton accessor for enterprise database operations.
     */
    public static DatabaseManager getInstance() {
        if (instance == null) {
            synchronized (DatabaseManager.class) {
                if (instance == null) {
                    instance = new DatabaseManager(DEFAULT_DB_URL);
                }
            }
        }
        return instance;
    }

    /**
     * Constructor initializing the JDBC driver and database schema.
     *
     * @param dbUrl JDBC connection string
     */
    public DatabaseManager(String dbUrl) {
        this.dbUrl = dbUrl;
        loadDriver();
        initializeDatabase();
        seedInitialSampleDataIfEmpty();
    }

    /**
     * Dynamically registers the JDBC Driver.
     */
    private void loadDriver() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("[DatabaseManager] SQLite JDBC Driver not found on classpath: " + e.getMessage());
        }
    }

    /**
     * Obtains a new JDBC connection instance.
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(this.dbUrl);
    }

    /**
     * Initializes the relational database schema using PreparedStatement/Statement.
     */
    public void initializeDatabase() {
        String sql = "CREATE TABLE IF NOT EXISTS transactions (" +
                     "  tx_id TEXT PRIMARY KEY, " +
                     "  user_id TEXT NOT NULL, " +
                     "  amount REAL NOT NULL, " +
                     "  location TEXT NOT NULL, " +
                     "  timestamp TEXT NOT NULL, " +
                     "  status TEXT NOT NULL, " +
                     "  risk_score INTEGER NOT NULL, " +
                     "  breach_reason TEXT" +
                     ");";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("[DatabaseManager] Database initialized successfully. Table 'transactions' verified.");
        } catch (SQLException e) {
            System.err.println("[DatabaseManager] Error initializing database: " + e.getMessage());
        }
    }

    /**
     * Seeds initial sample record matching the screenshot reference:
     * TX-42F8YND2 | 24BBS0048 | $10,000.00 | NG | Risk: 90
     */
    private void seedInitialSampleDataIfEmpty() {
        String countSql = "SELECT COUNT(*) FROM transactions";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(countSql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next() && rs.getInt(1) == 0) {
                // Seed flagged reference transaction
                Transaction sample1 = new Transaction(
                    "TX-42F8YND2",
                    "24BBS0048",
                    10000.00,
                    "NG",
                    "10/12/2026, 3:36:21 PM",
                    Transaction.STATUS_FLAGGED,
                    90,
                    "Transaction originates from high-risk jurisdiction [NG]"
                );
                saveTransaction(sample1);

                // Seed approved reference transaction
                Transaction sample2 = new Transaction(
                    "TX-99A1K47L",
                    "U-1001",
                    250.00,
                    "US",
                    "10/12/2026, 3:40:05 PM",
                    Transaction.STATUS_APPROVED,
                    0,
                    "Passed all deterministic screening checks."
                );
                // Seed approved reference transaction 2
                Transaction sample3 = new Transaction(
                    "TX-88B2C33M",
                    "U-1002",
                    500.00,
                    "US",
                    "10/12/2026, 3:45:10 PM",
                    Transaction.STATUS_APPROVED,
                    0,
                    "Passed all deterministic screening checks."
                );
                saveTransaction(sample3);
            }
        } catch (SQLException e) {
            System.err.println("[DatabaseManager] Seeding initial demo data error: " + e.getMessage());
        }
    }

    /**
     * Resets the database to the exact reference state matching the UI benchmark:
     * 3 transactions across 2 regions (1 EMEA flagged, 2 North America approved).
     */
    public synchronized void resetToDemoState() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM transactions;");
        } catch (SQLException e) {
            System.err.println("[DatabaseManager] Error clearing transactions table: " + e.getMessage());
        }

        Transaction sample1 = new Transaction(
            "TX-42F8YND2",
            "24BBS0048",
            10000.00,
            "NG",
            "10/12/2026, 3:36:21 PM",
            Transaction.STATUS_FLAGGED,
            90,
            "Transaction originates from high-risk jurisdiction [NG]"
        );
        saveTransaction(sample1);

        Transaction sample2 = new Transaction(
            "TX-99A1K47L",
            "U-1001",
            250.00,
            "US",
            "10/12/2026, 3:40:05 PM",
            Transaction.STATUS_APPROVED,
            0,
            "Passed all deterministic screening checks."
        );
        saveTransaction(sample2);

        Transaction sample3 = new Transaction(
            "TX-88B2C33M",
            "U-1002",
            500.00,
            "US",
            "10/12/2026, 3:45:10 PM",
            Transaction.STATUS_APPROVED,
            0,
            "Passed all deterministic screening checks."
        );
        saveTransaction(sample3);
    }

    /**
     * Persists a transaction entity using PreparedStatement to prevent SQL Injection.
     *
     * @param tx Transaction domain model
     * @return boolean indicating success
     */
    public boolean saveTransaction(Transaction tx) {
        String sql = "INSERT OR REPLACE INTO transactions " +
                     "(tx_id, user_id, amount, location, timestamp, status, risk_score, breach_reason) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?);";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // Parameterized bindings strictly preventing SQL injection
            ps.setString(1, tx.getTxId());
            ps.setString(2, tx.getUserId());
            ps.setDouble(3, tx.getAmount());
            ps.setString(4, tx.getLocation());
            ps.setString(5, tx.getTimestamp());
            ps.setString(6, tx.getStatus());
            ps.setInt(7, tx.getRiskScore());
            ps.setString(8, tx.getBreachReason());

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("[DatabaseManager] Failed to persist transaction [" + tx.getTxId() + "]: " + e.getMessage());
            return false;
        }
    }

    /**
     * Fetches all records flagged as fraudulent for analyst investigation.
     *
     * @return List of flagged Transaction entities
     */
    public List<Transaction> getFlaggedTransactions() {
        List<Transaction> flaggedList = new ArrayList<>();
        String sql = "SELECT tx_id, user_id, amount, location, timestamp, status, risk_score, breach_reason " +
                     "FROM transactions WHERE status = ? ORDER BY timestamp DESC;";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, Transaction.STATUS_FLAGGED);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Transaction tx = new Transaction(
                        rs.getString("tx_id"),
                        rs.getString("user_id"),
                        rs.getDouble("amount"),
                        rs.getString("location"),
                        rs.getString("timestamp"),
                        rs.getString("status"),
                        rs.getInt("risk_score"),
                        rs.getString("breach_reason")
                    );
                    flaggedList.add(tx);
                }
            }
        } catch (SQLException e) {
            System.err.println("[DatabaseManager] Error querying flagged transactions: " + e.getMessage());
        }
        return flaggedList;
    }

    /**
     * Fetches all transactions recorded in the system.
     */
    public List<Transaction> getAllTransactions() {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT tx_id, user_id, amount, location, timestamp, status, risk_score, breach_reason " +
                     "FROM transactions ORDER BY timestamp DESC;";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Transaction tx = new Transaction(
                    rs.getString("tx_id"),
                    rs.getString("user_id"),
                    rs.getDouble("amount"),
                    rs.getString("location"),
                    rs.getString("timestamp"),
                    rs.getString("status"),
                    rs.getInt("risk_score"),
                    rs.getString("breach_reason")
                );
                list.add(tx);
            }
        } catch (SQLException e) {
            System.err.println("[DatabaseManager] Error querying transactions: " + e.getMessage());
        }
        return list;
    }

    /**
     * Computes live aggregation statistics for dashboard metric cards.
     */
    public Map<String, Object> getLiveMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        String sql = "SELECT " +
                     "  COUNT(*) AS total_screened, " +
                     "  SUM(CASE WHEN status = 'FLAGGED' THEN 1 ELSE 0 END) AS total_flagged, " +
                     "  AVG(CASE WHEN status = 'FLAGGED' THEN risk_score ELSE NULL END) AS avg_risk " +
                     "FROM transactions;";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                int totalScreened = rs.getInt("total_screened");
                int totalFlagged = rs.getInt("total_flagged");
                double avgRisk = rs.getDouble("avg_risk");

                int approved = Math.max(0, totalScreened - totalFlagged);
                double approvalRate = totalScreened > 0 ? ((double) approved / totalScreened) * 100.0 : 100.0;

                metrics.put("screened", totalScreened);
                metrics.put("flagged", totalFlagged);
                metrics.put("approved", approved);
                metrics.put("approvalRate", Math.round(approvalRate));
                metrics.put("avgRisk", Math.round(avgRisk));
            }
        } catch (SQLException e) {
            System.err.println("[DatabaseManager] Error computing live metrics: " + e.getMessage());
            metrics.put("screened", 0);
            metrics.put("flagged", 0);
            metrics.put("approvalRate", 100);
            metrics.put("avgRisk", 0);
        }
        return metrics;
    }
}
