package com.hibernate.learning.relationships;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Author entity demonstrating various types of relationships in Hibernate.
 * <p>
 * This entity showcases:
 * 1. One-to-Many relationship with Book (bidirectional)
 * 2. Many-to-Many relationship with Publisher
 * 3. One-to-One relationship with AuthorProfile
 * <p>
 * Best Practices:
 * - Use Set instead of List for @ManyToMany to improve performance
 * - Implement proper equals/hashCode for collections
 * - Use lazy loading for collections
 * - Handle bidirectional relationships carefully
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "authors")
public class Author {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Author's full name
     */
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * Author's email address
     */
    @Column(name = "email", unique = true)
    private String email;

    /**
     * Books written by this author
     * Demonstrates bidirectional One-to-Many relationship
     */
    @OneToMany(
            mappedBy = "author",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @ToString.Exclude
    private List<Book> books = new ArrayList<>();

    /**
     * Publishers this author works with
     * Demonstrates Many-to-Many relationship
     */
    @ManyToMany(cascade = {
            CascadeType.PERSIST,
            CascadeType.MERGE
    })
    @JoinTable(
            name = "author_publisher",
            joinColumns = @JoinColumn(name = "author_id"),
            inverseJoinColumns = @JoinColumn(name = "publisher_id")
    )
    @ToString.Exclude
    private Set<Publisher> publishers = new HashSet<>();

    /**
     * Author's detailed profile
     * Demonstrates One-to-One relationship
     */
    @OneToOne(
            mappedBy = "author",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private AuthorProfile profile;

    /**
     * Helper method to maintain bidirectional relationship with Book
     */
    public void addBook(Book book) {
        books.add(book);
        book.setAuthor(this);
    }

    /**
     * Helper method to maintain bidirectional relationship with Book
     */
    public void removeBook(Book book) {
        books.remove(book);
        book.setAuthor(null);
    }

    /**
     * Helper method to maintain relationship with Publisher
     */
    public void addPublisher(Publisher publisher) {
        publishers.add(publisher);
        publisher.getAuthors().add(this);
    }

    /**
     * Helper method to maintain relationship with Publisher
     */
    public void removePublisher(Publisher publisher) {
        publishers.remove(publisher);
        publisher.getAuthors().remove(this);
    }

    /**
     * Helper method to maintain bidirectional relationship with AuthorProfile
     */
    public void setProfile(AuthorProfile profile) {
        if (profile == null) {
            if (this.profile != null) {
                this.profile.setAuthor(null);
            }
        } else {
            profile.setAuthor(this);
        }
        this.profile = profile;
    }
}
