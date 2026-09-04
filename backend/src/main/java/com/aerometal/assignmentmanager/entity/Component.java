package com.aerometal.assignmentmanager.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "component")
@Getter
@Setter
@RequiredArgsConstructor	
public class Component {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@NotBlank
	@Column(nullable = false, unique = true, length = 120)
	private String name;
	@Column(length = 500)
	private String description;
}
