package com.aerometal.assignmentmanager.controller;

import com.aerometal.assignmentmanager.dto.AccessRightRequest;
import com.aerometal.assignmentmanager.dto.AccessRightResponse;
import com.aerometal.assignmentmanager.entity.AccessRight;
import com.aerometal.assignmentmanager.service.AccesRightService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.*;

@RestController
@RequestMapping("/api/accessrights")
@RequiredArgsConstructor
public class AccessRightController {
	private final AccesRightService service;

	@GetMapping
	public ResponseEntity<List<AccessRightResponse>> all() {
		return ResponseEntity.ok(service.findAll());
	}

	@GetMapping("/{id}")
	public ResponseEntity<AccessRightResponse> one(@PathVariable Long id) {
		return ResponseEntity.ok(service.findById(id));
	}

	@PostMapping
	public ResponseEntity<AccessRightResponse> create(@Valid @RequestBody AccessRightRequest request) {

		AccessRightResponse created = service.create(request);

		URI location = URI.create("/api/accessrights/" + created.id());

		return ResponseEntity.created(location).body(created);
	}

	@PutMapping("/{id}")
	public ResponseEntity<AccessRightResponse> update(@PathVariable Long id,
			@Valid @RequestBody AccessRightRequest request) {
		return ResponseEntity.ok(service.update(id, request));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		service.delete(id);
		return ResponseEntity.noContent().build();
	}
}