package com.hibernate.learning.basics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Employee Entity - Basic Hibernate Entity Example
 * <p>
 * Entity Structure:
 * <pre>
 * ┌─────────────────────────┐
 * │        Employee         │
 * ├─────────────────────────┤
 * │ @Id                     │
 * │ - id: Long             │
 * │                         │
 * │ @Column                 │
 * │ - firstName: String     │
 * │ - lastName: String      │
 * │ - email: String        │
 * │ - age: int             │
 * │                         │
 * │ @Enumerated            │
 * │ - status: EmployeeStatus│
 * │                         │
 * │ @Column                 │
 * │ - salary: BigDecimal   │
 * │                         │
 * │ @Column                 │
 * │ - hireDate: DateTime   │
 * └─────────────────────────┘
 * </pre>
 * <p>
 * Database Mapping:
 * <pre>
 * Table: EMPLOYEE
 * ┌──────────────┬─────────────┬──────────┐
 * │   Column     │    Type     │ Nullable │
 * ├──────────────┼─────────────┼──────────┤
 * │ ID           │ BIGINT      │    No    │
 * │ FIRST_NAME   │ VARCHAR(50) │    No    │
 * │ LAST_NAME    │ VARCHAR(50) │    No    │
 * │ EMAIL        │ VARCHAR(100)│    No    │
 * │ AGE          │ INTEGER     │    Yes   │
 * │ STATUS       │ VARCHAR(20) │    No    │
 * │ SALARY       │ DECIMAL     │    Yes   │
 * │ HIRE_DATE    │ TIMESTAMP   │    Yes   │
 * └──────────────┴─────────────┴──────────┘
 * </pre>
 * <p>
 * Validation Rules:
 * <pre>
 * Field Validations
 * ├── firstName
 * │   ├── @NotBlank
 * │   └── Length: 2-50
 * │
 * ├── lastName
 * │   ├── @NotBlank
 * │   └── Length: 2-50
 * │
 * ├── email
 * │   ├── @Email format
 * │   └── Unique constraint
 * │
 * ├── age
 * │   ├── @Min: 18
 * │   └── @Max: 100
 * │
 * ├── status
 * │   └── @NotNull
 * │
 * ├── salary
 * │   ├── @PositiveOrZero
 * │   └── Scale: 2
 * │
 * └── hireDate
 *     └── @Past or present
 * </pre>
 */
@Entity
@Table(name = "EMPLOYEE",
        uniqueConstraints = {
                @UniqueConstraint(name = "UK_EMPLOYEE_EMAIL",
                        columnNames = "EMAIL")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Employee {

    /**
     * Primary key strategy:
     * - Uses SEQUENCE generator
     * - Allocation size: 1
     * - Initial value: 1
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Employee's first name
     * - Required field
     * - Length: 2-50 characters
     */
    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    @Column(name = "FIRST_NAME", nullable = false, length = 50)
    private String firstName;

    /**
     * Employee's last name
     * - Required field
     * - Length: 2-50 characters
     */
    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    @Column(name = "LAST_NAME", nullable = false, length = 50)
    private String lastName;

    /**
     * Employee's email address
     * - Required field
     * - Must be unique
     * - Valid email format
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Column(name = "EMAIL", nullable = false, unique = true, length = 100)
    private String email;

    /**
     * Employee's age
     * - Optional field
     * - Must be between 18 and 100
     */
    @Min(value = 18, message = "Age must be at least 18")
    @Max(value = 100, message = "Age must be less than 100")
    @Transient
    private Integer age;

    /**
     * Employee's status
     * - Required field
     * - Must be one of the EmployeeStatus enum values
     */
    @NotNull(message = "Employee status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false)
    private EmployeeStatus status = EmployeeStatus.ACTIVE;

    /**
     * Employee's salary
     * - Optional field
     * - Must be positive or zero
     * - Precision: 19, Scale: 2
     */
    @PositiveOrZero(message = "Salary must be positive or zero")
    @Digits(integer = 17, fraction = 2, message = "Invalid salary format")
    @Column(name = "SALARY", precision = 19, scale = 2)
    private BigDecimal salary;

    /**
     * Employee's hire date
     * - Optional field
     * - Must be past or present
     */
    @PastOrPresent(message = "Hire date must be in the past or present")
    @Column(name = "HIRE_DATE")
    private LocalDateTime hireDate;

    /**
     * Validates the employee data before persistence
     * <p>
     * Validation Flow:
     * <pre>
     * validate()
     * ├── Basic Validations
     * │   ├── Check required fields
     * │   └── Validate formats
     * │
     * └── Business Rules
     *     ├── Salary range
     *     └── Date constraints
     * </pre>
     *
     * @throws PersistenceException if validation fails
     */
    @PrePersist
    @PreUpdate
    public void validate() {
        StringBuilder errors = new StringBuilder();

        if (firstName == null || firstName.trim().isEmpty()) {
            errors.append("First name is required. ");
        } else if (firstName.length() < 2 || firstName.length() > 50) {
            errors.append("First name must be between 2 and 50 characters. ");
        }

        if (lastName == null || lastName.trim().isEmpty()) {
            errors.append("Last name is required. ");
        } else if (lastName.length() < 2 || lastName.length() > 50) {
            errors.append("Last name must be between 2 and 50 characters. ");
        }

        if (email == null || email.trim().isEmpty()) {
            errors.append("Email is required. ");
        } else if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            errors.append("Invalid email format. ");
        }

        if (age != null && (age < 18 || age > 100)) {
            errors.append("Age must be between 18 and 100. ");
        }

        if (status == null) {
            errors.append("Employee status is required. ");
        }

        if (salary != null && salary.compareTo(BigDecimal.ZERO) < 0) {
            errors.append("Salary must be positive or zero. ");
        }

        if (hireDate != null && hireDate.isAfter(LocalDateTime.now())) {
            errors.append("Hire date must be in the past or present. ");
        }

        if (errors.length() > 0) {
            throw new PersistenceException(errors.toString().trim());
        }
    }
}
