package com.hibernate.learning.relationships;

import com.hibernate.learning.relationships.entities.*;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.*;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RelationshipsTest {

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

    @AfterEach
    public void cleanup() {
        Session session = sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        session.createQuery("delete from RelationshipsTask").executeUpdate();
        session.createQuery("delete from RelationshipsProject").executeUpdate();
        session.createQuery("delete from RelationshipsEmployee").executeUpdate();
        session.createQuery("delete from RelationshipsDepartment").executeUpdate();
        session.createQuery("delete from RelationshipsEmployeeDetail").executeUpdate();
        tx.commit();
        session.close();
    }

    @Test
    public void testOneToOneRelationship() {
        Session session = sessionFactory.openSession();
        Transaction tx = session.beginTransaction();

        // Create employee with details
        Employee employee = new Employee("John Doe", "john@example.com");
        EmployeeDetail employeeDetail = new EmployeeDetail();
        employeeDetail.setPhoneNumber("123-456-7890");
        employeeDetail.setAddress("123 Main St");
        employeeDetail.setBirthDate(LocalDate.of(1990, 1, 1));

        employee.setEmployeeDetail(employeeDetail);

        session.save(employee);
        tx.commit();
        session.clear();

        // Verify the relationship
        Employee loadedEmployee = session.get(Employee.class, employee.getId());
        assertNotNull(loadedEmployee.getEmployeeDetail());
        assertEquals("123-456-7890", loadedEmployee.getEmployeeDetail().getPhoneNumber());
        assertEquals("123 Main St", loadedEmployee.getEmployeeDetail().getAddress());

        session.close();
    }

    @Test
    public void testOneToManyRelationship() {
        Session session = sessionFactory.openSession();
        Transaction tx = session.beginTransaction();

        // Create department with employees
        Department department = new Department("IT");
        Employee employee1 = new Employee("John Doe", "john@example.com");
        Employee employee2 = new Employee("Jane Doe", "jane@example.com");

        department.addEmployee(employee1);
        department.addEmployee(employee2);

        session.save(department);
        tx.commit();
        session.clear();

        // Verify the relationship
        Department loadedDepartment = session.get(Department.class, department.getId());
        assertEquals(2, loadedDepartment.getEmployees().size());
        assertTrue(loadedDepartment.getEmployees().stream()
                .map(Employee::getEmail)
                .anyMatch(email -> email.equals("john@example.com")));
        assertTrue(loadedDepartment.getEmployees().stream()
                .map(Employee::getEmail)
                .anyMatch(email -> email.equals("jane@example.com")));

        session.close();
    }

    @Test
    public void testManyToManyRelationship() {
        Session session = sessionFactory.openSession();
        Transaction tx = session.beginTransaction();

        // Create students and courses
        Student student1 = new Student("John Doe", "john@example.com");
        Student student2 = new Student("Jane Doe", "jane@example.com");

        Course course1 = new Course();
        course1.setTitle("Java Programming");
        course1.setDescription("Learn Java fundamentals");

        Course course2 = new Course();
        course2.setTitle("Database Design");
        course2.setDescription("Learn database concepts");

        // Associate students with courses
        student1.getCourses().add(course1);
        student1.getCourses().add(course2);
        student2.getCourses().add(course1);

        session.save(course1);
        session.save(course2);
        session.save(student1);
        session.save(student2);
        tx.commit();
        session.clear();

        // Verify the relationships
        Student loadedStudent = session.get(Student.class, student1.getId());
        assertEquals(2, loadedStudent.getCourses().size());

        Course loadedCourse = session.get(Course.class, course1.getId());
        assertEquals(2, loadedCourse.getStudents().size());

        session.close();
    }

    @Test
    public void testCascadingOperations() {
        Session session = sessionFactory.openSession();
        Transaction tx = session.beginTransaction();

        // Create project with tasks
        Project project = new Project();
        project.setName("New Project");
        project.setDescription("Project Description");

        Task task1 = new Task();
        task1.setTitle("Task 1");
        task1.setDescription("Task 1 Description");
        task1.setStatus("TODO");
        task1.setProject(project);

        Task task2 = new Task();
        task2.setTitle("Task 2");
        task2.setDescription("Task 2 Description");
        task2.setStatus("IN_PROGRESS");
        task2.setProject(project);

        project.getTasks().add(task1);
        project.getTasks().add(task2);

        // Save only the project - tasks should be cascaded
        session.save(project);
        tx.commit();
        session.clear();

        // Verify cascade persist
        Project loadedProject = session.get(Project.class, project.getId());
        assertEquals(2, loadedProject.getTasks().size());

        // Test cascade delete
        tx = session.beginTransaction();
        session.delete(loadedProject);
        tx.commit();
        session.clear();

        // Verify cascade delete
        assertNull(session.get(Project.class, project.getId()));
        assertTrue(session.createQuery("from RelationshipsTask", Task.class).list().isEmpty());

        session.close();
    }

    @Test
    public void testOrphanRemoval() {
        Session session = sessionFactory.openSession();
        Transaction tx = session.beginTransaction();

        Department department = new Department("IT");
        Employee employee1 = new Employee("John Doe", "john@example.com");
        Employee employee2 = new Employee("Jane Doe", "jane@example.com");

        department.addEmployee(employee1);
        department.addEmployee(employee2);

        session.save(department);
        tx.commit();
        session.clear();

        // Remove one employee from the department
        tx = session.beginTransaction();
        Department loadedDepartment = session.get(Department.class, department.getId());
        Employee employeeToRemove = loadedDepartment.getEmployees().iterator().next();
        loadedDepartment.getEmployees().remove(employeeToRemove);
        tx.commit();
        session.clear();

        // Verify the employee was removed
        Department reloadedDepartment = session.get(Department.class, department.getId());
        assertEquals(1, reloadedDepartment.getEmployees().size());

        session.close();
    }
}
