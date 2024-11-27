package com.hibernate.learning.relationships;

import com.hibernate.learning.relationships.entities.*;
import com.hibernate.learning.util.HibernateUtil;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Demonstrates different types of entity relationships in Hibernate:
 * - One-to-One
 * - One-to-Many
 * - Many-to-Many
 */
@Slf4j
public class RelationshipsDemo {
    private final SessionFactory sessionFactory;

    public RelationshipsDemo(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public static void main(String[] args) {
        RelationshipsDemo demo = new RelationshipsDemo(HibernateUtil.getSessionFactory());
        demo.demonstrateOneToOne();
        demo.demonstrateOneToMany();
        demo.demonstrateOneToManyBidirectional();
        demo.demonstrateManyToMany();
    }

    private void demonstrateOneToOne() {
        log.info("=== One-to-One Relationship Demo ===");
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();

            // Create Employee and EmployeeDetail with one-to-one relationship
            Employee employee = new Employee();
            employee.setName("John Doe");
            employee.setEmail("john.doe@example.com");

            EmployeeDetail detail = new EmployeeDetail();
            detail.setAddress("123 Main St");
            detail.setPhoneNumber("555-0123");
            detail.setBirthDate(LocalDate.of(1990, 1, 15));

            employee.setEmployeeDetail(detail);
            detail.setEmployee(employee);  // Bidirectional

            session.save(employee);
            session.getTransaction().commit();

            // Load and verify
            session.clear();
            Employee loadedEmployee = session.get(Employee.class, employee.getId());
            log.info("Loaded employee with detail: {} - {}",
                    loadedEmployee.getName(),
                    loadedEmployee.getEmployeeDetail().getPhoneNumber());
        }
    }

    private void demonstrateOneToMany() {
        log.info("=== One-to-Many Relationship Demo ===");
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();

            // Create Department with multiple employees
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
            session.getTransaction().commit();

            // Load and verify
            session.clear();
            Department loadedDept = session.get(Department.class, department.getId());
            log.info("Loaded department {} with {} employees",
                    loadedDept.getName(),
                    loadedDept.getEmployees().size());
        }
    }

    private void demonstrateOneToManyBidirectional() {
        log.info("=== One-to-Many Bidirectional Relationship Demo ===");
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();

            // Create Project with multiple tasks
            Project project = new Project();
            project.setName("Mobile App Development");
            project.setDescription("Develop a new mobile application");

            Task task1 = new Task();
            task1.setTitle("UI Design");
            task1.setDescription("Design user interface");
            task1.setProject(project);

            Task task2 = new Task();
            task2.setTitle("API Integration");
            task2.setDescription("Integrate backend APIs");
            task2.setProject(project);

            Set<Task> tasks = new HashSet<>();
            tasks.add(task1);
            tasks.add(task2);
            project.setTasks(tasks);

            session.save(project);
            session.getTransaction().commit();

            // Load and verify bidirectional relationship
            session.clear();
            Task loadedTask = session.get(Task.class, task1.getId());
            log.info("Task '{}' belongs to project '{}'",
                    loadedTask.getTitle(),
                    loadedTask.getProject().getName());
        }
    }

    private void demonstrateManyToMany() {
        log.info("=== Many-to-Many Relationship Demo ===");
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();

            // Create Students and Courses
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
            course2.setDescription("Learn database design principles");

            // Establish many-to-many relationships
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

            // Load and verify
            session.clear();
            Student loadedStudent = session.get(Student.class, student1.getId());
            log.info("Student {} is enrolled in {} courses",
                    loadedStudent.getName(),
                    loadedStudent.getCourses().size());

            Course loadedCourse = session.get(Course.class, course1.getId());
            log.info("Loaded course: {}", loadedCourse.getTitle());
            log.info("Course {} has {} students enrolled",
                    loadedCourse.getTitle(),
                    loadedCourse.getStudents().size());
        }
    }
}
