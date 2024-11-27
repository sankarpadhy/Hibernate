package com.hibernate.learning.querying;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO class for order statistics.
 * <p>
 * This class is used to hold aggregated order data returned by
 * native SQL queries. It demonstrates how to map complex SQL
 * query results to Java objects using ResultTransformer.
 */
public class OrderStatistics {

    /**
     * Date of the orders
     */
    private LocalDate orderDate;

    /**
     * Total number of orders for the date
     */
    private Long totalOrders;

    /**
     * Total revenue for the date
     */
    private BigDecimal totalRevenue;

    public OrderStatistics(LocalDate orderDate, Long totalOrders, BigDecimal totalRevenue) {
        this.orderDate = orderDate;
        this.totalOrders = totalOrders;
        this.totalRevenue = totalRevenue;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }

    public Long getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(Long totalOrders) {
        this.totalOrders = totalOrders;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }
}
