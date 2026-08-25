package com.aerometal.assignmentmanager.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.aerometal.assignmentmanager.entity.AccessRight;
import com.aerometal.assignmentmanager.exception.AccessRightNotFoundException;
import com.aerometal.assignmentmanager.repository.AccessRightRepository;

@ExtendWith(MockitoExtension.class)
class AccessRightServiceUnitTest {

    @Mock AccessRightRepository repository;
    @InjectMocks AccesRightService service;

    @Test
    void shouldCreateAndListAccessRights() {
        AccessRight right = right("INS");
        when(repository.save(right)).thenReturn(right);
        when(repository.findAll()).thenReturn(List.of(right));

        assertThat(service.create(right)).isSameAs(right);
        assertThat(service.findAll()).containsExactly(right);
    }

    @Test
    void shouldUpdateAndDeleteExistingAccessRight() {
        AccessRight right = right("INS");
        when(repository.existsById(1L)).thenReturn(true);
        when(repository.save(right)).thenReturn(right);

        assertThat(service.update(1L, right).getId()).isEqualTo(1L);
        service.delete(1L);
        verify(repository).deleteById(1L);
    }

    @Test
    void shouldRejectMissingAccessRight() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(AccessRightNotFoundException.class);
    }

    private AccessRight right(String name) {
        AccessRight right = new AccessRight();
        right.setName(name);
        right.setDescription("Test right");
        return right;
    }
}
