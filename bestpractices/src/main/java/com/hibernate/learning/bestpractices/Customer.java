package com.hibernate.learning.bestpractices;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.NaturalId;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.time.LocalDateTime;

/**
 * Customer entity demonstrating Hibernate best practices.
 * <p>
 * Best Practices Demonstrated:
 * 1. Use appropriate data types
 * 2. Implement validation constraints
 * 3. Use natural IDs for business keys
 * 4. Enable selective caching
 * 5. Use dynamic updates
 * 6. Implement proper equals/hashCode
 * 7. Use appropriate fetch strategies
 * 8. Implement audit fields
 * 9. Use meaningful column names
 * 10. Document entity and fields
 */
@Entity
@Table(
        name = "customers",
        indexes = {
                @Index(name = "idx_customer_email", columnList = "email"),
                @Index(name = "idx_customer_phone", columnList = "phone_number")
        }
)
@DynamicUpdate
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "customer")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Customer's email address - used as natural ID
     */
    @NaturalId
    @NotBlank
    @Email
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    /**
     * Customer's first name
     */
    @NotBlank
    @Column(name = "first_name", nullable = false)
    private String firstName;

    /**
     * Customer's last name
     */
    @NotBlank
    @Column(name = "last_name", nullable = false)
    private String lastName;

    /**
     * Customer's phone number
     */
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$")
    @Column(name = "phone_number")
    private String phoneNumber;

    /**
     * Customer's status
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CustomerStatus status = CustomerStatus.ACTIVE;

    /**
     * Customer's date of birth
     */
    @Column(name = "date_of_birth")
    private LocalDateTime dateOfBirth;

    /**
     * When the customer was created
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Who created the customer
     */
    @Column(name = "created_by", nullable = false, updatable = false)
    private String createdBy;

    /**
     * When the customer was last updated
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Who last updated the customer
     */
    @Column(name = "updated_by")
    private String updatedBy;

    /**
     * Version for optimistic locking
     */
    @Version
    @Column(name = "version")
    private Integer version;

    public Customer() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public CustomerStatus getStatus() {
        return status;
    }

    public void setStatus(CustomerStatus status) {
        this.status = status;
    }

    public LocalDateTime getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDateTime dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Customer)) return false;
        Customer customer = (Customer) o;
        return email != null && email.equals(customer.getEmail());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
