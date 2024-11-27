package com.hibernate.learning.demo;

import com.hibernate.learning.bestpractices.Customer;
import com.hibernate.learning.caching.Product;
import com.hibernate.learning.inheritance.singletable.BankTransferPayment;
import com.hibernate.learning.inheritance.singletable.CreditCardPayment;
import com.hibernate.learning.locking.BankAccount;
import com.hibernate.learning.querying.Order;
import com.hibernate.learning.querying.OrderItem;
import com.hibernate.learning.querying.OrderStatus;
import com.hibernate.learning.relationships.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.LockMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HibernateDemoIntegrationTest {

    private SessionFactory sessionFactory;

    @BeforeAll
    void setUp() {
        StandardServiceRegistry standardRegistry = new StandardServiceRegistryBuilder()
                .configure("hibernate-test.cfg.xml")
                .build();

        Metadata metadata = new MetadataSources(standardRegistry)
                .getMetadataBuilder()
                .build();

        sessionFactory = metadata.getSessionFactoryBuilder()
                .build();
    }

    @AfterAll
    void tearDown() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }

    @Test
    void testCaching() {
        log.info("Testing Caching...");
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();

            // Create and save a Product
            Product product = new Product();
            product.setSku("TEST-SKU-001");
            product.setName("Test Product");
            product.setDescription("A test product for caching demo");
            product.setPrice(new BigDecimal("99.99"));
            product.setStockQuantity(100);
            product.setCategory("Test Category");

            session.save(product);
            session.getTransaction().commit();

            // Test second-level cache
            session.clear(); // Clear first-level cache

            // First load - should hit the database
            Product loadedProduct1 = session.get(Product.class, product.getId());
            assertNotNull(loadedProduct1);
            assertEquals("Test Product", loadedProduct1.getName());

            session.clear();

            // Second load - should hit the cache
            Product loadedProduct2 = session.get(Product.class, product.getId());
            assertNotNull(loadedProduct2);
            assertEquals("Test Product", loadedProduct2.getName());
        }
    }

    @Test
    void testLocking() {
        log.info("Testing Locking...");
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();

            // Create a bank account
            BankAccount account = new BankAccount();
            account.setAccountNumber("1234567890");
            account.setBalance(new BigDecimal("1000.00"));
            account.setOwnerName("John Doe");
            session.save(account);

            session.getTransaction().commit();

            // Test concurrent access with optimistic locking
            ExecutorService executor = Executors.newFixedThreadPool(2);
            CountDownLatch latch = new CountDownLatch(2);

            executor.submit(() -> {
                try (Session s1 = sessionFactory.openSession()) {
                    s1.beginTransaction();
                    BankAccount acc1 = s1.get(BankAccount.class, account.getId());
                    acc1.setBalance(acc1.getBalance().add(new BigDecimal("500.00")));
                    Thread.sleep(1000); // Simulate some work
                    s1.update(acc1);
                    s1.getTransaction().commit();
                } catch (Exception e) {
                    log.error("Error in thread 1", e);
                } finally {
                    latch.countDown();
                }
            });

            executor.submit(() -> {
                try (Session s2 = sessionFactory.openSession()) {
                    s2.beginTransaction();
                    BankAccount acc2 = s2.get(BankAccount.class, account.getId(), LockMode.PESSIMISTIC_WRITE);
                    acc2.setBalance(acc2.getBalance().subtract(new BigDecimal("200.00")));
                    Thread.sleep(500); // Simulate some work
                    s2.update(acc2);
                    s2.getTransaction().commit();
                } catch (Exception e) {
                    log.error("Error in thread 2", e);
                } finally {
                    latch.countDown();
                }
            });

            assertTrue(latch.await(5, TimeUnit.SECONDS));
            executor.shutdown();

            // Verify final balance
            session.clear();
            BankAccount finalAccount = session.get(BankAccount.class, account.getId());
            assertNotNull(finalAccount);
            assertTrue(finalAccount.getBalance().compareTo(BigDecimal.ZERO) > 0);
        } catch (InterruptedException e) {
            fail("Test interrupted", e);
        }
    }

    @Test
    void testRelationships() {
        log.info("Testing Relationships...");
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();

            // Test One-to-One
            Employee employee = new Employee();
            employee.setName("John Doe");
            employee.setEmail("john.doe@example.com");

            EmployeeDetail detail = new EmployeeDetail();
            detail.setAddress("123 Main St");
            detail.setPhoneNumber("555-0123");
            detail.setBirthDate(LocalDate.of(1990, 1, 15));

            employee.setEmployeeDetail(detail);
            detail.setEmployee(employee);

            session.save(employee);

            // Test One-to-Many
            Department department = new Department();
            department.setName("Engineering");

            Employee emp1 = new Employee();
            emp1.setName("Jane Smith");
            emp1.setEmail("jane.smith@example.com");
            emp1.setDepartment(department);

            Employee emp2 = new Employee();
            emp2.setName("Bob Wilson");
            emp2.setEmail("bob.wilson@example.com");
            emp2.setDepartment(department);

            Set<Employee> employees = new HashSet<>();
            employees.add(emp1);
            employees.add(emp2);
            department.setEmployees(employees);

            session.save(department);

            // Test Many-to-Many
            Student student1 = new Student();
            student1.setName("Alice Johnson");
            student1.setEmail("alice@example.com");

            Student student2 = new Student();
            student2.setName("Charlie Brown");
            student2.setEmail("charlie@example.com");

            Course course1 = new Course();
            course1.setTitle("Java Programming");
            course1.setDescription("Learn Java programming language");

            Course course2 = new Course();
            course2.setTitle("Python Programming");
            course2.setDescription("Learn Python programming language");

            Set<Course> courses1 = new HashSet<>();
            courses1.add(course1);
            courses1.add(course2);
            student1.setCourses(courses1);

            Set<Course> courses2 = new HashSet<>();
            courses2.add(course1);
            student2.setCourses(courses2);

            Set<Student> students1 = new HashSet<>();
            students1.add(student1);
            students1.add(student2);
            course1.setStudents(students1);

            Set<Student> students2 = new HashSet<>();
            students2.add(student1);
            course2.setStudents(students2);

            session.save(student1);
            session.save(student2);
            session.save(course1);
            session.save(course2);

            session.getTransaction().commit();

            // Verify relationships
            session.clear();
            Department loadedDept = session.get(Department.class, department.getId());
            assertNotNull(loadedDept);
            assertEquals(2, loadedDept.getEmployees().size());

            Student loadedStudent = session.get(Student.class, student1.getId());
            assertNotNull(loadedStudent);
            assertEquals(2, loadedStudent.getCourses().size());

            Course loadedCourse = session.get(Course.class, course1.getId());
            assertNotNull(loadedCourse);
            assertEquals(2, loadedCourse.getStudents().size());
        }
    }

    @Test
    void testInheritance() {
        log.info("Testing Inheritance...");
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();

            // Create and test CreditCardPayment
            CreditCardPayment ccPayment = new CreditCardPayment();
            ccPayment.setAmount(new BigDecimal("100.00"));
            ccPayment.setCardNumber("1234-5678-9012-3456");
            ccPayment.setCardHolderName("John Doe");
            ccPayment.setExpirationMonth(12);
            ccPayment.setExpirationYear(25);
            ccPayment.setCvv("123");

            session.save(ccPayment);

            // Create and test BankTransferPayment
            BankTransferPayment btPayment = new BankTransferPayment();
            btPayment.setAmount(new BigDecimal("200.00"));
            btPayment.setBankName("Test Bank");
            btPayment.setAccountNumber("987654321");
            btPayment.setSwiftCode("TESTSWIFT");
            btPayment.setIban("TEST123IBAN");

            session.save(btPayment);

            session.getTransaction().commit();

            // Verify inheritance
            session.clear();
            CreditCardPayment loadedCcPayment = session.get(CreditCardPayment.class, ccPayment.getId());
            assertNotNull(loadedCcPayment);
            assertEquals("John Doe", loadedCcPayment.getCardHolderName());

            BankTransferPayment loadedBtPayment = session.get(BankTransferPayment.class, btPayment.getId());
            assertNotNull(loadedBtPayment);
            assertEquals("Test Bank", loadedBtPayment.getBankName());
        }
    }
}
