package com.fraudengine.rules;

import com.fraudengine.core.AbstractRule;
import com.fraudengine.core.Transaction;
import com.fraudengine.exceptions.FraudDetectedException;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * ============================================================================
 * JAVA CONCEPT 1: INHERITANCE (Extends AbstractRule)
 * JAVA CONCEPT 2: POLYMORPHISM (Overrides evaluate method)
 * ============================================================================
 * Concrete rule that flags transactions exceeding a financial threshold.
 * 
 * Default configuration flags transactions where amount > $10,000.00.
 * Directly corresponds to the UI rule "High Amount Threshold Rule".
 * ============================================================================
 */
public class HighAmountRule extends AbstractRule {

    private double threshold;
    private int riskScore;

    /**
     * Default constructor with enterprise production defaults.
     */
    public HighAmountRule() {
        this(10000.00, 85);
    }

    /**
     * Parameterized constructor allowing dynamic threshold and risk calibration.
     *
     * @param threshold monetary limit in USD
     * @param riskScore severity score (e.g. 85)
     */
    public HighAmountRule(double threshold, int riskScore) {
        super(
            "RULE-AMOUNT-01",
            "High Amount Threshold Rule",
            "Flags transactions exceeding $" + String.format("%,.2f", threshold),
            true // Enabled by default
        );
        this.threshold = threshold;
        this.riskScore = riskScore;
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
        this.description = "Flags transactions exceeding $" + String.format("%,.2f", threshold);
    }

    public int getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(int riskScore) {
        this.riskScore = riskScore;
    }

    /**
     * POLYMORPHIC IMPLEMENTATION:
     * Evaluates whether the transaction amount breaches the configured ceiling.
     *
     * @param transaction the transaction domain entity to be evaluated
     * @throws FraudDetectedException when amount exceeds the allowed threshold
     */
    @Override
    public void evaluate(Transaction transaction) throws FraudDetectedException {
        if (!isEnabled()) {
            return; // Skip inactive rule
        }

        if (transaction == null) {
            throw new IllegalArgumentException("Transaction cannot be null during rule evaluation.");
        }

        if (transaction.getAmount() > this.threshold) {
            NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.US);
            String formattedTxAmount = nf.format(transaction.getAmount());
            String formattedThreshold = nf.format(this.threshold);

            String reason = String.format(
                "Transaction amount %s exceeds maximum permissible threshold of %s",
                formattedTxAmount, formattedThreshold
            );

            throw new FraudDetectedException(this.ruleId, reason, this.riskScore);
        }
    }
}
