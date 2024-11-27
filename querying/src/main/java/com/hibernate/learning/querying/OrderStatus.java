package com.hibernate.learning.querying;

/**
 * Enum representing possible states of an Order.
 * <p>
 * This enum is used with @Enumerated(EnumType.STRING) to store
 * the status as a string in the database, making it more readable
 * and maintainable than using ordinal values.
 */
public enum OrderStatus {
    NEW,
    PENDING,
    CONFIRMED,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    REFUNDED
}
