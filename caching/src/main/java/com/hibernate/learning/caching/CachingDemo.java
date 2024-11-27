package com.hibernate.learning.caching;

import com.hibernate.learning.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.stat.Statistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;

/**
 * Demonstrates Hibernate's caching capabilities
 * <p>
 * Cache Architecture Overview:
 * <pre>
 * ┌─────────────────────────────────────────┐
 * │           Application Layer             │
 * │                                         │
 * │    ┌─────────────────────────────┐     │
 * │    │        Cache Demo           │     │
 * │    │                             │     │
 * │    │  ┌─────────┐   ┌─────────┐ │     │
 * │    │  │First    │   │Second   │ │     │
 * │    │  │Level    │   │Level    │ │     │
 * │    │  │Cache    │   │Cache    │ │     │
 * │    │  └─────────┘   └─────────┘ │     │
 * │    │                             │     │
 * │    │  ┌─────────┐   ┌─────────┐ │     │
 * │    │  │Query    │   │Natural  │ │     │
 * │    │  │Cache    │   │ID Cache │ │     │
 * │    │  └─────────┘   └─────────┘ │     │
 * │    └─────────────────────────────┘     │
 * │                                         │
 * └─────────────────────────────────────────┘
 * </pre>
 * <p>
 * Cache Hit Scenarios:
 * <pre>
 * 1. First Level Cache Hit
 * ┌──────────┐    ┌──────────┐    ┌──────────┐
 * │ Request  │ -> │ Session  │ -> │ Return   │
 * │          │    │  Cache   │    │ Entity   │
 * └──────────┘    └──────────┘    └──────────┘
 *
 * 2. Second Level Cache Hit
 * ┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐
 * │ Request  │ -> │ Session  │ -> │ SF Cache │ -> │ Return   │
 * │          │    │  Cache   │    │          │    │ Entity   │
 * └──────────┘    └──────────┘    └──────────┘    └──────────┘
 *
 * 3. Database Hit
 * ┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐
 * │ Request  │ -> │ Session  │ -> │ SF Cache │ -> │ Database │
 * │          │    │  Cache   │    │          │    │          │
 * └──────────┘    └──────────┘    └──────────┘    └──────────┘
 * </pre>
 * <p>
 * Cache Statistics:
 * <pre>
 * Performance Metrics
 * ├── Hit Ratio
 * │   ├── First Level
 * │   └── Second Level
 * │
 * ├── Miss Count
 * │   ├── Cache misses
 * │   └── DB queries
 * │
 * └── Memory Usage
 *     ├── Cache size
 *     └── Entry count
 * </pre>
 */
public class CachingDemo {
    private static final Logger logger = LoggerFactory.getLogger(CachingDemo.class);
    private final SessionFactory sessionFactory;
    private final Statistics statistics;

    public CachingDemo(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        this.statistics = sessionFactory.getStatistics();
        this.statistics.setStatisticsEnabled(true);
    }

    public static void main(String[] args) {
        CachingDemo cachingDemo = new CachingDemo(HibernateUtil.getSessionFactory());
        cachingDemo.firstLevelCacheDemo();
        cachingDemo.secondLevelCacheDemo();
        cachingDemo.naturalIdCacheDemo();
        cachingDemo.queryCacheDemo();
    }

    /**
     * Demonstrates first-level cache (Session cache)
     * <p>
     * Operation Flow:
     * <pre>
     * firstLevelCacheDemo()
     * ├── Create Product
     * │   └── Save to DB
     * │
     * ├── First Query
     * │   └── DB Hit
     * │
     * ├── Second Query
     * │   └── Cache Hit
     * │
     * └── Print Stats
     *     └── Hit/Miss Ratio
     * </pre>
     */
    public void firstLevelCacheDemo() {
        statistics.clear();

        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();

            // Create and save a product
            Product product = createProduct("FL-TEST-0001", "First Level Cache Test", new BigDecimal("99.99"));
            session.save(product);

            // First load - will hit the database
            Product firstLoad = session.get(Product.class, product.getId());
            logger.info("First load: {}", firstLoad.getName());

            // Second load - will hit the first-level cache
            Product secondLoad = session.get(Product.class, product.getId());
            logger.info("Second load (from cache): {}", secondLoad.getName());

            tx.commit();
        }

