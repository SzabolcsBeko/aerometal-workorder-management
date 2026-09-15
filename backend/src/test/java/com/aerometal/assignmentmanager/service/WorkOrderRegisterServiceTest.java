package com.aerometal.assignmentmanager.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class WorkOrderRegisterServiceTest {

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

    private WorkOrderRegisterService service;

    @BeforeEach
    void setUp() {
        service = new WorkOrderRegisterService(
            assignments,
            employees,
            components,
            accessRights,
            mapper
        );
    }

    @Test
    void shouldCreateWorkOrderRegister() {
        WorkOrderRegisterRequest request =
                request("WO-2026-001", null);

        Employee employee = employee();
        Component component = component();
        AccessRight accessRight = accessRight();

        WorkOrderRegister assignment =
                assignment(10L, 0L);

        WorkOrderRegisterResponse response =
                response(
                    10L,
                    0L,
                    "WO-2026-001",
                    LocalDate.of(2026, 9, 15)
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

        assertThat(assignment.getEmployee())
                .isSameAs(employee);

        assertThat(assignment.getComponent())
                .isSameAs(component);

        assertThat(assignment.getRight())
                .isSameAs(accessRight);

        verify(mapper).toEntity(request);
        verify(assignments).saveAndFlush(assignment);
        verify(mapper).toResponse(assignment);
    }

    @Test
    void shouldFindAllWorkOrderRegisters() {
        WorkOrderRegister first =
                assignment(10L, 0L);

        WorkOrderRegister second =
                assignment(11L, 0L);

        WorkOrderRegisterResponse firstResponse =
                response(
                    10L,
                    0L,
                    "WO-2026-001",
                    LocalDate.of(2026, 9, 15)
                );

        WorkOrderRegisterResponse secondResponse =
                response(
                    11L,
                    0L,
                    "WO-2026-002",
                    LocalDate.of(2026, 9, 16)
                );

        when(assignments.findAllByOrderByIdAsc())
                .thenReturn(List.of(first, second));

        when(mapper.toResponse(first))
                .thenReturn(firstResponse);

        when(mapper.toResponse(second))
                .thenReturn(secondResponse);

        assertThat(service.findAll())
                .containsExactly(
                    firstResponse,
                    secondResponse
                );
    }

    @Test
    void shouldFindWorkOrderRegisterById() {
        WorkOrderRegister assignment =
                assignment(10L, 0L);

        WorkOrderRegisterResponse response =
                response(
                    10L,
                    0L,
                    "WO-2026-001",
                    LocalDate.of(2026, 9, 15)
                );

        when(assignments.findById(10L))
                .thenReturn(Optional.of(assignment));

        when(mapper.toResponse(assignment))
                .thenReturn(response);

        assertThat(service.findById(10L))
                .isEqualTo(response);

        verify(mapper).toResponse(assignment);
    }

    @Test
    void shouldUpdateWithCurrentVersion() {
        WorkOrderRegisterRequest request =
                request("WO-2026-UPDATED", 0L);

        Employee employee = employee();
        Component component = component();
        AccessRight accessRight = accessRight();

        WorkOrderRegister assignment =
                assignment(10L, 0L);

        WorkOrderRegisterResponse updatedResponse =
                response(
                    10L,
                    1L,
                    "WO-2026-UPDATED",
                    LocalDate.of(2026, 9, 15)
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

        WorkOrderRegisterResponse result =
                service.update(10L, request);

        assertThat(result).isEqualTo(updatedResponse);
        assertThat(result.version()).isEqualTo(1L);

        assertThat(assignment.getEmployee())
                .isSameAs(employee);

        assertThat(assignment.getComponent())
                .isSameAs(component);

        assertThat(assignment.getRight())
                .isSameAs(accessRight);

        verify(mapper)
                .updateEntity(request, assignment);

        verify(assignments)
                .saveAndFlush(assignment);
    }

    @Test
    void shouldRejectUpdateWithStaleVersion() {
        /*
         * The database entity has version 2, but the request
         * contains the outdated version 1.
         */
        WorkOrderRegister assignment =
                assignment(10L, 2L);

        WorkOrderRegisterRequest staleRequest =
                request("WO-2026-STALE", 1L);

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

        /*
         * A stale request must be stopped before relationship
         * resolution, entity mapping or database writes.
         */
        verify(employees, never()).findById(1L);
        verify(components, never()).findById(2L);
        verify(accessRights, never()).findById(3L);

        verify(mapper, never())
                .updateEntity(staleRequest, assignment);

        verify(assignments, never())
                .saveAndFlush(assignment);
    }

    @Test
    void shouldRejectUpdateWithoutVersion() {
        WorkOrderRegister assignment =
                assignment(10L, 2L);

        WorkOrderRegisterRequest requestWithoutVersion =
                request("WO-2026-UPDATED", null);

        when(assignments.findById(10L))
                .thenReturn(Optional.of(assignment));

        assertThatThrownBy(() ->
            service.update(
                10L,
                requestWithoutVersion
            )
        )
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage(
                "Version is required when updating "
                    + "WorkOrderRegister"
            );

        verify(mapper, never())
                .updateEntity(
                    requestWithoutVersion,
                    assignment
                );

        verify(assignments, never())
                .saveAndFlush(assignment);
    }

    @Test
    void secondClientWithOldVersionShouldBeRejected() {
        WorkOrderRegister assignment =
                assignment(10L, 0L);

        WorkOrderRegisterRequest firstClientRequest =
                request("WO-FIRST", 0L);

        WorkOrderRegisterRequest secondClientRequest =
                request("WO-SECOND", 0L);

        Employee employee = employee();
        Component component = component();
        AccessRight accessRight = accessRight();

        WorkOrderRegisterResponse firstResponse =
                response(
                    10L,
                    1L,
                    "WO-FIRST",
                    LocalDate.of(2026, 9, 15)
                );

        /*
         * First update reads version 0.
         * Second update reads the incremented version 1.
         */
        ReflectionTestUtils.setField(
            assignment,
            "version",
            0L
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
                .thenAnswer(invocation -> {
                    ReflectionTestUtils.setField(
                        assignment,
                        "version",
                        1L
                    );

                    return assignment;
                });

        when(mapper.toResponse(assignment))
                .thenReturn(firstResponse);

        WorkOrderRegisterResponse firstResult =
                service.update(
                    10L,
                    firstClientRequest
                );

        assertThat(firstResult.version()).isEqualTo(1L);

        /*
         * The second client still submits version 0.
         */
        assertThatThrownBy(() ->
            service.update(
                10L,
                secondClientRequest
            )
        )
            .isInstanceOf(StaleEntityException.class)
            .hasMessageContaining(
                "Current version is 1"
            )
            .hasMessageContaining(
                "requested version was 0"
            );

        verify(mapper)
                .updateEntity(
                    firstClientRequest,
                    assignment
                );

        verify(mapper, never())
                .updateEntity(
                    secondClientRequest,
                    assignment
                );
    }

    @Test
    void shouldRejectCreateWhenEmployeeDoesNotExist() {
        WorkOrderRegisterRequest request =
                request("WO-2026-001", null);

        when(employees.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
            service.create(request)
        )
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Employee not found: 1");

        verify(mapper, never()).toEntity(request);
        verify(assignments, never())
                .saveAndFlush(
                    org.mockito.ArgumentMatchers.any()
                );
    }

    @Test
    void shouldRejectCreateWhenComponentDoesNotExist() {
        WorkOrderRegisterRequest request =
                request("WO-2026-001", null);

        when(employees.findById(1L))
                .thenReturn(Optional.of(employee()));

        when(components.findById(2L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
            service.create(request)
        )
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Component not found: 2");

        verify(mapper, never()).toEntity(request);
        verify(assignments, never())
                .saveAndFlush(
                    org.mockito.ArgumentMatchers.any()
                );
    }

    @Test
    void shouldRejectCreateWhenAccessRightDoesNotExist() {
        WorkOrderRegisterRequest request =
                request("WO-2026-001", null);

        when(employees.findById(1L))
                .thenReturn(Optional.of(employee()));

        when(components.findById(2L))
                .thenReturn(Optional.of(component()));

        when(accessRights.findById(3L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
            service.create(request)
        )
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("AccessRight not found: 3");

        verify(mapper, never()).toEntity(request);
        verify(assignments, never())
                .saveAndFlush(
                    org.mockito.ArgumentMatchers.any()
                );
    }

    @Test
    void shouldDeleteExistingWorkOrderRegister() {
        WorkOrderRegister assignment =
                assignment(10L, 1L);

        when(assignments.findById(10L))
                .thenReturn(Optional.of(assignment));

        service.delete(10L);

        verify(assignments).delete(assignment);
    }

    @Test
    void shouldRejectMissingWorkOrderRegister() {
        when(assignments.findById(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
            service.findById(99L)
        )
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Assignment not found: 99");
    }

    private WorkOrderRegisterRequest request(
            String workOrderNumber,
            Long version
    ) {
        return new WorkOrderRegisterRequest(
            1L,
            2L,
            workOrderNumber,
            LocalDate.of(2026, 9, 15),
            3L,
            version
        );
    }

    private WorkOrderRegister assignment(
            Long id,
            Long version
    ) {
        WorkOrderRegister assignment =
                new WorkOrderRegister();

        assignment.setId(id);
        assignment.setWorkOrderNumber("WO-2026-001");
        assignment.setWorkOrderDate(
            LocalDate.of(2026, 9, 15)
        );

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

    private Employee employee() {
        Employee employee = new Employee();

        employee.setId(1L);
        employee.setFirstName("John");
        employee.setLastName("Smith");
        employee.setAmpNumber("AMP-001");
        employee.setHireDate(
            LocalDate.of(2025, 1, 10)
        );

        return employee;
    }

    private Component component() {
        Component component = new Component();

        component.setId(2L);
        component.setName("Component A");
        component.setDescription("Test component");

        return component;
    }

    private AccessRight accessRight() {
        AccessRight accessRight = new AccessRight();

        accessRight.setId(3L);
        accessRight.setName("EDITOR");
        accessRight.setDescription("Editor access");

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
            1L,
            "John",
            "Smith",
            "AMP-001",
            2L,
            "Component A",
            workOrderNumber,
            workOrderDate,
            3L,
            "EDITOR"
        );
    }
}