package com.hibernate.learning.bestpractices;

/**
 * Enum representing possible customer statuses.
 * <p>
 * Best Practices:
 * 1. Use String enum type for better readability
 * 2. Keep enums simple and focused
 * 3. Use meaningful names
 * 4. Document enum values
 */
public enum CustomerStatus {
    /**
     * Customer is active and can make transactions
     */
    ACTIVE,

    /**
     * Customer is temporarily suspended
     */
    SUSPENDED,

    /**
     * Customer is inactive but can be reactivated
     */
    INACTIVE,

    /**
     * Customer is permanently blocked
     */
    BLOCKED
}
