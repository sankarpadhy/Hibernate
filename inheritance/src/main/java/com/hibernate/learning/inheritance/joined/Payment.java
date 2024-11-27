package com.hibernate.learning.inheritance.joined;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Base class for the payment inheritance hierarchy demonstrating the JOINED inheritance strategy.
 * <p>
 * Inheritance Structure:
 * <pre>
 * Payment (Abstract)
 * ├── Common Fields
 * │   ├── id
 * │   ├── amount
 * │   └── paymentDate
 * │
 * ├── CreditCardPayment
 * │   ├── cardNumber
 * │   ├── expirationMonth
 * │   ├── expirationYear
 * │   └── cardHolderName
 * │
 * └── BankTransferPayment
 *     ├── bankName
 *     ├── accountNumber
 *     └── routingNumber
 * </pre>
 * <p>
 * Database Schema:
 * <pre>
 * PAYMENT table
 * ├── ID (PK)
 * ├── DTYPE
 * ├── AMOUNT
 * └── PAYMENT_DATE
 *
 * CREDIT_CARD_PAYMENT table
 * ├── ID (PK, FK → PAYMENT)
 * ├── CARD_NUMBER
 * ├── EXPIRATION_MONTH
 * ├── EXPIRATION_YEAR
 * └── CARD_HOLDER_NAME
 *
 * BANK_TRANSFER_PAYMENT table
 * ├── ID (PK, FK → PAYMENT)
 * ├── BANK_NAME
 * ├── ACCOUNT_NUMBER
 * └── ROUTING_NUMBER
 * </pre>
 * <p>
 * Key Features:
 * 1. Normalized Schema
 * - Each subclass has its own table
 * - Foreign key relationships maintain integrity
 * - No null columns
 * <p>
 * 2. Polymorphic Queries
 * - Efficient type-specific queries
 * - Natural joins for full objects
 * - Good for frequent polymorphic queries
 * <p>
 * 3. Data Integrity
 * - Strong referential integrity
 * - No wasted space
 * - Clean schema design
 */
@Entity
@Table(name = "PAYMENT")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull(message = "Amount is required")
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    @NotBlank(message = "Currency is required")
    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "USD";
    @Column(name = "payment_date", nullable = false)
    private LocalDateTime paymentDate = LocalDateTime.now();
    @NotNull(message = "Payment status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status = PaymentStatus.PENDING;
    @Column(name = "transaction_id", unique = true)
    private String transactionId;

    @PrePersist
    protected void onCreate() {
        if (paymentDate == null) {
            paymentDate = LocalDateTime.now();
        }
        if (transactionId == null) {
            transactionId = generateTransactionId();
        }
    }

    protected String generateTransactionId() {
        return "TXN" + System.currentTimeMillis();
    }

    /**
     * Validates the payment before persistence.
     * <p>
     * Validation Flow:
     * <pre>
     * validate()
     * ├── Common Validation
     * │   ├── Check amount > 0
     * │   └── Check paymentDate not null
     * │
     * └── Specific Validation
     *     └── Delegated to subclasses
     * </pre>
     *
     * @throws IllegalStateException if validation fails
     */
    public void validate() {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Amount must be positive");
        }
        if (currency == null || currency.trim().isEmpty()) {
            throw new IllegalStateException("Currency is required");
        }
        if (!currency.matches("[A-Z]{3}")) {
            throw new IllegalStateException("Currency must be a 3-letter code");
        }
        if (status == null) {
            throw new IllegalStateException("Payment status is required");
        }
    }

    /**
     * Template method for processing payments.
     * <p>
     * Processing Flow:
     * <pre>
     * process()
     * ├── Validation
     * │   └── validate()
     * │
     * ├── Common Processing
     * │   ├── Record timestamp
     * │   └── Log transaction
     * │
     * └── Specific Processing
     *     └── processInternal()
     * </pre>
     */
    public final void process() {
        validate();
        status = PaymentStatus.PROCESSING;
        try {
            processInternal();
            status = PaymentStatus.COMPLETED;
        } catch (Exception e) {
            status = PaymentStatus.FAILED;
            throw new RuntimeException("Payment processing failed", e);
        }
    }

    /**
     * Internal processing method to be implemented by subclasses.
     * Each payment type will have its own processing logic.
     */
    protected abstract void processInternal();

    public enum PaymentStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED,
        REFUNDED
    }
}
