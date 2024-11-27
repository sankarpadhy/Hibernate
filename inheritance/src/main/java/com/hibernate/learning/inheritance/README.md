# Hibernate Inheritance Mapping Strategies

This package demonstrates the three inheritance mapping strategies available in Hibernate:

1. Single Table Strategy
2. Joined Table Strategy
3. Table Per Class Strategy

## Payment System Example

We use a payment system as an example, with a base `Payment` class and two concrete implementations:

- `CreditCardPayment`
- `BankTransferPayment`

## 1. Single Table Strategy (`singletable` package)

### Overview

- All classes in the hierarchy are mapped to a single database table
- A discriminator column identifies the concrete class for each row
- Most performant for polymorphic queries
- Wastes space due to unused columns

### Implementation

```java
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "payment_type")
public abstract class Payment { ... }

@Entity
@DiscriminatorValue("CREDIT_CARD")
public class CreditCardPayment extends Payment { ... }

@Entity
@DiscriminatorValue("BANK_TRANSFER")
public class BankTransferPayment extends Payment { ... }
```

### Database Schema

```sql
CREATE TABLE payments (
    id BIGINT PRIMARY KEY,
    amount DECIMAL NOT NULL,
    payment_date TIMESTAMP NOT NULL,
    currency VARCHAR(3) NOT NULL,
    transaction_id VARCHAR(36) UNIQUE,
    status VARCHAR(20) NOT NULL,
    payment_type VARCHAR(20) NOT NULL,
    -- Credit Card specific columns
    card_number VARCHAR(16),
    card_holder VARCHAR(100),
    expiry_month INTEGER,
    expiry_year INTEGER,
    -- Bank Transfer specific columns
    bank_name VARCHAR(100),
    account_number VARCHAR(20),
    routing_number VARCHAR(20),
    account_holder VARCHAR(100)
);
```

## Visual Database Schema Diagrams

### 1. Single Table Strategy

```
+---------------------------- PAYMENTS TABLE ----------------------------+
|                                                                      |
| - All classes mapped to single table                                 |
| - Discriminator column identifies concrete class                     |
|                                                                      |
+----------------------+---------------------+--------------------------|
| Common Columns      | Credit Card Columns | Bank Transfer Columns    |
+----------------------+---------------------+--------------------------|
| id                  | card_number         | bank_name               |
| amount              | card_holder         | account_number          |
| payment_date        | expiry_month        | routing_number          |
| currency            | expiry_year         | account_holder          |
| transaction_id      | (nullable)          | (nullable)              |
| status              |                     |                         |
| payment_type        |                     |                         |
+----------------------+---------------------+--------------------------|

Note: payment_type = 'CREDIT_CARD' or 'BANK_TRANSFER'
```

### 2. Joined Table Strategy (`joined` package)

### Overview

- Each class has its own table
- Child tables only contain columns specific to their class
- Primary key acts as a foreign key to the parent table
- Good for data integrity and normalization
- More complex queries due to joins

### Implementation

```java
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Payment { ... }

@Entity
@Table(name = "credit_card_payments")
@PrimaryKeyJoinColumn(name = "payment_id")
public class CreditCardPayment extends Payment { ... }

@Entity
@Table(name = "bank_transfer_payments")
@PrimaryKeyJoinColumn(name = "payment_id")
public class BankTransferPayment extends Payment { ... }
```

### Database Schema

```sql
CREATE TABLE payments (
    id BIGINT PRIMARY KEY,
    amount DECIMAL NOT NULL,
    payment_date TIMESTAMP NOT NULL,
    currency VARCHAR(3) NOT NULL,
    transaction_id VARCHAR(36) UNIQUE,
    status VARCHAR(20) NOT NULL
);

CREATE TABLE credit_card_payments (
    payment_id BIGINT PRIMARY KEY REFERENCES payments(id),
    card_number VARCHAR(16),
    card_holder VARCHAR(100),
    expiry_month INTEGER,
    expiry_year INTEGER
);

CREATE TABLE bank_transfer_payments (
    payment_id BIGINT PRIMARY KEY REFERENCES payments(id),
    bank_name VARCHAR(100),
    account_number VARCHAR(20),
    routing_number VARCHAR(20),
    account_holder VARCHAR(100)
);
```

