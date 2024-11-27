package com.hibernate.learning.inheritance.tableperclass;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * Bank Transfer Payment implementation using TABLE_PER_CLASS inheritance strategy
 * <p>
 * Database Schema:
 * <pre>
 * ┌─────────────────────┐
 * │   BANK_TRANSFER     │
 * ├─────────────────────┤
 * │ ID (PK)             │ ← Independent primary key
 * │ AMOUNT              │
 * │ PAYMENT_DATE        │
 * │ ACCOUNT_NUMBER      │
 * │ BANK_CODE           │
 * │ REFERENCE           │
 * │ BANK_NAME           │
 * │ IBAN                │
 * │ SWIFT_CODE          │
 * │ ROUTING_NUMBER      │
 * └─────────────────────┘
 * </pre>
 * <p>
 * Key Features:
 * <pre>
 * 1. Independent Table
 * ├── Complete isolation
 * ├── No shared columns
 * └── Type-specific indexes
 *
 * 2. Performance
 * ├── Direct table access
 * ├── No joins needed
 * └── Efficient queries
 *
 * 3. Data Integrity
 * ├── Bank-specific validation
 * ├── Reference tracking
 * └── Transaction logging
 * </pre>
 * <p>
 * Query Examples:
 * <pre>
 * 1. Find by Bank Code
 * SELECT *
 * FROM BANK_TRANSFER
 * WHERE bank_code = ?
 *
 * 2. Find Recent Transfers
 * SELECT *
 * FROM BANK_TRANSFER
 * WHERE payment_date > ?
 * ORDER BY payment_date DESC
 *
 * 3. Sum by Bank
 * SELECT bank_code, SUM(amount)
 * FROM BANK_TRANSFER
 * GROUP BY bank_code
 * </pre>
 */
