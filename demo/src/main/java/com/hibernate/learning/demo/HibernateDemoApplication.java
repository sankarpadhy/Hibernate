package com.hibernate.learning.demo;

import com.hibernate.learning.bestpractices.Customer;
import com.hibernate.learning.caching.Product;
import com.hibernate.learning.inheritance.singletable.BankTransferPayment;
import com.hibernate.learning.inheritance.singletable.CreditCardPayment;
import com.hibernate.learning.querying.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Main Application Class for Hibernate Learning Demo
 * <p>
 * Architecture Overview:
 * <pre>
 * ┌─────────────────────────────────────────┐
 * │           HibernateDemoApplication      │
 * │                                         │
 * │    ┌─────────────┐    ┌──────────────┐ │
 * │    │   Module    │    │    Module     │ │
 * │    │  Basics     │    │ BestPractices │ │
 * │    └─────────────┘    └──────────────┘ │
 * │    ┌─────────────┐    ┌──────────────┐ │
 * │    │   Module    │    │    Module     │ │
 * │    │  Caching    │    │ Inheritance   │ │
 * │    └─────────────┘    └──────────────┘ │
 * │    ┌─────────────┐    ┌──────────────┐ │
 * │    │   Module    │    │    Module     │ │
 * │    │ Querying    │    │Relationships  │ │
 * │    └─────────────┘    └──────────────┘ │
 * └─────────────────────────────────────────┘
 *
 * Module Contents:
 * 1. Basics
 *    - CRUD Operations
 *    - Session Management
 *    - Transaction Handling
 *
 * 2. Best Practices
 *    - Connection Pooling
 *    - Batch Processing
 *    - Exception Handling
 *
 * 3. Caching
 *    - First Level Cache
 *    - Second Level Cache
 *    - Query Cache
 *
 * 4. Inheritance
 *    - Single Table
 *    - Joined Table
 *    - Table Per Class
 *
 * 5. Querying
 *    - HQL
 *    - Criteria API
 *    - Native SQL
 *
 * 6. Relationships
 *    - One-to-One
 *    - One-to-Many
 *    - Many-to-Many
 * </pre>
 * <p>
 * SessionFactory Lifecycle:
 * <pre>
 * ┌─────────────────┐
 * │ Configuration   │
 * │ hibernate.cfg.xml│
 * └────────┬────────┘
 *          ▼
 * ┌─────────────────┐
 * │ServiceRegistry  │
 * │  Bootstrap      │
 * └────────┬────────┘
 *          ▼
 * ┌─────────────────┐
 * │   Metadata      │
 * │Entity Mappings  │
 * └────────┬────────┘
 *          ▼
 * ┌─────────────────┐
 * │ SessionFactory  │
 * │Thread-safe Cache│
 * └────────┬────────┘
 *          ▼
 * ┌─────────────────┐
 * │    Session      │
 * │Unit of Work     │
 * └─────────────────┘
 * </pre>
 */
@Slf4j
public class HibernateDemoApplication {
    private static SessionFactory sessionFactory;

    /**
     * Builds and returns the SessionFactory
     * <p>
     * Process Flow:
     * <pre>
     * 1. Load Configuration
     *    └── Read hibernate.cfg.xml
     *
     * 2. Build Registry
     *    ├── Configure Services
     *    └── Set Properties
     *
     * 3. Create Metadata
     *    ├── Scan Entities
     *    └── Process Mappings
     *
     * 4. Build SessionFactory
     *    ├── Initialize Caches
     *    └── Prepare Connections
     * </pre>
     *
     * @return SessionFactory instance
     */
    private static SessionFactory buildSessionFactory() {
        StandardServiceRegistry standardRegistry = new StandardServiceRegistryBuilder()
                .configure("hibernate.cfg.xml")
                .build();

        MetadataSources sources = new MetadataSources(standardRegistry);

        Metadata metadata = sources.getMetadataBuilder()
                .build();

        try {
            return metadata.getSessionFactoryBuilder()
                    .build();
        } catch (Exception e) {
            StandardServiceRegistryBuilder.destroy(standardRegistry);
            throw e;
        }
    }

    /**
     * Closes the SessionFactory and cleans up resources
     * <p>
     * Cleanup Process:
     * <pre>
     * 1. Close Sessions
     *    └── Flush pending changes
     *
     * 2. Close SessionFactory
     *    ├── Release connections
     *    └── Clear caches
     *
     * 3. Destroy Registry
     *    └── Release system resources
     * </pre>
     */
    public static void shutdown() {
        if (sessionFactory != null) {
            sessionFactory.close();
            log.info("SessionFactory closed");
        }
    }

    public static void main(String[] args) {
        try {
            // Initialize SessionFactory
            sessionFactory = buildSessionFactory();

            // Test each section
            testBasics();
            testInheritance();
            testCaching();
            testQuerying();
            testBestPractices();
            testLocking();
            testRelationships();

        } catch (Exception e) {
            log.error("Error in demo application", e);
        } finally {
            shutdown();
        }
    }

