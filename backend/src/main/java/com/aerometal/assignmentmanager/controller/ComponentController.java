package com.aerometal.assignmentmanager.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aerometal.assignmentmanager.dto.ComponentRequest;
import com.aerometal.assignmentmanager.dto.ComponentResponse;
import com.aerometal.assignmentmanager.entity.Component;
import com.aerometal.assignmentmanager.service.ComponentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/components")
public class ComponentController {
	private final ComponentService service;

	public ComponentController(ComponentService service) {
		this.service = service;
	}

	@GetMapping
	public List<ComponentResponse> all() {
		return service.findAll();
	}

	@GetMapping("/{id}")
	public ComponentResponse one(@PathVariable Long id) {
		return service.findById(id);
	}

	@PostMapping
	public ResponseEntity<ComponentResponse> create(@Valid @RequestBody ComponentRequest request) {
		ComponentResponse response = service.create(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@PutMapping("/{id}")
	public ComponentResponse update(@PathVariable Long id, @Valid @RequestBody ComponentRequest request) {
		return service.update(id, request);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id, @RequestParam Long version) {
		service.delete(id, version);
		return ResponseEntity.noContent().build();
	}
}
