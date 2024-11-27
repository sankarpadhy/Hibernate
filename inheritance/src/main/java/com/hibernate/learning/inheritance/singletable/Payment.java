package com.hibernate.learning.inheritance.singletable;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Base Payment entity using SINGLE_TABLE inheritance strategy.
 * <p>
 * This implementation creates a single table for all classes in the hierarchy.
 * The table includes columns for all fields of all subclasses, with nullable columns
 * for subclass-specific fields. A discriminator column is used to identify the specific subclass.
 * <p>
 * Advantages:
 * - Simple queries, no joins needed
 * - Better performance for polymorphic queries
 * <p>
 * Disadvantages:
 * - Table can become large with many nullable columns
 * - Cannot use NOT NULL constraints on subclass fields
 * <p>
 * Annotations explained:
 *
 * @Data - Lombok annotation that generates getters, setters, toString, equals and hashCode
 * @NoArgsConstructor - Lombok annotation that generates a no-args constructor
 * @Entity - Marks this class as a JPA entity with name "SingleTablePayment"
 * @Table - Specifies the table name in the database
 * @Inheritance - Defines the inheritance strategy as SINGLE_TABLE
 * @DiscriminatorColumn - Specifies the column used to differentiate between subclasses
 */
@Data
@NoArgsConstructor
@Entity(name = "SingleTablePayment")
@Table(name = "single_table_payment")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "payment_type", discriminatorType = DiscriminatorType.STRING)
public abstract class Payment {

    /**
     * Primary key for the payment entity.
     *
     * @Id - Marks this field as the primary key
     * @GeneratedValue - Configures auto-generation of IDs using identity strategy
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The amount of the payment
     *
     * @Column - Specifies column properties (nullable=false means this field is required)
     */
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    /**
     * The timestamp when the payment was created
     */
    @Column(name = "payment_date", nullable = false)
    private LocalDateTime paymentDate;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status = PaymentStatus.PENDING;
    /**
     * The currency of the payment
     */
    @NotBlank(message = "Currency is required")
    @Column(name = "currency", nullable = false, length = 3)
    private String currency;
    /**
     * The transaction ID of the payment
     */
    @Column(name = "transaction_id", unique = true)
    private String transactionId;

    public abstract void validate();

    protected abstract void processInternal();

    public void process() {
        validate();
        setStatus(PaymentStatus.PROCESSING);
        processInternal();
        setStatus(PaymentStatus.COMPLETED);
    }

    /**
     * The status of the payment
     */
    public enum PaymentStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED,
        REFUNDED
    }
}
