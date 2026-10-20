package com.fraudengine.ui;

import com.fraudengine.core.AbstractRule;
import com.fraudengine.core.EngineEvaluator;
import com.fraudengine.core.Transaction;
import com.fraudengine.data.AuditLogger;
import com.fraudengine.data.DatabaseManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.List;

/**
 * ============================================================================
 * JAVA CONCEPT 8: JAVAFX (For desktop UI)
 * JAVA CONCEPT 1: INHERITANCE (Extends javafx.application.Application)
 * JAVA CONCEPT 7: COLLECTIONS FRAMEWORK (ObservableList, List<Transaction>)
 * JAVA CONCEPT 9: JDBC CONNECTIVITY (Fetches live records via DatabaseManager)
 * JAVA CONCEPT 6: FILE HANDLING (Reads live log stream via AuditLogger)
 * ============================================================================
 * Analyst Dashboard Desktop Application.
 * 
 * Interactive enterprise desktop GUI for compliance officers and fraud analysts.
 * Features:
 *  - Checkboxes to dynamically toggle AbstractRule instances ON/OFF at runtime.
 *  - Interactive TableView populated directly via PreparedStatement JDBC queries.
 *  - Embedded Log Console (TextArea) displaying synchronous entries read from disk.
 *  - Quick Test screening launcher to observe live rule dispatches.
 * ============================================================================
 */
public class AnalystDashboard extends Application {

    private final EngineEvaluator evaluator = EngineEvaluator.getInstance();
    private final DatabaseManager dbManager = DatabaseManager.getInstance();

    private final ObservableList<TransactionModel> tableData = FXCollections.observableArrayList();
    private TableView<TransactionModel> tableView;
    private TextArea logConsole;
    private Label activeRulesCountLabel;
    private Label screenedCountLabel;
    private Label flaggedCountLabel;
    private Label avgRiskLabel;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("FraudEngine - Analyst Desktop Dashboard");

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f8fafc;");

        // 1. Header Bar
        HBox header = createHeader();
        root.setTop(header);

        // 2. Main Content
        VBox mainContent = new VBox(20);
        mainContent.setPadding(new Insets(24));

        // Metric Stat Cards Row
        HBox statsRow = createStatsRow();

        // Split Panes: Left (Rule Controls) | Right (Flagged Table & Log Console)
        HBox centerContent = new HBox(20);
        VBox leftPane = createRuleControlsPane();
        VBox rightPane = createAnalyticsPane();

        HBox.setHgrow(leftPane, Priority.NEVER);
        HBox.setHgrow(rightPane, Priority.ALWAYS);

        centerContent.getChildren().addAll(leftPane, rightPane);
        mainContent.getChildren().addAll(statsRow, centerContent);

