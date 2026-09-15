package com.aerometal.assignmentmanager.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.aerometal.assignmentmanager.dto.AccessRightRequest;
import com.aerometal.assignmentmanager.dto.AccessRightResponse;
import com.aerometal.assignmentmanager.dto.ComponentRequest;
import com.aerometal.assignmentmanager.dto.ComponentResponse;
import com.aerometal.assignmentmanager.dto.EmployeeRequest;
import com.aerometal.assignmentmanager.dto.EmployeeResponse;
import com.aerometal.assignmentmanager.dto.WorkOrderRegisterRequest;
import com.aerometal.assignmentmanager.dto.WorkOrderRegisterResponse;
import com.aerometal.assignmentmanager.entity.AccessRight;
import com.aerometal.assignmentmanager.entity.Component;
import com.aerometal.assignmentmanager.entity.Employee;
import com.aerometal.assignmentmanager.exception.StaleEntityException;
import com.aerometal.assignmentmanager.repository.AccessRightRepository;
import com.aerometal.assignmentmanager.repository.ComponentRepository;
import com.aerometal.assignmentmanager.repository.EmployeeRepository;
import com.aerometal.assignmentmanager.repository.WorkOrderRegisterRepository;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ServiceMySqlIntegrationTest {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private ComponentService componentService;

    @Autowired
    private AccesRightService accessRightService;

    @Autowired
    private WorkOrderRegisterService workOrderService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ComponentRepository componentRepository;

    @Autowired
    private AccessRightRepository accessRightRepository;

    @Autowired
    private WorkOrderRegisterRepository workOrderRepository;

    @Test
    void employeeServiceShouldPerformCrudAgainstMySql() {
        String uniqueSuffix = suffix();

        EmployeeRequest createRequest =
                new EmployeeRequest(
                    null,
                    "Test",
                    "Employee",
                    "AMP-" + uniqueSuffix,
                    LocalDate.of(2025, 1, 10)
                );

        EmployeeResponse created =
                employeeService.create(createRequest);

        assertThat(created.id()).isNotNull();
        assertThat(created.version()).isNotNull();
        assertThat(created.firstName()).isEqualTo("Test");
        assertThat(created.ampNumber())
                .isEqualTo("AMP-" + uniqueSuffix);

        EmployeeResponse found =
                employeeService.findResponseById(created.id());

        assertThat(found).isEqualTo(created);

        EmployeeRequest updateRequest =
                new EmployeeRequest(
                    created.version(),
                    "Updated",
                    "Employee",
                    "AMP-" + uniqueSuffix,
                    LocalDate.of(2025, 2, 10)
                );

        EmployeeResponse updated =
                employeeService.update(
                    created.id(),
                    updateRequest
                );

        assertThat(updated.firstName())
                .isEqualTo("Updated");

        assertThat(updated.hireDate())
                .isEqualTo(LocalDate.of(2025, 2, 10));

        assertThat(updated.version())
                .isGreaterThan(created.version());

        employeeService.delete(created.id());

        assertThat(
            employeeRepository.existsById(created.id())
        ).isFalse();
    }

    @Test
    void componentServiceShouldPerformCrudAgainstMySql() {
        String componentName = "COMP-" + suffix();

        ComponentRequest createRequest =
                new ComponentRequest(
                    null,
                    componentName,
                    "Test component"
                );

        ComponentResponse created =
                componentService.create(createRequest);

        assertThat(created.id()).isNotNull();
        assertThat(created.version()).isNotNull();
        assertThat(created.name()).isEqualTo(componentName);

        ComponentResponse found =
                componentService.findById(created.id());

        assertThat(found).isEqualTo(created);

        ComponentRequest updateRequest =
                new ComponentRequest(
                    created.version(),
                    componentName,
                    "Updated description"
                );

        ComponentResponse updated =
                componentService.update(
                    created.id(),
                    updateRequest
                );

        assertThat(updated.description())
                .isEqualTo("Updated description");

        assertThat(updated.version())
                .isGreaterThan(created.version());

        /*
         * Component deletion requires the current version.
         */
        componentService.delete(
            updated.id(),
            updated.version()
        );

        assertThat(
            componentRepository.existsById(updated.id())
        ).isFalse();
    }

    @Test
    void accessRightServiceShouldPerformCrudAgainstMySql() {
        String rightName = "RIGHT-" + suffix();

        AccessRightRequest createRequest =
                new AccessRightRequest(
                    rightName,
                    "Test right",
                    null
                );

        AccessRightResponse created =
                accessRightService.create(createRequest);

        assertThat(created.id()).isNotNull();
        assertThat(created.version()).isNotNull();
        assertThat(created.name()).isEqualTo(rightName);

        AccessRightResponse found =
                accessRightService.findById(created.id());

        assertThat(found).isEqualTo(created);

        AccessRightRequest updateRequest =
                new AccessRightRequest(
                    rightName,
                    "Updated description",
                    created.version()
                );

        AccessRightResponse updated =
                accessRightService.update(
                    created.id(),
                    updateRequest
                );

        assertThat(updated.description())
                .isEqualTo("Updated description");

        assertThat(updated.version())
                .isGreaterThan(created.version());

        accessRightService.delete(updated.id());

        assertThat(
            accessRightRepository.existsById(updated.id())
        ).isFalse();
    }

    @Test
    void workOrderServiceShouldPerformCrudAgainstMySql() {
        String uniqueSuffix = suffix();

        Employee employee = employeeRepository.saveAndFlush(
            employee("AMP-" + uniqueSuffix)
        );

        Component component = componentRepository.saveAndFlush(
            component("COMP-" + uniqueSuffix)
        );

        AccessRight accessRight =
                accessRightRepository.saveAndFlush(
                    accessRight("RIGHT-" + uniqueSuffix)
                );

        WorkOrderRegisterRequest createRequest =
                new WorkOrderRegisterRequest(
                    employee.getId(),
                    component.getId(),
                    "WO-" + uniqueSuffix,
                    LocalDate.of(2026, 8, 20),
                    accessRight.getId(),
                    null
                );

        WorkOrderRegisterResponse created =
                workOrderService.create(createRequest);

        assertThat(created.id()).isNotNull();
        assertThat(created.version()).isNotNull();

        assertThat(created.workOrderNumber())
                .isEqualTo("WO-" + uniqueSuffix);

        assertThat(created.employeeId())
                .isEqualTo(employee.getId());

        assertThat(created.componentId())
                .isEqualTo(component.getId());

        assertThat(created.accessRightId())
                .isEqualTo(accessRight.getId());

        WorkOrderRegisterResponse found =
                workOrderService.findById(created.id());

        assertThat(found).isEqualTo(created);

        WorkOrderRegisterRequest updateRequest =
                new WorkOrderRegisterRequest(
                    employee.getId(),
                    component.getId(),
                    "WO-" + uniqueSuffix,
                    LocalDate.of(2026, 8, 21),
                    accessRight.getId(),
                    created.version()
                );

        WorkOrderRegisterResponse updated =
                workOrderService.update(
                    created.id(),
                    updateRequest
                );

        assertThat(updated.workOrderDate())
                .isEqualTo(LocalDate.of(2026, 8, 21));

        assertThat(updated.version())
                .isGreaterThan(created.version());

        workOrderService.delete(updated.id());

        assertThat(
            workOrderRepository.existsById(updated.id())
        ).isFalse();
    }

    @Test
    void staleWorkOrderUpdateShouldBeRejected() {
        String uniqueSuffix = suffix();

        Employee employee = employeeRepository.saveAndFlush(
            employee("AMP-" + uniqueSuffix)
        );

        Component component = componentRepository.saveAndFlush(
            component("COMP-" + uniqueSuffix)
        );

        AccessRight accessRight =
                accessRightRepository.saveAndFlush(
                    accessRight("RIGHT-" + uniqueSuffix)
                );

        WorkOrderRegisterRequest createRequest =
                new WorkOrderRegisterRequest(
                    employee.getId(),
                    component.getId(),
                    "WO-" + uniqueSuffix,
                    LocalDate.of(2026, 8, 20),
                    accessRight.getId(),
                    null
                );

        WorkOrderRegisterResponse created =
                workOrderService.create(createRequest);

        /*
         * Both clients initially receive the same version.
         */
        Long originalVersion = created.version();

        WorkOrderRegisterRequest firstClientRequest =
                new WorkOrderRegisterRequest(
                    employee.getId(),
                    component.getId(),
                    "WO-FIRST-" + uniqueSuffix,
                    LocalDate.of(2026, 8, 21),
                    accessRight.getId(),
                    originalVersion
                );

        WorkOrderRegisterResponse firstUpdate =
                workOrderService.update(
                    created.id(),
                    firstClientRequest
                );

        assertThat(firstUpdate.version())
                .isGreaterThan(originalVersion);

        /*
         * The second client still submits the original version.
         */
        WorkOrderRegisterRequest staleSecondClientRequest =
                new WorkOrderRegisterRequest(
                    employee.getId(),
                    component.getId(),
                    "WO-SECOND-" + uniqueSuffix,
                    LocalDate.of(2026, 8, 22),
                    accessRight.getId(),
                    originalVersion
                );

        assertThatThrownBy(() ->
            workOrderService.update(
                created.id(),
                staleSecondClientRequest
            )
        )
            .isInstanceOf(StaleEntityException.class)
            .hasMessageContaining(
                "modified by another user"
            );

        /*
         * Verify that the first client's values remain unchanged.
         */
        WorkOrderRegisterResponse current =
                workOrderService.findById(created.id());

        assertThat(current.workOrderNumber())
                .isEqualTo("WO-FIRST-" + uniqueSuffix);

        assertThat(current.workOrderDate())
                .isEqualTo(LocalDate.of(2026, 8, 21));

        assertThat(current.version())
                .isEqualTo(firstUpdate.version());
    }

    private String suffix() {
        return UUID.randomUUID()
                .toString()
                .substring(0, 8);
    }

    private Employee employee(String ampNumber) {
        Employee employee = new Employee();

        employee.setFirstName("Test");
        employee.setLastName("Employee");
        employee.setAmpNumber(ampNumber);
        employee.setHireDate(
            LocalDate.of(2025, 1, 10)
        );

        return employee;
    }

    private Component component(String name) {
        Component component = new Component();

        component.setName(name);
        component.setDescription("Test component");

        return component;
    }

    private AccessRight accessRight(String name) {
        AccessRight accessRight = new AccessRight();

        accessRight.setName(name);
        accessRight.setDescription("Test right");

        return accessRight;
    }
}