# Hibernate Advanced Querying Guide

This package demonstrates advanced querying techniques in Hibernate.

## Querying Methods

### 1. HQL (Hibernate Query Language)

```java
String hql = "FROM Employee e WHERE e.department.name = :deptName";
List<Employee> employees = session.createQuery(hql)
    .setParameter("deptName", "IT")
    .list();
```

- Object-oriented query language
- Similar to SQL but works with entities
- Supports polymorphic queries

### 2. Criteria API

```java
CriteriaBuilder cb = session.getCriteriaBuilder();
CriteriaQuery<Employee> cr = cb.createQuery(Employee.class);
Root<Employee> root = cr.from(Employee.class);
cr.select(root).where(cb.equal(root.get("department").get("name"), "IT"));
```

- Type-safe queries
- Programmatic query building
- Good for dynamic queries

### 3. Native SQL

```java
String sql = "SELECT * FROM employees WHERE salary > ?";
List<Employee> employees = session.createNativeQuery(sql, Employee.class)
    .setParameter(1, 50000)
    .list();
```

- Direct SQL queries
- Database-specific features
- Performance optimization

### 4. Named Queries

```java
@NamedQuery(
    name = "Employee.findByDepartment",
    query = "FROM Employee e WHERE e.department.name = :deptName"
)
```

- Pre-compiled queries
- Better performance
- Centralized query management

## Advanced Features

### 1. Projections

```java
CriteriaBuilder cb = session.getCriteriaBuilder();
CriteriaQuery<Tuple> query = cb.createTupleQuery();
Root<Employee> root = query.from(Employee.class);
query.multiselect(
    root.get("name"),
    cb.avg(root.get("salary"))
);
```

### 2. Aggregate Functions

- COUNT
- SUM
- AVG
- MIN
- MAX

### 3. Joins

```java
String hql = "SELECT e FROM Employee e " +
    "LEFT JOIN FETCH e.department " +
    "LEFT JOIN FETCH e.projects";
```

- Inner Join
- Left Join
- Right Join
- Fetch Join

### 4. Subqueries

```java
String hql = "FROM Employee e WHERE e.salary > " +
    "(SELECT AVG(emp.salary) FROM Employee emp)";
```

## Performance Optimization

### 1. Query Optimization

- Use appropriate fetch strategies
- Minimize the number of queries
- Proper join usage

### 2. Pagination

```java
Query query = session.createQuery("FROM Employee");
query.setFirstResult(0);
query.setMaxResults(10);
```

### 3. Batch Processing

```java
for (int i = 0; i < entities.size(); i++) {
    session.save(entities.get(i));
    if (i % 20 == 0) {
        session.flush();
        session.clear();
    }
}
```

## Best Practices

1. **Choose the Right Query Method**
    - HQL for simple queries
    - Criteria API for dynamic queries
    - Native SQL for complex/optimized queries

2. **Optimize Performance**
    - Use fetch joins appropriately
    - Implement pagination
    - Batch processing for bulk operations

3. **Handle Results Properly**
    - Use appropriate result transformers
    - Implement proper error handling
    - Consider memory constraints

4. **Security Considerations**
    - Prevent SQL injection
    - Use parameter binding
    - Implement proper access control
