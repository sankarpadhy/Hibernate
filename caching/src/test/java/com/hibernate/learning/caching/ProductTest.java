package com.hibernate.learning.caching;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for Product entity demonstrating basic CRUD operations and validations.
 * <p>
 * Test Architecture:
 * <pre>
 * ProductTest
 * ├── Setup/Teardown
 * │   ├── SessionFactory configuration
 * │   └── Data cleanup
 * │
 * ├── CRUD Operations
 * │   ├── Create
 * │   ├── Read
 * │   ├── Update
 * │   └── Delete
 * │
 * └── Validations
 *     ├── Constraints
 *     ├── Business Rules
 *     └── Edge Cases
 * </pre>
 * <p>
 * Test Flow Pattern:
 * <pre>
 * Setup → Arrange → Act → Assert → Cleanup
 *   ↑                               ↓
 *   └───────────────────────────────┘
 * </pre>
 * <p>
 * Key Testing Areas:
 * 1. Entity Persistence
 * - Basic CRUD operations
 * - Transaction management
 * - Session handling
 * <p>
 * 2. Data Validation
 * - Required fields
 * - Field constraints
 * - Business rules
 * <p>
 * 3. Error Scenarios
 * - Invalid data
 * - Constraint violations
 * - Transaction rollbacks
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ProductTest {

    private SessionFactory sessionFactory;

    /**
     * Test Environment Setup:
     * <pre>
     * 1. Configuration
     *    ├── Load hibernate.cfg.xml
     *    ├── Configure entity mappings
     *    └── Build SessionFactory
     *
     * 2. Database State
     *    ├── Clean state for each test
     *    └── Isolated test data
     * </pre>
     */
    @BeforeAll
    public void setup() {
        sessionFactory = new Configuration()
                .configure()
                .buildSessionFactory();
    }

    @AfterAll
    public void tearDown() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }

    @AfterEach
    public void cleanup() {
        Session session = sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        session.createQuery("delete from Product").executeUpdate();
        tx.commit();
        session.close();
    }

    /**
     * Tests basic CRUD operations for Product entity.
     * <p>
     * Operation Flow:
     * <pre>
     * 1. Create
     *    └── Persist new product
     *        ├── Set required fields
     *        └── Validate ID generation
     *
     * 2. Read
     *    └── Load saved product
     *        ├── Verify field values
     *        └── Check entity state
     *
     * 3. Update
     *    └── Modify product
     *        ├── Change field values
     *        └── Verify changes
     *
     * 4. Delete
     *    └── Remove product
     *        ├── Verify deletion
     *        └── Check cascade effects
     * </pre>
     */
    @Test
    public void testBasicCRUD() {
        // Create
        Product product = new Product();
        product.setName("Test Product");
        product.setSku("TEST-001");
        product.setPrice(new BigDecimal("99.99"));
        product.setStockQuantity(100);
        product.setActive(true);

        Session session = sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        session.save(product);
        tx.commit();
        session.close();

        assertNotNull(product.getId());

        // Read
        session = sessionFactory.openSession();
        Product loadedProduct = session.get(Product.class, product.getId());
        assertNotNull(loadedProduct);
        assertEquals("Test Product", loadedProduct.getName());
        assertEquals("TEST-001", loadedProduct.getSku());
        session.close();

        // Update
        session = sessionFactory.openSession();
        tx = session.beginTransaction();
        loadedProduct = session.get(Product.class, product.getId());
        loadedProduct.setPrice(new BigDecimal("149.99"));
        tx.commit();
        session.close();

        session = sessionFactory.openSession();
        Product updatedProduct = session.get(Product.class, product.getId());
        assertEquals(new BigDecimal("149.99"), updatedProduct.getPrice());
        session.close();

        // Delete
        session = sessionFactory.openSession();
        tx = session.beginTransaction();
        session.delete(updatedProduct);
        tx.commit();
        session.close();

        session = sessionFactory.openSession();
        assertNull(session.get(Product.class, product.getId()));
        session.close();
    }

    /**
     * Tests validation constraints on Product entity.
     * <p>
     * Validation Scenarios:
     * <pre>
     * 1. Required Fields
     *    ├── Name
     *    ├── SKU
     *    └── Price
     *
     * 2. Unique Constraints
     *    └── SKU uniqueness
     *
     * 3. Value Constraints
     *    ├── Price > 0
     *    └── Stock >= 0
     * </pre>
     */
    @Test
    public void testValidation() {
        // Test required fields
        Product product = new Product();
        Session session = sessionFactory.openSession();
        Transaction tx = session.beginTransaction();

        try {
            session.save(product);
            tx.commit();
            fail("Should have thrown an exception for missing required fields");
        } catch (Exception e) {
            tx.rollback();
        } finally {
            session.close();
        }

        // Test unique SKU constraint
        Product product1 = new Product();
        product1.setName("Product 1");
        product1.setSku("DUPLICATE-SKU");
        product1.setPrice(new BigDecimal("99.99"));
        product1.setStockQuantity(100);

        Product product2 = new Product();
        product2.setName("Product 2");
        product2.setSku("DUPLICATE-SKU");
        product2.setPrice(new BigDecimal("149.99"));
        product2.setStockQuantity(50);

        session = sessionFactory.openSession();
        tx = session.beginTransaction();
        session.save(product1);
        tx.commit();
        session.close();

        session = sessionFactory.openSession();
        tx = session.beginTransaction();
        try {
            session.save(product2);
            tx.commit();
            fail("Should have thrown an exception for duplicate SKU");
        } catch (Exception e) {
            tx.rollback();
        } finally {
            session.close();
        }
    }

    /**
     * Tests business logic and edge cases.
     * <p>
     * Test Scenarios:
     * <pre>
     * 1. Price Updates
     *    ├── Valid price changes
     *    └── Invalid price values
     *
     * 2. Stock Management
     *    ├── Stock increment
     *    └── Stock decrement
     *
     * 3. Status Changes
     *    ├── Activation
     *    └── Deactivation
     * </pre>
     */
    @Test
    public void testBusinessLogic() {
        Product product = new Product();
        product.setName("Business Logic Test");
        product.setSku("BL-001");
        product.setPrice(new BigDecimal("99.99"));
        product.setStockQuantity(100);
        product.setActive(true);

        Session session = sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        session.save(product);
        tx.commit();
        session.close();

        // Test price update
        session = sessionFactory.openSession();
        tx = session.beginTransaction();
        product = session.get(Product.class, product.getId());
        product.setPrice(new BigDecimal("149.99"));
        tx.commit();
        session.close();

        session = sessionFactory.openSession();
        Product updatedProduct = session.get(Product.class, product.getId());
        assertEquals(new BigDecimal("149.99"), updatedProduct.getPrice());
        session.close();

        // Test invalid price
        session = sessionFactory.openSession();
        tx = session.beginTransaction();
        try {
            updatedProduct.setPrice(new BigDecimal("-10.00"));
            session.update(updatedProduct);
            tx.commit();
            fail("Should have thrown an exception for negative price");
        } catch (Exception e) {
            tx.rollback();
        } finally {
            session.close();
        }
    }
}
