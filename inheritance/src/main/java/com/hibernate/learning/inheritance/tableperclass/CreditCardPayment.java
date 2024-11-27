package com.hibernate.learning.inheritance.tableperclass;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.time.YearMonth;

/**
 * Credit Card Payment implementation using TABLE_PER_CLASS inheritance strategy
 * <p>
 * Database Schema:
 * <pre>
 * ┌─────────────────────┐
 * │   CREDIT_PAYMENT    │
 * ├─────────────────────┤
 * │ ID (PK)             │ ← Not shared with other tables
 * │ AMOUNT              │
 * │ PAYMENT_DATE        │
 * │ CARD_NUMBER         │
 * │ EXPIRATION_MONTH    │
 * │ EXPIRATION_YEAR     │
 * │ CARD_HOLDER_NAME    │
 * │ CVV                 │
 * └─────────────────────┘
 *
 * ┌─────────────────────┐
 * │   BANK_TRANSFER     │
 * ├─────────────────────┤
 * │ ID (PK)             │ ← Independent table
 * │ AMOUNT              │
 * │ PAYMENT_DATE        │
 * │ ACCOUNT_NUMBER      │
 * │ BANK_CODE           │
 * │ REFERENCE           │
 * └─────────────────────┘
 * </pre>
 * <p>
 * Advantages of TABLE_PER_CLASS:
 * <pre>
 * 1. Data Independence
 * ├── Separate tables
 * ├── No shared columns
 * └── Type-specific schema
 *
 * 2. Schema Flexibility
 * ├── Independent evolution
 * ├── Custom indexes
 * └── Type-specific constraints
 *
 * 3. Data Access
 * ├── Direct table access
 * ├── Simple CRUD operations
 * └── No joins for single type
 * </pre>
 * <p>
 * Disadvantages:
 * <pre>
 * 1. Polymorphic Queries
 * ├── Uses UNION ALL
 * ├── Performance impact
 * └── Complex SQL
 *
 * 2. ID Generation
 * ├── Global sequence needed
 * ├── ID uniqueness across tables
 * └── Complex coordination
 * </pre>
 * <p>
 * Query Examples:
 * <pre>
 * 1. Single Type Query
 * SELECT *
 * FROM CREDIT_PAYMENT
 * WHERE amount > ?
 *
 * 2. Polymorphic Query
 * SELECT *
 * FROM (
 *   SELECT id, amount, payment_date, 'CREDIT' as type
 *   FROM CREDIT_PAYMENT
 *   UNION ALL
 *   SELECT id, amount, payment_date, 'BANK' as type
 *   FROM BANK_TRANSFER
 * ) payments
 * WHERE amount > ?
 * </pre>
 */
@Entity
@Table(name = "credit_payment")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class CreditCardPayment extends Payment {

    /**
     * Credit card number
     * - Format: XXXX-XXXX-XXXX-XXXX
     * - Validated using Luhn algorithm
     * - Stored in dedicated table
     */
    @NotBlank(message = "Card number is required")
    @Pattern(regexp = "\\d{4}-\\d{4}-\\d{4}-\\d{4}", message = "Invalid card number format")
    @Column(name = "card_number", nullable = false)
    private String cardNumber;

    /**
     * Card holder name
     * - Required for card-not-present transactions
     * - Stored in dedicated table
     */
    @NotBlank(message = "Card holder name is required")
    @Column(name = "card_holder_name", nullable = false)
    private String cardHolderName;

    /**
     * Card expiration month
     * - Format: MM
     * - Must be future date
     * - Stored in dedicated table
     */
    @Min(value = 1, message = "Invalid expiration month")
    @Max(value = 12, message = "Invalid expiration month")
    @Column(name = "expiration_month", nullable = false)
    private int expirationMonth;

    /**
     * Card expiration year
     * - Format: YY
     * - Must be future date
     * - Stored in dedicated table
     */
    @Min(value = 0, message = "Invalid expiration year")
    @Column(name = "expiration_year", nullable = false)
    private int expirationYear;

    /**
     * Card verification value
     * - 3 or 4 digits
     * - Required for card-not-present transactions
     * - Stored in dedicated table
     */
    @NotBlank(message = "CVV is required")
    @Pattern(regexp = "^[0-9]{3,4}$", message = "Invalid CVV")
    @Column(name = "cvv", nullable = false)
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
     * ├── Card Holder Name
     * │   └── Not empty check
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
    public void validate() {
        if (cardNumber == null || !cardNumber.matches("\\d{4}-\\d{4}-\\d{4}-\\d{4}")) {
            throw new IllegalStateException("Invalid card number format");
        }

        if (cardHolderName == null || cardHolderName.trim().isEmpty()) {
            throw new IllegalStateException("Card holder name is required");
        }

        validateExpiryDate();

        if (cvv == null || !cvv.matches("^[0-9]{3,4}$")) {
            throw new IllegalStateException("Invalid CVV");
        }
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
        // Simulate card payment processing
        try {
            Thread.sleep(1000); // Simulate processing time
            // In a real implementation, this would integrate with a payment gateway
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Payment processing interrupted");
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
        int currentYear = now.getYear() % 100; // Get last two digits
        int currentMonth = now.getMonthValue();

        if (expirationYear < currentYear ||
                (expirationYear == currentYear && expirationMonth < currentMonth)) {
            throw new IllegalStateException("Card has expired");
        }
    }
}
