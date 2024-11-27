package com.hibernate.learning.inheritance.common;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Abstract base class for Payment entities demonstrating inheritance mapping strategies
 * <p>
 * Inheritance Strategies Overview:
 * <pre>
 * 1. SINGLE_TABLE
 * ┌─────────────────────────┐
 * │      PAYMENT_TABLE      │
 * ├─────────────────────────┤
 * │ ID                      │
 * │ AMOUNT                  │
 * │ PAYMENT_DATE            │
 * │ DISCRIMINATOR           │ ← Determines concrete type
 * │ CREDIT_CARD_NUMBER      │
 * │ CHEQUE_NUMBER           │
 * │ CASH_TENDER_AMOUNT      │
 * └─────────────────────────┘
 *
 * 2. JOINED
 * ┌─────────────────┐
 * │    PAYMENT      │
 * ├─────────────────┤      ┌─────────────────┐
 * │ ID              │ ◄─── │  CREDIT_CARD    │
 * │ AMOUNT          │      ├─────────────────┤
 * │ PAYMENT_DATE    │      │ PAYMENT_ID (FK) │
 * └─────────────────┘      │ CARD_NUMBER     │
 *         ▲                └─────────────────┘
 *         │                ┌─────────────────┐
 *         └────────────── │     CHEQUE      │
 *                         ├─────────────────┤
 *                         │ PAYMENT_ID (FK) │
 *                         │ CHEQUE_NUMBER   │
 *                         └─────────────────┘
 *
 * 3. TABLE_PER_CLASS
 * ┌─────────────────┐
 * │   CREDIT_CARD   │
 * ├─────────────────┤
 * │ ID              │
 * │ AMOUNT          │
 * │ PAYMENT_DATE    │
 * │ CARD_NUMBER     │
 * └─────────────────┘
 *
 * ┌─────────────────┐
 * │     CHEQUE      │
 * ├─────────────────┤
 * │ ID              │
 * │ AMOUNT          │
 * │ PAYMENT_DATE    │
 * │ CHEQUE_NUMBER   │
 * └─────────────────┘
 * </pre>
 * <p>
 * Strategy Comparison:
 * <pre>
 * ┌────────────────┬───────────────┬────────────────┬────────────────┐
 * │   Criteria     │ SINGLE_TABLE  │    JOINED      │ TABLE_PER_CLASS│
 * ├────────────────┼───────────────┼────────────────┼────────────────┤
 * │ Performance    │ Best          │ Medium         │ Worst          │
 * │ Normalization  │ Poor          │ Best           │ Medium         │
 * │ Polymorphism   │ Best          │ Good           │ Medium         │
 * │ Disk Space     │ Best          │ Medium         │ Worst          │
 * │ Query Complex. │ Simple        │ Medium         │ Complex        │
 * └────────────────┴───────────────┴────────────────┴────────────────┘
 * </pre>
 */
@Data
@NoArgsConstructor
@MappedSuperclass
public abstract class Payment {

    /**
     * Primary key strategy:
     * - Uses SEQUENCE generator
     * - Allocation size: 1
     * - Initial value: 1
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "payment_seq")
    @SequenceGenerator(
            name = "payment_seq",
            sequenceName = "PAYMENT_SEQ",
            allocationSize = 1,
            initialValue = 1
    )
    private Long id;

    /**
     * Payment amount
     * - Required field
     * - Must be positive
     * - Precision: 19, Scale: 2
     */
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    /**
     * Payment date
     * - Required field
     * - Automatically set on creation
     */
    @NotNull(message = "Payment date is required")
    @Column(name = "payment_date", nullable = false)
    private LocalDateTime paymentDate;

    /**
     * Validates payment data before persistence
     * <p>
     * Validation Flow:
     * <pre>
     * validate()
     * ├── Basic Validations
     * │   ├── Amount check
     * │   └── Date check
     * │
     * └── Specific Validations
     *     └── Implemented by subclasses
     * </pre>
     *
     * @throws IllegalStateException if validation fails
     */
    @PrePersist
    @PreUpdate
    public void validate() {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Payment amount must be positive");
        }

        if (paymentDate == null) {
            paymentDate = LocalDateTime.now();
        }

        // Call specific validation in subclass
        validatePaymentSpecifics();
    }

    /**
     * Template method for payment-specific validation
     * To be implemented by concrete payment types
     * <p>
     * Implementation Guide:
     * <pre>
     * validatePaymentSpecifics()
     * ├── CreditCardPayment
     * │   ├── Validate card number
     * │   └── Check expiration
     * │
     * ├── ChequePayment
     * │   ├── Validate cheque number
     * │   └── Check bank details
     * │
     * └── CashPayment
     *     └── Validate tender amount
     * </pre>
     */
    protected abstract void validatePaymentSpecifics();

    /**
     * Template method for payment processing
     * To be implemented by concrete payment types
     * <p>
     * Processing Flow:
     * <pre>
     * process()
     * ├── Validate Payment
     * │   └── Call validate()
     * │
     * ├── Process Payment
     * │   └── Call processInternal()
     * │
     * └── Record Transaction
     *     └── Save to database
     * </pre>
     */
    public final void process() {
        validate();
        processInternal();
        // Common post-processing logic
    }

    /**
     * Internal processing method to be implemented by subclasses
     * <p>
     * Implementation Guide:
     * <pre>
     * processInternal()
     * ├── CreditCardPayment
     * │   ├── Connect to gateway
     * │   └── Process transaction
     * │
     * ├── ChequePayment
     * │   ├── Verify funds
     * │   └── Process cheque
     * │
     * └── CashPayment
     *     ├── Validate cash
     *     └── Record tender
     * </pre>
     */
    protected abstract void processInternal();
}
