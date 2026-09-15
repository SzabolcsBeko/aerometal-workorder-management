package com.aerometal.assignmentmanager.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.aerometal.assignmentmanager.dto.WorkOrderRegisterRequest;
import com.aerometal.assignmentmanager.dto.WorkOrderRegisterResponse;
import com.aerometal.assignmentmanager.entity.AccessRight;
import com.aerometal.assignmentmanager.entity.Component;
import com.aerometal.assignmentmanager.entity.Employee;
import com.aerometal.assignmentmanager.entity.WorkOrderRegister;
import com.aerometal.assignmentmanager.exception.StaleEntityException;
import com.aerometal.assignmentmanager.mapper.WorkOrderRegisterMapper;
import com.aerometal.assignmentmanager.repository.AccessRightRepository;
import com.aerometal.assignmentmanager.repository.ComponentRepository;
import com.aerometal.assignmentmanager.repository.EmployeeRepository;
import com.aerometal.assignmentmanager.repository.WorkOrderRegisterRepository;

@ExtendWith(MockitoExtension.class)
class WorkOrderServiceUnitTest {

    @Mock
    private WorkOrderRegisterRepository assignments;

    @Mock
    private EmployeeRepository employees;

    @Mock
    private ComponentRepository components;

    @Mock
    private AccessRightRepository accessRights;

    @Mock
    private WorkOrderRegisterMapper mapper;

    @InjectMocks
    private WorkOrderRegisterService service;

    private Employee employee;
    private Component component;
    private AccessRight accessRight;

    @BeforeEach
    void setUp() {
        employee = employee(1L);
        component = component(2L);
        accessRight = accessRight(3L);
    }

    @Test
    void shouldCreateAssignment() {
        WorkOrderRegisterRequest request =
                request(
                    "WO-100",
                    LocalDate.of(2026, 8, 20),
                    null
                );

        WorkOrderRegister assignment =
                assignment(
                    10L,
                    0L,
                    "WO-100",
                    LocalDate.of(2026, 8, 20)
                );

        WorkOrderRegisterResponse response =
                response(
                    10L,
                    0L,
                    "WO-100",
                    LocalDate.of(2026, 8, 20)
                );

        when(employees.findById(1L))
                .thenReturn(Optional.of(employee));

        when(components.findById(2L))
                .thenReturn(Optional.of(component));

        when(accessRights.findById(3L))
                .thenReturn(Optional.of(accessRight));

        when(mapper.toEntity(request))
                .thenReturn(assignment);

        when(assignments.saveAndFlush(assignment))
                .thenReturn(assignment);

        when(mapper.toResponse(assignment))
                .thenReturn(response);

        WorkOrderRegisterResponse result =
                service.create(request);

        assertThat(result).isEqualTo(response);
        assertThat(result.id()).isEqualTo(10L);
        assertThat(result.version()).isEqualTo(0L);
        assertThat(result.workOrderNumber())
                .isEqualTo("WO-100");

        assertThat(assignment.getEmployee())
                .isSameAs(employee);

        assertThat(assignment.getComponent())
                .isSameAs(component);

        assertThat(assignment.getRight())
                .isSameAs(accessRight);

        verify(mapper).toEntity(request);
        verify(assignments).saveAndFlush(assignment);
    }

    @Test
    void shouldUpdateAndListAssignments() {
        WorkOrderRegister assignment =
                assignment(
                    10L,
                    0L,
                    "WO-100",
                    LocalDate.of(2026, 8, 20)
                );

        WorkOrderRegisterRequest updateRequest =
                request(
                    "WO-100",
                    LocalDate.of(2026, 8, 21),
                    0L
                );

        WorkOrderRegisterResponse updatedResponse =
                response(
                    10L,
                    1L,
                    "WO-100",
                    LocalDate.of(2026, 8, 21)
                );

        when(assignments.findById(10L))
                .thenReturn(Optional.of(assignment));

        when(employees.findById(1L))
                .thenReturn(Optional.of(employee));

        when(components.findById(2L))
                .thenReturn(Optional.of(component));

        when(accessRights.findById(3L))
                .thenReturn(Optional.of(accessRight));

        when(assignments.saveAndFlush(assignment))
                .thenReturn(assignment);

        when(mapper.toResponse(assignment))
                .thenReturn(updatedResponse);

        when(assignments.findAllByOrderByIdAsc())
                .thenReturn(List.of(assignment));

        WorkOrderRegisterResponse updated =
                service.update(
                    10L,
                    updateRequest
                );

        assertThat(updated.workOrderDate())
                .isEqualTo(LocalDate.of(2026, 8, 21));

        assertThat(updated.version()).isEqualTo(1L);

        assertThat(service.findAll())
                .extracting(WorkOrderRegisterResponse::id)
                .containsExactly(10L);

        verify(mapper)
                .updateEntity(updateRequest, assignment);

        verify(assignments)
                .saveAndFlush(assignment);
    }

