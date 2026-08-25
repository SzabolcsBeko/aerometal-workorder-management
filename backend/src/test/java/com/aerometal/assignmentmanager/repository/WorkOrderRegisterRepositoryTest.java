package com.aerometal.assignmentmanager.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.aerometal.assignmentmanager.entity.AccessRight;
import com.aerometal.assignmentmanager.entity.Component;
import com.aerometal.assignmentmanager.entity.Employee;
import com.aerometal.assignmentmanager.entity.WorkOrderRegister;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(
    replace = AutoConfigureTestDatabase.Replace.NONE
)
class WorkOrderRegisterRepositoryTest {

    @Autowired
    private WorkOrdertRepository repository;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private ComponentRepository componentRepository;
    @Autowired
    private AccessRightRepository accessRightRepository;

    private Employee employee;
    private Component component;
    private AccessRight right;

    @BeforeEach
    void setUpRelationships() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        employee = employeeRepository.save(employee("Test", "Employee", "AMP-" + suffix));
        component = componentRepository.save(component("COMP-" + suffix));
        right = accessRightRepository.save(right("RIGHT-" + suffix));
    }

    @Test
    void shouldCreateReadUpdateAndDeleteWorkOrderRegister() {
        WorkOrderRegister saved = repository.saveAndFlush(
                assignment("WO-100", LocalDate.of(2026, 8, 20)));

        assertThat(saved.getId()).isNotNull();
        WorkOrderRegister found = repository.findById(saved.getId()).orElseThrow();
        assertThat(found.getEmployee().getId()).isEqualTo(employee.getId());
        assertThat(found.getComponent().getId()).isEqualTo(component.getId());
        assertThat(found.getRight().getId()).isEqualTo(right.getId());
        assertThat(found.getWorkOrderNumber()).isEqualTo("WO-100");
        assertThat(found.getWorkOrderDate()).isEqualTo(LocalDate.of(2026, 8, 20));

        found.setWorkOrderDate(LocalDate.of(2026, 8, 21));
        repository.saveAndFlush(found);
        assertThat(repository.findById(saved.getId()).orElseThrow().getWorkOrderDate())
                .isEqualTo(LocalDate.of(2026, 8, 21));

        repository.deleteById(saved.getId());
        repository.flush();
        assertThat(repository.findById(saved.getId())).isEmpty();
    }

    @Test
    void shouldFindExistingEmployeeComponentAndRightCombination() {
        repository.saveAndFlush(assignment("WO-100", LocalDate.of(2026, 8, 20)));

        assertThat(repository.existsByEmployeeIdAndComponentIdAndRightId(
                employee.getId(), component.getId(), right.getId())).isTrue();
        assertThat(repository.existsByEmployeeIdAndComponentIdAndRightId(
                employee.getId(), component.getId(), Long.MAX_VALUE)).isFalse();
    }

    @Test
    void shouldReturnAssignmentsOrderedByIdWithRelationshipsLoaded() {
        WorkOrderRegister first = repository.saveAndFlush(
                assignment("WO-100", LocalDate.of(2026, 8, 20)));
        WorkOrderRegister second = repository.saveAndFlush(
                assignment("WO-200", LocalDate.of(2026, 8, 21)));

        List<WorkOrderRegister> results = repository.findAllByOrderByIdAsc();

        assertThat(results).extracting(WorkOrderRegister::getId).isSorted();
        assertThat(results).extracting(WorkOrderRegister::getId)
                .contains(first.getId(), second.getId());

        WorkOrderRegister loadedFirst = results.stream()
                .filter(assignment -> assignment.getId().equals(first.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(loadedFirst.getEmployee().getId()).isEqualTo(employee.getId());
        assertThat(loadedFirst.getComponent().getId()).isEqualTo(component.getId());
        assertThat(loadedFirst.getRight().getId()).isEqualTo(right.getId());
    }

    private WorkOrderRegister assignment(String number, LocalDate date) {
        WorkOrderRegister assignment = new WorkOrderRegister();
        assignment.setEmployee(employee);
        assignment.setComponent(component);
        assignment.setRight(right);
        assignment.setWorkOrderNumber(number);
        assignment.setWorkOrderDate(date);
        return assignment;
    }

    private Employee employee(String firstName, String lastName, String ampNumber) {
        Employee employee = new Employee();
        employee.setFirstName(firstName);
        employee.setLastName(lastName);
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
