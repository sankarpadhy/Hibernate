package com.hibernate.learning.relationships;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Book entity demonstrating the child side of relationships in Hibernate.
 * <p>
 * This entity showcases:
 * 1. Many-to-One relationship with Author (bidirectional)
 * 2. Many-to-One relationship with Publisher
 * <p>
 * Best Practices:
 * - Use FetchType.LAZY for @ManyToOne by default
 * - Implement proper equals/hashCode
 * - Avoid circular references in toString()
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Book's ISBN (International Standard Book Number)
     */
    @Column(name = "isbn", unique = true, nullable = false)
    private String isbn;

    /**
     * Book title
     */
    @Column(name = "title", nullable = false)
    private String title;

    /**
     * Book description
     */
    @Column(name = "description")
    private String description;

    /**
     * Publication date
     */
    @Column(name = "publication_date")
    private LocalDate publicationDate;

    /**
     * Price of the book
     */
    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;

    /**
     * Author of the book
     * Demonstrates Many-to-One side of bidirectional relationship
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    @ToString.Exclude
    private Author author;

    /**
     * Publisher of the book
     * Demonstrates Many-to-One relationship
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publisher_id")
    @ToString.Exclude
    private Publisher publisher;
}
