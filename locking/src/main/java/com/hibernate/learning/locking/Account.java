package com.hibernate.learning.locking;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Account entity demonstrating various locking strategies
 * <p>
 * Locking Types Demonstrated:
 * <pre>
 * 1. Optimistic Locking
 * ├── @Version annotation
 * ├── Automatic version tracking
 * └── Concurrent modification detection
 *
 * 2. Pessimistic Locking
 * ├── PESSIMISTIC_READ
 * │   └── Shared lock for reading
 * ├── PESSIMISTIC_WRITE
 * │   └── Exclusive lock for updates
 * └── PESSIMISTIC_FORCE_INCREMENT
 *     └── Forces version increment
 *
 * 3. Natural Locking
 * ├── Database constraints
 * └── Unique constraints
 * </pre>
 * <p>
 * Database Schema:
 * <pre>
 * ┌─────────────────────────┐
 * │         ACCOUNT         │
 * ├─────────────────────────┤
 * │ ID (PK)                 │
 * │ ACCOUNT_NUMBER (UNIQUE) │
 * │ BALANCE                 │
 * │ VERSION                 │
 * │ LAST_MODIFIED          │
 * │ STATUS                  │
 * └─────────────────────────┘
 * </pre>
 */
@Entity
@Table(name = "ACCOUNT")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@org.hibernate.annotations.DynamicUpdate
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Account number is required")
    @Column(name = "account_number", unique = true, nullable = false)
    private String accountNumber;

    @NotNull(message = "Balance is required")
    @DecimalMin(value = "0.0", message = "Balance must be positive")
    @Column(name = "balance", nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;

    /**
     * Version field for optimistic locking
     * Automatically incremented on each update
     */
    @Version
    @Column(name = "version")
    private Long version;

    @Column(name = "last_modified")
    private LocalDateTime lastModified;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private AccountStatus status = AccountStatus.ACTIVE;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Transaction> transactions = new ArrayList<>();

    /**
     * Adds a transaction to the account
     * <p>
     * Transaction Flow:
     * <pre>
     * addTransaction()
     * ├── Validate amount
     * ├── Update balance
     * ├── Create transaction
     * └── Update last modified
     * </pre>
     */
    public void addTransaction(BigDecimal amount, String description) {
        Transaction transaction = new Transaction();
        transaction.setAccount(this);
        transaction.setAmount(amount);
        transaction.setDescription(description);
        transaction.setTransactionDate(LocalDateTime.now());

        this.balance = this.balance.add(amount);
        this.lastModified = LocalDateTime.now();
        this.transactions.add(transaction);
    }

    /**
     * Pre-update callback to set last modified date
     */
    @PreUpdate
    protected void onUpdate() {
        this.lastModified = LocalDateTime.now();
    }

    /**
     * Account status enum
     */
    public enum AccountStatus {
        ACTIVE,
        BLOCKED,
        CLOSED
    }
}
