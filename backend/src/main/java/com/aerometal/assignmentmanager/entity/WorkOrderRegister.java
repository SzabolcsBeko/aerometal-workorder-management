package com.aerometal.assignmentmanager.entity;

import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "workorder_register")
@Getter
@Setter
@RequiredArgsConstructor	
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
}
