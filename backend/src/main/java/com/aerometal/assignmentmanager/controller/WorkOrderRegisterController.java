package com.aerometal.assignmentmanager.controller;

import com.aerometal.assignmentmanager.dto.WorkOrderRegisterResponse;
import com.aerometal.assignmentmanager.dto.WorkOrderRegisterRequest;
import com.aerometal.assignmentmanager.service.WorkOrderRegisterService;

import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/assignments")
public class WorkOrderRegisterController {
	private final WorkOrderRegisterService service;

	public WorkOrderRegisterController(WorkOrderRegisterService service) {
		this.service = service;
	}

	@GetMapping
	public List<WorkOrderRegisterResponse> all() {
		return service.findAll();
	}

	@PostMapping
	public WorkOrderRegisterResponse create(@Valid @RequestBody WorkOrderRegisterRequest request) {
		return service.create(request);
	}
	
	@PutMapping("/{id}")
	public WorkOrderRegisterResponse update(@PathVariable Long id, @Valid @RequestBody WorkOrderRegisterRequest request) {
		return service.update(id, request);
	}

	@DeleteMapping("/{id}")
	public void delete(@PathVariable Long id) {
		service.delete(id);
	}

	@GetMapping("/export")
	public ResponseEntity<byte[]> export() {
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=workorder-management.xlsx")
				.contentType(
						MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
				.body(service.exportExcel());
	}
}
