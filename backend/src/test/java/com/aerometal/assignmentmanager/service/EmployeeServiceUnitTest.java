package com.aerometal.assignmentmanager.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.aerometal.assignmentmanager.dto.EmployeeRequest;
import com.aerometal.assignmentmanager.dto.EmployeeResponse;
import com.aerometal.assignmentmanager.entity.Employee;
import com.aerometal.assignmentmanager.mapper.EmployeeMapper;
import com.aerometal.assignmentmanager.repository.EmployeeRepository;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceUnitTest {

    @Mock EmployeeRepository repository;
    @Mock EmployeeMapper mapper;
    @InjectMocks EmployeeService service;

    @Test
    void shouldCreateEmployee() {
        EmployeeRequest request = request("John");
        Employee entity = new Employee();
        EmployeeResponse response = response(1L, "John");
        when(mapper.toEntity(request)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toResponse(entity)).thenReturn(response);

        assertThat(service.create(request)).isEqualTo(response);
        verify(repository).save(entity);
    }

    @Test
    void shouldUpdateEmployee() {
        Employee employee = new Employee();
        EmployeeRequest request = request("Anna");
        EmployeeResponse response = response(1L, "Anna");
        when(repository.findById(1L)).thenReturn(Optional.of(employee));
        when(repository.save(employee)).thenReturn(employee);
        when(mapper.toResponse(employee)).thenReturn(response);

        assertThat(service.update(1L, request)).isEqualTo(response);
        verify(mapper).updateEntity(request, employee);
    }

    @Test
    void shouldRejectMissingEmployeeAndDeleteExistingEmployee() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(EntityNotFoundException.class);

        when(repository.existsById(1L)).thenReturn(true);
        service.delete(1L);
        verify(repository).deleteById(1L);
    }

    private EmployeeRequest request(String firstName) {
        return new EmployeeRequest(firstName, "Smith", "AMP-100", LocalDate.of(2025, 1, 10));
    }

    private EmployeeResponse response(Long id, String firstName) {
        return new EmployeeResponse(id, firstName, "Smith", "AMP-100", LocalDate.of(2025, 1, 10));
    }
}
