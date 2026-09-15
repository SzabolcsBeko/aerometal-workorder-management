package com.aerometal.assignmentmanager.service;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aerometal.assignmentmanager.dto.AccessRightRequest;
import com.aerometal.assignmentmanager.dto.AccessRightResponse;
import com.aerometal.assignmentmanager.entity.AccessRight;
import com.aerometal.assignmentmanager.exception.ResourceNotFoundException;
import com.aerometal.assignmentmanager.exception.StaleEntityException;
import com.aerometal.assignmentmanager.mapper.AccessRightMapper;
import com.aerometal.assignmentmanager.repository.AccessRightRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccesRightService {
    private final AccessRightRepository repository;
    private final AccessRightMapper accessRightMapper;

    @Transactional(readOnly = true)
    public List<AccessRightResponse> findAll() {
        return repository.findAll().stream().map(accessRightMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public AccessRightResponse findById(Long id) {
    	 AccessRight accessRight = getAccessRight(id);
         return accessRightMapper.toResponse(accessRight);
    }

    @Transactional
    public AccessRightResponse create(AccessRightRequest request) {
    	 if (repository.existsByName(request.name())) {
    	        throw new IllegalArgumentException(
    	            "AccessRight already exists with name: "
    	                + request.name()
    	        );
    	    }

    	    AccessRight accessRight =
    	            accessRightMapper.toEntity(request);

    	    AccessRight saved =
    	    		repository.saveAndFlush(accessRight);

    	    return accessRightMapper.toResponse(saved);
    }

    @Transactional
    public AccessRightResponse update(Long id, AccessRightRequest request) {

        AccessRight existingAccessRight = getAccessRight(id);

        validateVersion(
            existingAccessRight,
            request.version()
        );
        
        if (repository.existsByNameAndIdNot(
                request.name(),
                id
        )) {
            throw new IllegalArgumentException(
                "Another AccessRight already exists with name: "
                    + request.name()
            );
        }

        accessRightMapper.updateEntity(
            request,
            existingAccessRight
        );
        
        /*
         * saveAndFlush forces Hibernate to execute the UPDATE inside
         * this method. A concurrent update will therefore be detected
         * before the method returns.
         */

        AccessRight updatedAccessRight =
            repository.saveAndFlush(existingAccessRight);

        return accessRightMapper.toResponse(updatedAccessRight);
    }

    @Transactional
    public void delete(Long id) {
    	AccessRight accessRight = getAccessRight(id);
        repository.delete(accessRight);
    }
    
    private AccessRight getAccessRight(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "AccessRight was not found with id: " + id
            ));
    }
    
    private void validateVersion(
            AccessRight accessRight,
            Long requestedVersion) {

        if (requestedVersion == null) {
            throw new IllegalArgumentException(
                "Version is required when updating AccessRight"
            );
        }

        if (!Objects.equals(
                accessRight.getVersion(),
                requestedVersion)) {

            throw new StaleEntityException(
                "AccessRight with id " + accessRight.getId()
                    + " was modified by another user. "
                    + "Current version is "
                    + accessRight.getVersion()
                    + ", but the requested version was "
                    + requestedVersion
            );
        }
    }
}
