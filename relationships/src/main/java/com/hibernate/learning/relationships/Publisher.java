package com.hibernate.learning.relationships;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Publisher entity demonstrating Many-to-Many relationship in Hibernate.
 * <p>
 * This entity showcases:
 * 1. Many-to-Many relationship with Author
 * 2. One-to-Many relationship with Book
 * <p>
 * Best Practices:
 * - Use Set for @ManyToMany relationships
 * - Implement proper equals/hashCode
 * - Use lazy loading for collections
 * - Avoid CascadeType.ALL in @ManyToMany
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "publishers")
public class Publisher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Publisher's name
     */
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * Publisher's website
     */
    @Column(name = "website")
    private String website;

    /**
     * Authors published by this publisher
     * Demonstrates inverse side of Many-to-Many relationship
     */
    @ManyToMany(mappedBy = "publishers")
    @ToString.Exclude
    private Set<Author> authors = new HashSet<>();

    /**
     * Books published by this publisher
     * Demonstrates One-to-Many relationship
     */
    @OneToMany(
            mappedBy = "publisher",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @ToString.Exclude
    private Set<Book> books = new HashSet<>();

    /**
     * Helper method to maintain relationship with Book
     */
    public void addBook(Book book) {
        books.add(book);
        book.setPublisher(this);
    }

    /**
     * Helper method to maintain relationship with Book
     */
    public void removeBook(Book book) {
        books.remove(book);
        book.setPublisher(null);
    }
}
