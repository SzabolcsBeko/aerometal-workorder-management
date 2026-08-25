package com.aerometal.assignmentmanager.mapper;

import com.aerometal.assignmentmanager.dto.EmployeeRequest;
import com.aerometal.assignmentmanager.dto.EmployeeResponse;
import com.aerometal.assignmentmanager.entity.Employee;
import java.time.LocalDate;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-08-25T21:23:17+0200",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.37.0.v20240215-1558, environment: Java 17.0.10 (Eclipse Adoptium)"
)
@Component
public class EmployeeMapperImpl implements EmployeeMapper {

    @Override
    public Employee toEntity(EmployeeRequest request) {
        if ( request == null ) {
            return null;
        }

        Employee employee = new Employee();

        employee.setAmpNumber( request.ampNumber() );
        employee.setFirstName( request.firstName() );
        employee.setHireDate( request.hireDate() );
        employee.setLastName( request.lastName() );

        return employee;
    }

    @Override
    public EmployeeResponse toResponse(Employee employee) {
        if ( employee == null ) {
            return null;
        }

        Long id = null;
        String firstName = null;
        String lastName = null;
        String ampNumber = null;
        LocalDate hireDate = null;

        id = employee.getId();
        firstName = employee.getFirstName();
        lastName = employee.getLastName();
        ampNumber = employee.getAmpNumber();
        hireDate = employee.getHireDate();

        EmployeeResponse employeeResponse = new EmployeeResponse( id, firstName, lastName, ampNumber, hireDate );

        return employeeResponse;
    }

    @Override
    public void updateEntity(EmployeeRequest request, Employee employee) {
        if ( request == null ) {
            return;
        }

        employee.setAmpNumber( request.ampNumber() );
        employee.setFirstName( request.firstName() );
        employee.setHireDate( request.hireDate() );
        employee.setLastName( request.lastName() );
    }
}
