package com.hibernate.learning.locking;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.OptimisticLockException;
import javax.persistence.PessimisticLockException;
import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite demonstrating Hibernate locking strategies
 * <p>
 * Test Scenarios:
 * <pre>
 * 1. Optimistic Locking
 * ├── Concurrent updates
 * ├── Version conflicts
 * └── Resolution strategies
 *
 * 2. Pessimistic Locking
 * ├── Read locks
 * ├── Write locks
 * └── Timeout handling
 *
 * 3. Natural Locking
 * ├── Unique constraints
 * └── Database constraints
 * </pre>
 */
@SpringBootTest
@TestPropertySource(properties = {
        "spring.jpa.show-sql=true",
        "spring.jpa.properties.hibernate.format_sql=true"
})
public class LockingDemoTest {

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private AccountService accountService;

    private Account testAccount;

    @BeforeEach
    @Transactional
    public void setUp() {
        // Create test account
        testAccount = accountService.createAccount("TEST-001", new BigDecimal("1000.00"));
    }

    /**
     * Tests optimistic locking with concurrent updates
     * <p>
     * Scenario:
     * <pre>
     * 1. Thread 1 reads account
     * 2. Thread 2 reads account
     * 3. Thread 1 updates balance
     * 4. Thread 2 attempts update
     * 5. Thread 2 fails with OptimisticLockException
     * </pre>
     */
    @Test
    public void testOptimisticLocking() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(1);

        // First transaction
        executor.submit(() -> {
            try {
                latch.await(); // Wait for signal
                accountService.updateBalanceOptimistic(testAccount.getId(), new BigDecimal("100.00"));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                fail("Thread was interrupted", e);
            } catch (Exception e) {
                fail("First transaction should succeed", e);
            }
        });

        // Second transaction
        executor.submit(() -> {
            try {
                latch.await(); // Wait for signal
                accountService.updateBalanceOptimistic(testAccount.getId(), new BigDecimal("200.00"));
                fail("Second transaction should fail with OptimisticLockException");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                fail("Thread was interrupted", e);
            } catch (RuntimeException e) {
                assertTrue(e.getCause() instanceof OptimisticLockException);
            }
        });

        latch.countDown(); // Signal both threads to start
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
    }

    /**
     * Tests pessimistic read locking
     * <p>
     * Scenario:
     * <pre>
     * 1. Thread 1 acquires read lock
     * 2. Thread 2 attempts write
     * 3. Thread 2 fails with PessimisticLockException
     * </pre>
     */
    @Test
    public void testPessimisticReadLocking() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch readLatch = new CountDownLatch(1);
        CountDownLatch writeLatch = new CountDownLatch(1);

        // Read transaction
        executor.submit(() -> {
            try {
                BigDecimal balance = accountService.getBalancePessimisticRead(testAccount.getId());
                readLatch.countDown(); // Signal read lock acquired
                writeLatch.await(); // Wait for write attempt to complete
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                fail("Thread was interrupted", e);
            } catch (Exception e) {
                fail("Read transaction should succeed", e);
            }
        });

        // Write transaction
        executor.submit(() -> {
            try {
                readLatch.await(); // Wait for read lock
                accountService.updateBalancePessimisticWrite(testAccount.getId(), new BigDecimal("50.00"));
                fail("Write should fail with PessimisticLockException");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                fail("Thread was interrupted", e);
            } catch (RuntimeException e) {
                assertTrue(e.getCause() instanceof PessimisticLockException);
            } finally {
                writeLatch.countDown();
            }
        });

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
    }

    /**
     * Tests pessimistic write locking
     * <p>
     * Scenario:
     * <pre>
     * 1. Thread 1 acquires write lock
     * 2. Thread 2 attempts write
     * 3. Thread 2 fails with PessimisticLockException
     * </pre>
     */
    @Test
    public void testPessimisticWriteLocking() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch writeLatch = new CountDownLatch(1);
        CountDownLatch completionLatch = new CountDownLatch(1);

        // First write transaction
        executor.submit(() -> {
            try {
                accountService.updateBalancePessimisticWrite(testAccount.getId(), new BigDecimal("75.00"));
                writeLatch.countDown(); // Signal write lock acquired
                completionLatch.await(); // Wait for second transaction attempt
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                fail("Thread was interrupted", e);
            } catch (Exception e) {
                fail("First write should succeed", e);
            }
        });

        // Second write transaction
        executor.submit(() -> {
            try {
                writeLatch.await(); // Wait for first write lock
                accountService.updateBalancePessimisticWrite(testAccount.getId(), new BigDecimal("25.00"));
                fail("Second write should fail with PessimisticLockException");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                fail("Thread was interrupted", e);
            } catch (RuntimeException e) {
                assertTrue(e.getCause() instanceof PessimisticLockException);
            } finally {
                completionLatch.countDown();
            }
        });

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
    }

    /**
     * Tests force version increment
     * <p>
     * Scenario:
     * <pre>
     * 1. Get initial version
     * 2. Force increment
     * 3. Verify version increased
     * </pre>
     */
    @Test
    @Transactional
    public void testForceVersionIncrement() {
        Session session = sessionFactory.getCurrentSession();

        // Get initial version
        Long initialVersion = testAccount.getVersion();

        // Force version increment
        accountService.forceVersionIncrement(testAccount.getId());

        // Clear session to force reload
        session.clear();

        // Reload account and verify version
        Account reloadedAccount = session.get(Account.class, testAccount.getId());
        assertTrue(reloadedAccount.getVersion() > initialVersion);
    }

    /**
     * Tests natural locking with unique constraints
     * <p>
     * Scenario:
     * <pre>
     * 1. Create account with number
     * 2. Attempt duplicate creation
     * 3. Verify constraint violation
     * </pre>
     */
    @Test
    public void testNaturalLocking() throws InterruptedException {
        String accountNumber = "TEST-002";

        // Create first account
        Account account1 = accountService.createAccount(accountNumber, new BigDecimal("500.00"));
        assertNotNull(account1);

        // Attempt to create duplicate
        assertThrows(RuntimeException.class, () ->
                accountService.createAccount(accountNumber, new BigDecimal("750.00")));
    }
}
