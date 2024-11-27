package com.hibernate.learning.querying;

import org.hibernate.Session;
import org.hibernate.query.Query;
import org.hibernate.transform.ResultTransformer;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * Repository demonstrating different querying techniques in Hibernate.
 * <p>
 * Hibernate provides multiple ways to query data:
 * 1. JPQL - Java Persistence Query Language
 * 2. Criteria API - Type-safe programmatic queries
 * 3. Native SQL - Direct database queries
 * 4. Named Queries - Predefined, reusable queries
 * <p>
 * Note about query types:
 * - JPQL: Preferred for simple to medium complexity queries
 * - Criteria API: Best for dynamic queries
 * - Native SQL: Use for complex database-specific queries
 * - Named Queries: Ideal for frequently used, static queries
 * <p>
 * Performance Considerations:
 * - Use parameterized queries to enable caching
 * - Implement pagination for large result sets
 * - Use fetch joins to avoid N+1 problems
 * - Consider query plan caching
 * <p>
 * Security Note:
 * - Always use parameterized queries
 * - Never concatenate user input
 * - Be cautious with Native SQL
 * - Validate and sanitize inputs
 */
public class OrderRepository {

    private final EntityManager entityManager;

    public OrderRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * JPQL example: Find orders by customer with pagination
     * <p>
     * Features demonstrated:
     * - Named parameters
     * - Pagination
     * - Sorting
     * - Type-safe result
     */
    public List<Order> findOrdersByCustomer(String customerEmail, int offset, int limit) {
        TypedQuery<Order> query = entityManager.createQuery(
                "SELECT o FROM Order o WHERE o.customerEmail = :email ORDER BY o.orderDate DESC",
                Order.class);

        query.setParameter("email", customerEmail)
                .setFirstResult(offset)
                .setMaxResults(limit);

        return query.getResultList();
    }

    /**
     * Criteria API example: Find orders by status and minimum amount
     * <p>
     * Features demonstrated:
     * - Type-safe criteria
     * - Multiple conditions
     * - Complex predicates
     * - Strongly-typed parameters
     */
    public List<Order> findOrdersByStatusAndMinAmount(OrderStatus status, BigDecimal minAmount) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Order> cq = cb.createQuery(Order.class);
        Root<Order> order = cq.from(Order.class);

        cq.select(order)
                .where(
                        cb.and(
                                cb.equal(order.get("status"), status),
                                cb.greaterThanOrEqualTo(order.get("totalAmount"), minAmount)
                        )
                );

        return entityManager.createQuery(cq).getResultList();
    }

    /**
     * Native SQL example: Find orders with complex statistics
     * <p>
     * Features demonstrated:
     * - Complex aggregations
     * - Database-specific features
     * - Custom result mapping
     * - Date functions
     */
    public List<OrderStatistics> getOrderStatistics(LocalDateTime startDate) {
        String jpql = "SELECT NEW com.hibernate.learning.querying.OrderStatistics(" +
                "function('date', o.orderDate), " +
                "COUNT(o), " +
                "COALESCE(SUM(o.totalAmount), 0)" +
                ") " +
                "FROM Order o " +
                "WHERE o.orderDate >= :startDate " +
                "GROUP BY function('date', o.orderDate) " +
                "ORDER BY function('date', o.orderDate)";
            
        TypedQuery<OrderStatistics> query = entityManager.createQuery(jpql, OrderStatistics.class);
        query.setParameter("startDate", startDate);
        return query.getResultList();
    }

    /**
     * Named Query example: Find recent orders by status
     * <p>
     * Features demonstrated:
     * - Predefined query
     * - Parameter binding
     * - Result limiting
     * - Query reusability
     */
    public List<Order> findRecentOrdersByStatus(OrderStatus status, int limit) {
        return entityManager.createNamedQuery("Order.findRecentByStatus", Order.class)
                .setParameter("status", status)
                .setMaxResults(limit)
                .getResultList();
    }

    public List<Order> findOrdersByDateRange(LocalDate startDate, LocalDate endDate) {
        TypedQuery<Order> query = entityManager.createQuery(
            "SELECT o FROM Order o WHERE DATE(o.orderDate) BETWEEN :startDate AND :endDate",
            Order.class);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        return query.getResultList();
    }
}
