# Hibernate Basics - Interview Preparation Guide

## Core Concepts

### 1. What is Hibernate?

- An Object-Relational Mapping (ORM) framework for Java
- Handles the conversion between Java objects and database tables
- Eliminates the need for manual JDBC code
- Provides powerful querying capabilities

### 2. Key Components Demonstrated

#### Entity Class (Employee.java)

```java
@Entity
@Table(name = "employees")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // ...
}
```

- **@Entity**: Marks a class as a JPA entity
- **@Table**: Specifies the database table name
- **@Id**: Marks the primary key field
- **@GeneratedValue**: Defines how the primary key is generated
- **@Column**: Customizes column properties
- **@Enumerated**: Handles enum mappings
- **@Transient**: Marks fields that shouldn't be persisted

#### Data Access Object (EmployeeDAO.java)

Demonstrates core Hibernate operations:

1. **Session Management**

```java
try (Session session = HibernateUtil.getSessionFactory().openSession()) {
    // Session operations
}
```

2. **Transaction Handling**

```java
Transaction transaction = null;
try {
    transaction = session.beginTransaction();
    // operations
    transaction.commit();
} catch (Exception e) {
    if (transaction != null) {
        transaction.rollback();
    }
    throw e;
}
```

3. **CRUD Operations**

- Create: `session.save(entity)`
- Read: `session.get(Entity.class, id)`
- Update: `session.update(entity)`
- Delete: `session.delete(entity)`

### 3. Interview Questions & Answers

1. **Q: What is the difference between get() and load() in Hibernate?**
    - `get()`: Returns null if entity not found, always hits database
    - `load()`: Returns proxy, throws exception if entity not found, lazy loading

2. **Q: What is the difference between save() and persist()?**
    - `save()`: Returns generated ID, can be called outside transaction
    - `persist()`: Void return type, must be called within transaction

3. **Q: Explain Hibernate's session states:**
    - **Transient**: New object, not associated with session
    - **Persistent**: Object associated with session, changes tracked
    - **Detached**: Object was persistent, session closed
    - **Removed**: Object marked for deletion

4. **Q: What is dirty checking in Hibernate?**
    - Automatic detection of changes in persistent objects
    - Hibernate tracks object state and generates update SQL if needed
    - Only works within session

5. **Q: Explain different types of caching in Hibernate:**
    - **First-level**: Session-level cache (automatic)
    - **Second-level**: SessionFactory-level cache (optional)
    - **Query cache**: Caches query results

### 4. Best Practices Demonstrated

1. **Resource Management**
    - Using try-with-resources for sessions
    - Proper transaction handling
    - Exception management

2. **DAO Pattern**
    - Separation of concerns
    - Encapsulated database operations
    - Reusable code

3. **Entity Design**
    - Proper use of annotations
    - Meaningful column names
    - Validation constraints

### 5. Common Pitfalls to Avoid

1. **N+1 Query Problem**
    - Problem: Separate query for each related entity
    - Solution: Use join fetch or eager loading when needed

2. **Session Management**
    - Don't keep sessions open too long
    - Always close sessions in finally block or use try-with-resources

3. **Transaction Handling**
    - Always use transactions for write operations
    - Proper exception handling and rollback

### 6. Performance Tips

1. **Batch Processing**
    - Use batch size for bulk operations
    - Clear session periodically during bulk operations

2. **Fetching Strategies**
    - Use lazy loading by default
    - Choose eager loading only when necessary

3. **Query Optimization**
    - Use criteria API for dynamic queries
    - Named queries for static queries
    - Proper indexing in database

### 7. Debugging

1. **SQL Logging**
    - Enable SQL logging in logback.xml
    - View generated SQL queries
    - Monitor query performance

2. **Transaction Monitoring**
    - Log transaction boundaries
    - Track session state

## Running the Examples

1. Start the PostgreSQL database:

```bash
docker-compose up -d
```

2. Run BasicHibernateDemo.java to see:

- Entity creation
- CRUD operations
- Transaction handling
- Query execution
- Logging output

## Next Steps

After mastering these basics, proceed to:

1. Relationships (One-to-One, One-to-Many, Many-to-Many)
2. Inheritance Mapping
3. Caching Strategies
4. Advanced Querying
5. Best Practices
