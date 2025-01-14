# Hibernate Relationships Guide

This package demonstrates different types of entity relationships in Hibernate.

## Types of Relationships

### 1. One-to-One

- Department Head to Department
- Employee to EmployeeDetail
- Demonstrated with both unidirectional and bidirectional mappings

### 2. One-to-Many

- Department to Employees
- Project to Tasks
- Demonstrated with both unidirectional and bidirectional mappings

### 3. Many-to-Many

- Employee to Projects
- Courses to Students
- Demonstrated with join tables and additional attributes

## Implementation Examples

Check the following packages:

- `onetoone`: One-to-One relationship examples
- `onetomany`: One-to-Many relationship examples
- `manytomany`: Many-to-Many relationship examples

## Key Concepts Covered

1. Relationship Annotations
2. Cascade Types
3. Fetch Types
4. Join Columns
5. Mappings
6. Performance Considerations
