# Deterministic Rule-Based Fraud Detection Engine
### Production-Grade Enterprise Java Architecture & Exact UI Replica

A complete, enterprise-grade, deterministic transaction screening and compliance engine designed to replicate the FinTech fraud portal pixel-for-pixel while strictly integrating and showcasing **all 11 mandatory Java concepts**.

---

## 🏛️ System Architecture & 11 Core Java Concepts Mapping

Every single mandatory Java concept has been implemented with enterprise rigor, full comments, and clean separation of concerns:

| # | Java Concept | Component / File | Enterprise Architecture Role & Explanation |
|---|---|---|---|
| **1** | **Inheritance** | [`HighAmountRule.java`](src/main/java/com/fraudengine/rules/HighAmountRule.java)<br>[`BlacklistGeoRule.java`](src/main/java/com/fraudengine/rules/BlacklistGeoRule.java)<br>[`AnalystDashboard.java`](src/main/java/com/fraudengine/ui/AnalystDashboard.java) | `HighAmountRule` and `BlacklistGeoRule` extend `AbstractRule`, inheriting state fields (`ruleId`, `ruleName`, `enabled`) and accessor methods while specializing behavior. `AnalystDashboard` extends JavaFX `Application`. |
| **2** | **Polymorphism** | [`EngineEvaluator.java`](src/main/java/com/fraudengine/core/EngineEvaluator.java) | Dynamic method dispatch in `processTransaction()`: iterates through `List<AbstractRule>` where each element's runtime type determines whether `HighAmountRule.evaluate()` or `BlacklistGeoRule.evaluate()` executes. |
| **3** | **Packages (Modular Structure)** | `com.fraudengine.*` | Organized into 6 distinct, decoupled packages:<br>&bull; `core`: Domain models & evaluation pipeline<br>&bull; `exceptions`: Domain violation exceptions<br>&bull; `rules`: Deterministic rule implementations<br>&bull; `data`: Persistence & synchronous file logging<br>&bull; `web`: HTTP Servlets & MVC controller<br>&bull; `ui`: JavaFX analyst desktop console |
| **4** | **Abstract Class** | [`AbstractRule.java`](src/main/java/com/fraudengine/core/AbstractRule.java) | Abstract base class encapsulating rule state and concrete getters/setters, while enforcing an abstract method contract: `public abstract void evaluate(Transaction tx) throws FraudDetectedException;`. Cannot be instantiated directly. |
| **5** | **User Defined Exceptions** | [`FraudDetectedException.java`](src/main/java/com/fraudengine/exceptions/FraudDetectedException.java) | Custom checked domain exception extending `java.lang.Exception`. Encapsulates `ruleId`, `breachReason`, and integer `riskScore`. Caught and collected by the engine during transaction evaluation. |
| **6** | **File Handling (I/O)** | [`AuditLogger.java`](src/main/java/com/fraudengine/data/AuditLogger.java) | Thread-safe file I/O using `BufferedWriter` and `FileWriter` with a dedicated synchronization monitor (`FILE_WRITE_LOCK`) to append security alerts to `logs/fraud_alerts.log`. Reads logs back using `BufferedReader` and `FileReader`. |
| **7** | **Collections Framework** | [`EngineEvaluator.java`](src/main/java/com/fraudengine/core/EngineEvaluator.java)<br>[`BlacklistGeoRule.java`](src/main/java/com/fraudengine/rules/BlacklistGeoRule.java) | Utilizes `CopyOnWriteArrayList<AbstractRule>` for thread-safe rule pipeline, `HashSet<String>` for $O(1)$ jurisdiction blacklist checks, `LinkedHashMap<String, Object>` for structured JSON/results, and `Collections.unmodifiableList`. |
| **8** | **JavaFX (Desktop UI)** | [`AnalystDashboard.java`](src/main/java/com/fraudengine/ui/AnalystDashboard.java) | Interactive desktop GUI featuring runtime rule toggles (`CheckBox`), flagged transaction data grid (`TableView`), live log stream viewer (`TextArea`), and metrics display. |
| **9** | **JDBC Connectivity** | [`DatabaseManager.java`](src/main/java/com/fraudengine/data/DatabaseManager.java) | Relational persistence using `DriverManager`, `Connection`, `PreparedStatement` (parameterized queries strictly preventing SQL injection), and `ResultSet`. Automatically initializes SQLite schema and handles seeding. |
| **10** | **Servlets (Java EE)** | [`TransactionServlet.java`](src/main/java/com/fraudengine/web/TransactionServlet.java)<br>[`AdminServlet.java`](src/main/java/com/fraudengine/web/AdminServlet.java) | Extends `HttpServlet`. Handles HTTP POST parameter extraction, executes evaluation pipeline, coordinates audit logging and database persistence, and forwards request attributes to JSP. |
| **11** | **JSP (JavaServer Pages)** | [`index.jsp`](src/main/webapp/index.jsp)<br>[`client-portal.jsp`](src/main/webapp/client-portal.jsp)<br>[`admin-dashboard.jsp`](src/main/webapp/admin-dashboard.jsp)<br>[`result.jsp`](src/main/webapp/result.jsp) | Server-side dynamic views rendering real-time statistics, form inputs, interactive tables, and formatted "APPROVED" (green) or "FLAGGED" (red) outcomes with bulleted breach telemetry. |
| **12** | **Multithreading & Concurrency** | [`ParallelScreeningService.java`](src/main/java/com/fraudengine/concurrency/ParallelScreeningService.java)<br>[`RegionalScanService.java`](src/main/java/com/fraudengine/concurrency/RegionalScanService.java)<br>[`NamedThreadFactory.java`](src/main/java/com/fraudengine/concurrency/NamedThreadFactory.java) | Enterprise high-throughput parallel execution using `ExecutorService`, `NamedThreadFactory`, asynchronous tasks via `CompletableFuture.supplyAsync()`, and barrier synchronization with `CompletableFuture.allOf()`. Powers concurrent Bulk Parallel Transaction Screening and Regional Threat Analytics. |

