package com.aerometal.assignmentmanager.entity;

import java.time.LocalDate;

import jakarta.persistence.*;

@Entity
@Table(name = "workorder_register")
public class WorkOrderRegister {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "employee_id", nullable = false)
	private Employee employee;
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "component_id", nullable = false)
	private Component component;
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "access_right_id", nullable = false)
	private AccessRight right;
	@Column(name="workorder_number", nullable = false, length = 120)
	private String workOrderNumber;
	@Column(name="workorder_date", nullable = false)
	private LocalDate workOrderDate;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	public String getWorkOrderNumber() {
		return workOrderNumber;
	}

	public void setWorkOrderNumber(String workOrderNumber) {
		this.workOrderNumber = workOrderNumber;
	}

	public LocalDate getWorkOrderDate() {
		return workOrderDate;
	}

	public void setWorkOrderDate(LocalDate workOrderDate) {
		this.workOrderDate = workOrderDate;
	}

	public Employee getEmployee() {
		return employee;
	}

	public void setEmployee(Employee employee) {
		this.employee = employee;
	}

	public Component getComponent() {
		return component;
	}

	public void setComponent(Component component) {
		this.component = component;
	}

	public AccessRight getRight() {
		return right;
	}

	public void setRight(AccessRight right) {
		this.right = right;
	}
}
