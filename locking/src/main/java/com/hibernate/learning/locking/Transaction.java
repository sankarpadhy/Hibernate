package com.hibernate.learning.locking;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Transaction entity for demonstrating locking with relationships
 * <p>
 * Database Schema:
 * <pre>
 * ┌─────────────────────────┐
 * │       TRANSACTION       │
 * ├─────────────────────────┤
 * │ ID (PK)                 │
 * │ ACCOUNT_ID (FK)         │
 * │ AMOUNT                  │
 * │ DESCRIPTION            │
 * │ TRANSACTION_DATE        │
 * │ VERSION                 │
 * └─────────────────────────┘
 * </pre>
 */
@Entity
@Table(name = "TRANSACTION")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    @ToString.Exclude
    private Account account;

    @NotNull(message = "Amount is required")
    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "description")
    private String description;

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate = LocalDateTime.now();

    /**
     * Version field for optimistic locking
     * Automatically incremented on each update
     */
    @Version
    @Column(name = "version")
    private Long version;
}
