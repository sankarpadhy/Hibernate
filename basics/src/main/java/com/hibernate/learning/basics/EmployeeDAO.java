package com.hibernate.learning.basics;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Employee Entity
 * <p>
 * Architecture Overview:
 * <pre>
 * ┌──────────────┐     ┌──────────────┐     ┌──────────────┐
 * │   Service    │     │EmployeeDAO   │     │  Database    │
 * │   Layer      │ ──► │  (This Class)│ ──► │              │
 * └──────────────┘     └──────────────┘     └──────────────┘
 * </pre>
 * <p>
 * Operation Flow:
 * <pre>
 * ┌─────────────────┐
 * │  Begin Session  │
 * └────────┬────────┘
 *          ▼
 * ┌─────────────────┐
 * │Begin Transaction│
 * └────────┬────────┘
 *          ▼
 * ┌─────────────────┐
 * │ Execute Query   │
 * └────────┬────────┘
 *          ▼
 * ┌─────────────────┐
 * │   Commit/       │
 * │   Rollback      │
 * └────────┬────────┘
 *          ▼
 * ┌─────────────────┐
 * │  Close Session  │
 * └─────────────────┘
 * </pre>
 */
public class EmployeeDAO {
    private static final Logger logger = LoggerFactory.getLogger(EmployeeDAO.class);
    private final SessionFactory sessionFactory;

    public EmployeeDAO(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /**
     * Saves a new employee or updates existing one
     * <p>
     * Process Flow:
     * <pre>
     * save()
     * ├── Validate Entity
     * │   └── Check constraints
     * │
     * ├── Begin Transaction
     * │   └── Get Session
     * │
     * ├── Persist Entity
     * │   └── Flush changes
     * │
     * └── Commit Transaction
     *     └── Handle exceptions
     * </pre>
     *
     * @param employee Entity to save
     * @return Saved entity with generated ID
     */
    public Employee save(Employee employee) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();

            session.saveOrUpdate(employee);

            transaction.commit();
            logger.info("Saved employee: {}", employee);
            return employee;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error saving employee", e);
            throw new RuntimeException("Error saving employee", e);
        }
    }

    /**
     * Retrieves an employee by ID
     * <p>
     * Query Flow:
     * <pre>
     * findById()
     * ├── Open Session
     * │   └── No transaction needed
     * │
     * ├── Execute Query
     * │   └── First-level cache check
     * │
     * └── Return Result
     *     └── Optional wrapper
     * </pre>
     *
     * @param id Employee ID
     * @return Optional containing employee if found
     */
    public Optional<Employee> findById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            Employee employee = session.get(Employee.class, id);
            return Optional.ofNullable(employee);
        } catch (Exception e) {
            logger.error("Error finding employee by ID: {}", id, e);
            throw new RuntimeException("Error finding employee", e);
        }
    }

    /**
     * Retrieves all employees
     * <p>
     * Query Flow:
     * <pre>
     * findAll()
     * ├── Create Criteria
     * │   └── No restrictions
     * │
     * ├── Execute Query
     * │   └── Fetch all records
     * │
     * └── Return List
     *     └── Empty if none found
     * </pre>
     *
     * @return List of all employees
     */
    public List<Employee> findAll() {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Employee> cq = cb.createQuery(Employee.class);
            Root<Employee> rootEntry = cq.from(Employee.class);
            CriteriaQuery<Employee> all = cq.select(rootEntry);
            Query<Employee> allQuery = session.createQuery(all);
            return allQuery.getResultList();
        } catch (Exception e) {
            logger.error("Error finding all employees", e);
            throw new RuntimeException("Error finding all employees", e);
        }
    }

    /**
     * Deletes an employee by ID
     * <p>
     * Process Flow:
     * <pre>
     * delete()
     * ├── Begin Transaction
     * │   └── Get Session
     * │
     * ├── Load Entity
     * │   └── Check existence
     * │
     * ├── Remove Entity
     * │   └── Cascade deletes
     * │
     * └── Commit Transaction
     *     └── Handle exceptions
     * </pre>
     *
     * @param id Employee ID to delete
     * @return true if deleted, false if not found
     */
    public boolean delete(Long id) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();

            Employee employee = session.get(Employee.class, id);
            if (employee != null) {
                session.delete(employee);
                transaction.commit();
                logger.info("Deleted employee with ID: {}", id);
                return true;
            }

            transaction.commit();
            return false;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error deleting employee with ID: {}", id, e);
            throw new RuntimeException("Error deleting employee", e);
        }
    }

    /**
     * Finds employees by email
     * <p>
     * Query Flow:
     * <pre>
     * findByEmail()
     * ├── Create Query
     * │   └── With parameters
     * │
     * ├── Set Parameters
     * │   └── Prevent SQL injection
     * │
     * └── Execute Query
     *     └── Return first match
     * </pre>
     *
     * @param email Email to search for
     * @return Optional containing employee if found
     */
    public Optional<Employee> findByEmail(String email) {
        try (Session session = sessionFactory.openSession()) {
            Query<Employee> query = session.createQuery(
                    "from Employee where email = :email", Employee.class);
            query.setParameter("email", email);
            return query.uniqueResultOptional();
        } catch (Exception e) {
            logger.error("Error finding employee by email: {}", email, e);
            throw new RuntimeException("Error finding employee by email", e);
        }
    }

    /**
     * Updates an employee's salary
     * <p>
     * Process Flow:
     * <pre>
     * updateSalary()
     * ├── Begin Transaction
     * │   └── Get Session
     * │
     * ├── Load Entity
     * │   └── Check existence
     * │
     * ├── Update Salary
     * │   └── Validate amount
     * │
     * └── Commit Transaction
     *     └── Handle exceptions
     * </pre>
     *
     * @param id        Employee ID
     * @param newSalary New salary amount
     * @return true if updated, false if not found
     */
    public boolean updateSalary(Long id, Double newSalary) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();

            Employee employee = session.get(Employee.class, id);
            if (employee != null) {
                employee.setSalary(BigDecimal.valueOf(newSalary));
                session.update(employee);
                transaction.commit();
                logger.info("Updated salary for employee ID {}: {}", id, newSalary);
                return true;
            }

            transaction.commit();
            return false;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error updating salary for employee ID: {}", id, e);
            throw new RuntimeException("Error updating salary", e);
        }
    }
}
