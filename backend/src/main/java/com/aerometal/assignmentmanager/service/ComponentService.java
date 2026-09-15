package com.aerometal.assignmentmanager.service;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aerometal.assignmentmanager.dto.ComponentRequest;
import com.aerometal.assignmentmanager.dto.ComponentResponse;
import com.aerometal.assignmentmanager.entity.Component;
import com.aerometal.assignmentmanager.exception.ResourceNotFoundException;
import com.aerometal.assignmentmanager.exception.StaleEntityException;
import com.aerometal.assignmentmanager.mapper.ComponentMapper;
import com.aerometal.assignmentmanager.repository.ComponentRepository;

@Service
public class ComponentService {
    private final ComponentRepository repository;
    private final ComponentMapper componentMapper;
    

    public ComponentService(ComponentRepository repository, ComponentMapper componentMapper) {
        this.repository = repository;
        this.componentMapper = componentMapper;
    }

    @Transactional(readOnly = true)
    public List<ComponentResponse> findAll() {
        return repository.findAll()
        		.stream()
        		.map(componentMapper::toResponse)
        		.toList();
    }

    @Transactional(readOnly = true)
    public ComponentResponse findById(Long id) {
    	Component component = findComponent(id);
        return componentMapper.toResponse(component);
    }

    @Transactional
    public ComponentResponse create(ComponentRequest request) {
        Component component = componentMapper.toEntity(request);
        Component save = repository.saveAndFlush(component);
        return componentMapper.toResponse(save);
    }

    @Transactional
    public ComponentResponse update(Long id, ComponentRequest request) {
    	Component component = findComponent(id);
    	
    	validateVersion(request, component);
    	
    	componentMapper.updateEntity(request, component);
    	
    	Component saved = repository.saveAndFlush(component);
    	
    	return componentMapper.toResponse(saved);
   	
    }

    @Transactional
    public void delete(Long id, Long version) {
    	Component component = findComponent(id);
    	
    	validateVersion(version, component);
    	
    	repository.delete(component);
    	repository.flush();

    }
    
    private Component findComponent(Long id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Component not found with id: " + id
                        )
                );
    }
    
    private void validateVersion(
            ComponentRequest request,
            Component component
    ) {
        validateVersion(request.version(), component);
    }
    
    private void validateVersion(
            Long requestVersion,
            Component component
    ) {
        if (requestVersion == null) {
            throw new StaleEntityException(
                    "Version is required when updating a component."
            );
        }

        if (!Objects.equals(
                requestVersion,
                component.getVersion()
        )) {
            throw new StaleEntityException(
                    "The component was modified by another user. "
                            + "Reload the latest data and try again."
            );
        }
    }
}
