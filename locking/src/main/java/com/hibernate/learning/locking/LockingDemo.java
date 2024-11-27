package com.hibernate.learning.locking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Demonstrates various locking scenarios in Hibernate
 * <p>
 * Scenarios:
 * <pre>
 * 1. Concurrent Balance Updates
 * ├── Optimistic Locking
 * ├── Version Conflicts
 * └── Retry Logic
 *
 * 2. Account Transfers
 * ├── Pessimistic Locking
 * ├── Deadlock Prevention
 * └── Transaction Isolation
 *
 * 3. Account Creation
 * ├── Natural Locking
 * ├── Unique Constraints
 * └── Error Handling
 * </pre>
 */
@Component
public class LockingDemo {

    private static final Logger logger = LoggerFactory.getLogger(LockingDemo.class);
    private final AccountService accountService;

    public LockingDemo(AccountService accountService) {
        this.accountService = accountService;
    }

    /**
     * Demonstrates concurrent balance updates with optimistic locking
     * <p>
     * Flow:
     * <pre>
     * demonstrateConcurrentUpdates()
     * ├── Create test account
     * ├── Start concurrent updates
     * │   ├── Thread 1: Add amount
     * │   └── Thread 2: Subtract amount
     * └── Handle version conflicts
     * </pre>
     */
    public void demonstrateConcurrentUpdates() {
        // Create test account
        Account account = accountService.createAccount("DEMO-001", new BigDecimal("1000.00"));
        logger.info("Created test account with ID: {}", account.getId());

        // Create thread pool
        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            // Submit concurrent updates
            executor.submit(() -> {
                try {
                    accountService.updateBalanceOptimistic(account.getId(), new BigDecimal("500.00"));
                    logger.info("Added 500.00 to account");
                } catch (Exception e) {
                    logger.error("Error adding amount", e);
                }
            });

            executor.submit(() -> {
                try {
                    accountService.updateBalanceOptimistic(account.getId(), new BigDecimal("-200.00"));
                    logger.info("Subtracted 200.00 from account");
                } catch (Exception e) {
                    logger.error("Error subtracting amount", e);
                }
            });

            // Shutdown executor and wait for completion
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);

            // Check final balance
            Account finalAccount = accountService.findByAccountNumber(account.getAccountNumber()).orElseThrow();
            logger.info("Final balance: {}", finalAccount.getBalance());

        } catch (Exception e) {
            logger.error("Error in concurrent updates demo", e);
        }
    }

    /**
     * Demonstrates pessimistic locking with account transfers
     * <p>
     * Flow:
     * <pre>
     * demonstrateAccountTransfers()
     * ├── Create source account
     * ├── Create target account
     * ├── Start concurrent transfers
     * │   ├── Thread 1: Transfer amount
     * │   └── Thread 2: Transfer amount
     * └── Handle lock timeouts
     * </pre>
     */
    public void demonstrateAccountTransfers() {
        // Create test accounts
        Account source = accountService.createAccount("DEMO-002", new BigDecimal("1000.00"));
        Account target = accountService.createAccount("DEMO-003", new BigDecimal("0.00"));
        logger.info("Created test accounts - Source: {}, Target: {}", source.getId(), target.getId());

        // Create thread pool
        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            // Submit concurrent transfers
            executor.submit(() -> {
                try {
                    accountService.updateBalancePessimisticWrite(source.getId(), new BigDecimal("-300.00"));
                    accountService.updateBalancePessimisticWrite(target.getId(), new BigDecimal("300.00"));
                    logger.info("Transferred 300.00 from source to target");
                } catch (Exception e) {
                    logger.error("Error in first transfer", e);
                }
            });

            executor.submit(() -> {
                try {
                    accountService.updateBalancePessimisticWrite(source.getId(), new BigDecimal("-200.00"));
                    accountService.updateBalancePessimisticWrite(target.getId(), new BigDecimal("200.00"));
                    logger.info("Transferred 200.00 from source to target");
                } catch (Exception e) {
                    logger.error("Error in second transfer", e);
                }
            });

            // Shutdown executor and wait for completion
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);

            // Check final balances
            Account finalSource = accountService.findByAccountNumber(source.getAccountNumber()).orElseThrow();
            Account finalTarget = accountService.findByAccountNumber(target.getAccountNumber()).orElseThrow();
            logger.info("Final balances - Source: {}, Target: {}",
                    finalSource.getBalance(), finalTarget.getBalance());

        } catch (Exception e) {
            logger.error("Error in account transfers demo", e);
        }
    }

    /**
     * Demonstrates natural locking with unique constraints
     * <p>
     * Flow:
     * <pre>
     * demonstrateNaturalLocking()
     * ├── Create initial account
     * ├── Attempt duplicate creation
     * └── Handle constraint violation
     * </pre>
     */
    public void demonstrateNaturalLocking() {
        String accountNumber = "DEMO-004";

        try {
            // Create initial account
            Account account1 = accountService.createAccount(accountNumber, new BigDecimal("500.00"));
            logger.info("Created first account with number: {}", account1.getAccountNumber());

            // Attempt to create duplicate
            try {
                Account account2 = accountService.createAccount(accountNumber, new BigDecimal("750.00"));
                logger.error("Unexpected success creating duplicate account: {}", account2.getAccountNumber());
            } catch (RuntimeException e) {
                logger.info("Expected failure creating duplicate account: {}", e.getMessage());
            }

        } catch (Exception e) {
            logger.error("Error in natural locking demo", e);
        }
    }

    /**
     * Run all locking demonstrations
     */
    public void runAllDemos() {
        logger.info("Starting Locking Demonstrations");

        logger.info("1. Demonstrating Concurrent Updates (Optimistic Locking)");
        demonstrateConcurrentUpdates();

        logger.info("2. Demonstrating Account Transfers (Pessimistic Locking)");
        demonstrateAccountTransfers();

        logger.info("3. Demonstrating Natural Locking");
        demonstrateNaturalLocking();

        logger.info("Completed Locking Demonstrations");
    }
}
