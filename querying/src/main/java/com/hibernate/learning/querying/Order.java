package com.hibernate.learning.querying;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Order entity demonstrating various JPA/Hibernate querying features.
 * <p>
 * This entity is designed to showcase:
 * 1. Named Queries - Predefined JPQL queries
 * 2. Complex Relationships - One-to-Many with OrderItems
 * 3. Enum Handling - OrderStatus mapping
 * 4. Temporal Data - Date/Time handling
 * <p>
 * Note about Named Queries:
 * - Parsed and validated at startup
 * - Cached for better performance
 * - Can be overridden by deployment descriptors
 * - Support query hints and comments
 * <p>
 * Performance Considerations:
 * - Use lazy loading for collections
 * - Index frequently queried columns
 * - Consider fetch joins in queries
 * - Use batch size for collections
 * <p>
 * Annotations explained:
 *
 * @Data - Lombok annotation to generate getters, setters, equals, hashCode and toString
 * @NoArgsConstructor - Lombok annotation to generate a no-args constructor
 * @Entity - Marks this class as a JPA entity
 * @NamedQueries - Defines reusable queries for this entity
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "orders")
@NamedQueries({
        @NamedQuery(
                name = "Order.findByStatus",
                query = "SELECT o FROM Order o WHERE o.status = :status"
        ),
        @NamedQuery(
                name = "Order.findRecentByStatus",
                query = "SELECT o FROM Order o WHERE o.status = :status ORDER BY o.orderDate DESC"
        ),
        @NamedQuery(
                name = "Order.findByCustomerAndDateRange",
                query = "SELECT o FROM Order o WHERE o.customerEmail = :email " +
                        "AND o.orderDate BETWEEN :startDate AND :endDate"
        )
})
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Customer's email address
     * Used for order tracking and communication
     */
    @Column(name = "customer_email", nullable = false)
    private String customerEmail;

    /**
     * Date and time when the order was placed
     * Stored with timezone information
     */
    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate;

    /**
     * Total amount of the order
     * Calculated as sum of all order items
     */
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    /**
     * Current status of the order
     * Mapped as a String enum for readability
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    /**
     * Shipping address for the order
     * Full address including postal code
     */
    @Column(name = "shipping_address", nullable = false)
    private String shippingAddress;

    /**
     * List of items in the order
     * Demonstrates One-to-Many relationship
     */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    /**
     * Notes or special instructions for the order
     * Optional field for customer requests
     */
    @Column(name = "notes")
    private String notes;

    /**
     * Whether the order has been paid
     * Used to track payment status
     */
    @Column(name = "paid", nullable = false)
    private boolean paid = false;

    /**
     * Tracking number for shipment
     * Available after order is shipped
     */
    @Column(name = "tracking_number")
    private String trackingNumber;
}
