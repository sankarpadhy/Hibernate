package com.hibernate.learning.inheritance.tableperclass;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Base Payment class for TABLE_PER_CLASS inheritance strategy
 * <p>
 * Inheritance Strategy Overview:
 * <pre>
 * TABLE_PER_CLASS
 * ├── Separate Tables
 * │   ├── Each subclass gets its own table
 * │   ├── No shared table structure
 * │   └── Complete data independence
 * │
 * ├── Table Structure
 * │   ├── Contains all parent fields
 * │   ├── Contains subclass fields
 * │   └── No discriminator needed
 * │
 * └── Query Behavior
 *     ├── UNION ALL for polymorphic queries
 *     ├── Direct access for concrete types
 *     └── Efficient single-type queries
 * </pre>
 * <p>
 * Database Schema Example:
 * <pre>
 * ┌─────────────────────┐
 * │   CREDIT_PAYMENT    │
 * ├─────────────────────┤
 * │ ID                  │
 * │ AMOUNT              │
 * │ PAYMENT_DATE        │
 * │ CARD_NUMBER         │
 * │ EXPIRY_DATE         │
 * │ CVV                 │
 * └─────────────────────┘
 *
 * ┌─────────────────────┐
 * │   BANK_TRANSFER     │
 * ├─────────────────────┤
 * │ ID                  │
 * │ AMOUNT              │
 * │ PAYMENT_DATE        │
 * │ ACCOUNT_NUMBER      │
 * │ BANK_CODE           │
 * │ REFERENCE           │
 * └─────────────────────┘
 * </pre>
 * <p>
 * Key Characteristics:
 * <pre>
 * 1. Data Storage
 * ├── Complete table separation
 * ├── No shared storage
 * └── Type-specific optimization
 *
 * 2. Performance
 * ├── Fast concrete queries
 * ├── UNION ALL overhead
 * └── No joins needed
 *
 * 3. Flexibility
 * ├── Independent evolution
 * ├── Type-specific indexes
 * └── Custom constraints
 * </pre>
 * <p>
 * Example Queries:
 * <pre>
 * 1. All Payments (Polymorphic)
 * SELECT * FROM (
 *   SELECT id, amount, payment_date, 'CREDIT' as type
 *   FROM CREDIT_PAYMENT
 *   UNION ALL
 *   SELECT id, amount, payment_date, 'BANK' as type
 *   FROM BANK_TRANSFER
 * ) payments
 *
 * 2. Type-Specific Query
 * SELECT *
 * FROM CREDIT_PAYMENT
 * WHERE amount > 1000
 *
 * 3. Date Range Query
 * SELECT * FROM (
 *   SELECT * FROM CREDIT_PAYMENT
 *   UNION ALL
 *   SELECT * FROM BANK_TRANSFER
 * ) payments
 * WHERE payment_date BETWEEN ? AND ?
 * </pre>
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Data
public abstract class Payment {

    /**
     * Unique identifier for the payment
     * - Generated using sequence
     * - Must be unique across all payment types
     * - Used for tracking and referencing
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    /**
     * Payment amount
     * - Must be positive
     * - Stored with high precision
     * - Currency handling in real implementation
     */
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    /**
     * Payment date and time
     * - Automatically set on creation
     * - Used for tracking and reporting
     * - Timezone handling in real implementation
     */
    @NotNull(message = "Payment date is required")
    @Column(name = "payment_date", nullable = false)
    private LocalDateTime paymentDate;
    /**
     * Payment status tracking
     * - Managed internally
     * - Used for payment lifecycle
     * - Not stored in database
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status = PaymentStatus.PENDING;
    /**
     * Currency of the payment
     * - Must be a valid ISO 4217 code
     * - Used for currency conversion
     * - Stored as a string for simplicity
     */
    @NotBlank(message = "Currency is required")
    @Column(name = "currency", nullable = false, length = 3)
    private String currency;
    /**
     * Unique transaction ID for the payment
     * - Used for tracking and referencing
     * - Must be unique across all payments
     * - Stored as a string for simplicity
     */
    @Column(name = "transaction_id", unique = true)
    private String transactionId;

    /**
     * Validate payment data
     * <p>
     * Validation Flow:
     * <pre>
     * validate()
     * ├── Basic validation
     * │   ├── Amount check
     * │   └── Date validation
     * │
     * └── Type-specific validation
     *     ├── Implemented by concrete classes
     *     └── Handles type-specific validation
     * </pre>
     */
    public abstract void validate();

    /**
     * Process payment type specific logic
     * - Implemented by concrete classes
     * - Handles type-specific processing
     * - Called during processing
     */
    protected abstract void processInternal();

    /**
     * Process the payment
     * <p>
     * Processing Flow:
     * <pre>
     * process()
     * ├── Validation
     * │   ├── Basic validation
     * │   └── Type-specific validation
     * │
     * ├── Processing
     * │   ├── Status update
     * │   ├── Type-specific logic
     * │   └── Error handling
     * │
     * └── Confirmation
     *     ├── Status finalization
     *     └── Success/failure handling
     * </pre>
     */
    public void process() {
        validate();
        setStatus(PaymentStatus.PROCESSING);
        processInternal();
        setStatus(PaymentStatus.COMPLETED);
    }

    /**
     * Set the payment status
     *
     * @param status the new payment status
     */
    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    /**
     * Payment status enumeration
     * - Tracks payment lifecycle
     * - Used for state management
     * - Supports payment flow
     */
    public enum PaymentStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED,
        REFUNDED
    }
}
