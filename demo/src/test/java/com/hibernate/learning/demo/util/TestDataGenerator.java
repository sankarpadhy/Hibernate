package com.hibernate.learning.demo.util;

import com.hibernate.learning.bestpractices.Customer;
import com.hibernate.learning.bestpractices.CustomerStatus;
import com.hibernate.learning.caching.Product;
import com.hibernate.learning.querying.Order;
import com.hibernate.learning.querying.OrderItem;
import com.hibernate.learning.querying.OrderStatus;
import com.hibernate.learning.relationships.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
public class TestDataGenerator {
    private final SessionFactory sessionFactory;
    private final Random random = new Random();

    public TestDataGenerator(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public void generateBulkData(int numCustomers, int numProducts, int numOrders) {
        log.info("Generating bulk test data: {} customers, {} products, {} orders",
                numCustomers, numProducts, numOrders);

        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();

            // Generate Customers
            List<Customer> customers = generateCustomers(numCustomers);
            customers.forEach(session::save);

            // Generate Products
            List<Product> products = generateProducts(numProducts);
            products.forEach(session::save);

            // Generate Orders
            List<Order> orders = generateOrders(numOrders, customers, products);
            orders.forEach(session::save);

            tx.commit();
        }
    }

    public void generateRelationshipData(int numDepartments, int numEmployeesPerDept,
                                       int numCourses, int numStudents) {
        log.info("Generating relationship test data");

        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();

            // Generate Departments and Employees
            List<Department> departments = new ArrayList<>();
            for (int i = 0; i < numDepartments; i++) {
                Department dept = new Department();
                dept.setName("Department " + i);
                Set<Employee> employees = IntStream.range(0, numEmployeesPerDept)
                        .mapToObj(j -> {
                            Employee emp = new Employee();
                            emp.setName("Employee " + i + "-" + j);
                            emp.setEmail("emp" + i + "-" + j + "@example.com");
                            emp.setDepartment(dept);

                            // Add employee details
                            EmployeeDetail detail = new EmployeeDetail();
                            detail.setAddress(j + " Main St, City " + i);
                            detail.setPhoneNumber("555-" + String.format("%04d", j));
                            detail.setBirthDate(LocalDate.now().minusYears(20 + random.nextInt(30)));
                            detail.setEmployee(emp);
                            emp.setEmployeeDetail(detail);

                            return emp;
                        })
                        .collect(Collectors.toSet());
                dept.setEmployees(employees);
                departments.add(dept);
                session.save(dept);
            }

            // Generate Courses and Students
            List<Course> courses = IntStream.range(0, numCourses)
                    .mapToObj(i -> {
                        Course course = new Course();
                        course.setTitle("Course " + i);
                        course.setDescription("Description for course " + i);
                        return course;
                    })
                    .collect(Collectors.toList());

            List<Student> students = IntStream.range(0, numStudents)
                    .mapToObj(i -> {
                        Student student = new Student();
                        student.setName("Student " + i);
                        student.setEmail("student" + i + "@example.com");
                        
                        // Randomly assign courses to students
                        Set<Course> studentCourses = new HashSet<>();
                        int numCoursesForStudent = random.nextInt(courses.size() / 2) + 1;
                        while (studentCourses.size() < numCoursesForStudent) {
                            studentCourses.add(courses.get(random.nextInt(courses.size())));
                        }
                        student.setCourses(studentCourses);
                        studentCourses.forEach(course -> {
                            if (course.getStudents() == null) {
                                course.setStudents(new HashSet<>());
                            }
                            course.getStudents().add(student);
                        });
                        return student;
                    })
                    .collect(Collectors.toList());

            courses.forEach(session::save);
            students.forEach(session::save);

            tx.commit();
        }
    }

    private List<Customer> generateCustomers(int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> {
                    Customer customer = new Customer();
                    customer.setEmail("customer" + i + "@example.com");
                    customer.setFirstName("FirstName" + i);
                    customer.setLastName("LastName" + i);
                    customer.setPhoneNumber("+1-555-" + String.format("%04d", i));
                    customer.setStatus(CustomerStatus.ACTIVE);
                    customer.setCreatedAt(LocalDateTime.now().minusDays(random.nextInt(365)));
                    customer.setCreatedBy("system");
                    return customer;
                })
                .collect(Collectors.toList());
    }

    private List<Product> generateProducts(int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> {
                    Product product = new Product();
                    product.setSku("SKU-" + String.format("%06d", i));
                    product.setName("Product " + i);
                    product.setDescription("Description for product " + i);
                    product.setPrice(new BigDecimal(random.nextInt(10000) / 100.0));
                    product.setStockQuantity(random.nextInt(1000));
                    product.setCategory("Category " + (i % 10));
                    return product;
                })
                .collect(Collectors.toList());
    }

    private List<Order> generateOrders(int count, List<Customer> customers, List<Product> products) {
        return IntStream.range(0, count)
                .mapToObj(i -> {
                    Order order = new Order();
                    order.setCustomerEmail(customers.get(random.nextInt(customers.size())).getEmail());
                    order.setOrderDate(LocalDateTime.now().minusDays(random.nextInt(30)));
                    order.setStatus(OrderStatus.values()[random.nextInt(OrderStatus.values().length)]);
                    order.setShippingAddress(random.nextInt(999) + " Main St, City " + (i % 50));

                    // Generate 1-5 order items
                    int numItems = random.nextInt(5) + 1;
                    List<OrderItem> items = new ArrayList<>();
                    BigDecimal totalAmount = BigDecimal.ZERO;

                    for (int j = 0; j < numItems; j++) {
                        Product product = products.get(random.nextInt(products.size()));
                        OrderItem item = new OrderItem();
                        item.setOrder(order);
                        item.setProductName(product.getName());
                        item.setQuantity(random.nextInt(5) + 1);
                        item.setUnitPrice(product.getPrice());
                        items.add(item);
                        
                        totalAmount = totalAmount.add(item.getUnitPrice()
                                .multiply(new BigDecimal(item.getQuantity())));
                    }

                    order.setItems(items);
                    order.setTotalAmount(totalAmount);
                    return order;
                })
                .collect(Collectors.toList());
    }
}
