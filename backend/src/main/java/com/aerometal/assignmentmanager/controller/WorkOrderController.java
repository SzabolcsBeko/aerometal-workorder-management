package com.aerometal.assignmentmanager.controller;

import com.aerometal.assignmentmanager.dto.WorkOrderResponse;
import com.aerometal.assignmentmanager.dto.WorkOrderRequest;
import com.aerometal.assignmentmanager.service.WorkOrderService;

import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/assignments")
public class WorkOrderController {
	private final WorkOrderService service;

	public WorkOrderController(WorkOrderService service) {
		this.service = service;
	}

	@GetMapping
	public List<WorkOrderResponse> all() {
		return service.findAll();
	}

	@PostMapping
	public WorkOrderResponse create(@Valid @RequestBody WorkOrderRequest request) {
		return service.create(request);
	}
	
	@PutMapping("/{id}")
	public WorkOrderResponse update(@PathVariable Long id, @Valid @RequestBody WorkOrderRequest request) {
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
