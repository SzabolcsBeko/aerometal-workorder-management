package com.aerometal.assignmentmanager.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.aerometal.assignmentmanager.dto.ComponentRequest;
import com.aerometal.assignmentmanager.dto.ComponentResponse;
import com.aerometal.assignmentmanager.entity.Component;

@Mapper(componentModel = "spring")
public interface ComponentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    Component toEntity(ComponentRequest request);

    ComponentResponse toResponse(Component component);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntity(
            ComponentRequest request,
            @MappingTarget Component component
    );
}