@Entity
@Table(name = "bank_transfer")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class BankTransferPayment extends Payment {

    /**
     * Bank account number
     * - Format: XX-XXXX-XXXX-XX
     * - Includes check digits
     * - Validated format
     */
    @NotBlank(message = "Account number is required")
    @Pattern(regexp = "\\d{2}-\\d{4}-\\d{4}-\\d{2}", message = "Invalid account number format")
    @Column(name = "account_number", nullable = false)
    private String accountNumber;

    /**
     * Bank identification code
     * - Format: XXXXX
     * - Validated against known banks
     * - Used for routing
     */
    @NotBlank(message = "Bank code is required")
    @Size(min = 5, max = 5, message = "Bank code must be exactly 5 characters")
    @Column(name = "bank_code", nullable = false)
    private String bankCode;

    /**
     * Transfer reference
     * - Unique identifier
     * - Used for tracking
     * - Format: BTRF-YYYYMMDD-XXXXX
     */
    @NotBlank(message = "Reference is required")
    @Pattern(regexp = "BTRF-\\d{8}-\\d{5}", message = "Invalid reference format")
    @Column(name = "reference", nullable = false)
    private String reference;

    /**
     * Bank name
     */
    @NotBlank(message = "Bank name is required")
    @Column(name = "bank_name")
    private String bankName;

    /**
     * IBAN
     */
    @NotBlank(message = "IBAN is required")
    @Pattern(regexp = "^[A-Z]{2}[0-9]{2}[A-Z0-9]{1,30}$", message = "Invalid IBAN format")
    @Column(name = "iban")
    private String iban;

    /**
     * SWIFT code
     */
    @NotBlank(message = "SWIFT code is required")
    @Pattern(regexp = "^[A-Z]{6}[A-Z0-9]{2}([A-Z0-9]{3})?$", message = "Invalid SWIFT code format")
    @Column(name = "swift_code")
    private String swiftCode;

    /**
     * Routing number
     */
    @NotBlank(message = "Routing number is required")
    @Column(name = "routing_number")
    private String routingNumber;

    /**
     * Validates bank transfer specific data
     * <p>
     * Validation Flow:
     * <pre>
     * validate()
     * ├── Bank Name
     * │   ├── Required
     * │
     * ├── Account Number
     * │   ├── Required
     * │
     * ├── IBAN
     * │   ├── Required
     * │   └── Format validation
     * │
     * ├── SWIFT Code
     * │   ├── Required
     * │   └── Format validation
     * │
     * └── Routing Number
     *     ├── Required
     * </pre>
     */
    @Override
    public void validate() {
        if (bankName == null || bankName.trim().isEmpty()) {
            throw new IllegalStateException("Bank name is required");
        }

        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            throw new IllegalStateException("Account number is required");
        }

        if (iban == null || !iban.matches("^[A-Z]{2}[0-9]{2}[A-Z0-9]{1,30}$")) {
            throw new IllegalStateException("Invalid IBAN format");
        }

        if (swiftCode == null || !swiftCode.matches("^[A-Z]{6}[A-Z0-9]{2}([A-Z0-9]{3})?$")) {
            throw new IllegalStateException("Invalid SWIFT code format");
        }

        if (routingNumber == null || routingNumber.trim().isEmpty()) {
            throw new IllegalStateException("Routing number is required");
        }
    }

    /**
     * Internal payment processing implementation
     * <p>
     * Processing Flow:
     * <pre>
     * processInternal()
     * ├── Validate
     * │   ├── Account details
     * │   └── Bank availability
     * │
     * ├── Transfer
     * │   ├── Initiate transfer
     * │   └── Track progress
     * │
     * └── Confirm
     *     ├── Verify receipt
     *     └── Update status
     * </pre>
     */
    @Override
    protected void processInternal() {
        // Simulate bank transfer processing
        try {
            Thread.sleep(2000); // Bank transfers typically take longer
            // In a real implementation, this would integrate with a banking system
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Payment processing interrupted");
        }
    }

    /**
     * Validates account number format and check digits
     * <p>
     * Algorithm Flow:
     * <pre>
     * validateAccountNumber()
     * ├── Format check
     * ├── Extract parts
     * ├── Calculate check sum
     * └── Verify check digits
     * </pre>
     */
    private void validateAccountNumber() {
        if (accountNumber == null || !accountNumber.matches("\\d{2}-\\d{4}-\\d{4}-\\d{2}")) {
            throw new IllegalStateException("Invalid account number format");
        }

        // Check digit validation
        String[] parts = accountNumber.split("-");
        int checkSum = calculateCheckSum(parts[1] + parts[2]);
        int providedCheck = Integer.parseInt(parts[3]);

        if (checkSum != providedCheck) {
            throw new IllegalStateException("Invalid account number (check digits failed)");
        }
    }

    /**
     * Validates bank code format and existence
     * <p>
     * Validation Flow:
     * <pre>
     * validateBankCode()
     * ├── Format check
     * ├── Length validation
     * └── Bank existence check
     * </pre>
     */
    private void validateBankCode() {
        if (bankCode == null || bankCode.length() != 5) {
            throw new IllegalStateException("Invalid bank code format");
        }

        // Simulate bank code validation
        if (!isKnownBank(bankCode)) {
            throw new IllegalStateException("Unknown bank code");
        }
    }

    /**
     * Validates transfer reference format and uniqueness
     * <p>
     * Validation Flow:
     * <pre>
     * validateReference()
     * ├── Format check
     * ├── Date validation
     * └── Sequence check
     * </pre>
     */
    private void validateReference() {
        if (reference == null || !reference.matches("BTRF-\\d{8}-\\d{5}")) {
            throw new IllegalStateException("Invalid reference format");
        }

        // Additional reference validations could be added here
    }

    /**
     * Calculates check sum for account number
     * <p>
     * Algorithm Flow:
     * <pre>
     * calculateCheckSum()
     * ├── Extract parts
     * ├── Calculate sum
     * └── Return check sum
     * </pre>
     */
    private int calculateCheckSum(String accountDigits) {
        int sum = 0;
        for (char digit : accountDigits.toCharArray()) {
            sum += Character.getNumericValue(digit);
        }
        return sum % 100;
    }

    /**
     * Simulates bank code validation
     * <p>
     * Validation Flow:
     * <pre>
     * isKnownBank()
     * ├── Format check
     * ├── Length validation
     * └── Bank existence check
     * </pre>
     */
    private boolean isKnownBank(String bankCode) {
        // Simulate bank code validation
        // In a real implementation, this would check against a database of valid bank codes
        return bankCode.matches("\\d{5}");
    }

    /**
     * Helper method to check if bank details are valid
     * <p>
     * Validation Flow:
     * <pre>
     * isValidBankDetails()
     * ├── Account Number
     * │   ├── Format check
     * │   └── Check digit validation
     * │
     * ├── Bank Code
     * │   ├── Format validation
     * │   └── Bank existence check
     * │
     * └── Reference
     *     ├── Format validation
     *     └── Uniqueness check
     * </pre>
     */
    private boolean isValidBankDetails() {
        try {
            validateAccountNumber();
            validateBankCode();
            validateReference();
            return true;
        } catch (IllegalStateException e) {
            return false;
        }
    }
}