        logCacheStatistics("First Level Cache");
    }

    /**
     * Demonstrates second-level cache (SessionFactory cache)
     * <p>
     * Operation Flow:
     * <pre>
     * secondLevelCacheDemo()
     * ├── Create Product
     * │   └── Save to DB
     * │
     * ├── First Session
     * │   ├── Load product
     * │   └── Close session
     * │
     * ├── Second Session
     * │   ├── Load product
     * │   └── Cache hit
     * │
     * └── Print Stats
     *     └── Hit/Miss Ratio
     * </pre>
     */
    public void secondLevelCacheDemo() {
        statistics.clear();
        Long productId;

        // First session - save and load
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();

            Product product = createProduct("SL-TEST-0001", "Second Level Cache Test", new BigDecimal("149.99"));
            session.save(product);
            productId = product.getId();

            tx.commit();
        }

        // Clear first-level cache by closing session

        // Second session - load from second-level cache
        try (Session session = sessionFactory.openSession()) {
            Product product = session.get(Product.class, productId);
            logger.info("Loaded from second-level cache: {}", product.getName());
        }

        logCacheStatistics("Second Level Cache");
    }

    /**
     * Demonstrates natural ID cache
     * <p>
     * Operation Flow:
     * <pre>
     * naturalIdCacheDemo()
     * ├── Create Product
     * │   └── Save to DB
     * │
     * ├── First Load
     * │   ├── By natural ID
     * │   └── Cache mapping
     * │
     * ├── Second Load
     * │   ├── By natural ID
     * │   └── Cache hit
     * │
     * └── Print Stats
     *     └── Natural ID stats
     * </pre>
     */
    public void naturalIdCacheDemo() {
        statistics.clear();

        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();

            // Create and save product
            Product product = createProduct("NI-TEST-0001", "Natural ID Cache Test", new BigDecimal("199.99"));
            session.save(product);

            // First load by natural ID - will hit database
            Product firstLoad = session.bySimpleNaturalId(Product.class)
                    .load("NI-TEST-0001");
            logger.info("First natural ID load: {}", firstLoad.getName());

            // Second load - will use natural ID cache
            Product secondLoad = session.bySimpleNaturalId(Product.class)
                    .load("NI-TEST-0001");
            logger.info("Second natural ID load (from cache): {}", secondLoad.getName());

            tx.commit();
        }

        logCacheStatistics("Natural ID Cache");
    }

    /**
     * Demonstrates query cache
     * <p>
     * Operation Flow:
     * <pre>
     * queryCacheDemo()
     * ├── Create Products
     * │   └── Save to DB
     * │
     * ├── First Query
     * │   ├── Execute HQL
     * │   └── Cache results
     * │
     * ├── Second Query
     * │   ├── Same HQL
     * │   └── Cache hit
     * │
     * └── Print Stats
     *     └── Query cache stats
     * </pre>
     */
    public void queryCacheDemo() {
        statistics.clear();

        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();

            // Create test products
            createProduct("QC-TEST-0001", "Query Cache Test 1", new BigDecimal("299.99"));
            createProduct("QC-TEST-0002", "Query Cache Test 2", new BigDecimal("399.99"));

            // First query - will hit database
            List<Product> firstQuery = session.createQuery("from Product where sku like 'QC-TEST%'", Product.class)
                    .setCacheable(true)
                    .getResultList();
            logger.info("First query result count: {}", firstQuery.size());

            // Second query - will hit query cache
            List<Product> secondQuery = session.createQuery("from Product where sku like 'QC-TEST%'", Product.class)
                    .setCacheable(true)
                    .getResultList();
            logger.info("Second query result count: {}", secondQuery.size());

            tx.commit();
        }

        logCacheStatistics("Query Cache");
    }

    /**
     * Helper method to create a product
     * <p>
     * Creation Flow:
     * <pre>
     * createProduct()
     * ├── Set Properties
     * │   ├── SKU
     * │   ├── Name
     * │   └── Price
     * │
     * └── Validate
     *     └── Check constraints
     * </pre>
     */
    private Product createProduct(String sku, String name, BigDecimal price) {
        Product product = new Product();
        product.setSku(sku);
        product.setName(name);
        product.setPrice(price);
        product.validate();
        return product;
    }

    /**
     * Helper method to log cache statistics
     * <p>
     * Statistics Flow:
     * <pre>
     * logCacheStatistics()
     * ├── Cache Hits
     * │   ├── Second level
     * │   └── Query cache
     * │
     * ├── Cache Misses
     * │   ├── Second level
     * │   └── Query cache
     * │
     * └── Put Counts
     *     ├── Second level
     *     └── Query cache
     * </pre>
     */
    private void logCacheStatistics(String cacheType) {
        logger.info("=== {} Statistics ===", cacheType);
        logger.info("Second level cache hit count: {}", statistics.getSecondLevelCacheHitCount());
        logger.info("Second level cache miss count: {}", statistics.getSecondLevelCacheMissCount());
        logger.info("Second level cache put count: {}", statistics.getSecondLevelCachePutCount());
        logger.info("Query cache hit count: {}", statistics.getQueryCacheHitCount());
        logger.info("Query cache miss count: {}", statistics.getQueryCacheMissCount());
        logger.info("Query cache put count: {}", statistics.getQueryCachePutCount());
    }
}
