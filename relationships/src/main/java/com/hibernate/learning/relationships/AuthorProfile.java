package com.hibernate.learning.relationships;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDate;

/**
 * AuthorProfile entity demonstrating One-to-One relationship in Hibernate.
 * <p>
 * This entity showcases:
 * 1. One-to-One relationship with Author
 * 2. Date handling
 * 3. Enumerated type mapping
 * <p>
 * Best Practices:
 * - Use FetchType.LAZY for @OneToOne
 * - Implement proper equals/hashCode
 * - Avoid circular references in toString()
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "author_profiles")
public class AuthorProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Reference to the associated Author
     * Demonstrates owning side of One-to-One relationship
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", unique = true)
    @ToString.Exclude
    private Author author;

    /**
     * Author's biography
     */
    @Column(name = "biography", columnDefinition = "TEXT")
    private String biography;

    /**
     * Author's birth date
     */
    @Column(name = "birth_date")
    private LocalDate birthDate;

    /**
     * Author's website URL
     */
    @Column(name = "website")
    private String website;

    /**
     * Author's Twitter handle
     */
    @Column(name = "twitter_handle")
    private String twitterHandle;

    /**
     * Author's preferred writing genre
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_genre")
    private Genre preferredGenre;

    /**
     * Whether the author is currently accepting new projects
     */
    @Column(name = "accepting_projects")
    private boolean acceptingProjects = true;
}
