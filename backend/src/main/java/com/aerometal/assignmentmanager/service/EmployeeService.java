package com.aerometal.assignmentmanager.service;

import com.aerometal.assignmentmanager.dto.EmployeeRequest;
import com.aerometal.assignmentmanager.dto.EmployeeResponse;
import com.aerometal.assignmentmanager.entity.Employee;
import com.aerometal.assignmentmanager.mapper.EmployeeMapper;
import com.aerometal.assignmentmanager.repository.EmployeeRepository;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EmployeeService {
	
    private final EmployeeRepository repository;
    private final EmployeeMapper employeeMapper;

    
    public EmployeeService(EmployeeRepository repository,
    		EmployeeMapper employeeMapper) {
        this.repository = repository;
        this.employeeMapper = employeeMapper;
    }

    @Transactional(readOnly = true)
    public List<EmployeeResponse> findAll() {
    	return repository.findAll().stream().map(employeeMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public EmployeeResponse findResponseById(Long id) {
    	Employee employee = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Employee not found: " + id));
    	return employeeMapper.toResponse(employee);
    }

    @Transactional(readOnly = true)
    public Employee findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found: " + id));
    }

    @Transactional
    public EmployeeResponse create(EmployeeRequest request) {
    	Employee employee = employeeMapper.toEntity(request);
    	Employee saved = repository.save(employee);
    	return employeeMapper.toResponse(saved);
    }

    @Transactional
    public EmployeeResponse update(Long id, EmployeeRequest request) {
    	 Employee employee = repository.findById(id)
                 .orElseThrow(() ->
                         new EntityNotFoundException(
                                 "Employee not found: " + id));
         employeeMapper.updateEntity(request, employee);
         Employee saved = repository.save(employee);
         return employeeMapper.toResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
    	if (!repository.existsById(id)) {
            throw new EntityNotFoundException(
                    "Employee not found: " + id);
        }

        repository.deleteById(id);
    }
}
