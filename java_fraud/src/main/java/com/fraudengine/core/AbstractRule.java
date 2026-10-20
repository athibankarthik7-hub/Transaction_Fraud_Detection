package com.fraudengine.core;

import com.fraudengine.exceptions.FraudDetectedException;

/**
 * ============================================================================
 * JAVA CONCEPT 4: ABSTRACT CLASS
 * JAVA CONCEPT 1: INHERITANCE (Base class for concrete rules)
 * JAVA CONCEPT 2: POLYMORPHISM (Defines contract for dynamic dispatch)
 * ============================================================================
 * Abstract base class representing a deterministic screening rule.
 * 
 * An abstract class cannot be instantiated directly; it serves as a common template
 * and partial implementation for concrete rule definitions (e.g., HighAmountRule, BlacklistGeoRule).
 * 
 * Key architectural characteristics:
 *  1. State encapsulation: Holds rule identity, name, description, and runtime activation state.
 *  2. Concrete methods: Implements reusable getters and setters.
 *  3. Abstract contract: Mandates that all child classes implement `evaluate(Transaction)`.
 * ============================================================================
 */
public abstract class AbstractRule {

    protected String ruleId;
    protected String ruleName;
    protected String description;
    protected boolean enabled;

    /**
     * Protected constructor to be invoked by subclass constructors.
     *
     * @param ruleId      unique identifier (e.g. "RULE-AMOUNT-01")
     * @param ruleName    human-readable rule title
     * @param description brief description of the screening policy
     * @param enabled     initial activation state
     */
    protected AbstractRule(String ruleId, String ruleName, String description, boolean enabled) {
        this.ruleId = ruleId;
        this.ruleName = ruleName;
        this.description = description;
        this.enabled = enabled;
    }

    /**
     * ABSTRACT METHOD: Contract for evaluating a transaction against rule logic.
     * Subclasses must override this method with specific business rules.
     *
     * @param transaction the transaction domain entity to be screened
     * @throws FraudDetectedException if the transaction breaches rule constraints
     */
    public abstract void evaluate(Transaction transaction) throws FraudDetectedException;

    // Concrete Getter and Setter methods shared across all inheriting rules

    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String toString() {
        return "AbstractRule{" +
                "ruleId='" + ruleId + '\'' +
                ", ruleName='" + ruleName + '\'' +
                ", enabled=" + enabled +
                '}';
    }
}
