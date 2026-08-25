package com.aerometal.assignmentmanager.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.aerometal.assignmentmanager.dto.EmployeeRequest;
import com.aerometal.assignmentmanager.dto.EmployeeResponse;
import com.aerometal.assignmentmanager.entity.Employee;

@Mapper(componentModel = "spring")
public interface EmployeeMapper {

    @Mapping(target = "id", ignore = true)
    Employee toEntity(EmployeeRequest request);

    EmployeeResponse toResponse(Employee employee);

    @Mapping(target = "id", ignore = true)
    void updateEntity(
        EmployeeRequest request,
        @MappingTarget Employee employee
    );
}