---

## 🗂️ Project Directory Structure

```text
java_fraud/
├── .classpath                          # Eclipse classpath configuration
├── .project                            # Eclipse project descriptor
├── .settings/
│   └── org.eclipse.jdt.core.prefs      # Eclipse Java 17 compiler preferences
├── pom.xml                             # Maven build configuration
├── build.bat                           # 1-Click Windows compiler
├── run_web_server.bat                  # 1-Click Embedded Web Server runner
├── run_javafx_dashboard.bat            # 1-Click JavaFX Desktop GUI runner
├── fraud_engine.db                     # Embedded SQLite database
├── lib/                                # Standalone library dependencies
│   ├── javax.servlet-api-4.0.1.jar
│   ├── sqlite-jdbc-3.45.1.0.jar
│   ├── slf4j-api-2.0.12.jar
│   ├── slf4j-simple-2.0.12.jar
│   ├── javafx-base-17.0.10-win.jar
│   ├── javafx-controls-17.0.10-win.jar
│   └── javafx-graphics-17.0.10-win.jar
├── logs/
│   └── fraud_alerts.log                # Synchronous audit log file
└── src/
    └── main/
        ├── java/
        │   └── com/
        │       └── fraudengine/
        │           ├── core/
        │           │   ├── Transaction.java
        │           │   ├── AbstractRule.java
        │           │   └── EngineEvaluator.java
        │           ├── exceptions/
        │           │   └── FraudDetectedException.java
        │           ├── rules/
        │           │   ├── HighAmountRule.java
        │           │   └── BlacklistGeoRule.java
        │           ├── data/
        │           │   ├── DatabaseManager.java
        │           │   └── AuditLogger.java
        │           ├── concurrency/
        │           │   ├── NamedThreadFactory.java
        │           │   ├── TransactionBatchItem.java
        │           │   ├── BulkScreeningItemResult.java
        │           │   ├── BulkScreeningResult.java
        │           │   ├── ParallelScreeningService.java
        │           │   ├── RegionalStat.java
        │           │   ├── RegionalScanResult.java
        │           │   └── RegionalScanService.java
        │           ├── web/
        │           │   ├── TransactionServlet.java
        │           │   └── AdminServlet.java
        │           ├── ui/
        │           │   └── AnalystDashboard.java
        │           └── server/
        │               └── AppServer.java
        └── webapp/
            ├── WEB-INF/
            │   ├── web.xml
            │   └── lib/
            ├── css/
            │   └── style.css           # Exact pixel-for-pixel replica CSS
            ├── index.jsp               # Overview Page (Screenshot 1)
            ├── client-portal.jsp       # Client Screening Portal (Screenshot 2)
            ├── admin-dashboard.jsp     # Admin Control Center (Screenshots 3 & 4)
            └── result.jsp              # Dynamic Screening Result View
```

