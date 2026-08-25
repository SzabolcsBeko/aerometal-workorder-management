package com.aerometal.assignmentmanager.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.aerometal.assignmentmanager.dto.EmployeeRequest;
import com.aerometal.assignmentmanager.dto.EmployeeResponse;
import com.aerometal.assignmentmanager.dto.WorkOrderRequest;
import com.aerometal.assignmentmanager.dto.WorkOrderResponse;
import com.aerometal.assignmentmanager.entity.AccessRight;
import com.aerometal.assignmentmanager.entity.Component;
import com.aerometal.assignmentmanager.entity.Employee;
import com.aerometal.assignmentmanager.repository.AccessRightRepository;
import com.aerometal.assignmentmanager.repository.ComponentRepository;
import com.aerometal.assignmentmanager.repository.EmployeeRepository;
import com.aerometal.assignmentmanager.repository.WorkOrdertRepository;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ServiceMySqlIntegrationTest {

    @Autowired EmployeeService employeeService;
    @Autowired ComponentService componentService;
    @Autowired AccesRightService accessRightService;
    @Autowired WorkOrderService workOrderService;
    @Autowired EmployeeRepository employeeRepository;
    @Autowired ComponentRepository componentRepository;
    @Autowired AccessRightRepository accessRightRepository;
    @Autowired WorkOrdertRepository workOrderRepository;

    @Test
    void employeeServiceShouldPerformCrudAgainstMySql() {
        String suffix = suffix();
        EmployeeRequest create = new EmployeeRequest(
                "Test", "Employee", "AMP-" + suffix, LocalDate.of(2025, 1, 10));

        EmployeeResponse saved = employeeService.create(create);
        assertThat(employeeService.findResponseById(saved.id()).ampNumber()).isEqualTo("AMP-" + suffix);

        EmployeeRequest update = new EmployeeRequest(
                "Updated", "Employee", "AMP-" + suffix, LocalDate.of(2025, 2, 10));
        assertThat(employeeService.update(saved.id(), update).firstName()).isEqualTo("Updated");

        employeeService.delete(saved.id());
        assertThat(employeeRepository.existsById(saved.id())).isFalse();
    }

    @Test
    void componentServiceShouldPerformCrudAgainstMySql() {
        Component component = component("COMP-" + suffix());
        Component saved = componentService.create(component);

        assertThat(componentService.findById(saved.getId()).getName()).isEqualTo(saved.getName());
        saved.setDescription("Updated description");
        assertThat(componentService.update(saved.getId(), saved).getDescription())
                .isEqualTo("Updated description");

        componentService.delete(saved.getId());
        assertThat(componentRepository.existsById(saved.getId())).isFalse();
    }

    @Test
    void accessRightServiceShouldPerformCrudAgainstMySql() {
        AccessRight right = right("RIGHT-" + suffix());
        AccessRight saved = accessRightService.create(right);

        assertThat(accessRightService.findById(saved.getId()).getName()).isEqualTo(saved.getName());
        saved.setDescription("Updated description");
        assertThat(accessRightService.update(saved.getId(), saved).getDescription())
                .isEqualTo("Updated description");

        accessRightService.delete(saved.getId());
        assertThat(accessRightRepository.existsById(saved.getId())).isFalse();
    }

    @Test
    void workOrderServiceShouldPerformCrudAgainstMySql() {
        String suffix = suffix();
        Employee employee = employeeRepository.save(employee("AMP-" + suffix));
        Component component = componentRepository.save(component("COMP-" + suffix));
        AccessRight right = accessRightRepository.save(right("RIGHT-" + suffix));

        WorkOrderRequest create = new WorkOrderRequest(
                employee.getId(), component.getId(), "WO-" + suffix,
                LocalDate.of(2026, 8, 20), right.getId());
        WorkOrderResponse saved = workOrderService.create(create);

        assertThat(saved.workOrderNumber()).isEqualTo("WO-" + suffix);
        assertThat(workOrderService.findAll()).extracting(WorkOrderResponse::id).contains(saved.id());

        WorkOrderRequest update = new WorkOrderRequest(
                employee.getId(), component.getId(), "WO-" + suffix,
                LocalDate.of(2026, 8, 21), right.getId());
        assertThat(workOrderService.update(saved.id(), update).workOrderDate())
                .isEqualTo(LocalDate.of(2026, 8, 21));

        workOrderService.delete(saved.id());
        assertThat(workOrderRepository.existsById(saved.id())).isFalse();
    }

    private String suffix() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private Employee employee(String ampNumber) {
        Employee employee = new Employee();
        employee.setFirstName("Test");
        employee.setLastName("Employee");
        employee.setAmpNumber(ampNumber);
        employee.setHireDate(LocalDate.of(2025, 1, 10));
        return employee;
    }

    private Component component(String name) {
        Component component = new Component();
        component.setName(name);
        component.setDescription("Test component");
        return component;
    }

    private AccessRight right(String name) {
        AccessRight right = new AccessRight();
        right.setName(name);
        right.setDescription("Test right");
        return right;
    }
}
