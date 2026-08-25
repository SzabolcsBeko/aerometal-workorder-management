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

import com.aerometal.assignmentmanager.entity.Component;
import com.aerometal.assignmentmanager.exception.ComponentNotFoundException;
import com.aerometal.assignmentmanager.repository.ComponentRepository;

@ExtendWith(MockitoExtension.class)
class ComponentServiceUnitTest {

    @Mock ComponentRepository repository;
    @InjectMocks ComponentService service;

    @Test
    void shouldCreateAndListComponents() {
        Component component = component("ENGINE-01");
        when(repository.save(component)).thenReturn(component);
        when(repository.findAll()).thenReturn(List.of(component));

        assertThat(service.create(component)).isSameAs(component);
        assertThat(service.findAll()).containsExactly(component);
        assertThat(component.getId()).isNull();
    }

    @Test
    void shouldUpdateAndDeleteExistingComponent() {
        Component component = component("ENGINE-01");
        when(repository.existsById(1L)).thenReturn(true);
        when(repository.save(component)).thenReturn(component);

        assertThat(service.update(1L, component).getId()).isEqualTo(1L);
        service.delete(1L);
        verify(repository).deleteById(1L);
    }

    @Test
    void shouldRejectMissingComponent() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(ComponentNotFoundException.class);
    }

    private Component component(String name) {
        Component component = new Component();
        component.setName(name);
        component.setDescription("Test component");
        return component;
    }
}
