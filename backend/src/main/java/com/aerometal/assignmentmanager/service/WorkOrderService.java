package com.aerometal.assignmentmanager.service;

import com.aerometal.assignmentmanager.dto.*;
import com.aerometal.assignmentmanager.entity.*;
import com.aerometal.assignmentmanager.repository.*;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.*;
import java.util.*;

@Service
public class WorkOrderService {
	private final WorkOrdertRepository assignments;
	private final EmployeeRepository employees;
	private final ComponentRepository components;
	private final AccessRightRepository accessRights;

	public WorkOrderService(WorkOrdertRepository assignments, EmployeeRepository employees,
			ComponentRepository components, AccessRightRepository accessRights) {
		this.assignments = assignments;
		this.employees = employees;
		this.components = components;
		this.accessRights = accessRights;
	}

	@Transactional(readOnly = true)
	public List<WorkOrderResponse> findAll() {
		return assignments.findAllByOrderByIdAsc().stream().map(this::toDto).toList();
	}

	@Transactional
	public WorkOrderResponse create(WorkOrderRequest r) {
		/*if (assignments.existsByEmployeeIdAndComponentIdAndRightId(r.employeeId(), r.componentId(), r.accessRightId()))
			throw new IllegalArgumentException("This employee/component/right assignment already exists.");*/
		Employee e = employees.findById(r.employeeId())
				.orElseThrow(() -> new IllegalArgumentException("Employee not found"));
		Component c = components.findById(r.componentId())
				.orElseThrow(() -> new IllegalArgumentException("Component not found"));
		AccessRight right = accessRights.findById(r.accessRightId())
				.orElseThrow(() -> new IllegalArgumentException("Right not found"));
		WorkOrderRegister a = new WorkOrderRegister();
		apply(a, r, e, c, right);
		return toDto(assignments.save(a));
	}
	
	@Transactional
	public WorkOrderResponse update(Long id, WorkOrderRequest r) {
		WorkOrderRegister a = assignments.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Assignment not found: " + id));
		Employee e = employees.findById(r.employeeId())
				.orElseThrow(() -> new IllegalArgumentException("Employee not found"));
		Component c = components.findById(r.componentId())
				.orElseThrow(() -> new IllegalArgumentException("Component not found"));
		AccessRight right = accessRights.findById(r.accessRightId())
				.orElseThrow(() -> new IllegalArgumentException("Right not found"));
		apply(a, r, e, c, right);
		return toDto(assignments.save(a));
	}

	@Transactional
	public void delete(Long id) {
		assignments.deleteById(id);
	}

	@Transactional(readOnly = true)
	public byte[] exportExcel() {
		List<WorkOrderResponse> rows = findAll();
		try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			Sheet sheet = wb.createSheet("Assignments");
			Row h = sheet.createRow(0);
			String[] heads = { "ID", "Component", "Employee First Name", "Employee Last Name", "AmpNumber",
					"Workorder Number", "Workorder Date", "Right" };
			for (int i = 0; i < heads.length; i++)
				h.createCell(i).setCellValue(heads[i]);
			int rn = 1;
			for (WorkOrderResponse a : rows) {
				Row row = sheet.createRow(rn++);
				row.createCell(0).setCellValue(a.id());
				row.createCell(1).setCellValue(a.componentName());
				row.createCell(2).setCellValue(a.employeeFirstName());
				row.createCell(3).setCellValue(a.employeeLastName());
				row.createCell(4).setCellValue(a.employeeAmpNumber());
				row.createCell(5).setCellValue(a.workOrderNumber());
				row.createCell(6).setCellValue(a.workOrderDate().toString());
				row.createCell(7).setCellValue(a.rightName());
			}
			for (int i = 0; i < heads.length; i++)
				sheet.autoSizeColumn(i);
			wb.write(out);
			return out.toByteArray();
		} catch (IOException ex) {
			throw new IllegalStateException("Could not create Excel export", ex);
		}
	}
	
	private void apply(WorkOrderRegister assignment, WorkOrderRequest request, Employee employee,
			Component component, AccessRight right) {
		assignment.setEmployee(employee);
		assignment.setComponent(component);
		assignment.setWorkOrderNumber(request.workOrderNumber().trim());
		assignment.setWorkOrderDate(request.workOrderDate());
		assignment.setRight(right);
	}

	private WorkOrderResponse toDto(WorkOrderRegister a) {
		return new WorkOrderResponse(
				a.getId(), 
				a.getEmployee().getId(), 
				a.getEmployee().getFirstName(),
				a.getEmployee().getLastName(), 
				a.getEmployee().getAmpNumber(), 
				a.getComponent().getId(),
				a.getComponent().getName(), 
				a.getWorkOrderNumber(),
				a.getWorkOrderDate(),
				a.getRight().getId(), 
				a.getRight().getName());
	}
}
