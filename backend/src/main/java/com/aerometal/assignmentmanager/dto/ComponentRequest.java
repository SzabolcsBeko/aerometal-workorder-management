package com.aerometal.assignmentmanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ComponentRequest(

		Long version,

		@NotBlank @Size(max = 100) String name,

		@Size(max = 500) String description) {

}
