package com.aerometal.assignmentmanager.entity;


import java.time.LocalDate;

import com.aerometal.assignmentmanager.common.VersionedEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "employee")
@Getter
@Setter
@NoArgsConstructor	
public class Employee extends VersionedEntity {
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
}
