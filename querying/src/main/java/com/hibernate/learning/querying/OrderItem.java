package com.hibernate.learning.querying;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * OrderItem entity representing items within an order.
 * <p>
 * This entity demonstrates:
 * - Many-to-One relationship with Order
 * - Composite primary key
 * - Precise decimal handling
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Reference to the parent order
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    /**
     * Product SKU
     */
    @Column(name = "product_sku", nullable = false)
    private String productSku;

    /**
     * Product name at time of order
     */
    @Column(name = "product_name", nullable = false)
    private String productName;

    /**
     * Quantity ordered
     */
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    /**
     * Price per unit at time of order
     */
    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    /**
     * Total price for this item (quantity * unit_price)
     */
    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;
}
