package com.aerometal.assignmentmanager.dto;

import java.time.LocalDate;

public record WorkOrderRegisterResponse(
        Long id,
        Long version,
        Long employeeId,
        String employeeFirstName,
        String employeeLastName,
        String employeeAmpNumber,
        Long componentId,
        String componentName,
        String workOrderNumber,
        LocalDate workOrderDate,
        Long accessRightId,
        String rightName
) {
}
