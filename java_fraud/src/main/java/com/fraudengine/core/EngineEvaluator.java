package com.fraudengine.core;

import com.fraudengine.exceptions.FraudDetectedException;
import com.fraudengine.rules.BlacklistGeoRule;
import com.fraudengine.rules.HighAmountRule;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ============================================================================
 * JAVA CONCEPT 7: COLLECTIONS FRAMEWORK (List<AbstractRule>, Map<String, Object>)
 * JAVA CONCEPT 2: POLYMORPHISM (Dynamic dispatch across List<AbstractRule>)
 * JAVA CONCEPT 5: USER-DEFINED EXCEPTIONS (Catches FraudDetectedException)
 * ============================================================================
 * Central deterministic screening engine.
 * 
 * Orchestrates a pipeline of AbstractRule instances, sequentially dispatching
 * the transaction against each active rule.
 * 
 * Architectural Highlights:
 *  - Uses CopyOnWriteArrayList for thread-safe rule registration and runtime toggles.
 *  - Dynamic Dispatch: `rule.evaluate(tx)` resolves at runtime to the specific subclass.
 *  - Returns a structured Map<String, Object> representation of the evaluation result.
 * ============================================================================
 */
public class EngineEvaluator {

    private static volatile EngineEvaluator instance;

    // Collections Framework: Thread-safe list maintaining the active rule pipeline
    private final List<AbstractRule> rules = new CopyOnWriteArrayList<>();

    // Real-time screening metric accumulators
    private final AtomicInteger screenedCount = new AtomicInteger(0);
    private final AtomicInteger flaggedCount = new AtomicInteger(0);

    private static final DateTimeFormatter TIMESTAMP_FORMATTER = 
            DateTimeFormatter.ofPattern("MM/dd/yyyy, h:mm:ss a");

    /**
     * Singleton instance provider for enterprise application-wide consistency.
     */
    public static EngineEvaluator getInstance() {
        if (instance == null) {
            synchronized (EngineEvaluator.class) {
                if (instance == null) {
                    instance = new EngineEvaluator();
                }
            }
        }
        return instance;
    }

    /**
     * Private constructor initializing standard deterministic enterprise rules.
     */
    public EngineEvaluator() {
        registerDefaultRules();
    }

    /**
     * Registers default deterministic rules showcased in the application UI.
     */
    public void registerDefaultRules() {
        this.rules.clear();
        this.rules.add(new HighAmountRule(10000.00, 85));
        this.rules.add(new BlacklistGeoRule());
    }

    /**
     * Adds an AbstractRule to the evaluation pipeline.
     *
     * @param rule concrete subclass instance extending AbstractRule
     */
    public void addRule(AbstractRule rule) {
        if (rule != null) {
            this.rules.add(rule);
        }
    }

    /**
     * Removes a rule by its unique identifier.
     */
    public boolean removeRule(String ruleId) {
        return this.rules.removeIf(r -> r.getRuleId().equalsIgnoreCase(ruleId));
    }

    /**
     * Retrieves an unmodifiable view of registered rules.
     */
    public List<AbstractRule> getRules() {
        return Collections.unmodifiableList(this.rules);
    }

    /**
     * Finds a registered rule by its identifier.
     */
    public AbstractRule getRuleById(String ruleId) {
        for (AbstractRule rule : this.rules) {
            if (rule.getRuleId().equalsIgnoreCase(ruleId)) {
                return rule;
            }
        }
        return null;
    }

    /**
     * Runtime toggle for individual rules (used by Admin Dashboard and JavaFX).
     */
    public boolean setRuleEnabled(String ruleId, boolean enabled) {
        AbstractRule rule = getRuleById(ruleId);
        if (rule != null) {
            rule.setEnabled(enabled);
            return true;
        }
        return false;
    }

    /**
     * Returns total count of currently enabled rules.
     */
    public int getActiveRuleCount() {
        int count = 0;
        for (AbstractRule rule : this.rules) {
            if (rule.isEnabled()) {
                count++;
            }
        }
        return count;
    }

