package com.hibernate.learning.demo;

import com.hibernate.learning.bestpractices.Customer;
import com.hibernate.learning.caching.Product;
import com.hibernate.learning.demo.util.TestDataGenerator;
import com.hibernate.learning.querying.Order;
import com.hibernate.learning.querying.OrderStatus;
import com.hibernate.learning.relationships.Course;
import com.hibernate.learning.relationships.Department;
import com.hibernate.learning.relationships.Student;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.*;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HibernatePerformanceTest {

    private SessionFactory sessionFactory;
    private TestDataGenerator testDataGenerator;
    private Statistics statistics;

    @BeforeAll
    void setUp() {
        StandardServiceRegistry standardRegistry = new StandardServiceRegistryBuilder()
                .configure("hibernate-test.cfg.xml")
                .build();

        Metadata metadata = new MetadataSources(standardRegistry)
                .getMetadataBuilder()
                .build();

        sessionFactory = metadata.getSessionFactoryBuilder()
                .build();

        testDataGenerator = new TestDataGenerator(sessionFactory);
        statistics = sessionFactory.getStatistics();
        statistics.setStatisticsEnabled(true);

        // Generate test data
        testDataGenerator.generateBulkData(1000, 100, 5000);
        testDataGenerator.generateRelationshipData(10, 50, 20, 500);
    }

    @AfterAll
    void tearDown() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }

    @BeforeEach
    void clearStats() {
        statistics.clear();
    }

    @Test
    void testBatchInsertPerformance() {
        log.info("Testing batch insert performance...");
        Instant start = Instant.now();

        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();

            IntStream.range(0, 1000).forEach(i -> {
                Customer customer = new Customer();
                customer.setEmail("perf.test" + i + "@example.com");
                customer.setFirstName("PerfTest");
                customer.setLastName("Customer" + i);
                customer.setCreatedAt(LocalDateTime.now());
                customer.setCreatedBy("performance_test");
                
                if (i > 0 && i % 50 == 0) {
                    session.flush();
                    session.clear();
                }
                
                session.save(customer);
            });

            session.getTransaction().commit();
        }

        Duration duration = Duration.between(start, Instant.now());
        log.info("Batch insert completed in {} ms", duration.toMillis());
        assertTrue(duration.toMillis() < 10000, "Batch insert should complete within 10 seconds");
    }

    @Test
    void testCachePerformance() {
        log.info("Testing cache performance...");
        
        try (Session session = sessionFactory.openSession()) {
            // First query - should hit the database
            Instant start = Instant.now();
            List<Product> products = session.createQuery("from Product p where p.category = :category", Product.class)
                    .setParameter("category", "Category 1")
                    .setCacheable(true)
                    .getResultList();
            Duration firstQueryDuration = Duration.between(start, Instant.now());
            
            session.clear(); // Clear first-level cache

            // Second query - should hit the second-level cache
            start = Instant.now();
            List<Product> cachedProducts = session.createQuery("from Product p where p.category = :category", Product.class)
                    .setParameter("category", "Category 1")
                    .setCacheable(true)
                    .getResultList();
            Duration secondQueryDuration = Duration.between(start, Instant.now());

            log.info("First query (DB hit): {} ms", firstQueryDuration.toMillis());
            log.info("Second query (cache hit): {} ms", secondQueryDuration.toMillis());
            log.info("Cache hit ratio: {}", statistics.getSecondLevelCacheHitRatio());

            assertTrue(secondQueryDuration.toMillis() < firstQueryDuration.toMillis(),
                    "Cached query should be faster than database query");
            assertTrue(statistics.getSecondLevelCacheHitCount() > 0,
                    "Second-level cache should be used");
        }
    }

    @Test
    void testConcurrentQueryPerformance() {
        log.info("Testing concurrent query performance...");
        int numThreads = 10;
        int queriesPerThread = 100;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        Instant start = Instant.now();

        List<CompletableFuture<Void>> futures = IntStream.range(0, numThreads)
                .mapToObj(threadNum -> CompletableFuture.runAsync(() -> {
                    for (int i = 0; i < queriesPerThread; i++) {
                        try (Session session = sessionFactory.openSession()) {
                            // Randomly choose between different query types
                            switch (i % 3) {
                                case 0:
                                    // Query orders
                                    session.createQuery("from Order o where o.status = :status", Order.class)
                                            .setParameter("status", OrderStatus.NEW)
                                            .setMaxResults(10)
                                            .getResultList();
                                    break;
                                case 1:
                                    // Query products
                                    session.createQuery("from Product p where p.price > :price", Product.class)
                                            .setParameter("price", new BigDecimal("50.00"))
                                            .setMaxResults(10)
                                            .getResultList();
                                    break;
                                case 2:
                                    // Query with joins
                                    session.createQuery(
                                            "select s from Student s join s.courses c where c.title like :title",
                                            Student.class)
                                            .setParameter("title", "Course%")
                                            .setMaxResults(10)
                                            .getResultList();
                                    break;
                            }
                        }
                    }
                }, executor))
                .collect(java.util.stream.Collectors.toList());

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        executor.shutdown();

        Duration duration = Duration.between(start, Instant.now());
        double queriesPerSecond = (double) (numThreads * queriesPerThread) / duration.toSeconds();

        log.info("Concurrent query test completed in {} seconds", duration.toSeconds());
        log.info("Queries per second: {}", String.format("%.2f", queriesPerSecond));
        log.info("Average query time: {} ms", String.format("%.2f", duration.toMillis() / (double) (numThreads * queriesPerThread)));

        assertTrue(queriesPerSecond > 50, "Should handle at least 50 queries per second");
    }

    @Test
    void testComplexQueryPerformance() {
        log.info("Testing complex query performance...");

        try (Session session = sessionFactory.openSession()) {
            Instant start = Instant.now();

            // Complex query with multiple joins and aggregations
            List<Object[]> results = session.createQuery(
                    "select d.name, count(e), avg(size(e.employeeDetail.courses)) " +
                    "from Department d " +
                    "left join d.employees e " +
                    "group by d.name " +
                    "having count(e) > 0 " +
                    "order by count(e) desc",
                    Object[].class)
                    .setMaxResults(10)
                    .getResultList();

            Duration queryDuration = Duration.between(start, Instant.now());
            log.info("Complex query completed in {} ms", queryDuration.toMillis());
            
            // Test Criteria API performance
            start = Instant.now();
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Department> criteria = cb.createQuery(Department.class);
            Root<Department> root = criteria.from(Department.class);
            
            criteria.select(root)
                    .where(cb.gt(cb.size(root.get("employees")), 10))
                    .orderBy(cb.desc(cb.size(root.get("employees"))));

            List<Department> criteriaResults = session.createQuery(criteria)
                    .setMaxResults(10)
                    .getResultList();

            Duration criteriaDuration = Duration.between(start, Instant.now());
            log.info("Criteria query completed in {} ms", criteriaDuration.toMillis());

            assertTrue(queryDuration.toMillis() < 5000,
                    "Complex query should complete within 5 seconds");
            assertTrue(criteriaDuration.toMillis() < 5000,
                    "Criteria query should complete within 5 seconds");
        }
    }

    @Test
    void testLazyLoadingPerformance() {
        log.info("Testing lazy loading performance...");

        try (Session session = sessionFactory.openSession()) {
            Instant start = Instant.now();

            // Get all departments without loading employees
            List<Department> departments = session.createQuery("from Department", Department.class)
                    .getResultList();

            Duration initialLoadDuration = Duration.between(start, Instant.now());
            log.info("Initial department load: {} ms", initialLoadDuration.toMillis());

            start = Instant.now();
            // Access lazy-loaded collections
            departments.forEach(dept -> {
                dept.getEmployees().size(); // Trigger lazy loading
            });

            Duration lazyLoadDuration = Duration.between(start, Instant.now());
            log.info("Lazy loading of employees: {} ms", lazyLoadDuration.toMillis());
            log.info("Lazy load count: {}", statistics.getEntityLoadCount());

            assertTrue(initialLoadDuration.toMillis() < lazyLoadDuration.toMillis(),
                    "Initial load should be faster than lazy loading");
        }
    }
}
