package com.hibernate.learning.querying;

import com.hibernate.learning.util.HibernateUtil;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Demonstrates different querying techniques in Hibernate:
 * - HQL/JPQL queries
 * - Criteria API
 * - Native SQL queries
 * - Named queries
 */
@Slf4j
public class QueryingDemo {
    public static void main(String[] args) {
        createSampleData();
        demonstrateHQLQueries();
        demonstrateCriteriaAPI();
        demonstrateNativeQueries();
        demonstrateNamedQueries();
    }

    private static void createSampleData() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();

            // Create sample orders
            for (int i = 1; i <= 5; i++) {
                Order order = new Order();
                order.setCustomerEmail("customer" + i + "@example.com");
                order.setOrderDate(LocalDateTime.now().minusDays(i));
                order.setStatus(OrderStatus.NEW);
                order.setTotalAmount(new BigDecimal(100 * i));
                order.setShippingAddress("Address " + i);

                OrderItem item1 = new OrderItem();
                item1.setOrder(order);
                item1.setProductName("Product " + i);
                item1.setProductSku("SKU-" + i);
                item1.setQuantity(i);
                item1.setUnitPrice(new BigDecimal("99.99"));
                item1.setTotalPrice(item1.getUnitPrice().multiply(new BigDecimal(item1.getQuantity())));

                OrderItem item2 = new OrderItem();
                item2.setOrder(order);
                item2.setProductName("Product " + (i + 5));
                item2.setProductSku("SKU-" + (i + 5));
                item2.setQuantity(i + 1);
                item2.setUnitPrice(new BigDecimal("149.99"));
                item2.setTotalPrice(item2.getUnitPrice().multiply(new BigDecimal(item2.getQuantity())));

                order.getItems().add(item1);
                order.getItems().add(item2);

                session.save(order);
            }

            session.getTransaction().commit();
        }
    }

    private static void demonstrateHQLQueries() {
        log.info("=== HQL Queries Demo ===");
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Basic HQL query
            List<Order> orders = session.createQuery(
                            "from Order o where o.totalAmount > :amount", Order.class)
                    .setParameter("amount", new BigDecimal("300"))
                    .list();
            log.info("Orders with amount > 300: {}", orders.size());

            // Join query
            List<OrderItem> items = session.createQuery(
                            "select i from Order o join o.items i where o.status = :status", OrderItem.class)
                    .setParameter("status", OrderStatus.NEW)
                    .list();
            log.info("Items in NEW orders: {}", items.size());

            // Aggregate query
            Double avgAmount = session.createQuery(
                            "select avg(o.totalAmount) from Order o", Double.class)
                    .getSingleResult();
            log.info("Average order amount: {}", avgAmount);
        }
    }

    private static void demonstrateCriteriaAPI() {
        log.info("=== Criteria API Demo ===");
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();

            // Simple criteria query
            CriteriaQuery<Order> cr = cb.createQuery(Order.class);
            Root<Order> root = cr.from(Order.class);
            cr.select(root)
                    .where(cb.greaterThan(root.get("totalAmount"), new BigDecimal("200")));

            List<Order> orders = session.createQuery(cr).getResultList();
            log.info("Orders with criteria API: {}", orders.size());

            // Criteria with multiple conditions
            CriteriaQuery<Order> cr2 = cb.createQuery(Order.class);
            Root<Order> root2 = cr2.from(Order.class);
            cr2.select(root2)
                    .where(cb.and(
                            cb.equal(root2.get("status"), OrderStatus.NEW),
                            cb.greaterThan(root2.get("totalAmount"), new BigDecimal("150"))
                    ));

            orders = session.createQuery(cr2).getResultList();
            log.info("Orders with multiple criteria: {}", orders.size());
        }
    }

    private static void demonstrateNativeQueries() {
        log.info("=== Native SQL Queries Demo ===");
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Simple native query
            List<Order> orders = session.createNativeQuery(
                            "SELECT * FROM orders WHERE total_amount > ?1", Order.class)
                    .setParameter(1, 250)
                    .list();
            log.info("Orders from native query: {}", orders.size());

            // Native query with joins
            @SuppressWarnings("unchecked")
            List<Object[]> orderStats = session.createNativeQuery(
                            "SELECT o.customer_email, COUNT(*), SUM(o.total_amount) " +
                                    "FROM orders o GROUP BY o.customer_email")
                    .getResultList();

            for (Object[] stat : orderStats) {
                log.info("Customer: {}, Orders: {}, Total: {}",
                        stat[0], stat[1], stat[2]);
            }
        }
    }

    private static void demonstrateNamedQueries() {
        log.info("=== Named Queries Demo ===");
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Using named query defined in Order entity
            List<Order> orders = session.createNamedQuery(
                            "Order.findByStatus", Order.class)
                    .setParameter("status", OrderStatus.NEW)
                    .list();
            log.info("Orders from named query: {}", orders.size());

            // Using named query with multiple parameters
            List<Order> recentOrders = session.createNamedQuery(
                            "Order.findRecentByStatus", Order.class)
                    .setParameter("status", OrderStatus.NEW)
                    .list();
            log.info("Recent orders from named query: {}", recentOrders.size());

            // Using date range query
            List<Order> dateRangeOrders = session.createNamedQuery(
                            "Order.findByCustomerAndDateRange", Order.class)
                    .setParameter("email", "customer1@example.com")
                    .setParameter("startDate", LocalDateTime.now().minusDays(7))
                    .setParameter("endDate", LocalDateTime.now())
                    .list();
            log.info("Orders in date range: {}", dateRangeOrders.size());
        }
    }
}