### Visual Database Schema Diagrams

### 2. Joined Table Strategy

```
+----------------------- PAYMENTS TABLE ------------------------+
|                                                             |
| - Base table with common fields                             |
| - Primary key shared with child tables                      |
|                                                             |
+-------------------------------------------------------------
| id                                                          |
| amount                                                      |
| payment_date                                                |
| currency                                                    |
| transaction_id                                              |
| status                                                      |
+-------------------------------------------------------------
                           ↑
                           | (1:1 relationship)
                           |
        +------------------+-------------------+
        |                                     |
+--------+  +--------+
|Credit  |  | Bank   |
|Card    |  |Transfer|
+--------+  +--------+
   Pros: Normalized
   Cons: Joins needed

3. Table Per Class Strategy (`tableperclass` package)

### Overview
- Each concrete class has its own table with all properties
- No discriminator column needed
- Tables contain both inherited and specific properties
- Requires UNION queries for polymorphic queries
- Can be inefficient for polymorphic queries

### Implementation
```java
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class Payment { ... }

@Entity
@Table(name = "credit_card_payments")
public class CreditCardPayment extends Payment { ... }

@Entity
@Table(name = "bank_transfer_payments")
public class BankTransferPayment extends Payment { ... }
```

### Database Schema

```sql
CREATE TABLE credit_card_payments (
    id BIGINT PRIMARY KEY,
    amount DECIMAL NOT NULL,
    payment_date TIMESTAMP NOT NULL,
    currency VARCHAR(3) NOT NULL,
    transaction_id VARCHAR(36) UNIQUE,
    status VARCHAR(20) NOT NULL,
    card_number VARCHAR(16),
    card_holder VARCHAR(100),
    expiry_month INTEGER,
    expiry_year INTEGER
);

CREATE TABLE bank_transfer_payments (
    id BIGINT PRIMARY KEY,
    amount DECIMAL NOT NULL,
    payment_date TIMESTAMP NOT NULL,
    currency VARCHAR(3) NOT NULL,
    transaction_id VARCHAR(36) UNIQUE,
    status VARCHAR(20) NOT NULL,
    bank_name VARCHAR(100),
    account_number VARCHAR(20),
    routing_number VARCHAR(20),
    account_holder VARCHAR(100)
);
```

### Visual Database Schema Diagrams

### 3. Table Per Class Strategy

```
+----------------CREDIT_CARD_PAYMENTS---------+ +-------------BANK_TRANSFER_PAYMENTS----------+
|                                            | |                                             |
| - Complete table for each concrete class   | | - Complete table for each concrete class    |
| - All columns (common + specific)          | | - All columns (common + specific)           |
|                                            | |                                             |
+--------------------------------------------+ +---------------------------------------------+
| Common Columns:                            | | Common Columns:                             |
| - id                                       | | - id                                        |
| - amount                                   | | - amount                                    |
| - payment_date                             | | - payment_date                              |
| - currency                                 | | - currency                                  |
| - transaction_id                           | | - transaction_id                            |
| - status                                   | | - status                                    |
|                                            | |                                             |
| Specific Columns:                          | | Specific Columns:                           |
| - card_number                              | | - bank_name                                 |
| - card_holder                              | | - account_number                            |
| - expiry_month                             | | - routing_number                            |
| - expiry_year                              | | - account_holder                            |
+--------------------------------------------+ +---------------------------------------------+

Note: No relationship between tables
      UNION queries used for polymorphic operations

### Key Differences Visualization

