package com.hibernate.learning.basics;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Basic Hibernate Demo showcasing fundamental operations
 * <p>
 * Demo Structure:
 * <pre>
 * ┌─────────────────────────────────┐
 * │     BasicHibernateDemo          │
 * │                                 │
 * │ ┌─────────────┐ ┌────────────┐ │
 * │ │  Employee   │ │EmployeeDAO │ │
 * │ │   Entity    │ │           │ │
 * │ └─────────────┘ └────────────┘ │
 * │                                 │
 * │ ┌─────────────────────────────┐ │
 * │ │       Demo Methods          │ │
 * │ │ - CRUD Operations          │ │
 * │ │ - Transaction Management   │ │
 * │ │ - Error Handling          │ │
 * │ └─────────────────────────────┘ │
 * └─────────────────────────────────┘
 * </pre>
 * <p>
 * Learning Objectives:
 * <pre>
 * 1. Basic Operations
 *    ├── Create
 *    ├── Read
 *    ├── Update
 *    └── Delete
 *
 * 2. Transaction Management
 *    ├── Begin
 *    ├── Commit
 *    └── Rollback
 *
 * 3. Session Handling
 *    ├── Open
 *    ├── Close
 *    └── Resource Management
 *
 * 4. Error Handling
 *    ├── Try-Catch Blocks
 *    ├── Transaction Rollback
 *    └── Resource Cleanup
 * </pre>
 */
public class BasicHibernateDemo {
    private static final Logger logger = LoggerFactory.getLogger(BasicHibernateDemo.class);
    private final SessionFactory sessionFactory;
    private final EmployeeDAO employeeDAO;

    public BasicHibernateDemo(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        this.employeeDAO = new EmployeeDAO(sessionFactory);
    }

    /**
     * Demonstrates basic CRUD operations
     * <p>
     * Operation Flow:
     * <pre>
     * runDemo()
     * ├── Create Employee
     * │   └── Save to DB
     * │
     * ├── Read Employee
     * │   └── Query by ID
     * │
     * ├── Update Employee
     * │   └── Modify salary
     * │
     * └── Delete Employee
     *     └── Remove from DB
     * </pre>
     */
    public void runDemo() {
        try {
            // Create
            Employee employee = createEmployee();
            logger.info("Created employee: {}", employee);

            // Read
            employee = employeeDAO.findById(employee.getId())
                    .orElseThrow(() -> new RuntimeException("Employee not found"));
            logger.info("Retrieved employee: {}", employee);

            // Update
            employeeDAO.updateSalary(employee.getId(), 75000.0);
            logger.info("Updated employee salary");

            // Delete
            employeeDAO.delete(employee.getId());
            logger.info("Deleted employee");

            // List all
            List<Employee> employees = employeeDAO.findAll();
            logger.info("All employees: {}", employees);

        } catch (Exception e) {
            logger.error("Error in demo", e);
            throw new RuntimeException("Demo failed", e);
        }
    }

    /**
     * Creates a sample employee
     * <p>
     * Creation Process:
     * <pre>
     * createEmployee()
     * ├── Set Basic Info
     * │   ├── Name
     * │   └── Email
     * │
     * ├── Set Employment Info
     * │   ├── Salary
     * │   └── Hire Date
     * │
     * └── Save to Database
     *     └── Generate ID
     * </pre>
     *
     * @return Created employee entity
     */
    private Employee createEmployee() {
        Employee employee = new Employee();
        employee.setFirstName("John");
        employee.setLastName("Doe");
        employee.setEmail("john.doe@example.com");
        employee.setSalary(new BigDecimal("50000.00"));
        employee.setHireDate(LocalDateTime.now());

        return employeeDAO.save(employee);
    }

    /**
     * Demonstrates transaction management
     * <p>
     * Transaction Flow:
     * <pre>
     * demonstrateTransaction()
     * ├── Begin Transaction
     * │   └── Get Session
     * │
     * ├── Execute Operations
     * │   ├── Success → Commit
     * │   └── Failure → Rollback
     * │
     * └── Close Resources
     *     └── Handle cleanup
     * </pre>
     */
    public void demonstrateTransaction() {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();

            // Perform multiple operations
            Employee employee1 = new Employee();
            employee1.setFirstName("Jane");
            employee1.setLastName("Smith");
            employee1.setEmail("jane.smith@example.com");
            session.save(employee1);

            Employee employee2 = new Employee();
            employee2.setFirstName("Bob");
            employee2.setLastName("Johnson");
            employee2.setEmail("bob.johnson@example.com");
            session.save(employee2);

            transaction.commit();
            logger.info("Transaction completed successfully");

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Transaction failed", e);
            throw new RuntimeException("Transaction failed", e);
        }
    }
}
