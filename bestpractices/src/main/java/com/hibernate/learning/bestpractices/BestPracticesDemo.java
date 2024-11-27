package com.hibernate.learning.bestpractices;

import com.hibernate.learning.util.HibernateUtil;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.stat.Statistics;

import java.time.LocalDateTime;

/**
 * Demonstrates Hibernate best practices and common patterns.
 * <p>
 * Best Practices Architecture:
 * <pre>
 * Application Layer
 * ├── Session Management
 * │   ├── Session per request
 * │   └── Transaction boundaries
 * │
 * ├── Entity Design
 * │   ├── Proper ID generation
 * │   ├── Relationship mapping
 * │   └── Fetch strategies
 * │
 * ├── Performance Optimization
 * │   ├── Caching strategies
 * │   ├── Batch processing
 * │   └── Query optimization
 * │
 * └── Error Handling
 *     ├── Transaction management
 *     ├── Exception handling
 *     └── Resource cleanup
 * </pre>
 * <p>
 * Session Lifecycle:
 * <pre>
 * Open Session → Begin Transaction → Operations → Commit/Rollback → Close
 *      ↑                                                              ↓
 *      └──────────────────── Resource Cleanup ───────────────────────┘
 * </pre>
 */
@Slf4j
public class BestPracticesDemo {
    private final SessionFactory sessionFactory;

    public BestPracticesDemo(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public static void main(String[] args) {
        BestPracticesDemo demo = new BestPracticesDemo(HibernateUtil.getSessionFactory());
        demo.demonstrateEntityDesign();
        demo.demonstrateTransactionManagement();
        demo.demonstrateBatchProcessing();
        demo.demonstrateExceptionHandling();
        demo.demonstrateStatistics();
    }

    /**
     * Demonstrates proper session and transaction handling.
     * <p>
     * Pattern Flow:
     * <pre>
     * 1. Resource Acquisition
     *    └── Session opening
     *        └── Transaction begin
     *
     * 2. Operation Execution
     *    ├── Try block
     *    │   └── Business logic
     *    └── Catch block
     *        └── Error handling
     *
     * 3. Resource Cleanup
     *    ├── Transaction end
     *    │   ├── Commit
     *    │   └── Rollback
     *    └── Session close
     * </pre>
     */
    private void demonstrateEntityDesign() {
        log.info("=== Entity Design Best Practices Demo ===");
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();

            // Create a customer using best practices
            Customer customer = new Customer();
            customer.setEmail("john.doe@example.com");
            customer.setFirstName("John");
            customer.setLastName("Doe");
            customer.setPhoneNumber("+1-555-123-4567");
            customer.setDateOfBirth(LocalDateTime.of(1990, 1, 1, 0, 0));
            customer.setStatus(CustomerStatus.ACTIVE);
            customer.setCreatedBy("system");
            customer.setCreatedAt(LocalDateTime.now());

            session.save(customer);
            tx.commit();

            // Demonstrate optimistic locking
            tx = session.beginTransaction();
            customer.setPhoneNumber("+1-555-987-6543");
            session.update(customer);
            tx.commit();

            log.info("Customer created and updated with version control: {}", customer.getVersion());
        }
    }

