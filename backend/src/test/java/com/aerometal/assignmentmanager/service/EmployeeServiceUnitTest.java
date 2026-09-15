package com.aerometal.assignmentmanager.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.aerometal.assignmentmanager.dto.EmployeeRequest;
import com.aerometal.assignmentmanager.dto.EmployeeResponse;
import com.aerometal.assignmentmanager.entity.Employee;
import com.aerometal.assignmentmanager.exception.ResourceNotFoundException;
import com.aerometal.assignmentmanager.exception.StaleEntityException;
import com.aerometal.assignmentmanager.mapper.EmployeeMapper;
import com.aerometal.assignmentmanager.repository.EmployeeRepository;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceUnitTest {

    @Mock
    private EmployeeRepository repository;

    @Mock
    private EmployeeMapper employeeMapper;

    @InjectMocks
    private EmployeeService service;

    @Test
    void shouldCreateAndListEmployees() {
        EmployeeRequest request =
                request(null, "John");

        Employee entity =
                employee(1L, 0L, "John");

        EmployeeResponse response =
                response(1L, 0L, "John");

        when(employeeMapper.toEntity(request))
                .thenReturn(entity);

        when(repository.saveAndFlush(entity))
                .thenReturn(entity);

        when(employeeMapper.toResponse(entity))
                .thenReturn(response);

        when(repository.findAll())
                .thenReturn(List.of(entity));

        assertThat(service.create(request))
                .isEqualTo(response);

        assertThat(service.findAll())
                .containsExactly(response);

        verify(employeeMapper).toEntity(request);
        verify(repository).saveAndFlush(entity);
    }

    @Test
    void shouldFindEmployeeResponseById() {
        Employee entity =
                employee(1L, 0L, "John");

        EmployeeResponse response =
                response(1L, 0L, "John");

        when(repository.findById(1L))
                .thenReturn(Optional.of(entity));

        when(employeeMapper.toResponse(entity))
                .thenReturn(response);

        assertThat(service.findResponseById(1L))
                .isEqualTo(response);

        verify(employeeMapper).toResponse(entity);
    }

    @Test
    void shouldUpdateEmployeeWithCurrentVersion() {
        EmployeeRequest request =
                request(0L, "Anna");

        Employee entity =
                employee(1L, 0L, "John");

        EmployeeResponse updatedResponse =
                response(1L, 1L, "Anna");

        when(repository.findById(1L))
                .thenReturn(Optional.of(entity));

        when(repository.saveAndFlush(entity))
                .thenReturn(entity);

        when(employeeMapper.toResponse(entity))
                .thenReturn(updatedResponse);

        assertThat(service.update(1L, request))
                .isEqualTo(updatedResponse);

        verify(employeeMapper)
                .updateEntity(request, entity);

        verify(repository)
                .saveAndFlush(entity);
    }

    @Test
    void shouldRejectUpdateWithStaleVersion() {
        EmployeeRequest staleRequest =
                request(1L, "Anna");

        /*
         * The entity is already version 2, but the client
         * submits the outdated version 1.
         */
        Employee entity =
                employee(1L, 2L, "John");

        when(repository.findById(1L))
                .thenReturn(Optional.of(entity));

        assertThatThrownBy(() ->
            service.update(1L, staleRequest)
        )
            .isInstanceOf(StaleEntityException.class)
            .hasMessageContaining(
                "modified by another user"
            );

        verify(employeeMapper, never())
                .updateEntity(staleRequest, entity);

        verify(repository, never())
                .saveAndFlush(entity);
    }

    @Test
    void shouldRejectUpdateWithoutVersion() {
        EmployeeRequest requestWithoutVersion =
                request(null, "Anna");

        Employee entity =
                employee(1L, 2L, "John");

        when(repository.findById(1L))
                .thenReturn(Optional.of(entity));

        assertThatThrownBy(() ->
            service.update(1L, requestWithoutVersion)
        )
            .isInstanceOf(StaleEntityException.class)
            .hasMessage(
                "Version is required when updating an employee."
            );

        verify(employeeMapper, never())
                .updateEntity(
                    requestWithoutVersion,
                    entity
                );

        verify(repository, never())
                .saveAndFlush(entity);
    }

    @Test
    void shouldRejectMissingEmployeeDuringUpdate() {
        EmployeeRequest request =
                request(0L, "Anna");

        when(repository.findById(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
            service.update(99L, request)
        )
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage(
                "Employee not found with id: 99"
            );

        verify(employeeMapper, never())
                .updateEntity(
                    org.mockito.ArgumentMatchers.any(),
                    org.mockito.ArgumentMatchers.any()
                );
    }

    @Test
    void shouldRejectMissingEmployeeDuringLookup() {
        when(repository.findById(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
            service.findById(99L)
        )
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessage(
                "Employee not found: 99"
            );
    }

    @Test
    void shouldDeleteExistingEmployee() {
        when(repository.existsById(1L))
                .thenReturn(true);

        service.delete(1L);

        verify(repository).deleteById(1L);
    }

    @Test
    void shouldRejectDeleteForMissingEmployee() {
        when(repository.existsById(99L))
                .thenReturn(false);

        assertThatThrownBy(() ->
            service.delete(99L)
        )
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessage(
                "Employee not found: 99"
            );

        verify(repository, never())
                .deleteById(99L);
    }

    private EmployeeRequest request(
            Long version,
            String firstName
    ) {
        return new EmployeeRequest(
            version,
            firstName,
            "Smith",
            "AMP-100",
            LocalDate.of(2025, 1, 10)
        );
    }

    private Employee employee(
            Long id,
            Long version,
            String firstName
    ) {
        Employee employee = new Employee();

        employee.setId(id);
        employee.setFirstName(firstName);
        employee.setLastName("Smith");
        employee.setAmpNumber("AMP-100");
        employee.setHireDate(
            LocalDate.of(2025, 1, 10)
        );

        /*
         * VersionedEntity deliberately has no version setter.
         * Reflection is used only to prepare the unit-test entity.
         */
        ReflectionTestUtils.setField(
            employee,
            "version",
            version
        );

        return employee;
    }

    private EmployeeResponse response(
            Long id,
            Long version,
            String firstName
    ) {
        return new EmployeeResponse(
            id,
            version,
            firstName,
            "Smith",
            "AMP-100",
            LocalDate.of(2025, 1, 10)
        );
    }
}