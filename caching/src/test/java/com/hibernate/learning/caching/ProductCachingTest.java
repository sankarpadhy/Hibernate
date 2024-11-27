package com.hibernate.learning.caching;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Comprehensive demonstration of Hibernate's caching mechanisms.
 * <p>
 * Hibernate Caching Architecture:
 * <pre>
 *  Application Layer
 *        ↓ ↑
 *  First-Level Cache (Session Cache)
 *        ↓ ↑
 *  Second-Level Cache (SessionFactory Cache)
 *        ↓ ↑
 *     Database
 * </pre>
 * <p>
 * Cache Types Demonstrated:
 * 1. First-Level Cache (Session Cache)
 * - Enabled by default
 * - Scope: Single Session
 * - Cannot be shared between sessions
 * <p>
 * 2. Second-Level Cache
 * - Must be explicitly enabled
 * - Scope: SessionFactory (shared across sessions)
 * - Configurable cache providers (EHCache, etc.)
 * <p>
 * 3. Query Cache
 * - Caches results of HQL/Criteria queries
 * - Must be explicitly enabled per query
 * - Requires second-level cache
 * <p>
 * Cache Region Hierarchy:
 * <pre>
 * SessionFactory Cache
 * ├── Entity Regions
 * │   └── Product Region
 * ├── Collection Regions
 * ├── Query Regions
 * └── Natural ID Regions
 * </pre>
 * <p>
 * Cache Hit Flow:
 * <pre>
 * Request → Check L1 Cache → [Hit] → Return Entity
 *                ↓ [Miss]
 *         Check L2 Cache → [Hit] → Store in L1 → Return Entity
 *                ↓ [Miss]
 *            Database → Store in L2 → Store in L1 → Return Entity
 * </pre>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ProductCachingTest {

    private SessionFactory sessionFactory;
    private Statistics statistics;

    /**
     * Test Setup Configuration:
     * <pre>
     * 1. Create SessionFactory
     *    └── Load hibernate.cfg.xml
     *        ├── Database settings
     *        ├── Cache settings
     *        └── Entity mappings
     *
     * 2. Enable Statistics
     *    └── Track cache performance
     *        ├── Hit counts
     *        ├── Miss counts
     *        └── Load counts
     * </pre>
     */
    @BeforeAll
    public void setup() {
        sessionFactory = new Configuration()
                .configure()
                .buildSessionFactory();

        statistics = sessionFactory.getStatistics();
        statistics.setStatisticsEnabled(true);
    }

    @AfterAll
    public void tearDown() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }

    @BeforeEach
    public void resetStats() {
        statistics.clear();
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
     * Demonstrates Second-Level Cache behavior.
     * <p>
     * Test Flow:
     * <pre>
     * 1. Save Entity
     *    └── Product saved to database
     *
     * 2. First Load (Cache Miss)
     *    ├── Check L1 Cache (Miss - new session)
     *    ├── Check L2 Cache (Miss - first access)
     *    └── Load from Database
     *
     * 3. Second Load (Cache Hit)
     *    ├── Check L1 Cache (Miss - new session)
     *    ├── Check L2 Cache (Hit!)
     *    └── Return from L2 Cache
     * </pre>
     */
    @Test
    public void testSecondLevelCache() {
        // Create and save a product
        Product product = new Product();
        product.setName("Test Product");
        product.setSku("TEST-001");
        product.setPrice(new BigDecimal("99.99"));
        product.setStockQuantity(100);
        product.setActive(true);
        product.setCategory("Test Category");
        product.setDescription("Test Description");

        Session session1 = sessionFactory.openSession();
        Transaction tx1 = session1.beginTransaction();
        session1.save(product);
        tx1.commit();
        session1.close();

        // Clear statistics
        statistics.clear();

        // First load - should be a cache miss
        Session session2 = sessionFactory.openSession();
        Product loadedProduct1 = session2.get(Product.class, product.getId());
        session2.close();

        assertEquals(1, statistics.getSecondLevelCacheMissCount());
        assertEquals(0, statistics.getSecondLevelCacheHitCount());

        // Second load - should be a cache hit
        Session session3 = sessionFactory.openSession();
        Product loadedProduct2 = session3.get(Product.class, product.getId());
        session3.close();

        assertEquals(1, statistics.getSecondLevelCacheMissCount());
        assertEquals(1, statistics.getSecondLevelCacheHitCount());
    }

    /**
     * Tests Natural ID Cache functionality.
     * <p>
     * Natural ID:
     * - Business identifier (e.g., SKU)
     * - Alternative to surrogate primary key
     * - Optimized for frequent lookups
     * <p>
     * Cache Flow:
     * <pre>
     * 1. First Lookup
     *    └── Natural ID → PK mapping cached
     *
     * 2. Subsequent Lookups
     *    ├── Natural ID → PK (from cache)
     *    └── Entity load using cached PK
     * </pre>
     */
    @Test
    public void testNaturalIdCache() {
        // Create and save a product
        Product product = new Product();
        product.setName("Test Product");
        product.setSku("TEST-002");
        product.setPrice(new BigDecimal("99.99"));
        product.setStockQuantity(100);
        product.setActive(true);

        Session session1 = sessionFactory.openSession();
        Transaction tx1 = session1.beginTransaction();
        session1.save(product);
        tx1.commit();
        session1.close();

        // Clear statistics
        statistics.clear();

        // First load by natural id - should be a cache miss
        Session session2 = sessionFactory.openSession();
        Product loadedProduct1 = session2
                .byNaturalId(Product.class)
                .using("sku", "TEST-002")
                .load();
        session2.close();

        assertTrue(statistics.getNaturalIdCacheMissCount() > 0);
        assertEquals(0, statistics.getNaturalIdCacheHitCount());

        // Second load by natural id - should be a cache hit
        Session session3 = sessionFactory.openSession();
        Product loadedProduct2 = session3
                .byNaturalId(Product.class)
                .using("sku", "TEST-002")
                .load();
        session3.close();

        assertTrue(statistics.getNaturalIdCacheHitCount() > 0);
    }

    /**
     * Demonstrates Query Cache behavior.
     * <p>
     * Query Cache Architecture:
     * <pre>
     * Query Cache
     * ├── Query Results Cache
     * │   └── Store query results (entity IDs)
     * └── Update Timestamps Cache
     *     └── Track entity modifications
     *
     * Cache Invalidation:
     * 1. Entity updates trigger timestamp update
     * 2. Cached query results invalidated
     * 3. Query re-executed on next access
     * </pre>
     */
    @Test
    public void testQueryCache() {
        // Create and save multiple products
        Session session1 = sessionFactory.openSession();
        Transaction tx1 = session1.beginTransaction();

        for (int i = 1; i <= 3; i++) {
            Product product = new Product();
            product.setName("Product " + i);
            product.setSku("SKU-00" + i);
            product.setPrice(new BigDecimal("99.99"));
            product.setStockQuantity(100);
            product.setActive(true);
            product.setCategory("Test Category");
            session1.save(product);
        }

        tx1.commit();
        session1.close();

        // Clear statistics
        statistics.clear();

        // First query execution - should be a cache miss
        Session session2 = sessionFactory.openSession();
        session2.createQuery("from Product p where p.category = :category", Product.class)
                .setParameter("category", "Test Category")
                .setCacheable(true)
                .getResultList();
        session2.close();

        assertEquals(1, statistics.getQueryCacheMissCount());
        assertEquals(0, statistics.getQueryCacheHitCount());

        // Second query execution - should be a cache hit
        Session session3 = sessionFactory.openSession();
        session3.createQuery("from Product p where p.category = :category", Product.class)
                .setParameter("category", "Test Category")
                .setCacheable(true)
                .getResultList();
        session3.close();

        assertEquals(1, statistics.getQueryCacheMissCount());
        assertEquals(1, statistics.getQueryCacheHitCount());
    }

    /**
     * Demonstrates Cache Eviction scenarios.
     * <p>
     * Eviction Triggers:
     * <pre>
     * 1. Explicit Updates
     *    └── session.update()
     *
     * 2. Entity Modifications
     *    └── Dirty checking
     *
     * 3. Bulk Operations
     *    └── HQL/Criteria updates
     *
     * 4. Cache Region Clear
     *    └── Programmatic eviction
     * </pre>
     * <p>
     * Note: Cache entries are also subject to:
     * - Memory constraints
     * - Time-based expiration
     * - Custom eviction policies
     */
    @Test
    public void testCacheEviction() {
        // Create and save a product
        Product product = new Product();
        product.setName("Test Product");
        product.setSku("TEST-003");
        product.setPrice(new BigDecimal("99.99"));
        product.setStockQuantity(100);
        product.setActive(true);

        Session session1 = sessionFactory.openSession();
        Transaction tx1 = session1.beginTransaction();
        session1.save(product);
        tx1.commit();
        session1.close();

        // Load product to cache it
        Session session2 = sessionFactory.openSession();
        session2.get(Product.class, product.getId());
        session2.close();

        // Clear statistics
        statistics.clear();

        // Update product to evict it from cache
        Session session3 = sessionFactory.openSession();
        Transaction tx3 = session3.beginTransaction();
        Product loadedProduct = session3.get(Product.class, product.getId());
        loadedProduct.setPrice(new BigDecimal("149.99"));
        session3.update(loadedProduct);
        tx3.commit();
        session3.close();

        // Load product again - should be a cache miss due to eviction
        Session session4 = sessionFactory.openSession();
        session4.get(Product.class, product.getId());
        session4.close();

        assertEquals(1, statistics.getSecondLevelCacheMissCount());
        assertEquals(0, statistics.getSecondLevelCacheHitCount());
    }
}
