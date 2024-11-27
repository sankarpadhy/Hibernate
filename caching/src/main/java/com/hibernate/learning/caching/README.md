# Hibernate Caching Module

This module demonstrates Hibernate's comprehensive caching capabilities, including first-level cache, second-level
cache, query cache, and natural ID cache.

## Architecture Overview

```
┌─────────────────────────────────────────┐
│           Application Layer             │
│                                         │
│    ┌─────────────────────────────┐     │
│    │        Cache Demo           │     │
│    │                             │     │
│    │  ┌─────────┐   ┌─────────┐ │     │
│    │  │First    │   │Second   │ │     │
│    │  │Level    │   │Level    │ │     │
│    │  │Cache    │   │Cache    │ │     │
│    │  └─────────┘   └─────────┘ │     │
│    │                             │     │
│    │  ┌─────────┐   ┌─────────┐ │     │
│    │  │Query    │   │Natural  │ │     │
│    │  │Cache    │   │ID Cache │ │     │
│    │  └─────────┘   └─────────┘ │     │
│    └─────────────────────────────┘     │
│                                         │
└─────────────────────────────────────────┘
```

## Cache Types

### 1. First-Level Cache

- Session-scoped cache
- Enabled by default
- No configuration needed
- Cleared when session is closed

### 2. Second-Level Cache

- SessionFactory-scoped cache
- Shared across sessions
- Requires explicit configuration
- Supports different providers (EHCache, Infinispan)

### 3. Query Cache

- Caches HQL/Criteria query results
- Must be explicitly enabled
- Requires second-level cache
- Results invalidated on entity updates

### 4. Natural ID Cache

- Optimizes natural ID lookups
- Separate cache region
- Automatic invalidation
- Improves performance for business keys

## Configuration

### hibernate.cfg.xml

```xml
<property name="hibernate.cache.use_second_level_cache">true</property>
<property name="hibernate.cache.region.factory_class">
    org.hibernate.cache.ehcache.EhCacheRegionFactory
</property>
<property name="hibernate.cache.use_query_cache">true</property>
```

### Entity Configuration

```java
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "product")
@NaturalIdCache(region = "product-natural-id")
public class Product {
    @NaturalId
    private String sku;
    // ...
}
```

## Cache Strategies

### READ_ONLY

- For immutable data
- Best performance
- No locking required
- Throws exception on update

### NONSTRICT_READ_WRITE

- For rarely updated data
- No strict transaction isolation
- Potential stale reads
- Simple implementation

### READ_WRITE

- For frequently updated data
- Uses soft locks
- Consistent reads
- Higher overhead

### TRANSACTIONAL

- Full XA transaction support
- Highest consistency
- Requires JTA
- Most resource intensive

## Performance Monitoring

### Statistics

- Hit/Miss ratios
- Cache region sizes
- Query cache effectiveness
- Memory usage

### Metrics

```
Performance Metrics
├── Hit Ratio
│   ├── First Level
│   └── Second Level
│
├── Miss Count
│   ├── Cache misses
│   └── DB queries
│
└── Memory Usage
    ├── Cache size
    └── Entry count
```

## Best Practices

1. Cache Strategy Selection
    - Choose appropriate strategy per entity
    - Consider update frequency
    - Balance consistency vs performance

2. Cache Regions
    - Group related entities
    - Size regions appropriately
    - Monitor hit ratios

3. Query Cache
    - Use selectively
    - Monitor cache hit ratio
    - Consider result set size

4. Natural ID Cache
    - Use for business keys
    - Enable for frequent lookups
    - Monitor performance impact

## Example Usage

```java
// First-level cache demo
try (Session session = sessionFactory.openSession()) {
    Product product = session.get(Product.class, 1L); // DB hit
    Product cachedProduct = session.get(Product.class, 1L); // Cache hit
}

// Second-level cache demo
try (Session session1 = sessionFactory.openSession()) {
    Product product = session1.get(Product.class, 1L); // DB hit
}
try (Session session2 = sessionFactory.openSession()) {
    Product product = session2.get(Product.class, 1L); // L2 cache hit
}

// Query cache demo
List<Product> products = session.createQuery("from Product p where p.price > :price")
    .setParameter("price", new BigDecimal("100"))
    .setCacheable(true)
    .list();

// Natural ID cache demo
Product product = session.bySimpleNaturalId(Product.class)
    .load("SKU-001"); // Uses natural ID cache
```

## Performance Considerations

1. Memory Usage
    - Monitor cache sizes
    - Set appropriate region sizes
    - Watch for memory leaks

2. Cache Invalidation
    - Consider update patterns
    - Monitor stale data
    - Plan eviction strategy

3. Concurrency
    - Choose appropriate strategy
    - Consider transaction isolation
    - Monitor lock contention

4. Network Impact
    - Clustered caches
    - Replication overhead
    - Network bandwidth

## Testing

1. Unit Tests
    - Cache hit/miss scenarios
    - Concurrent access
    - Cache invalidation

2. Integration Tests
    - Cache across sessions
    - Transaction boundaries
    - Query cache behavior

3. Performance Tests
    - Cache hit ratios
    - Memory consumption
    - Response times

## References

- [Hibernate Documentation](https://docs.jboss.org/hibernate/orm/5.4/userguide/html_single/Hibernate_User_Guide.html#caching)
- [EHCache Documentation](https://www.ehcache.org/documentation/)
- [JCache Specification](https://www.jcp.org/en/jsr/detail?id=107)