---

## 🗄️ Relational Database Schema (`transactions`)

The database table is initialized automatically upon first startup:

```sql
CREATE TABLE IF NOT EXISTS transactions (
    tx_id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL,
    amount REAL NOT NULL,
    location TEXT NOT NULL,
    timestamp TEXT NOT NULL,
    status TEXT NOT NULL,
    risk_score INTEGER NOT NULL,
    breach_reason TEXT
);
```

All SQL statements use `PreparedStatement` with parameterized placeholders (`?`) to ensure security and prevent SQL injection.

---

## 🚀 How to Run in Eclipse IDE

### Step 1: Import Project into Eclipse
1. Open **Eclipse IDE**.
2. Click **File** &rarr; **Import...**
3. Select **General** &rarr; **Existing Projects into Workspace** and click **Next**.
4. In the **Select root directory** field, browse to:
   ```text
   D:\Projects\java_fraud
   ```
5. Ensure `DeterministicRuleBasedFraudEngine` is checked and click **Finish**.
6. Eclipse will automatically recognize the source folders, Java 17 configuration, and all JAR dependencies in `lib/`.

---

### Step 2: Run the Web Application (Zero Tomcat Setup Required!)
1. In the Eclipse **Package Explorer**, expand:
   `src/main/java` &rarr; `com.fraudengine.server` &rarr; `AppServer.java`
2. **Right-click** `AppServer.java` &rarr; **Run As** &rarr; **Java Application**.
3. The embedded server starts on port `8080`!
4. Open your web browser and navigate to:
   - **Overview:** [http://localhost:8080/](http://localhost:8080/)
   - **Client Portal:** [http://localhost:8080/client-portal.jsp](http://localhost:8080/client-portal.jsp)
   - **Admin Dashboard:** [http://localhost:8080/admin](http://localhost:8080/admin)

> *Alternative for Apache Tomcat users:* If you use Eclipse IDE for Enterprise Java with Apache Tomcat 9/10, you can also right-click the project &rarr; **Run As** &rarr; **Run on Server**.

---

### Step 3: Run the JavaFX Desktop GUI in Eclipse
1. In the Eclipse **Package Explorer**, expand:
   `src/main/java` &rarr; `com.fraudengine.ui` &rarr; `AnalystDashboard.java`
2. **Right-click** `AnalystDashboard.java` &rarr; **Run As** &rarr; **Java Application**.
3. The desktop GUI launches with live rule checkboxes, TableView, and log console!

> *Note on VM arguments (if needed on custom Eclipse setups):*
> If prompted for JavaFX modules, add under Run Configurations &rarr; VM Arguments:
> `--module-path lib --add-modules javafx.controls,javafx.graphics,javafx.base`

---

## ⚡ 1-Click Execution via Windows Batch Files

You can also run everything directly from the project directory:

1. **Compile the code:** Double-click `build.bat`
2. **Start the Web Server:** Double-click `run_web_server.bat` (opens `http://localhost:8080/`)
3. **Launch the JavaFX Dashboard:** Double-click `run_javafx_dashboard.bat`
