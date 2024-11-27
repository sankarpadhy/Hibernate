package com.hibernate.learning.querying;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class OrderRepositoryTest {

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("hibernatedb")
            .withUsername("postgres")
            .withPassword("postgres");

    private static SessionFactory sessionFactory;
    private static OrderRepository orderRepository;
    private static EntityManager entityManager;
    private static Order testOrder;

    @BeforeAll
    static void setup() {
        // Start the container
        postgres.start();

        // Configure Hibernate to use the TestContainers PostgreSQL instance
        StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                .applySetting("hibernate.connection.url", postgres.getJdbcUrl())
                .applySetting("hibernate.connection.username", postgres.getUsername())
                .applySetting("hibernate.connection.password", postgres.getPassword())
                .applySetting("hibernate.connection.driver_class", "org.postgresql.Driver")
                .applySetting("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect")
                .applySetting("hibernate.hbm2ddl.auto", "create-drop")
                .applySetting("hibernate.show_sql", "false")
                .applySetting("hibernate.format_sql", "false")
                .build();

        try {
            sessionFactory = new MetadataSources(registry)
                    .addAnnotatedClass(Order.class)
                    .addAnnotatedClass(OrderItem.class)
                    .buildMetadata()
                    .buildSessionFactory();

            entityManager = sessionFactory.createEntityManager();
            orderRepository = new OrderRepository(entityManager);
            
            // Create test data
            entityManager.getTransaction().begin();
            
            testOrder = new Order();
            testOrder.setCustomerEmail("test@example.com");
            testOrder.setOrderDate(LocalDateTime.now());
            testOrder.setTotalAmount(new BigDecimal("100.00"));
            testOrder.setStatus(OrderStatus.NEW);
            testOrder.setShippingAddress("123 Test St, Test City, 12345");
            
            OrderItem item1 = new OrderItem();
            item1.setOrder(testOrder);
            item1.setProductSku("SKU001");
            item1.setProductName("Test Product 1");
            item1.setQuantity(2);
            item1.setUnitPrice(new BigDecimal("25.00"));
            item1.setTotalPrice(new BigDecimal("50.00"));
            
            OrderItem item2 = new OrderItem();
            item2.setOrder(testOrder);
            item2.setProductSku("SKU002");
            item2.setProductName("Test Product 2");
            item2.setQuantity(1);
            item2.setUnitPrice(new BigDecimal("50.00"));
            item2.setTotalPrice(new BigDecimal("50.00"));
            
            testOrder.getItems().add(item1);
            testOrder.getItems().add(item2);
            
            entityManager.persist(testOrder);
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            StandardServiceRegistryBuilder.destroy(registry);
            throw e;
        }
    }

    @AfterAll
    static void tearDown() {
        if (entityManager != null) {
            entityManager.close();
        }
        if (sessionFactory != null) {
            sessionFactory.close();
        }
        // Stop the container
        postgres.stop();
    }

    @Test
    @org.junit.jupiter.api.Order(1)
    void testFindOrdersByCustomer() {
        List<Order> orders = orderRepository.findOrdersByCustomer("test@example.com", 0, 10);
        assertFalse(orders.isEmpty());
        assertEquals("test@example.com", orders.get(0).getCustomerEmail());
    }

    @Test
    @org.junit.jupiter.api.Order(2)
    void testFindOrdersByStatusAndMinAmount() {
        List<Order> orders = orderRepository.findOrdersByStatusAndMinAmount(
            OrderStatus.NEW, new BigDecimal("50.00"));
        assertFalse(orders.isEmpty());
        assertTrue(orders.get(0).getTotalAmount().compareTo(new BigDecimal("50.00")) >= 0);
    }

    @Test
    @org.junit.jupiter.api.Order(3)
    void testGetOrderStatistics() {
        List<OrderStatistics> statistics = orderRepository.getOrderStatistics(
            LocalDateTime.now().minusDays(1));
        assertFalse(statistics.isEmpty());
        OrderStatistics stats = statistics.get(0);
        assertEquals(LocalDate.now(), stats.getOrderDate());
        assertEquals(1L, stats.getTotalOrders());
        assertEquals(new BigDecimal("100.00"), stats.getTotalRevenue());
    }

    @Test
    @org.junit.jupiter.api.Order(4)
    void testFindRecentOrdersByStatus() {
        List<Order> orders = orderRepository.findRecentOrdersByStatus(OrderStatus.NEW, 10);
        assertFalse(orders.isEmpty());
        assertEquals(OrderStatus.NEW, orders.get(0).getStatus());
    }

    @Test
    @org.junit.jupiter.api.Order(5)
    void testFindOrdersByDateRange() {
        LocalDate today = LocalDate.now();
        List<Order> orders = orderRepository.findOrdersByDateRange(
            today.minusDays(1), today.plusDays(1));
        assertFalse(orders.isEmpty());
        assertTrue(orders.get(0).getOrderDate().toLocalDate().equals(today));
    }
}
