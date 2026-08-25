package com.aerometal.assignmentmanager.entity;


import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "employee")
public class Employee {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

	@Column(name="amp_number", nullable = false, length = 100)
	private String ampNumber;

	@Column(name = "hire_date")
	private LocalDate hireDate;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public String getAmpNumber() {
		return ampNumber;
	}
	
	public void setAmpNumber(String ampNumber) {
		this.ampNumber = ampNumber;
	}
	
	public LocalDate getHireDate() {
		return hireDate;
	}
	
	public void setHireDate(LocalDate hireDate) {
		this.hireDate = hireDate;
	}
}
