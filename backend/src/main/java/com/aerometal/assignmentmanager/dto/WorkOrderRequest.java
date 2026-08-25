package com.aerometal.assignmentmanager.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record WorkOrderRequest(
		@NotNull Long employeeId, 
		@NotNull Long componentId, 
		@NotBlank @Size(max=120) String workOrderNumber,
		@NotNull LocalDate workOrderDate, 
		@NotNull Long accessRightId) {
}
