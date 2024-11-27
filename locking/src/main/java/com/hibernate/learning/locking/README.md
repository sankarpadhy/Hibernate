# Hibernate Locking Strategies Demo

This module demonstrates various locking strategies in Hibernate for handling concurrent access to data.

## Overview

### Locking Types

```
1. Optimistic Locking
├── Version-based
│   ├── @Version annotation
│   └── Automatic version increment
└── Timestamp-based
    ├── Last modified tracking
    └── Temporal version control

2. Pessimistic Locking
├── PESSIMISTIC_READ
│   ├── Shared lock
│   └── Allows concurrent reads
├── PESSIMISTIC_WRITE
│   ├── Exclusive lock
│   └── Prevents concurrent access
└── PESSIMISTIC_FORCE_INCREMENT
    ├── Exclusive lock
    └── Forces version increment

3. Natural Locking
├── Database constraints
└── Unique constraints
```

## Implementation Details

### Entity Structure

```
┌─────────────────────────┐
│         ACCOUNT         │
├─────────────────────────┤
│ ID (PK)                 │
│ ACCOUNT_NUMBER (UNIQUE) │
│ BALANCE                 │
│ VERSION                 │
│ LAST_MODIFIED          │
│ STATUS                  │
└─────────────────────────┘

┌─────────────────────────┐
│       TRANSACTION       │
├─────────────────────────┤
│ ID (PK)                 │
│ ACCOUNT_ID (FK)         │
│ AMOUNT                  │
│ DESCRIPTION            │
│ TRANSACTION_DATE        │
│ VERSION                 │
└─────────────────────────┘
```

### Key Components

1. **Account Entity**
    - Demonstrates version-based optimistic locking
    - Implements natural locking through unique constraints
    - Tracks modification timestamps

2. **Transaction Entity**
    - Demonstrates relationship locking
    - Implements version tracking
    - Maintains referential integrity

3. **AccountService**
    - Implements various locking strategies
    - Handles concurrent access
    - Manages transactions

## Test Scenarios

### 1. Optimistic Locking Test

```java
@Test
public void testOptimisticLocking() {
    // Concurrent update scenario
    // First transaction succeeds
    // Second transaction fails with OptimisticLockException
}
```

### 2. Pessimistic Read Lock Test

```java
@Test
public void testPessimisticReadLocking() {
    // Read lock scenario
    // Read operation acquires shared lock
    // Write operation fails with PessimisticLockException
}
```

### 3. Pessimistic Write Lock Test

```java
@Test
public void testPessimisticWriteLocking() {
    // Write lock scenario
    // First write acquires exclusive lock
    // Second write fails with PessimisticLockException
}
```

## Best Practices

1. **Optimistic Locking**
    - Use for low contention scenarios
    - Suitable for most web applications
    - Minimal database overhead

2. **Pessimistic Locking**
    - Use for high contention scenarios
    - When data consistency is critical
    - Consider performance impact

3. **Natural Locking**
    - Use database constraints
    - Enforce business rules
    - Maintain data integrity

## Performance Considerations

1. **Lock Timeouts**
    - Configure appropriate timeouts
    - Handle timeout exceptions
    - Implement retry logic

2. **Transaction Isolation**
    - Choose appropriate isolation level
    - Consider performance vs consistency
    - Document isolation requirements

3. **Lock Scope**
    - Minimize lock duration
    - Lock at appropriate level
    - Consider lock escalation

## Error Handling

1. **OptimisticLockException**
    - Implement retry logic
    - Provide user feedback
    - Log conflicts

2. **PessimisticLockException**
    - Handle timeouts
    - Implement backoff strategy
    - Monitor lock contention

3. **Deadlock Prevention**
    - Consistent lock ordering
    - Timeout configuration
    - Deadlock detection

## Usage Examples

### Optimistic Locking

```java
@Version
private Long version;

public void updateBalance(BigDecimal amount) {
    // Version automatically checked
    this.balance = this.balance.add(amount);
}
```

### Pessimistic Locking

```java
session.get(Account.class, id, 
    new LockOptions(LockMode.PESSIMISTIC_WRITE)
        .setTimeOut(LockOptions.WAIT_FOREVER));
```

### Natural Locking

```java
@Column(unique = true)
private String accountNumber;
```

## References

1. [Hibernate Documentation - Locking](https://docs.jboss.org/hibernate/orm/5.4/userguide/html_single/Hibernate_User_Guide.html#locking)
2. [JPA Specification - Locking](https://jakarta.ee/specifications/persistence/3.0/jakarta-persistence-spec-3.0.html#locking)
3. [Database Transaction Isolation Levels](https://docs.oracle.com/cd/E17952_01/mysql-5.7-en/innodb-transaction-isolation-levels.html)
