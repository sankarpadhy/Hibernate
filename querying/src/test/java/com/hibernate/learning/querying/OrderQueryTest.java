package com.hibernate.learning.querying;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class OrderQueryTest {

    private SessionFactory sessionFactory;
    private Order testOrder1;
    private Order testOrder2;

    @BeforeAll
    public void setup() {
        sessionFactory = new Configuration()
                .configure()
                .buildSessionFactory();
    }

    @BeforeEach
    public void setupTestData() {
        Session session = sessionFactory.openSession();
        Transaction tx = session.beginTransaction();

        // Create first order
        testOrder1 = new Order();
        testOrder1.setCustomerEmail("john@example.com");
        testOrder1.setOrderDate(LocalDateTime.now().minusDays(1));
        testOrder1.setShippingAddress("123 Main St");
        testOrder1.setStatus(OrderStatus.PENDING);
        testOrder1.setPaid(false);
        testOrder1.setTotalAmount(new BigDecimal("100.00"));

        OrderItem item1 = new OrderItem();
        item1.setOrder(testOrder1);
        item1.setProductSku("SKU001");
        item1.setProductName("Product 1");
        item1.setQuantity(2);
        item1.setUnitPrice(new BigDecimal("50.00"));
        item1.setTotalPrice(new BigDecimal("100.00"));
        testOrder1.getItems().add(item1);

        // Create second order
        testOrder2 = new Order();
        testOrder2.setCustomerEmail("jane@example.com");
        testOrder2.setOrderDate(LocalDateTime.now());
        testOrder2.setShippingAddress("456 Oak St");
        testOrder2.setStatus(OrderStatus.SHIPPED);
        testOrder2.setPaid(true);
        testOrder2.setTotalAmount(new BigDecimal("150.00"));

        OrderItem item2 = new OrderItem();
        item2.setOrder(testOrder2);
        item2.setProductSku("SKU002");
        item2.setProductName("Product 2");
        item2.setQuantity(1);
        item2.setUnitPrice(new BigDecimal("150.00"));
        item2.setTotalPrice(new BigDecimal("150.00"));
        testOrder2.getItems().add(item2);

        session.save(testOrder1);
        session.save(testOrder2);
        tx.commit();
        session.close();
    }

    @AfterEach
    public void cleanupTestData() {
        Session session = sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        session.createQuery("delete from OrderItem").executeUpdate();
        session.createQuery("delete from Order").executeUpdate();
        tx.commit();
        session.close();
    }

    @AfterAll
    public void tearDown() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }

    @Test
    public void testNamedQueryFindByStatus() {
        Session session = sessionFactory.openSession();
        Query<Order> query = session.createNamedQuery("Order.findByStatus", Order.class);
        query.setParameter("status", OrderStatus.PENDING);

        List<Order> pendingOrders = query.getResultList();
        assertEquals(1, pendingOrders.size());
        assertEquals(testOrder1.getId(), pendingOrders.get(0).getId());
        assertEquals("john@example.com", pendingOrders.get(0).getCustomerEmail());

        session.close();
    }

    @Test
    public void testNamedQueryFindRecentByStatus() {
        Session session = sessionFactory.openSession();
        Query<Order> query = session.createNamedQuery("Order.findRecentByStatus", Order.class);
        query.setParameter("status", OrderStatus.SHIPPED);

        List<Order> shippedOrders = query.getResultList();
        assertEquals(1, shippedOrders.size());
        assertEquals(testOrder2.getId(), shippedOrders.get(0).getId());
        assertEquals("jane@example.com", shippedOrders.get(0).getCustomerEmail());

        session.close();
    }

    @Test
    public void testHQLQuery() {
        Session session = sessionFactory.openSession();
        String hql = "FROM Order o WHERE o.paid = :paid AND o.totalAmount > :minAmount";
        Query<Order> query = session.createQuery(hql, Order.class);
        query.setParameter("paid", true);
        query.setParameter("minAmount", new BigDecimal("100.00"));

        List<Order> orders = query.getResultList();
        assertEquals(1, orders.size());
        assertEquals(testOrder2.getId(), orders.get(0).getId());
        assertTrue(orders.get(0).isPaid());
        assertTrue(orders.get(0).getTotalAmount().compareTo(new BigDecimal("100.00")) > 0);

        session.close();
    }

    @Test
    public void testCriteriaQuery() {
        Session session = sessionFactory.openSession();

        // Using JPA Criteria API
        var cb = session.getCriteriaBuilder();
        var cq = cb.createQuery(Order.class);
        var root = cq.from(Order.class);

        cq.select(root)
                .where(cb.equal(root.get("status"), OrderStatus.SHIPPED));

        List<Order> orders = session.createQuery(cq).getResultList();
        assertEquals(1, orders.size());
        assertEquals(OrderStatus.SHIPPED, orders.get(0).getStatus());

        session.close();
    }

    @Test
    public void testJoinFetch() {
        Session session = sessionFactory.openSession();
        String hql = "SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.items WHERE o.id = :orderId";
        Query<Order> query = session.createQuery(hql, Order.class);
        query.setParameter("orderId", testOrder1.getId());

        Order order = query.uniqueResult();
        assertNotNull(order);
        assertEquals(1, order.getItems().size());
        assertEquals("SKU001", order.getItems().get(0).getProductSku());

        session.close();
    }

    @Test
    public void testAggregateQuery() {
        Session session = sessionFactory.openSession();
        String hql = "SELECT new com.hibernate.learning.querying.OrderStatistics(" +
                "COUNT(o), SUM(o.totalAmount), AVG(o.totalAmount)) " +
                "FROM Order o WHERE o.paid = :paid";

        Query<OrderStatistics> query = session.createQuery(hql, OrderStatistics.class);
        query.setParameter("paid", true);

        OrderStatistics stats = query.uniqueResult();
        assertNotNull(stats);
        assertEquals(1L, stats.getTotalOrders());
        assertEquals(new BigDecimal("150.00"), stats.getTotalRevenue());
        assertEquals(new BigDecimal("150.00"), stats.getAverageOrderValue());

        session.close();
    }
}
