package com.aerometal.assignmentmanager.entity;

import com.aerometal.assignmentmanager.common.VersionedEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "component")
@Getter
@Setter
@NoArgsConstructor	
public class Component extends VersionedEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@NotBlank
	@Column(nullable = false, unique = true, length = 120)
	private String name;
	@Column(length = 500)
	private String description;
}
