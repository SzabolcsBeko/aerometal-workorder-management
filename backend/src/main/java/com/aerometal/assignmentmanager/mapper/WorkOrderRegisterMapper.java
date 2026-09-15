package com.aerometal.assignmentmanager.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.aerometal.assignmentmanager.dto.WorkOrderRegisterRequest;
import com.aerometal.assignmentmanager.dto.WorkOrderRegisterResponse;
import com.aerometal.assignmentmanager.entity.WorkOrderRegister;

@Mapper(componentModel = "spring")
public interface WorkOrderRegisterMapper {

    /*
     * Relationships are resolved by the service using repositories.
     * Hibernate manages the inherited version field.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "component", ignore = true)
    @Mapping(target = "right", ignore = true)
    WorkOrderRegister toEntity(
            WorkOrderRegisterRequest request
    );

    @Mapping(target = "employeeId", source = "employee.id")
    @Mapping(
        target = "employeeFirstName",
        source = "employee.firstName"
    )
    @Mapping(
        target = "employeeLastName",
        source = "employee.lastName"
    )
    @Mapping(
        target = "employeeAmpNumber",
        source = "employee.ampNumber"
    )
    @Mapping(target = "componentId", source = "component.id")
    @Mapping(
        target = "componentName",
        source = "component.name"
    )
    @Mapping(target = "accessRightId", source = "right.id")
    @Mapping(target = "rightName", source = "right.name")
    WorkOrderRegisterResponse toResponse(
            WorkOrderRegister workOrderRegister
    );

    /*
     * Only scalar business fields are updated here.
     * Relationships are assigned by the service.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "component", ignore = true)
    @Mapping(target = "right", ignore = true)
    void updateEntity(
            WorkOrderRegisterRequest request,
            @MappingTarget WorkOrderRegister workOrderRegister
    );
}