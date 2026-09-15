package com.aerometal.assignmentmanager.entity;

import java.time.LocalDate;

import com.aerometal.assignmentmanager.common.VersionedEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "workorder_register")
@Getter
@Setter
@NoArgsConstructor
public class WorkOrderRegister extends VersionedEntity {
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