    @Test
    void shouldRejectUpdateWithStaleVersion() {
        WorkOrderRegister assignment =
                assignment(
                    10L,
                    2L,
                    "WO-100",
                    LocalDate.of(2026, 8, 20)
                );

        WorkOrderRegisterRequest staleRequest =
                request(
                    "WO-UPDATED",
                    LocalDate.of(2026, 8, 21),
                    1L
                );

        when(assignments.findById(10L))
                .thenReturn(Optional.of(assignment));

        assertThatThrownBy(() ->
            service.update(10L, staleRequest)
        )
            .isInstanceOf(StaleEntityException.class)
            .hasMessageContaining(
                "modified by another user"
            )
            .hasMessageContaining(
                "Current version is 2"
            )
            .hasMessageContaining(
                "requested version was 1"
            );

        verify(mapper, never())
                .updateEntity(
                    staleRequest,
                    assignment
                );

        verify(assignments, never())
                .saveAndFlush(assignment);

        verifyNoInteractions(
            employees,
            components,
            accessRights
        );
    }

    @Test
    void shouldRejectCreateWhenEmployeeIsMissing() {
        WorkOrderRegisterRequest request =
                request(
                    "WO-100",
                    LocalDate.of(2026, 8, 20),
                    null
                );

        when(employees.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
            service.create(request)
        )
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Employee not found: 1");

        verifyNoInteractions(
            components,
            accessRights,
            mapper
        );

        verify(assignments, never())
                .saveAndFlush(
                    org.mockito.ArgumentMatchers.any()
                );
    }

    @Test
    void shouldDeleteAssignment() {
        WorkOrderRegister assignment =
                assignment(
                    10L,
                    1L,
                    "WO-100",
                    LocalDate.of(2026, 8, 20)
                );

        when(assignments.findById(10L))
                .thenReturn(Optional.of(assignment));

        service.delete(10L);

        verify(assignments).delete(assignment);
    }

    @Test
    void shouldRejectDeleteWhenAssignmentIsMissing() {
        when(assignments.findById(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
            service.delete(99L)
        )
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Assignment not found: 99");

        verify(assignments, never())
                .delete(
                    org.mockito.ArgumentMatchers.any()
                );
    }

    private WorkOrderRegisterRequest request(
            String workOrderNumber,
            LocalDate workOrderDate,
            Long version
    ) {
        return new WorkOrderRegisterRequest(
            1L,
            2L,
            workOrderNumber,
            workOrderDate,
            3L,
            version
        );
    }

    private WorkOrderRegister assignment(
            Long id,
            Long version,
            String workOrderNumber,
            LocalDate workOrderDate
    ) {
        WorkOrderRegister assignment =
                new WorkOrderRegister();

        assignment.setId(id);
        assignment.setEmployee(employee);
        assignment.setComponent(component);
        assignment.setRight(accessRight);
        assignment.setWorkOrderNumber(workOrderNumber);
        assignment.setWorkOrderDate(workOrderDate);

        /*
         * VersionedEntity intentionally has no version setter.
         */
        ReflectionTestUtils.setField(
            assignment,
            "version",
            version
        );

        return assignment;
    }

    private Employee employee(Long id) {
        Employee employee = new Employee();

        employee.setId(id);
        employee.setFirstName("John");
        employee.setLastName("Smith");
        employee.setAmpNumber("AMP-100");
        employee.setHireDate(
            LocalDate.of(2025, 1, 10)
        );

        return employee;
    }

    private Component component(Long id) {
        Component component = new Component();

        component.setId(id);
        component.setName("ENGINE-01");
        component.setDescription("Test component");

        return component;
    }

    private AccessRight accessRight(Long id) {
        AccessRight accessRight = new AccessRight();

        accessRight.setId(id);
        accessRight.setName("INS");
        accessRight.setDescription("Inspector");

        return accessRight;
    }

    private WorkOrderRegisterResponse response(
            Long id,
            Long version,
            String workOrderNumber,
            LocalDate workOrderDate
    ) {
        return new WorkOrderRegisterResponse(
            id,
            version,
            employee.getId(),
            employee.getFirstName(),
            employee.getLastName(),
            employee.getAmpNumber(),
            component.getId(),
            component.getName(),
            workOrderNumber,
            workOrderDate,
            accessRight.getId(),
            accessRight.getName()
        );
    }
}