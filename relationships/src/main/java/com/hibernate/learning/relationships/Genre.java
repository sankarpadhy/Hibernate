package com.hibernate.learning.relationships;

/**
 * Enum representing literary genres.
 * <p>
 * This enum is used with @Enumerated(EnumType.STRING) to store
 * the genre as a string in the database, making it more readable
 * and maintainable than using ordinal values.
 */
public enum Genre {
    FICTION,
    NON_FICTION,
    SCIENCE_FICTION,
    FANTASY,
    MYSTERY,
    THRILLER,
    ROMANCE,
    HORROR,
    BIOGRAPHY,
    HISTORY,
    POETRY,
    DRAMA,
    CHILDREN
}