```

1. Single Table:
   [All Data in One Table]
   +-----------------+
   | PAYMENTS |
   | (All columns)   |
   +-----------------+
   Pros: Fast queries
   Cons: Wasted space

2. Joined Table:
   [Parent-Child Tables]
   +---------------+
   | PAYMENTS |
   | (Base fields) |
   +---------------+
   ↑
   +------+------+
   | |
   +--------+ +--------+
   |Credit | | Bank |
   |Card | |Transfer|
   +--------+ +--------+
   Pros: Normalized
   Cons: Joins needed

3. Table Per Class:
   [Separate Complete Tables]
   +--------+ +--------+
   |Credit | | Bank |
   |Card | |Transfer|
   |(All | |(All |
   |fields) | |fields) |
   +--------+ +--------+
   Pros: Independent
   Cons: Duplicated columns

## Strategy Comparison

### Single Table

- **Pros**:
    - Best performance for polymorphic queries
    - Simple queries (no joins)
    - Easy to add new types
- **Cons**:
    - Table can become very wide
    - Nullable columns waste space
    - Not suitable for many subclasses

### Joined Table

- **Pros**:
    - Normalized database design
    - No nullable columns
    - Flexible for future changes
- **Cons**:
    - Requires joins for polymorphic queries
    - More complex queries
    - Insert/update requires multiple tables

### Table Per Class

- **Pros**:
    - Clean separation of data
    - No nullable columns
    - Simple queries for concrete classes
- **Cons**:
    - Duplicate columns in each table
    - Complex UNION queries for polymorphic queries
    - Can be inefficient for large hierarchies

## Usage Example

Check the `InheritanceDemo.java` class for examples of how to use each strategy. The demo shows:

1. Creating and saving Credit Card payments
2. Creating and saving Bank Transfer payments
3. Transaction management
4. Basic CRUD operations

## Best Practices

1. **Choose the Right Strategy**:
    - Use Single Table for simple hierarchies with few subclasses
    - Use Joined Table for complex hierarchies with many attributes
    - Use Table Per Class when polymorphic queries are rare

2. **Optimization Tips**:
    - Use lazy loading for associations
    - Consider fetch joins for frequent queries
    - Index commonly queried columns

3. **Common Pitfalls**:
    - Avoid deep inheritance hierarchies
    - Be careful with polymorphic queries in Table Per Class
    - Watch out for N+1 query problems

## Interview Questions and Examples

1. **What are the three inheritance mapping strategies in Hibernate? When would you use each?**

   Answer:
   ```java
   // 1. Single Table Strategy
   @Entity
   @Inheritance(strategy = InheritanceType.SINGLE_TABLE)
   @DiscriminatorColumn(name = "vehicle_type")
   public abstract class Vehicle {
       @Id private Long id;
       private String manufacturer;
   }
   
   @Entity
   @DiscriminatorValue("CAR")
   public class Car extends Vehicle {
       private int numDoors;
   }
   
   // Use when: 
   // - Simple inheritance
   // - Performance is critical
   // - Few subclasses
   
   // 2. Joined Table Strategy
   @Entity
   @Inheritance(strategy = InheritanceType.JOINED)
   public abstract class Employee {
       @Id private Long id;
       private String name;
       private BigDecimal baseSalary;
   }
   
   @Entity
   @PrimaryKeyJoinColumn(name = "emp_id")
   public class Manager extends Employee {
       private BigDecimal bonus;
       private String department;
   }
   
   // Use when:
   // - Complex inheritance
   // - Data integrity is important
   // - Normalized design needed
   
   // 3. Table Per Class Strategy
   @Entity
   @Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
   public abstract class Account {
       @Id private Long id;
       private String accountNumber;
       private BigDecimal balance;
   }
   
   @Entity
   public class SavingsAccount extends Account {
       private double interestRate;
   }
   
   // Use when:
   // - Independent entities
   // - Rare polymorphic queries
   // - Different table structures needed
   ```

2. **How does Hibernate handle polymorphic queries in each strategy? Show examples.**

   Answer:
   ```java
   // Single Table: Most efficient
   List<Vehicle> vehicles = session.createQuery(
       "from Vehicle where manufacturer = :maker", Vehicle.class)
       .setParameter("maker", "Toyota")
       .list();
   // Generates: SELECT * FROM vehicles WHERE manufacturer = 'Toyota'
   
   // Joined Table: Requires joins
   List<Employee> employees = session.createQuery(
       "from Employee where baseSalary > :minSalary", Employee.class)
       .setParameter("minSalary", new BigDecimal("50000"))
       .list();
   // Generates: 
   // SELECT e.*, m.* 
   // FROM employees e 
   // LEFT JOIN managers m ON e.id = m.emp_id 
   // WHERE e.baseSalary > 50000
   
   // Table Per Class: Uses UNION
   List<Account> accounts = session.createQuery(
       "from Account where balance > :minBalance", Account.class)
       .setParameter("minBalance", new BigDecimal("10000"))
       .list();
   // Generates:
   // SELECT * FROM 
   // (SELECT * FROM savings_accounts 
   //  UNION ALL 
   //  SELECT * FROM checking_accounts) 
   // WHERE balance > 10000
   ```

3. **How would you optimize performance for each inheritance strategy?**

   Answer:
   ```java
   // 1. Single Table Strategy
   @Entity
   @Inheritance(strategy = InheritanceType.SINGLE_TABLE)
   @DiscriminatorColumn(name = "payment_type")
   // Add indexes for commonly queried columns
   @Table(indexes = {
       @Index(name = "idx_status", columnList = "status"),
       @Index(name = "idx_payment_type", columnList = "payment_type")
   })
   public abstract class Payment { }
   
   // 2. Joined Table Strategy
   // Use fetch joins for frequent queries
   String hql = "SELECT e FROM Employee e " +
                "LEFT JOIN FETCH e.department " +
                "WHERE e.salary > :minSalary";
   
   // 3. Table Per Class Strategy
   // Use concrete class queries when possible
   String hql = "FROM SavingsAccount s " +
                "WHERE s.interestRate > :rate";
   // Instead of querying the base class
   ```

4. **How do you handle null values and constraints in Single Table strategy?**

   Answer:
   ```java
   @Entity
   @Inheritance(strategy = InheritanceType.SINGLE_TABLE)
   @DiscriminatorColumn(name = "doc_type")
   public abstract class Document {
       @Id private Long id;
       @Column(nullable = false) // Common fields can be non-null
       private String title;
   }
   
   @Entity
   @DiscriminatorValue("INVOICE")
   public class Invoice extends Document {
       @Column(nullable = true) // Specific fields should be nullable
       private String invoiceNumber;
       
       @Column(nullable = true)
       private BigDecimal amount;
   }
   
   @Entity
   @DiscriminatorValue("CONTRACT")
   public class Contract extends Document {
       @Column(nullable = true)
       private LocalDate expiryDate;
       
       @Column(nullable = true)
       private String partyNames;
   }
   ```

5. **How would you implement and query a deep inheritance hierarchy?**

   Answer:
   ```java
   // Example: Insurance Policy Hierarchy
   @Entity
   @Inheritance(strategy = InheritanceType.JOINED)
   public abstract class InsurancePolicy {
       @Id private Long id;
       private String policyNumber;
       private BigDecimal premium;
   }
   
   @Entity
   public abstract class VehicleInsurance extends InsurancePolicy {
       private String vehicleNumber;
       private int yearOfManufacture;
   }
   
   @Entity
   public class CarInsurance extends VehicleInsurance {
       private boolean hasAirbags;
       private String carModel;
   }
   
   // Querying deep hierarchy
   // 1. Using HQL
   String hql = "FROM InsurancePolicy p " +
                "WHERE TYPE(p) = CarInsurance " +
                "AND p.premium < :maxPremium";
   
   // 2. Using Criteria API
   CriteriaBuilder cb = session.getCriteriaBuilder();
   CriteriaQuery<InsurancePolicy> query = cb.createQuery(InsurancePolicy.class);
   Root<InsurancePolicy> root = query.from(InsurancePolicy.class);
   
   query.where(
       cb.and(
           cb.equal(root.type(), CarInsurance.class),
           cb.lessThan(root.get("premium"), maxPremium)
       )
   );
   ```

6. **How do you handle bidirectional relationships in inherited classes?**

   Answer:
   ```java
   @Entity
   @Inheritance(strategy = InheritanceType.JOINED)
   public abstract class Person {
       @Id private Long id;
       private String name;
       
       @OneToMany(mappedBy = "person")
       private Set<Address> addresses = new HashSet<>();
   }
   
   @Entity
   public class Employee extends Person {
       @ManyToOne
       @JoinColumn(name = "department_id")
       private Department department;
       
       @OneToMany(mappedBy = "employee")
       private Set<Project> projects = new HashSet<>();
   }
   
   @Entity
   public class Department {
       @Id private Long id;
       
       @OneToMany(mappedBy = "department")
       private Set<Employee> employees = new HashSet<>();
       
       // Helper methods for bidirectional relationship
       public void addEmployee(Employee employee) {
           employees.add(employee);
           employee.setDepartment(this);
       }
       
       public void removeEmployee(Employee employee) {
           employees.remove(employee);
           employee.setDepartment(null);
       }
   }
   ```

7. **What are the trade-offs between storage space and query performance?**

   Answer with examples:
   ```sql
   -- 1. Single Table Strategy
   -- Pros: Fast queries, no joins
   -- Cons: Wasted space due to null columns
   CREATE TABLE payments (
       id BIGINT PRIMARY KEY,
       amount DECIMAL,
       payment_type VARCHAR(31),
       -- Credit Card specific
       card_number VARCHAR(16) NULL,
       expiry_date DATE NULL,
       -- Bank Transfer specific
       bank_name VARCHAR(100) NULL,
       account_number VARCHAR(20) NULL
   );
   
   -- 2. Joined Table Strategy
   -- Pros: No wasted space
   -- Cons: Joins needed for queries
   CREATE TABLE payments (
       id BIGINT PRIMARY KEY,
       amount DECIMAL
   );
   
   CREATE TABLE credit_card_payments (
       payment_id BIGINT PRIMARY KEY,
       card_number VARCHAR(16),
       expiry_date DATE,
       FOREIGN KEY (payment_id) REFERENCES payments(id)
   );
   
   -- 3. Table Per Class Strategy
   -- Pros: Independent tables
   -- Cons: Duplicated columns, complex UNION queries
   CREATE TABLE credit_card_payments (
       id BIGINT PRIMARY KEY,
       amount DECIMAL,
       card_number VARCHAR(16),
       expiry_date DATE
   );
   
   CREATE TABLE bank_transfers (
       id BIGINT PRIMARY KEY,
       amount DECIMAL,
       bank_name VARCHAR(100),
       account_number VARCHAR(20)
   );
   ```

8. **How do you handle inheritance-specific validations and business logic?**

   Answer:
   ```java
   @Entity
   @Inheritance(strategy = InheritanceType.SINGLE_TABLE)
   public abstract class Payment {
       @Id private Long id;
       private BigDecimal amount;
       
       // Common validation logic
       @PrePersist
       public void validateBeforeSave() {
           if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
               throw new IllegalStateException("Invalid payment amount");
           }
           validatePaymentSpecifics();
       }
       
       // Abstract method for specific validations
       protected abstract void validatePaymentSpecifics();
   }
   
   @Entity
   public class CreditCardPayment extends Payment {
       private String cardNumber;
       private String cvv;
       
       @Override
       protected void validatePaymentSpecifics() {
           if (!isValidCreditCard(cardNumber)) {
               throw new IllegalStateException("Invalid credit card number");
           }
           if (!isValidCVV(cvv)) {
               throw new IllegalStateException("Invalid CVV");
           }
       }
       
       private boolean isValidCreditCard(String cardNumber) {
           // Luhn algorithm implementation
           return true; // simplified
       }
       
       private boolean isValidCVV(String cvv) {
           return cvv != null && cvv.matches("\\d{3,4}");
       }
   }
   ```

These examples cover real-world scenarios and demonstrate best practices for implementing inheritance in Hibernate. They
also show how to handle common challenges like performance optimization, validation, and relationship management.
