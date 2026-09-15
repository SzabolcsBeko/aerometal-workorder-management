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

import com.aerometal.assignmentmanager.dto.AccessRightRequest;
import com.aerometal.assignmentmanager.dto.AccessRightResponse;
import com.aerometal.assignmentmanager.entity.AccessRight;
import com.aerometal.assignmentmanager.exception.ResourceNotFoundException;
import com.aerometal.assignmentmanager.exception.StaleEntityException;
import com.aerometal.assignmentmanager.mapper.AccessRightMapper;
import com.aerometal.assignmentmanager.repository.AccessRightRepository;

@ExtendWith(MockitoExtension.class)
class AccessRightServiceUnitTest {

    @Mock
    private AccessRightRepository repository;

    @Mock
    private AccessRightMapper accessRightMapper;

    @InjectMocks
    private AccesRightService service;

    @Test
    void shouldCreateAndListAccessRights() {
        AccessRightRequest request = request("INS", null);
        AccessRight entity = entity(1L, "INS", 0L);
        AccessRightResponse response =
                response(1L, "INS", 0L);

        when(repository.existsByName("INS"))
                .thenReturn(false);

        when(accessRightMapper.toEntity(request))
                .thenReturn(entity);

        when(repository.saveAndFlush(entity))
                .thenReturn(entity);

        when(accessRightMapper.toResponse(entity))
                .thenReturn(response);

        when(repository.findAll())
                .thenReturn(List.of(entity));

        assertThat(service.create(request))
                .isEqualTo(response);

        assertThat(service.findAll())
                .containsExactly(response);

        verify(repository).saveAndFlush(entity);
    }

    @Test
    void shouldUpdateAndDeleteExistingAccessRight() {
        AccessRightRequest request =
                request("INS_UPDATED", 0L);

        AccessRight entity =
                entity(1L, "INS", 0L);

        AccessRightResponse response =
                response(1L, "INS_UPDATED", 1L);

        when(repository.findById(1L))
                .thenReturn(Optional.of(entity));

        when(repository.existsByNameAndIdNot(
                "INS_UPDATED",
                1L
        )).thenReturn(false);

        when(repository.saveAndFlush(entity))
                .thenReturn(entity);

        when(accessRightMapper.toResponse(entity))
                .thenReturn(response);

        assertThat(service.update(1L, request))
                .isEqualTo(response);

        verify(accessRightMapper)
                .updateEntity(request, entity);

        verify(repository)
                .saveAndFlush(entity);

        service.delete(1L);

        verify(repository).delete(entity);
    }

    @Test
    void shouldRejectMissingAccessRight() {
        when(repository.findById(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
            service.findById(99L)
        )
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage(
                "AccessRight was not found with id: 99"
            );
    }

    @Test
    void shouldRejectUpdateWithStaleVersion() {
        AccessRightRequest staleRequest =
                request("INS_UPDATED", 1L);

        AccessRight entity =
                entity(1L, "INS", 2L);

        when(repository.findById(1L))
                .thenReturn(Optional.of(entity));

        assertThatThrownBy(() ->
            service.update(1L, staleRequest)
        )
            .isInstanceOf(StaleEntityException.class)
            .hasMessageContaining(
                "modified by another user"
            )
            .hasMessageContaining(
                "Current version is 2"
            )
            .hasMessageContaining(
                "requested version was 1"
            );

        verify(accessRightMapper, never())
                .updateEntity(staleRequest, entity);

        verify(repository, never())
                .saveAndFlush(entity);
    }

    private AccessRightRequest request(
            String name,
            Long version
    ) {
        return new AccessRightRequest(
            name,
            "Test right",
            version
        );
    }

    private AccessRight entity(
            Long id,
            String name,
            Long version
    ) {
        AccessRight accessRight = new AccessRight();

        accessRight.setId(id);
        accessRight.setName(name);
        accessRight.setDescription("Test right");

        /*
         * VersionedEntity intentionally has no version setter.
         * Reflection is used only to prepare the unit-test entity.
         */
        ReflectionTestUtils.setField(
            accessRight,
            "version",
            version
        );

        return accessRight;
    }

    private AccessRightResponse response(
            Long id,
            String name,
            Long version
    ) {
        return new AccessRightResponse(
            id,
            name,
            "Test right",
            version
        );
    }
}