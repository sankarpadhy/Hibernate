# Hibernate Tutorial Project

This project is a comprehensive guide to learning Hibernate from the ground up. It includes examples of all major
Hibernate concepts and features.

## Project Structure

This is a multi-module Maven project organized into the following modules:

1. **Common** (`common`)
    - Shared utilities and configurations
    - Base entity classes
    - Common dependencies

2. **Basics** (`basics`)
    - Entity creation and basic annotations
    - Primary key generation strategies
    - Basic CRUD operations
    - Session management

3. **Caching** (`caching`)
    - First-level cache
    - Second-level cache
    - Query cache
    - Cache statistics and monitoring

4. **Querying** (`querying`)
    - HQL (Hibernate Query Language)
    - Criteria API
    - Native SQL queries
    - Named queries
    - Result transformers

5. **Relationships** (`relationships`)
    - One-to-One mappings
    - One-to-Many mappings
    - Many-to-Many mappings
    - Bidirectional relationships
    - Cascade operations

6. **Inheritance** (`inheritance`)
    - Single Table strategy
    - Joined Table strategy
    - Table Per Class strategy
    - Mapped Superclass

7. **Best Practices** (`bestpractices`)
    - Connection pooling
    - Session management
    - Batch processing
    - Performance optimization
    - Error handling

8. **Locking** (`locking`)
    - Optimistic locking
    - Pessimistic locking
    - Version-based concurrency control
    - Lock modes and timeouts

## Prerequisites

- Java 11 or higher
- Docker and Docker Compose
- Maven 3.6 or higher
- PostgreSQL (provided via Docker)

## Running the Project

### Option 1: Using Docker Compose

1. Build and run all modules:
   ```bash
   docker-compose up
   ```

2. Run a specific module demo:
   ```bash
   docker-compose up <module-name>-demo
   ```
   
   Available demo services:
   - basics-demo
   - caching-demo
   - querying-demo
   - relationships-demo
   - inheritance-demo
   - bestpractices-demo
   - locking-demo

3. Stop all services:
   ```bash
   docker-compose down
   ```

### Option 2: Running Locally

1. Start PostgreSQL:
   ```bash
   docker-compose up -d postgres
   ```

2. Build the project:
   ```bash
   mvn clean install
   ```

3. Run a specific module:
   ```bash
   java -cp "module-name/target/module-name-1.0-SNAPSHOT.jar:common/target/common-1.0-SNAPSHOT.jar" com.hibernate.learning.<module>.Demo
   ```

## Key Concepts Covered

### 1. Entity Lifecycle
- Transient state
- Persistent state
- Detached state
- Removed state

### 2. Mapping Types
- Basic types
- Embedded types
- Collection mappings
- Entity mappings
- Custom type converters

### 3. Querying
- HQL/JPQL
- Criteria API
- Native SQL
- Named queries
- Dynamic queries
- Result transformers

### 4. Caching
- First-level cache (Session)
- Second-level cache (SessionFactory)
- Query cache
- Collection cache
- Cache regions and strategies

### 5. Performance
- Lazy loading
- Batch processing
- Connection pooling
- Query optimization
- N+1 problem solutions

### 6. Concurrency
- Optimistic locking
- Pessimistic locking
- Version-based concurrency
- Lock modes
- Deadlock prevention

## Project Configuration

### Database Configuration
- Database: PostgreSQL
- Host: localhost (or 'postgres' in Docker)
- Port: 5432
- Database Name: hibernatedb
- Username: postgres
- Password: postgres

### Hibernate Configuration
- Dialect: PostgreSQL
- Show SQL: true (for learning purposes)
- Format SQL: true
- Statistics: enabled
- Second-level cache: enabled
- Query cache: enabled

## Best Practices Implemented

1. **Entity Design**
   - Use of appropriate identifiers
   - Proper relationship mappings
   - Effective use of cascade types
   - Strategic fetch types

2. **Performance Optimization**
   - Connection pooling
   - Batch processing
   - Cache utilization
   - Query optimization

3. **Error Handling**
   - Proper transaction management
   - Exception handling
   - Connection leak prevention
   - Deadlock handling

4. **Testing**
   - Unit tests for entities
   - Integration tests for repositories
   - Cache testing
   - Concurrency testing

## Contributing

Feel free to contribute to this project by:
1. Forking the repository
2. Creating a feature branch
3. Committing your changes
4. Creating a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.