    private static void testInheritance() {
        log.info("Testing Inheritance Strategies...");

        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();

            // Create and save a CreditCardPayment
            CreditCardPayment ccPayment = new CreditCardPayment();
            ccPayment.setAmount(new BigDecimal("100.00"));
            ccPayment.setCardNumber("1234-5678-9012-3456");
            ccPayment.setCardHolderName("John Doe");
            ccPayment.setExpirationMonth(12);
            ccPayment.setExpirationYear(25);
            ccPayment.setCvv("123");

            session.save(ccPayment);

            // Create and save a BankTransferPayment
            BankTransferPayment btPayment = new BankTransferPayment();
            btPayment.setAmount(new BigDecimal("200.00"));
            btPayment.setBankName("Test Bank");
            btPayment.setAccountNumber("987654321");
            btPayment.setSwiftCode("TESTSWIFT");
            btPayment.setIban("TEST123IBAN");

            session.save(btPayment);

            session.getTransaction().commit();
            log.info("Successfully saved payments using Single Table strategy");
        }
    }

    private static void testCaching() {
        log.info("Testing Caching...");

        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();

            // Create and save a Product
            Product product = new Product();
            product.setSku("TEST-SKU-001");
            product.setName("Test Product");
            product.setDescription("A test product for caching demo");
            product.setPrice(new BigDecimal("99.99"));
            product.setStockQuantity(100);
            product.setCategory("Test Category");

            session.save(product);
            session.getTransaction().commit();

            // Test second-level cache
            session.clear(); // Clear first-level cache

            // First load - should hit the database
            product = session.get(Product.class, product.getId());
            log.info("First load of product: {}", product.getName());

            session.clear();

            // Second load - should hit the cache
            product = session.get(Product.class, product.getId());
            log.info("Second load of product (from cache): {}", product.getName());
        }
    }

    private static void testQuerying() {
        log.info("Testing Querying...");

        EntityManager entityManager = sessionFactory.createEntityManager();
        try {
            entityManager.getTransaction().begin();

            // Create test data
            Order order = new Order();
            order.setCustomerEmail("test@example.com");
            order.setOrderDate(LocalDateTime.now());
            order.setStatus(OrderStatus.NEW);
            order.setTotalAmount(new BigDecimal("299.98"));
            order.setShippingAddress("123 Test St, Test City");

            OrderItem item1 = new OrderItem();
            item1.setOrder(order);
            item1.setProductName("Test Product 1");
            item1.setQuantity(2);
            item1.setUnitPrice(new BigDecimal("99.99"));

            OrderItem item2 = new OrderItem();
            item2.setOrder(order);
            item2.setProductName("Test Product 2");
            item2.setQuantity(1);
            item2.setUnitPrice(new BigDecimal("100.00"));

            order.setItems(Arrays.asList(item1, item2));
            entityManager.persist(order);

            entityManager.getTransaction().commit();

            // Test querying
            OrderRepository orderRepo = new OrderRepository(entityManager);

            log.info("Testing JPQL query...");
            List<Order> customerOrders = orderRepo.findOrdersByCustomer("test@example.com", 0, 10);
            log.info("Found {} orders for customer", customerOrders.size());

            log.info("Testing Criteria query...");
            List<Order> ordersWithMinAmount = orderRepo.findOrdersByStatusAndMinAmount(
                    OrderStatus.NEW, new BigDecimal("200.00"));
            log.info("Found {} orders with minimum amount", ordersWithMinAmount.size());

            // Test native query with statistics
            log.info("Testing native query for statistics...");
            List<OrderStatistics> statistics = orderRepo.getOrderStatistics(LocalDateTime.now().minusDays(7));
            log.info("Found statistics for {} days", statistics.size());

        } finally {
            if (entityManager != null) {
                entityManager.close();
            }
        }
    }

    private static void testBestPractices() {
        log.info("Testing Best Practices...");

        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();

            // Create and save a Customer with best practices
            Customer customer = new Customer();
            customer.setEmail("john.doe@example.com");
            customer.setFirstName("John");
            customer.setLastName("Doe");
            customer.setPhoneNumber("+1-555-123-4567");
            customer.setDateOfBirth(LocalDateTime.of(1990, 1, 1, 0, 0));

            session.save(customer);
            session.getTransaction().commit();

            // Test optimistic locking
            session.beginTransaction();
            customer.setPhoneNumber("+1-555-987-6543");
            session.update(customer);
            session.getTransaction().commit();

            log.info("Successfully demonstrated best practices with Customer entity");
        }
    }

    private static void testBasics() {
        log.info("Testing Basics...");
        try (Session session = sessionFactory.openSession()) {
            BasicHibernateDemo basicDemo = new BasicHibernateDemo(sessionFactory);
            basicDemo.runDemo();
        }
    }

    private static void testLocking() {
        log.info("Testing Locking...");
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();

            // Create a bank account
            BankAccount account = new BankAccount();
            account.setAccountNumber("1234567890");
            account.setBalance(new BigDecimal("1000.00"));
            account.setOwnerName("John Doe");
            session.save(account);

            session.getTransaction().commit();

            // Demonstrate optimistic locking
            session.beginTransaction();
            account.setBalance(account.getBalance().add(new BigDecimal("500.00")));
            session.update(account);
            session.getTransaction().commit();

            // Demonstrate pessimistic locking
            session.beginTransaction();
            BankAccount lockedAccount = session.get(BankAccount.class, account.getId(), LockMode.PESSIMISTIC_WRITE);
            lockedAccount.setBalance(lockedAccount.getBalance().subtract(new BigDecimal("200.00")));
            session.getTransaction().commit();

            log.info("Successfully demonstrated locking with account balance: {}", 
                    lockedAccount.getBalance());
        }
    }

    private static void testRelationships() {
        log.info("Testing Relationships...");
        try (Session session = sessionFactory.openSession()) {
            RelationshipsDemo demo = new RelationshipsDemo(sessionFactory);
            demo.demonstrateOneToOne();
            demo.demonstrateOneToMany();
            demo.demonstrateOneToManyBidirectional();
            demo.demonstrateManyToMany();
        }
    }
}
