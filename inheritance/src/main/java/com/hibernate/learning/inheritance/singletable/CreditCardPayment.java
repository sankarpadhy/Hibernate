package com.hibernate.learning.inheritance.singletable;

import com.hibernate.learning.inheritance.common.Payment;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.time.YearMonth;

/**
 * Credit Card Payment implementation using SINGLE_TABLE inheritance strategy
 * <p>
 * Database Schema:
 * <pre>
 * ┌──────────────────────────────┐
 * │         PAYMENT              │
 * ├──────────────────────────────┤
 * │ ID (PK)                      │
 * │ DTYPE                        │ ← Discriminator
 * │ AMOUNT                       │
 * │ PAYMENT_DATE                 │
 * │ CARD_NUMBER                  │ ↑
 * │ EXPIRY_DATE                  │ │ Credit Card
 * │ CVV                          │ │ specific
 * │ CHEQUE_NUMBER               *│ │ fields
 * │ BANK_NAME                   *│ ↓
 * │ CASH_TENDER_AMOUNT         *│
 * └──────────────────────────────┘
 * * Nullable fields for other payment types
 * </pre>
 * <p>
 * Advantages of SINGLE_TABLE:
 * <pre>
 * 1. Performance
 * ├── No joins needed
 * ├── Simple queries
 * └── Fast polymorphic queries
 *
 * 2. Easy Setup
 * ├── Single table
 * ├── Simple mappings
 * └── Default strategy
 *
 * 3. Versioning
 * ├── Single version column
 * ├── Simple concurrency
 * └── Easy auditing
 * </pre>
 * <p>
 * Disadvantages:
 * <pre>
 * 1. Database Design
 * ├── Denormalized
 * ├── Nullable columns
 * └── Wasted space
 *
 * 2. Constraints
 * ├── Limited column constraints
 * ├── Type-specific validation
 * └── Data integrity challenges
 * </pre>
 */
@Entity(name = "SingleTableCreditCardPayment")
@DiscriminatorValue("CREDIT_CARD")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class CreditCardPayment extends Payment {

    /**
     * Credit card number
     * - Format: XXXX-XXXX-XXXX-XXXX
     * - Validated using Luhn algorithm
     */
    @NotBlank(message = "Card number is required")
    @Pattern(regexp = "\\d{4}-\\d{4}-\\d{4}-\\d{4}", message = "Invalid card number format")
    @Column(name = "card_number")
    private String cardNumber;

    /**
     * Card holder name
     */
    @NotBlank(message = "Card holder name is required")
    @Column(name = "card_holder_name")
    private String cardHolderName;

    /**
     * Card expiry month
     * - Must be between 1 and 12
     */
    @Min(value = 1, message = "Invalid expiration month")
    @Max(value = 12, message = "Invalid expiration month")
    @Column(name = "expiration_month")
    private int expirationMonth;

    /**
     * Card expiry year
     * - Must be greater than or equal to 0
     */
    @Min(value = 0, message = "Invalid expiration year")
    @Column(name = "expiration_year")
    private int expirationYear;

    /**
     * Card verification value
     * - 3 or 4 digits
     * - Required for card-not-present transactions
     */
    @NotBlank(message = "CVV is required")
    @Pattern(regexp = "^[0-9]{3,4}$", message = "Invalid CVV")
    @Column(name = "cvv")
    private String cvv;

    /**
     * Validates credit card specific data
     * <p>
     * Validation Flow:
     * <pre>
     * validatePaymentSpecifics()
     * ├── Card Number
     * │   ├── Format check
     * │   └── Luhn validation
     * │
     * ├── Expiry Date
     * │   ├── Format validation
     * │   └── Not expired check
     * │
     * └── CVV
     *     └── Format validation
     * </pre>
     */
    @Override
    protected void validatePaymentSpecifics() {
        validateCardNumber();
        validateExpiryDate();
        validateCVV();
    }

    /**
     * Internal payment processing implementation
     * <p>
     * Processing Flow:
     * <pre>
     * processInternal()
     * ├── Authorize
     * │   ├── Validate card
     * │   └── Check funds
     * │
     * ├── Capture
     * │   ├── Reserve amount
     * │   └── Process payment
     * │
     * └── Confirm
     *     ├── Get confirmation
     *     └── Update status
     * </pre>
     */
    @Override
    protected void processInternal() {
        // Authorization
        if (!isValidCard()) {
            throw new IllegalStateException("Invalid credit card");
        }

        // Processing logic
        processPayment();

        // Confirmation
        confirmPayment();
    }

    /**
     * Validates credit card number using Luhn algorithm
     * <p>
     * Algorithm Flow:
     * <pre>
     * validateCardNumber()
     * ├── Remove hyphens
     * ├── Reverse digits
     * ├── Double alternate digits
     * ├── Sum all digits
     * └── Check modulo 10
     * </pre>
     */
    private void validateCardNumber() {
        if (cardNumber == null || !cardNumber.matches("\\d{4}-\\d{4}-\\d{4}-\\d{4}")) {
            throw new IllegalStateException("Invalid card number format");
        }

        // Luhn algorithm implementation
        String digits = cardNumber.replaceAll("-", "");
        int sum = 0;
        boolean alternate = false;

        for (int i = digits.length() - 1; i >= 0; i--) {
            int digit = digits.charAt(i) - '0';
            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            sum += digit;
            alternate = !alternate;
        }

        if (sum % 10 != 0) {
            throw new IllegalStateException("Invalid card number (Luhn check failed)");
        }
    }

    /**
     * Validates card expiry date
     * <p>
     * Validation Flow:
     * <pre>
     * validateExpiryDate()
     * ├── Format check (MM/YY)
     * ├── Parse date
     * └── Compare with current date
     * </pre>
     */
    private void validateExpiryDate() {
        YearMonth now = YearMonth.now();
        int currentYear = now.getYear();
        int currentMonth = now.getMonthValue();

        if (expirationYear < (currentYear % 100) || (expirationYear == (currentYear % 100) && expirationMonth < currentMonth)) {
            throw new IllegalStateException("Card has expired");
        }
    }

    /**
     * Validates CVV format
     * <p>
     * Validation Flow:
     * <pre>
     * validateCVV()
     * ├── Not null check
     * ├── Length check (3-4 digits)
     * └── Format validation
     * </pre>
     */
    private void validateCVV() {
        if (cvv == null || !cvv.matches("^[0-9]{3,4}$")) {
            throw new IllegalStateException("Invalid CVV");
        }
    }

    // Helper methods for payment processing
    private boolean isValidCard() {
        try {
            validateCardNumber();
            validateExpiryDate();
            validateCVV();
            return true;
        } catch (IllegalStateException e) {
            return false;
        }
    }

    private void processPayment() {
        // Simulate payment processing
        try {
            Thread.sleep(1000); // Simulate network delay
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Payment processing interrupted");
        }
    }

    private void confirmPayment() {
        // Simulate payment confirmation
        if (Math.random() < 0.001) { // 0.1% failure rate
            throw new IllegalStateException("Payment confirmation failed");
        }
    }
}
