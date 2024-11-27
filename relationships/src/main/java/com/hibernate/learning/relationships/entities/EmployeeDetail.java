package com.hibernate.learning.relationships.entities;

import javax.persistence.*;
import java.time.LocalDate;

@Entity(name = "RelationshipsEmployeeDetail")
@Table(name = "relationships_employee_details")
public class EmployeeDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String address;
    private String phoneNumber;
    private LocalDate birthDate;

    @OneToOne(mappedBy = "employeeDetail")
    private Employee employee;

    public EmployeeDetail() {
    }

    public EmployeeDetail(String address, String phoneNumber) {
        this.address = address;
        this.phoneNumber = phoneNumber;
    }

    public EmployeeDetail(String address, String phoneNumber, LocalDate birthDate) {
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.birthDate = birthDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }
}
