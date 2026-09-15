package com.aerometal.assignmentmanager.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.aerometal.assignmentmanager.dto.ComponentRequest;
import com.aerometal.assignmentmanager.dto.ComponentResponse;
import com.aerometal.assignmentmanager.entity.Component;
import com.aerometal.assignmentmanager.exception.ResourceNotFoundException;
import com.aerometal.assignmentmanager.exception.StaleEntityException;
import com.aerometal.assignmentmanager.mapper.ComponentMapper;
import com.aerometal.assignmentmanager.repository.ComponentRepository;

@ExtendWith(MockitoExtension.class)
class ComponentServiceUnitTest {

    @Mock
    private ComponentRepository repository;

    @Mock
    private ComponentMapper componentMapper;

    @InjectMocks
    private ComponentService service;

    @Test
    void shouldCreateAndListComponents() {
        ComponentRequest request =
                request(null, "ENGINE-01");

        Component entity =
                component(1L, 0L, "ENGINE-01");

        ComponentResponse response =
                response(1L, 0L, "ENGINE-01");

        when(componentMapper.toEntity(request))
                .thenReturn(entity);

        when(repository.saveAndFlush(entity))
                .thenReturn(entity);

        when(componentMapper.toResponse(entity))
                .thenReturn(response);

        when(repository.findAll())
                .thenReturn(List.of(entity));

        assertThat(service.create(request))
                .isEqualTo(response);

        assertThat(service.findAll())
                .containsExactly(response);

        verify(componentMapper).toEntity(request);
        verify(repository).saveAndFlush(entity);
    }

    @Test
    void shouldUpdateAndDeleteExistingComponent() {
        ComponentRequest updateRequest =
                request(0L, "ENGINE-01-UPDATED");

        Component entity =
                component(1L, 0L, "ENGINE-01");

        ComponentResponse updatedResponse =
                response(
                    1L,
                    1L,
                    "ENGINE-01-UPDATED"
                );

        when(repository.findById(1L))
                .thenReturn(Optional.of(entity));

        when(repository.saveAndFlush(entity))
                .thenReturn(entity);

        when(componentMapper.toResponse(entity))
                .thenReturn(updatedResponse);

        assertThat(service.update(1L, updateRequest))
                .isEqualTo(updatedResponse);

        verify(componentMapper)
                .updateEntity(updateRequest, entity);

        verify(repository).saveAndFlush(entity);

        /*
         * The current entity version is 0 in this unit test,
         * so delete must receive version 0.
         */
        service.delete(1L, 0L);

        verify(repository).delete(entity);
        verify(repository).flush();
    }

    @Test
    void shouldRejectMissingComponent() {
        when(repository.findById(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
            service.findById(99L)
        )
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage(
                "Component not found with id: 99"
            );
    }

    @Test
    void shouldRejectUpdateWithStaleVersion() {
        ComponentRequest staleRequest =
                request(1L, "ENGINE-01-UPDATED");

        /*
         * The request contains version 1, but the entity
         * has already reached version 2.
         */
        Component entity =
                component(1L, 2L, "ENGINE-01");

        when(repository.findById(1L))
                .thenReturn(Optional.of(entity));

        assertThatThrownBy(() ->
            service.update(1L, staleRequest)
        )
            .isInstanceOf(StaleEntityException.class)
            .hasMessageContaining(
                "modified by another user"
            );

        /*
         * A stale request must be rejected before mapping
         * or executing a database update.
         */
        verify(componentMapper, never())
                .updateEntity(staleRequest, entity);

        verify(repository, never())
                .saveAndFlush(entity);
    }

    @Test
    void shouldRejectUpdateWithoutVersion() {
        ComponentRequest requestWithoutVersion =
                request(null, "ENGINE-01-UPDATED");

        Component entity =
                component(1L, 2L, "ENGINE-01");

        when(repository.findById(1L))
                .thenReturn(Optional.of(entity));

        assertThatThrownBy(() ->
            service.update(1L, requestWithoutVersion)
        )
            .isInstanceOf(StaleEntityException.class)
            .hasMessage(
                "Version is required when updating a component."
            );

        verify(componentMapper, never())
                .updateEntity(
                    requestWithoutVersion,
                    entity
                );

        verify(repository, never())
                .saveAndFlush(entity);
    }

    @Test
    void shouldRejectDeleteWithStaleVersion() {
        Component entity =
                component(1L, 2L, "ENGINE-01");

        when(repository.findById(1L))
                .thenReturn(Optional.of(entity));

        assertThatThrownBy(() ->
            service.delete(1L, 1L)
        )
            .isInstanceOf(StaleEntityException.class)
            .hasMessageContaining(
                "modified by another user"
            );

        verify(repository, never()).delete(entity);
        verify(repository, never()).flush();
    }

    private ComponentRequest request(
            Long version,
            String name
    ) {
        return new ComponentRequest(
            version,
            name,
            "Test component"
        );
    }

    private Component component(
            Long id,
            Long version,
            String name
    ) {
        Component component = new Component();

        component.setId(id);
        component.setName(name);
        component.setDescription("Test component");

        /*
         * VersionedEntity intentionally has no version setter.
         * Reflection is used only to prepare the unit-test entity.
         */
        ReflectionTestUtils.setField(
            component,
            "version",
            version
        );

        return component;
    }

    private ComponentResponse response(
            Long id,
            Long version,
            String name
    ) {
        return new ComponentResponse(
            id,
            version,
            name,
            "Test component"
        );
    }
}