package com.aerometal.assignmentmanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AccessRightRequest(

        @NotBlank(message = "Access-right name is required")
        @Size(max = 120)
        String name,

        @Size(max = 500)
        String description,

        Long version
) {
}