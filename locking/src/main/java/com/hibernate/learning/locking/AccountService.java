package com.hibernate.learning.locking;

import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.exception.LockAcquisitionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.OptimisticLockException;
import javax.persistence.PessimisticLockException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service demonstrating various locking strategies in Hibernate
 * <p>
 * Locking Strategies:
 * <pre>
 * 1. Optimistic Locking
 * ├── Version-based
 * │   ├── Automatic version increment
 * │   └── Concurrent modification check
 * └── Timestamp-based
 *     ├── Last modified tracking
 *     └── Temporal version control
 *
 * 2. Pessimistic Locking
 * ├── PESSIMISTIC_READ
 * │   ├── Shared lock
 * │   └── Allows concurrent reads
 * ├── PESSIMISTIC_WRITE
 * │   ├── Exclusive lock
 * │   └── Prevents concurrent access
 * └── PESSIMISTIC_FORCE_INCREMENT
 *     ├── Exclusive lock
 *     └── Forces version increment
 *
 * 3. Isolation Levels
 * ├── READ_UNCOMMITTED
 * │   └── Dirty reads possible
 * ├── READ_COMMITTED
 * │   └── No dirty reads
 * ├── REPEATABLE_READ
 * │   └── Consistent reads
 * └── SERIALIZABLE
 *     └── Complete isolation
 * </pre>
 */
@Service
public class AccountService {

    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);
    private final SessionFactory sessionFactory;

    public AccountService(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /**
     * Demonstrates optimistic locking with version checking
     * <p>
     * Flow:
     * <pre>
     * updateBalanceOptimistic()
     * ├── Load account
     * ├── Modify balance
     * ├── Version check
     * └── Update or fail
     * </pre>
     */
    @Transactional
    public void updateBalanceOptimistic(Long accountId, BigDecimal amount) {
        try {
            Session session = sessionFactory.getCurrentSession();
            Account account = session.get(Account.class, accountId);

            if (account != null) {
                account.setBalance(account.getBalance().add(amount));
                account.addTransaction(amount, "Optimistic lock demo");
                session.update(account);
            }
        } catch (OptimisticLockException e) {
            logger.error("Concurrent modification detected", e);
            throw new RuntimeException("Transaction failed due to concurrent modification");
        }
    }

    /**
     * Demonstrates pessimistic read locking
     * <p>
     * Flow:
     * <pre>
     * getBalancePessimisticRead()
     * ├── Acquire shared lock
     * ├── Read balance
     * └── Release lock
     * </pre>
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public BigDecimal getBalancePessimisticRead(Long accountId) {
        try {
            Session session = sessionFactory.getCurrentSession();
            Account account = session.get(Account.class, accountId,
                    new LockOptions(LockMode.PESSIMISTIC_READ)
                            .setTimeOut(LockOptions.WAIT_FOREVER));

            return account != null ? account.getBalance() : BigDecimal.ZERO;
        } catch (PessimisticLockException e) {
            logger.error("Could not acquire lock", e);
            throw new RuntimeException("Could not acquire lock for reading balance");
        }
    }

    /**
     * Demonstrates pessimistic write locking
     * <p>
     * Flow:
     * <pre>
     * updateBalancePessimisticWrite()
     * ├── Acquire exclusive lock
     * ├── Update balance
     * ├── Commit changes
     * └── Release lock
     * </pre>
     */
    @Transactional
    public void updateBalancePessimisticWrite(Long accountId, BigDecimal amount) {
        try {
            Session session = sessionFactory.getCurrentSession();
            Account account = session.get(Account.class, accountId,
                    new LockOptions(LockMode.PESSIMISTIC_WRITE)
                            .setTimeOut(LockOptions.WAIT_FOREVER));

            if (account != null) {
                account.setBalance(account.getBalance().add(amount));
                account.addTransaction(amount, "Pessimistic write lock demo");
                session.update(account);
            }
        } catch (LockAcquisitionException e) {
            logger.error("Could not acquire lock", e);
            throw new RuntimeException("Could not acquire lock for updating balance");
        }
    }

    /**
     * Demonstrates force increment locking
     * <p>
     * Flow:
     * <pre>
     * forceVersionIncrement()
     * ├── Acquire exclusive lock
     * ├── Force version increment
     * ├── Update timestamp
     * └── Release lock
     * </pre>
     */
    @Transactional
    public void forceVersionIncrement(Long accountId) {
        try {
            Session session = sessionFactory.getCurrentSession();
            Account account = session.get(Account.class, accountId,
                    new LockOptions(LockMode.PESSIMISTIC_FORCE_INCREMENT)
                            .setTimeOut(LockOptions.WAIT_FOREVER));

            if (account != null) {
                account.setLastModified(LocalDateTime.now());
                session.update(account);
            }
        } catch (LockAcquisitionException e) {
            logger.error("Could not acquire lock", e);
            throw new RuntimeException("Could not acquire lock for force increment");
        }
    }

    /**
     * Demonstrates natural locking with unique constraints
     * <p>
     * Flow:
     * <pre>
     * createAccount()
     * ├── Check unique constraint
     * ├── Create account
     * └── Handle violation
     * </pre>
     */
    @Transactional
    public Account createAccount(String accountNumber, BigDecimal initialBalance) {
        Session session = sessionFactory.getCurrentSession();

        // Natural locking through unique constraint
        Optional<Account> existing = findByAccountNumber(accountNumber);
        if (existing.isPresent()) {
            throw new RuntimeException("Account number already exists");
        }

        Account account = new Account();
        account.setAccountNumber(accountNumber);
        account.setBalance(initialBalance);
        account.setLastModified(LocalDateTime.now());

        session.save(account);
        return account;
    }

    /**
     * Retrieves account with specified lock mode
     */
    @Transactional(readOnly = true)
    public Optional<Account> findByAccountNumber(String accountNumber) {
        Session session = sessionFactory.getCurrentSession();
        return session.createQuery("FROM Account a WHERE a.accountNumber = :accountNumber", Account.class)
                .setParameter("accountNumber", accountNumber)
                .setMaxResults(1)
                .getResultList()
                .stream()
                .findFirst();
    }

    /**
     * Lists all accounts with their transactions
     */
    @Transactional(readOnly = true)
    public List<Account> findAllWithTransactions() {
        Session session = sessionFactory.getCurrentSession();
        return session.createQuery(
                        "SELECT DISTINCT a FROM Account a LEFT JOIN FETCH a.transactions",
                        Account.class)
                .getResultList();
    }
}
