package com.hibernate.learning.basics;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import javax.persistence.PersistenceException;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EmployeeTest {

    private SessionFactory sessionFactory;

    @BeforeAll
    public void setup() {
        sessionFactory = new Configuration()
                .configure()
                .buildSessionFactory();
    }

    @AfterAll
    public void tearDown() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }

    @Test
    public void testBasicCRUDOperations() {
        // Create an employee
        Employee employee = new Employee();
        employee.setFirstName("John");
        employee.setLastName("Doe");
        employee.setEmail("john.doe@example.com");
        employee.setHireDate(LocalDateTime.now());
        employee.setSalary(new BigDecimal("75000.00"));
        employee.setStatus(EmployeeStatus.ACTIVE);
        employee.setAge(30); // @Transient field

        Session session = sessionFactory.openSession();
        Transaction tx = session.beginTransaction();

        try {
            // Create
            session.save(employee);
            tx.commit();
            assertNotNull(employee.getId(), "Employee ID should be generated");

            // Read
            session.clear(); // Clear session to force reload
            Employee loadedEmployee = session.get(Employee.class, employee.getId());
            assertNotNull(loadedEmployee);
            assertEquals("John", loadedEmployee.getFirstName());
            assertEquals("Doe", loadedEmployee.getLastName());
            assertEquals("john.doe@example.com", loadedEmployee.getEmail());
            assertEquals(EmployeeStatus.ACTIVE, loadedEmployee.getStatus());
            assertNull(loadedEmployee.getAge(), "Transient field should not be persisted");

            // Update
            tx = session.beginTransaction();
            loadedEmployee.setSalary(new BigDecimal("80000.00"));
            loadedEmployee.setStatus(EmployeeStatus.ON_LEAVE);
            session.update(loadedEmployee);
            tx.commit();

            session.clear();
            Employee updatedEmployee = session.get(Employee.class, employee.getId());
            assertEquals(new BigDecimal("80000.00"), updatedEmployee.getSalary());
            assertEquals(EmployeeStatus.ON_LEAVE, updatedEmployee.getStatus());

            // Delete
            tx = session.beginTransaction();
            session.delete(updatedEmployee);
            tx.commit();

            assertNull(session.get(Employee.class, employee.getId()), "Employee should be deleted");

        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        } finally {
            session.close();
        }
    }

    @Test
    public void testUniqueEmailConstraint() {
        Employee employee1 = new Employee();
        employee1.setFirstName("John");
        employee1.setLastName("Doe");
        employee1.setEmail("john.doe@example.com");
        employee1.setHireDate(LocalDateTime.now());
        employee1.setStatus(EmployeeStatus.ACTIVE);
        employee1.setAge(25); // Set valid age

        Employee employee2 = new Employee();
        employee2.setFirstName("Jane");
        employee2.setLastName("Smith");
        employee2.setEmail("john.doe@example.com"); // Same email as employee1
        employee2.setHireDate(LocalDateTime.now());
        employee2.setSalary(new BigDecimal("85000.00"));
        employee2.setStatus(EmployeeStatus.ACTIVE);
        employee2.setAge(30); // Set valid age

        Session session = sessionFactory.openSession();
        Transaction tx = session.beginTransaction();

        try {
            session.save(employee1);
            tx.commit();

            // Start a new transaction for the second employee
            Transaction finalTx = session.beginTransaction();
            assertThrows(PersistenceException.class, () -> {
                session.save(employee2);
                finalTx.commit();
            }, "Should throw exception for duplicate email");

        } finally {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            session.close();
        }
    }

    @Test
    public void testNullableConstraints() {
        Employee employee = new Employee();
        // Not setting required fields firstName, lastName, email

        Session session = sessionFactory.openSession();
        Transaction finalTx = session.beginTransaction();

        assertThrows(PersistenceException.class, () -> {
            session.save(employee);
            finalTx.commit();
        }, "Should throw exception for null required fields");

        if (finalTx != null && finalTx.isActive()) {
            finalTx.rollback();
        }
        session.close();
    }

    @Test
    public void testValidation() {
        Employee invalidEmployee = new Employee();
        invalidEmployee.setFirstName(""); // Invalid: empty name
        invalidEmployee.setLastName("Doe");
        invalidEmployee.setEmail("invalid-email"); // Invalid: wrong email format
        invalidEmployee.setHireDate(LocalDateTime.now());
        invalidEmployee.setStatus(EmployeeStatus.ACTIVE);

        Session session = sessionFactory.openSession();
        Transaction tx = session.beginTransaction();

        assertThrows(PersistenceException.class, () -> {
            session.save(invalidEmployee);
            tx.commit();
        });

        tx.rollback();
        session.close();
    }
}
