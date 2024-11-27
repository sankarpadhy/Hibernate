package com.hibernate.learning.caching;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.NaturalIdCache;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Product Entity demonstrating Hibernate caching strategies
 * <p>
 * Caching Architecture:
 * <pre>
 * ┌─────────────────────────────────────┐
 * │           Caching Layers            │
 * │                                     │
 * │    ┌───────────────────────┐       │
 * │    │   First Level Cache   │       │
 * │    │    (Session Scope)    │       │
 * │    └───────────────────────┘       │
 * │                                     │
 * │    ┌───────────────────────┐       │
 * │    │  Second Level Cache   │       │
 * │    │(SessionFactory Scope) │       │
 * │    └───────────────────────┘       │
 * │                                     │
 * │    ┌───────────────────────┐       │
 * │    │    Query Cache       │       │
 * │    │  (Query Results)     │       │
 * │    └───────────────────────┘       │
 * └─────────────────────────────────────┘
 * </pre>
 * <p>
 * Cache Regions:
 * <pre>
 * ┌─────────────────┐
 * │ Product Cache   │
 * ├─────────────────┤
 * │ Entity Data     │
 * │ Natural ID      │
 * │ Query Results   │
 * └─────────────────┘
 * </pre>
 * <p>
 * Cache Strategies:
 * <pre>
 * Strategy Types
 * ├── READ_ONLY
 * │   └── Never updated
 * │
 * ├── NONSTRICT_READ_WRITE
 * │   └── Occasional updates
 * │
 * ├── READ_WRITE
 * │   └── Regular updates
 * │
 * └── TRANSACTIONAL
 *     └── Full XA support
 * </pre>
 * <p>
 * Database Mapping:
 * <pre>
 * Table: PRODUCT
 * ┌──────────────┬─────────────┬──────────┐
 * │   Column     │    Type     │ Nullable │
 * ├──────────────┼─────────────┼──────────┤
 * │ ID           │ BIGINT      │    No    │
 * │ SKU          │ VARCHAR(50) │    No    │
 * │ NAME         │ VARCHAR(100)│    No    │
 * │ PRICE        │ DECIMAL     │    No    │
 * │ CREATED_DATE │ TIMESTAMP   │    No    │
 * │ UPDATED_DATE │ TIMESTAMP   │    Yes   │
 * │ DESCRIPTION  │ VARCHAR(255)│    No    │
 * │ STOCK_QUANTITY│ INT         │    No    │
 * │ CATEGORY     │ VARCHAR(50) │    No    │
 * │ ACTIVE       │ BOOLEAN     │    No    │
 * └──────────────┴─────────────┴──────────┘
 * </pre>
 */
@Entity
@Table(name = "PRODUCT")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@NaturalIdCache
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    /**
     * Primary key strategy:
     * - Uses SEQUENCE generator
     * - Cached in first and second level cache
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "product_seq")
    @SequenceGenerator(
            name = "product_seq",
            sequenceName = "PRODUCT_SEQ",
            allocationSize = 1
    )
    private Long id;

    /**
     * Natural ID (SKU)
     * - Business identifier
     * - Cached separately
     * - Immutable after creation
     */
    @NaturalId
    @Column(name = "sku", nullable = false, unique = true, length = 50)
    @NotBlank(message = "SKU is required")
    @Pattern(regexp = "^[A-Z0-9]{10}$", message = "SKU must be 10 characters of uppercase letters and numbers")
    private String sku;

    /**
     * Product name
     * - Required field
     * - Length: 5-100 characters
     */
    @NotBlank(message = "Product name is required")
    @Size(min = 5, max = 100, message = "Product name must be between 5 and 100 characters")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * Product description
     * - Required field
     * - Length: 5-255 characters
     */
    @NotBlank(message = "Product description is required")
    @Size(min = 5, max = 255, message = "Product description must be between 5 and 255 characters")
    @Column(name = "description", nullable = false, length = 255)
    private String description;

    /**
     * Product price
     * - Required field
     * - Must be positive
     * - Precision: 19, Scale: 2
     */
    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    @Column(name = "price", nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    /**
     * Stock quantity
     * - Required field
     * - Must be non-negative
     */
    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock quantity must be non-negative")
    @Column(name = "stock_quantity", nullable = false)
    private int stockQuantity;

    /**
     * Product category
     * - Required field
     * - Length: 5-50 characters
     */
    @NotBlank(message = "Product category is required")
    @Size(min = 5, max = 50, message = "Product category must be between 5 and 50 characters")
    @Column(name = "category", nullable = false, length = 50)
    private String category;

    /**
     * Product active status
     * - Required field
     * - Default value: true
     */
    @Column(name = "active")
    private boolean active = true;

    /**
     * Creation timestamp
     * - Set automatically
     * - Immutable
     */
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    /**
     * Last update timestamp
     * - Updated automatically
     */
    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    /**
     * Lifecycle callback - Pre persist
     * Sets creation and update timestamps
     * <p>
     * Lifecycle Flow:
     * <pre>
     * @PrePersist
     * ├── Set createdDate
     * └── Set updatedDate
     * </pre>
     */
    @PrePersist
    public void onCreate() {
        createdDate = LocalDateTime.now();
        updatedDate = LocalDateTime.now();
    }

    /**
     * Lifecycle callback - Pre update
     * Updates the update timestamp
     * <p>
     * Lifecycle Flow:
     * <pre>
     * @PreUpdate
     * └── Set updatedDate
     * </pre>
     */
    @PreUpdate
    protected void onUpdate() {
        updatedDate = LocalDateTime.now();
    }

    /**
     * Validates the product data
     * <p>
     * Validation Flow:
     * <pre>
     * validate()
     * ├── Check SKU format
     * ├── Validate name
     * ├── Validate description
     * ├── Verify price
     * ├── Verify stock quantity
     * └── Verify category
     * </pre>
     *
     * @throws IllegalStateException if validation fails
     */
    public void validate() {
        if (sku != null && !sku.matches("^[A-Z0-9]{10}$")) {
            throw new IllegalStateException("Invalid SKU format");
        }

        if (name == null || name.trim().length() < 5 || name.trim().length() > 100) {
            throw new IllegalStateException("Invalid product name length");
        }

        if (description == null || description.trim().length() < 5 || description.trim().length() > 255) {
            throw new IllegalStateException("Invalid product description length");
        }

        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Price must be positive");
        }

        if (stockQuantity < 0) {
            throw new IllegalStateException("Stock quantity must be non-negative");
        }

        if (category == null || category.trim().length() < 5 || category.trim().length() > 50) {
            throw new IllegalStateException("Invalid product category length");
        }
    }
}
