package com.fraudengine.exceptions;

/**
 * ============================================================================
 * JAVA CONCEPT 5: USER-DEFINED EXCEPTIONS
 * ============================================================================
 * Custom checked exception class extending java.lang.Exception.
 * 
 * In an enterprise deterministic fraud screening engine, rule breaches are
 * exceptional domain events that must be explicitly caught and handled by the
 * evaluator or service layer.
 * 
 * This checked exception encapsulates:
 *  - ruleId: Unique identifier of the violated rule (e.g., "RULE-AMOUNT-01")
 *  - breachReason: Human-readable narrative detailing why the transaction violated policy
 *  - riskScore: Integer penalty score assigned for this specific infraction (0-100)
 * ============================================================================
 */
public class FraudDetectedException extends Exception {
    
    private static final long serialVersionUID = 1L;
    
    private final String ruleId;
    private final String breachReason;
    private final int riskScore;

    /**
     * Constructs a new FraudDetectedException with explicit breach telemetry.
     *
     * @param ruleId       the unique identifier of the violated rule
     * @param breachReason the detailed explanation of the breach
     * @param riskScore    the severity risk score assigned to this infraction
     */
    public FraudDetectedException(String ruleId, String breachReason, int riskScore) {
        super(String.format("[%s] Breach detected: %s (Risk Score: %d)", ruleId, breachReason, riskScore));
        this.ruleId = ruleId;
        this.breachReason = breachReason;
        this.riskScore = riskScore;
    }

    /**
     * Constructs a new FraudDetectedException with a root cause cause throwable.
     *
     * @param ruleId       the unique identifier of the violated rule
     * @param breachReason the detailed explanation of the breach
     * @param riskScore    the severity risk score assigned
     * @param cause        underlying root cause exception
     */
    public FraudDetectedException(String ruleId, String breachReason, int riskScore, Throwable cause) {
        super(String.format("[%s] Breach detected: %s (Risk Score: %d)", ruleId, breachReason, riskScore), cause);
        this.ruleId = ruleId;
        this.breachReason = breachReason;
        this.riskScore = riskScore;
    }

    public String getRuleId() {
        return ruleId;
    }

    public String getBreachReason() {
        return breachReason;
    }

    public int getRiskScore() {
        return riskScore;
    }

    @Override
    public String toString() {
        return "FraudDetectedException{" +
                "ruleId='" + ruleId + '\'' +
                ", breachReason='" + breachReason + '\'' +
                ", riskScore=" + riskScore +
                ", message='" + getMessage() + '\'' +
                '}';
    }
}
