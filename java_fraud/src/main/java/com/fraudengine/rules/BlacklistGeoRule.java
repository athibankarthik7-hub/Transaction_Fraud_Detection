package com.fraudengine.rules;

import com.fraudengine.core.AbstractRule;
import com.fraudengine.core.Transaction;
import com.fraudengine.exceptions.FraudDetectedException;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * ============================================================================
 * JAVA CONCEPT 1: INHERITANCE (Extends AbstractRule)
 * JAVA CONCEPT 2: POLYMORPHISM (Overrides evaluate method)
 * JAVA CONCEPT 7: COLLECTIONS FRAMEWORK (Uses Set<String> for O(1) lookups)
 * ============================================================================
 * Concrete rule that flags transactions originating from sanctioned or high-risk jurisdictions.
 * 
 * Default blacklisted jurisdictions: NG (Nigeria), KP (North Korea), SY (Syria), RU (Russia).
 * Directly matches the UI hint and screenshot configuration.
 * ============================================================================
 */
public class BlacklistGeoRule extends AbstractRule {

    // Collections Framework: Set provides thread-safe immutable lookup semantics
    private final Set<String> blacklistedCountries;
    private int riskScore;

    /**
     * Default constructor initializing standard high-risk jurisdictions.
     */
    public BlacklistGeoRule() {
        this(new HashSet<>(Arrays.asList("NG", "KP", "SY", "RU")), 90);
    }

    /**
     * Parameterized constructor allowing dynamic blacklist jurisdiction configuration.
     *
     * @param countries Set of ISO 2-letter country codes
     * @param riskScore severity score (e.g. 90)
     */
    public BlacklistGeoRule(Set<String> countries, int riskScore) {
        super(
            "RULE-GEO-02",
            "Black-listed Geo-location Rule",
            "Flags transactions from high-risk locations (NG, KP, SY, RU)",
            true // Enabled by default
        );
        this.blacklistedCountries = new HashSet<>();
        if (countries != null) {
            for (String code : countries) {
                if (code != null) {
                    this.blacklistedCountries.add(code.trim().toUpperCase());
                }
            }
        }
        this.riskScore = riskScore;
    }

    public Set<String> getBlacklistedCountries() {
        return Collections.unmodifiableSet(this.blacklistedCountries);
    }

    public void addBlacklistedCountry(String countryCode) {
        if (countryCode != null && !countryCode.trim().isEmpty()) {
            this.blacklistedCountries.add(countryCode.trim().toUpperCase());
        }
    }

    public void removeBlacklistedCountry(String countryCode) {
        if (countryCode != null) {
            this.blacklistedCountries.remove(countryCode.trim().toUpperCase());
        }
    }

    public int getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(int riskScore) {
        this.riskScore = riskScore;
    }

    /**
     * POLYMORPHIC IMPLEMENTATION:
     * Evaluates whether the transaction origin is within the prohibited country set.
     *
     * @param transaction the transaction domain entity to be evaluated
     * @throws FraudDetectedException when origin country matches the blacklist
     */
    @Override
    public void evaluate(Transaction transaction) throws FraudDetectedException {
        if (!isEnabled()) {
            return; // Skip inactive rule
        }

        if (transaction == null) {
            throw new IllegalArgumentException("Transaction cannot be null during rule evaluation.");
        }

        String location = transaction.getLocation();
        if (location != null && this.blacklistedCountries.contains(location.trim().toUpperCase())) {
            String reason = String.format(
                "Transaction originates from high-risk jurisdiction [%s], which is present on the embargo blacklist %s",
                location.trim().toUpperCase(),
                this.blacklistedCountries
            );

            throw new FraudDetectedException(this.ruleId, reason, this.riskScore);
        }
    }
}
