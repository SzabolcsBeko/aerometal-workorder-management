package com.aerometal.assignmentmanager.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.aerometal.assignmentmanager.dto.WorkOrderRequest;
import com.aerometal.assignmentmanager.dto.WorkOrderResponse;
import com.aerometal.assignmentmanager.entity.*;
import com.aerometal.assignmentmanager.repository.*;

@ExtendWith(MockitoExtension.class)
class WorkOrderServiceUnitTest {

    @Mock WorkOrdertRepository assignments;
    @Mock EmployeeRepository employees;
    @Mock ComponentRepository components;
    @Mock AccessRightRepository accessRights;
    @InjectMocks WorkOrderService service;

    private Employee employee;
    private Component component;
    private AccessRight right;

    @BeforeEach
    void setUp() {
        employee = employee(1L);
        component = component(2L);
        right = right(3L);
    }

    @Test
    void shouldCreateAssignmentAndTrimWorkOrderNumber() {
        when(employees.findById(1L)).thenReturn(Optional.of(employee));
        when(components.findById(2L)).thenReturn(Optional.of(component));
        when(accessRights.findById(3L)).thenReturn(Optional.of(right));
        when(assignments.save(any())).thenAnswer(invocation -> {
            WorkOrderRegister saved = invocation.getArgument(0);
            saved.setId(10L);
            return saved;
        });

        WorkOrderResponse response = service.create(request("  WO-100  ", LocalDate.of(2026, 8, 20)));

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.workOrderNumber()).isEqualTo("WO-100");
        assertThat(response.employeeId()).isEqualTo(1L);
    }

    @Test
    void shouldUpdateAndListAssignments() {
        WorkOrderRegister assignment = assignment(10L, "WO-100", LocalDate.of(2026, 8, 20));
        when(assignments.findById(10L)).thenReturn(Optional.of(assignment));
        when(employees.findById(1L)).thenReturn(Optional.of(employee));
        when(components.findById(2L)).thenReturn(Optional.of(component));
        when(accessRights.findById(3L)).thenReturn(Optional.of(right));
        when(assignments.save(assignment)).thenReturn(assignment);
        when(assignments.findAllByOrderByIdAsc()).thenReturn(List.of(assignment));

        WorkOrderResponse updated = service.update(10L, request("WO-100", LocalDate.of(2026, 8, 21)));

        assertThat(updated.workOrderDate()).isEqualTo(LocalDate.of(2026, 8, 21));
        assertThat(service.findAll()).extracting(WorkOrderResponse::id).containsExactly(10L);
    }

    @Test
    void shouldRejectCreateWhenEmployeeIsMissing() {
        when(employees.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.create(request("WO-100", LocalDate.now())))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Employee not found");
        verifyNoInteractions(components, accessRights);
    }

    @Test
    void shouldDeleteAssignment() {
        service.delete(10L);
        verify(assignments).deleteById(10L);
    }

    private WorkOrderRequest request(String number, LocalDate date) {
        return new WorkOrderRequest(1L, 2L, number, date, 3L);
    }

    private WorkOrderRegister assignment(Long id, String number, LocalDate date) {
        WorkOrderRegister assignment = new WorkOrderRegister();
        assignment.setId(id);
        assignment.setEmployee(employee);
        assignment.setComponent(component);
        assignment.setRight(right);
        assignment.setWorkOrderNumber(number);
        assignment.setWorkOrderDate(date);
        return assignment;
    }

    private Employee employee(Long id) {
        Employee employee = new Employee();
        employee.setId(id);
        employee.setFirstName("John");
        employee.setLastName("Smith");
        employee.setAmpNumber("AMP-100");
        return employee;
    }

    private Component component(Long id) {
        Component component = new Component();
        component.setId(id);
        component.setName("ENGINE-01");
        return component;
    }

    private AccessRight right(Long id) {
        AccessRight right = new AccessRight();
        right.setId(id);
        right.setName("INS");
        return right;
    }
}
