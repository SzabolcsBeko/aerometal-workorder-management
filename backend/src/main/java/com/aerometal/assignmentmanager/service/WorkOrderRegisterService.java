package com.aerometal.assignmentmanager.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

@Service
public class WorkOrderRegisterService {

    private final WorkOrderRegisterRepository assignments;
    private final EmployeeRepository employees;
    private final ComponentRepository components;
    private final AccessRightRepository accessRights;
    private final WorkOrderRegisterMapper mapper;

    public WorkOrderRegisterService(
            WorkOrderRegisterRepository assignments,
            EmployeeRepository employees,
            ComponentRepository components,
            AccessRightRepository accessRights,
            WorkOrderRegisterMapper mapper
    ) {
        this.assignments = assignments;
        this.employees = employees;
        this.components = components;
        this.accessRights = accessRights;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<WorkOrderRegisterResponse> findAll() {
        return assignments.findAllByOrderByIdAsc()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public WorkOrderRegisterResponse findById(Long id) {
        WorkOrderRegister assignment = findAssignment(id);
        return mapper.toResponse(assignment);
    }

    @Transactional
    public WorkOrderRegisterResponse create(
            WorkOrderRegisterRequest request
    ) {
        Employee employee =
                findEmployee(request.employeeId());

        Component component =
                findComponent(request.componentId());

        AccessRight accessRight =
                findAccessRight(request.accessRightId());

        WorkOrderRegister assignment =
                mapper.toEntity(request);

        assignment.setEmployee(employee);
        assignment.setComponent(component);
        assignment.setRight(accessRight);

        WorkOrderRegister saved =
                assignments.saveAndFlush(assignment);

        return mapper.toResponse(saved);
    }

    @Transactional
    public WorkOrderRegisterResponse update(
            Long id,
            WorkOrderRegisterRequest request
    ) {
        WorkOrderRegister assignment = findAssignment(id);

        validateVersion(
            assignment,
            request.version()
        );

        Employee employee =
                findEmployee(request.employeeId());

        Component component =
                findComponent(request.componentId());

        AccessRight accessRight =
                findAccessRight(request.accessRightId());

        /*
         * Updates only workOrderNumber and workOrderDate.
         * The mapper ignores id, version and relationships.
         */
        mapper.updateEntity(request, assignment);

        assignment.setEmployee(employee);
        assignment.setComponent(component);
        assignment.setRight(accessRight);

        /*
         * Executes the SQL update before the method returns.
         * Hibernate includes the previous version in the WHERE clause.
         */
        WorkOrderRegister saved =
                assignments.saveAndFlush(assignment);

        return mapper.toResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        WorkOrderRegister assignment = findAssignment(id);
        assignments.delete(assignment);
    }

    @Transactional(readOnly = true)
    public byte[] exportExcel() {
        List<WorkOrderRegisterResponse> rows = findAll();

        try (
            Workbook workbook = new XSSFWorkbook();
            ByteArrayOutputStream output =
                    new ByteArrayOutputStream()
        ) {
            Sheet sheet = workbook.createSheet("Assignments");
            Row header = sheet.createRow(0);

            String[] columnNames = {
                "ID",
                "Component",
                "Employee First Name",
                "Employee Last Name",
                "AmpNumber",
                "Workorder Number",
                "Workorder Date",
                "Right"
            };

            for (int i = 0; i < columnNames.length; i++) {
                header.createCell(i)
                        .setCellValue(columnNames[i]);
            }

            int rowNumber = 1;

            for (WorkOrderRegisterResponse assignment : rows) {
                Row row = sheet.createRow(rowNumber++);

                row.createCell(0)
                        .setCellValue(assignment.id());

                row.createCell(1)
                        .setCellValue(assignment.componentName());

                row.createCell(2)
                        .setCellValue(
                            assignment.employeeFirstName()
                        );

                row.createCell(3)
                        .setCellValue(
                            assignment.employeeLastName()
                        );

                row.createCell(4)
                        .setCellValue(
                            assignment.employeeAmpNumber()
                        );

                row.createCell(5)
                        .setCellValue(
                            assignment.workOrderNumber()
                        );

                row.createCell(6)
                        .setCellValue(
                            assignment.workOrderDate().toString()
                        );

                row.createCell(7)
                        .setCellValue(assignment.rightName());
            }

            for (int i = 0; i < columnNames.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(output);
            return output.toByteArray();

        } catch (IOException exception) {
            throw new IllegalStateException(
                "Could not create Excel export",
                exception
            );
        }
    }

    private WorkOrderRegister findAssignment(Long id) {
        return assignments.findById(id)
                .orElseThrow(() ->
                    new IllegalArgumentException(
                        "Assignment not found: " + id
                    )
                );
    }

    private Employee findEmployee(Long id) {
        return employees.findById(id)
                .orElseThrow(() ->
                    new IllegalArgumentException(
                        "Employee not found: " + id
                    )
                );
    }

    private Component findComponent(Long id) {
        return components.findById(id)
                .orElseThrow(() ->
                    new IllegalArgumentException(
                        "Component not found: " + id
                    )
                );
    }

    private AccessRight findAccessRight(Long id) {
        return accessRights.findById(id)
                .orElseThrow(() ->
                    new IllegalArgumentException(
                        "AccessRight not found: " + id
                    )
                );
    }

    private void validateVersion(
            WorkOrderRegister assignment,
            Long requestedVersion
    ) {
        if (requestedVersion == null) {
            throw new IllegalArgumentException(
                "Version is required when updating "
                    + "WorkOrderRegister"
            );
        }

        if (!Objects.equals(
                assignment.getVersion(),
                requestedVersion
        )) {
            throw new StaleEntityException(
                "WorkOrderRegister with id "
                    + assignment.getId()
                    + " was modified by another user. "
                    + "Current version is "
                    + assignment.getVersion()
                    + ", but the requested version was "
                    + requestedVersion
            );
        }
    }
}