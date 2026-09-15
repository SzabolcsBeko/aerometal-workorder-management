package com.aerometal.assignmentmanager.dto;

import java.time.LocalDate;

public record EmployeeResponse(
        Long id,
        Long version,
        String firstName,
        String lastName,
        String ampNumber,
        LocalDate hireDate

) {
}