    /**
     * Demonstrates proper transaction management.
     * <p>
     * Transaction Management:
     * <pre>
     * Transaction Boundaries
     * ├── Begin transaction
     * │   └── Session begin
     * │
     * ├── Operation Execution
     * │   ├── Try block
     * │   │   └── Business logic
     * │   └── Catch block
     * │       └── Error handling
     * │
     * └── Transaction End
     *     ├── Commit
     *     └── Rollback
     * </pre>
     */
    private void demonstrateTransactionManagement() {
        log.info("=== Transaction Management Best Practices Demo ===");
        Session session = null;
        Transaction tx = null;
        try {
            session = sessionFactory.openSession();
            tx = session.beginTransaction();

            Customer customer = new Customer();
            customer.setEmail("jane.smith@example.com");
            customer.setFirstName("Jane");
            customer.setLastName("Smith");
            customer.setStatus(CustomerStatus.ACTIVE);
            customer.setCreatedBy("system");
            customer.setCreatedAt(LocalDateTime.now());

            session.save(customer);

            // Demonstrate proper transaction commit
            tx.commit();
            log.info("Transaction committed successfully");

        } catch (Exception e) {
            // Demonstrate proper transaction rollback
            if (tx != null) {
                tx.rollback();
                log.error("Transaction rolled back due to error", e);
            }
            throw e;
        } finally {
            // Demonstrate proper resource cleanup
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    /**
     * Demonstrates batch processing best practices.
     * <p>
     * Batch Processing Flow:
     * <pre>
     * Start Batch
     * ├── Set batch size
     * └── Process items
     *     ├── Add to batch
     *     │   └── Check batch size
     *     │       ├── Flush if full
     *     │       └── Clear session
     *     └── Final flush
     * </pre>
     */
    private void demonstrateBatchProcessing() {
        log.info("=== Batch Processing Best Practices Demo ===");
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();

            // Demonstrate batch inserts
            int batchSize = 5;
            for (int i = 0; i < 20; i++) {
                Customer customer = new Customer();
                customer.setEmail("customer" + i + "@example.com");
                customer.setFirstName("FirstName" + i);
                customer.setLastName("LastName" + i);
                customer.setStatus(CustomerStatus.ACTIVE);
                customer.setCreatedBy("system");
                customer.setCreatedAt(LocalDateTime.now());

                session.save(customer);

                // Clear session and flush every batch
                if (i > 0 && i % batchSize == 0) {
                    session.flush();
                    session.clear();
                    log.info("Processed batch of {}", batchSize);
                }
            }

            tx.commit();
            log.info("Batch processing completed");
        }
    }

    /**
     * Demonstrates proper exception handling.
     * <p>
     * Exception Handling:
     * <pre>
     * Exception Types
     * ├── JDBCException
     * │   ├── SQL errors
     * │   └── Connection issues
     * │
     * ├── StaleObjectStateException
     * │   ├── Optimistic locking
     * │   └── Concurrent updates
     * │
     * └── HibernateException
     *     ├── Configuration
     *     └── Mapping issues
     * </pre>
     */
    private void demonstrateExceptionHandling() {
        log.info("=== Exception Handling Best Practices Demo ===");
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();

            try {
                // Attempt to create a customer with invalid data
                Customer customer = new Customer();
                // Deliberately missing required fields
                session.save(customer);

                tx.commit();
            } catch (Exception e) {
                tx.rollback();
                log.error("Error occurred while saving customer: {}", e.getMessage());
                // In real application, you might want to:
                // 1. Log the full stack trace
                // 2. Translate to a business-specific exception
                // 3. Handle specific Hibernate exceptions differently
            }
        }
    }

    /**
     * Demonstrates statistics and monitoring.
     * <p>
     * Statistics and Monitoring:
     * <pre>
     * Statistics
     * ├── Entity loads
     * │   └── First level cache hits
     * │
     * ├── Entity updates
     * │   └── Dirty checking
     * │
     * └── Query statistics
     *     ├── Query execution time
     *     └── Query cache hits
     * </pre>
     */
    private void demonstrateStatistics() {
        log.info("=== Statistics and Monitoring Demo ===");
        Statistics stats = sessionFactory.getStatistics();
        stats.setStatisticsEnabled(true);

        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();

            // Perform some operations
            Customer customer = new Customer();
            customer.setEmail("stats.demo@example.com");
            customer.setFirstName("Stats");
            customer.setLastName("Demo");
            customer.setStatus(CustomerStatus.ACTIVE);
            customer.setCreatedAt(LocalDateTime.now());
            customer.setCreatedBy("system");
            session.save(customer);

            // First load - should hit the database
            customer = session.get(Customer.class, customer.getId());

            // Second load - should hit the session cache
            customer = session.get(Customer.class, customer.getId());

            tx.commit();

            // Log statistics
            log.info("Session statistics:");
            log.info("- Entity loads: {}", stats.getEntityLoadCount());
            log.info("- Entity updates: {}", stats.getEntityUpdateCount());
            log.info("- Entity inserts: {}", stats.getEntityInsertCount());
            log.info("- Second level cache hits: {}", stats.getSecondLevelCacheHitCount());
            log.info("- Second level cache misses: {}", stats.getSecondLevelCacheMissCount());
        }
    }
}