        ScrollPane scrollPane = new ScrollPane(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: #f8fafc;");
        root.setCenter(scrollPane);

        // Initial Data Load
        refreshTableData();
        loadAuditLogs();
        updateMetrics();

        Scene scene = new Scene(root, 1180, 780);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private HBox createHeader() {
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(16, 24, 16, 24));
        header.setStyle("-fx-background-color: #0c1022;");

        Label logoIcon = new Label("🛡");
        logoIcon.setStyle("-fx-font-size: 20px; -fx-text-fill: #818cf8;");

        Label title = new Label("FraudEngine — Analyst Desktop Dashboard");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        title.setStyle("-fx-text-fill: #ffffff;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label subTitle = new Label("Deterministic Rule-Based Engine v1.0");
        subTitle.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 13px;");

        header.getChildren().addAll(logoIcon, title, spacer, subTitle);
        return header;
    }

    private HBox createStatsRow() {
        HBox stats = new HBox(16);
        stats.setAlignment(Pos.CENTER);

        activeRulesCountLabel = new Label(String.valueOf(evaluator.getActiveRuleCount()));
        screenedCountLabel = new Label("0");
        flaggedCountLabel = new Label("0");
        avgRiskLabel = new Label("0");

        stats.getChildren().addAll(
            createStatCard("Active Rules", activeRulesCountLabel, "🎯", "#4f46e5"),
            createStatCard("Screened", screenedCountLabel, "📈", "#0284c7"),
            createStatCard("Flagged", flaggedCountLabel, "⚠️", "#dc2626"),
            createStatCard("Avg Risk", avgRiskLabel, "⚡", "#d97706")
        );
        return stats;
    }

    private VBox createStatCard(String title, Label valueLabel, String icon, String colorHex) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(16, 20, 16, 20));
        card.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 12; " +
                      "-fx-border-color: #e2e8f0; -fx-border-radius: 12; -fx-border-width: 1;");
        card.setPrefWidth(260);

        HBox top = new HBox();
        top.setAlignment(Pos.CENTER_LEFT);

        Label lblTitle = new Label(title);
        lblTitle.setStyle("-fx-text-fill: #64748b; -fx-font-size: 13px; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label lblIcon = new Label(icon);
        lblIcon.setStyle("-fx-font-size: 16px; -fx-background-color: #f1f5f9; -fx-padding: 6 10 6 10; -fx-background-radius: 8;");

        top.getChildren().addAll(lblTitle, spacer, lblIcon);

        valueLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));
        valueLabel.setStyle("-fx-text-fill: " + colorHex + ";");

        card.getChildren().addAll(top, valueLabel);
        return card;
    }

    private VBox createRuleControlsPane() {
        VBox pane = new VBox(16);
        pane.setPadding(new Insets(20));
        pane.setPrefWidth(350);
        pane.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 12; " +
                      "-fx-border-color: #e2e8f0; -fx-border-radius: 12; -fx-border-width: 1;");

        Label title = new Label("Rule Controls");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        title.setStyle("-fx-text-fill: #0f172a;");

        Label subtitle = new Label("Active Rules — toggle ON/OFF at runtime");
        subtitle.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px;");

        pane.getChildren().addAll(title, subtitle);

        // Dynamically populate checkboxes for each AbstractRule
        List<AbstractRule> rules = evaluator.getRules();
        for (AbstractRule rule : rules) {
            VBox ruleBox = new VBox(4);
            ruleBox.setPadding(new Insets(12));
            ruleBox.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 8; -fx-border-color: #e2e8f0; -fx-border-radius: 8;");

            CheckBox cb = new CheckBox(rule.getRuleName());
            cb.setSelected(rule.isEnabled());
            cb.setStyle("-fx-font-weight: bold; -fx-text-fill: #1e293b;");

            // Event handler to toggle rule at runtime (POLYMORPHISM)
            cb.setOnAction(e -> {
                rule.setEnabled(cb.isSelected());
                activeRulesCountLabel.setText(String.valueOf(evaluator.getActiveRuleCount()));
                System.out.println("[AnalystDashboard] Toggled rule [" + rule.getRuleId() + "] -> " + rule.isEnabled());
            });

            Label desc = new Label(rule.getDescription());
            desc.setWrapText(true);
            desc.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px;");

            ruleBox.getChildren().addAll(cb, desc);
            pane.getChildren().add(ruleBox);
        }

        // Quick simulation test button
        Button btnTestScreen = new Button("Screen Sample High-Risk Transaction");
        btnTestScreen.setStyle("-fx-background-color: #0f172a; -fx-text-fill: white; " +
                               "-fx-font-weight: bold; -fx-padding: 10 16 10 16; -fx-background-radius: 8;");
        btnTestScreen.setMaxWidth(Double.MAX_VALUE);
        btnTestScreen.setOnAction(e -> {
            Transaction simTx = new Transaction(
                "TX-" + Long.toHexString(System.currentTimeMillis()).toUpperCase().substring(4),
                "SIM-USER",
                15000.00,
                "NG"
            );
            evaluator.processTransaction(simTx);
            if (simTx.isFlagged()) {
                AuditLogger.logFraudAlert(simTx, simTx.getRiskScore());
            }
            dbManager.saveTransaction(simTx);
            refreshTableData();
            loadAuditLogs();
            updateMetrics();
        });

        pane.getChildren().add(btnTestScreen);
        return pane;
    }

    @SuppressWarnings("unchecked")
    private VBox createAnalyticsPane() {
        VBox pane = new VBox(16);

        // --- Card 1: Flagged Records Table ---
        VBox tableCard = new VBox(12);
        tableCard.setPadding(new Insets(20));
        tableCard.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 12; " +
                           "-fx-border-color: #e2e8f0; -fx-border-radius: 12; -fx-border-width: 1;");

        HBox tableHeader = new HBox(12);
        tableHeader.setAlignment(Pos.CENTER_LEFT);

        Label tblTitle = new Label("Flagged Records");
        tblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        tblTitle.setStyle("-fx-text-fill: #0f172a;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnRefresh = new Button("🔄 Refresh DB");
        btnRefresh.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #0f172a; -fx-font-weight: bold; -fx-padding: 6 12 6 12; -fx-background-radius: 6;");
        btnRefresh.setOnAction(e -> {
            refreshTableData();
            updateMetrics();
        });

        Button btnLoadLog = new Button("📄 Load Audit Log");
        btnLoadLog.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #0f172a; -fx-font-weight: bold; -fx-padding: 6 12 6 12; -fx-background-radius: 6;");
        btnLoadLog.setOnAction(e -> loadAuditLogs());

        tableHeader.getChildren().addAll(tblTitle, spacer, btnRefresh, btnLoadLog);

        // TableView Configuration
        tableView = new TableView<>();
        tableView.setItems(tableData);
        tableView.setPrefHeight(220);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<TransactionModel, String> colTxId = new TableColumn<>("Tx ID");
        colTxId.setCellValueFactory(new PropertyValueFactory<>("txId"));

        TableColumn<TransactionModel, String> colUser = new TableColumn<>("User");
        colUser.setCellValueFactory(new PropertyValueFactory<>("userId"));

        TableColumn<TransactionModel, String> colAmount = new TableColumn<>("Amount");
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));

        TableColumn<TransactionModel, String> colLocation = new TableColumn<>("Location");
        colLocation.setCellValueFactory(new PropertyValueFactory<>("location"));

        TableColumn<TransactionModel, Integer> colRisk = new TableColumn<>("Risk");
        colRisk.setCellValueFactory(new PropertyValueFactory<>("riskScore"));
        colRisk.setCellFactory(column -> new TableCell<TransactionModel, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.toString());
                    setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold;");
                }
            }
        });

        tableView.getColumns().addAll(colTxId, colUser, colAmount, colLocation, colRisk);
        tableCard.getChildren().addAll(tableHeader, tableView);

        // --- Card 2: Log Console ---
        VBox consoleCard = new VBox(12);
        consoleCard.setPadding(new Insets(20));
        consoleCard.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 12; " +
                             "-fx-border-color: #e2e8f0; -fx-border-radius: 12; -fx-border-width: 1;");

        Label consoleTitle = new Label("Log Console");
        consoleTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        consoleTitle.setStyle("-fx-text-fill: #0f172a;");

        logConsole = new TextArea();
        logConsole.setEditable(false);
        logConsole.setPrefHeight(180);
        logConsole.setStyle("-fx-control-inner-background: #0b1120; -fx-font-family: 'Consolas', monospace; " +
                            "-fx-text-fill: #38bdf8; -fx-highlight-fill: #1e293b;");
        logConsole.setText("Audit log lines appear here after loading.");

        consoleCard.getChildren().addAll(consoleTitle, logConsole);
        pane.getChildren().addAll(tableCard, consoleCard);

        return pane;
    }

    private void refreshTableData() {
        List<Transaction> flaggedList = dbManager.getFlaggedTransactions();
        tableData.clear();
        for (Transaction t : flaggedList) {
            tableData.add(new TransactionModel(
                t.getTxId(),
                t.getUserId(),
                t.getFormattedAmount(),
                t.getLocation(),
                t.getRiskScore()
            ));
        }
    }

    private void loadAuditLogs() {
        String logContent = AuditLogger.readAuditLogsAsString();
        logConsole.setText(logContent);
    }

    private void updateMetrics() {
        var metrics = dbManager.getLiveMetrics();
        Platform.runLater(() -> {
            activeRulesCountLabel.setText(String.valueOf(evaluator.getActiveRuleCount()));
            screenedCountLabel.setText(String.valueOf(metrics.getOrDefault("screened", 0)));
            flaggedCountLabel.setText(String.valueOf(metrics.getOrDefault("flagged", 0)));
            avgRiskLabel.setText(String.valueOf(metrics.getOrDefault("avgRisk", 0)));
        });
    }

    /**
     * JavaFX TableView Model Wrapper.
     */
    public static class TransactionModel {
        private final SimpleStringProperty txId;
        private final SimpleStringProperty userId;
        private final SimpleStringProperty amount;
        private final SimpleStringProperty location;
        private final SimpleIntegerProperty riskScore;

        public TransactionModel(String txId, String userId, String amount, String location, int riskScore) {
            this.txId = new SimpleStringProperty(txId);
            this.userId = new SimpleStringProperty(userId);
            this.amount = new SimpleStringProperty(amount);
            this.location = new SimpleStringProperty(location);
            this.riskScore = new SimpleIntegerProperty(riskScore);
        }

        public String getTxId() { return txId.get(); }
        public String getUserId() { return userId.get(); }
        public String getAmount() { return amount.get(); }
        public String getLocation() { return location.get(); }
        public int getRiskScore() { return riskScore.get(); }
    }
}