    /**
     * ========================================================================
     * CORE EVALUATION METHOD (POLYMORPHISM & EXCEPTION HANDLING)
     * ========================================================================
     * Iterates through the collection of AbstractRule instances and evaluates
     * the transaction using dynamic method dispatch.
     *
     * @param transaction the transaction domain entity to be screened
     * @return Map<String, Object> containing structured outcome and telemetry
     */
    public Map<String, Object> processTransaction(Transaction transaction) {
        if (transaction == null) {
            throw new IllegalArgumentException("Cannot process a null transaction.");
        }

        // Increment total screened metric
        int currentScreened = this.screenedCount.incrementAndGet();

        // Collections Framework: List to aggregate all detected rule violations
        List<String> violations = new ArrayList<>();
        int cumulativeRiskScore = 0;

        /*
         * POLYMORPHISM IN ACTION:
         * Although the reference type is 'AbstractRule', the JVM invokes
         * HighAmountRule.evaluate() or BlacklistGeoRule.evaluate() at runtime.
         */
        for (AbstractRule rule : this.rules) {
            if (!rule.isEnabled()) {
                continue; // Skip toggled-off rules
            }

            try {
                // Dynamic dispatch triggers polymorphic evaluation
                rule.evaluate(transaction);
            } catch (FraudDetectedException ex) {
                // USER-DEFINED EXCEPTION caught and processed
                violations.add(String.format("[%s] %s: %s", ex.getRuleId(), rule.getRuleName(), ex.getBreachReason()));
                cumulativeRiskScore = Math.max(cumulativeRiskScore, ex.getRiskScore());
            }
        }

        // Determine final decision status
        boolean isFlagged = !violations.isEmpty();
        if (isFlagged) {
            this.flaggedCount.incrementAndGet();
            transaction.setStatus(Transaction.STATUS_FLAGGED);
            transaction.setRiskScore(cumulativeRiskScore);
            transaction.setBreachReason(String.join("; ", violations));
        } else {
            transaction.setStatus(Transaction.STATUS_APPROVED);
            transaction.setRiskScore(0);
            transaction.setBreachReason("Passed all deterministic screening checks.");
        }

        // Collections Framework: Map structure returned to caller
        Map<String, Object> outcome = new LinkedHashMap<>();
        outcome.put("txId", transaction.getTxId());
        outcome.put("userId", transaction.getUserId());
        outcome.put("amount", transaction.getAmount());
        outcome.put("formattedAmount", transaction.getFormattedAmount());
        outcome.put("location", transaction.getLocation());
        outcome.put("status", transaction.getStatus());
        outcome.put("riskScore", transaction.getRiskScore());
        outcome.put("violations", violations);
        outcome.put("breachReason", transaction.getBreachReason());
        outcome.put("evaluatedAt", LocalDateTime.now().format(TIMESTAMP_FORMATTER));
        outcome.put("activeRulesCount", getActiveRuleCount());
        outcome.put("totalScreened", currentScreened);
        outcome.put("totalFlagged", this.flaggedCount.get());

        int approved = currentScreened - this.flaggedCount.get();
        double approvalRate = currentScreened > 0 ? ((double) approved / currentScreened) * 100.0 : 100.0;
        outcome.put("approvalRate", Math.round(approvalRate));

        return outcome;
    }

    // Telemetry metric getters
    public int getScreenedCount() {
        return screenedCount.get();
    }

    public int getFlaggedCount() {
        return flaggedCount.get();
    }

    public int getApprovedCount() {
        return Math.max(0, screenedCount.get() - flaggedCount.get());
    }

    public double getApprovalRate() {
        int screened = screenedCount.get();
        if (screened == 0) return 100.0;
        return ((double) getApprovedCount() / screened) * 100.0;
    }

    public void setMetrics(int screened, int flagged) {
        this.screenedCount.set(screened);
        this.flaggedCount.set(flagged);
    }
}
