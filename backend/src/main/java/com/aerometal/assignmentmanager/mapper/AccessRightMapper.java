package com.aerometal.assignmentmanager.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.aerometal.assignmentmanager.dto.AccessRightRequest;
import com.aerometal.assignmentmanager.dto.AccessRightResponse;
import com.aerometal.assignmentmanager.entity.AccessRight;

@Mapper(componentModel = "spring")
public interface AccessRightMapper {
	
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "version", ignore = true)
	AccessRight toEntity(AccessRightRequest request);
	
	AccessRightResponse toResponse(AccessRight accessRight);
	
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "version", ignore = true)
	void updateEntity(
			AccessRightRequest request,
			@MappingTarget AccessRight accessRight);

}
