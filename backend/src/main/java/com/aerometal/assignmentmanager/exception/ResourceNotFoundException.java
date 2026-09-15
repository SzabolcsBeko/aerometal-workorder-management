package com.aerometal.assignmentmanager.exception;

public class ResourceNotFoundException extends RuntimeException{

	
	private static final long serialVersionUID = 1L;

	public ResourceNotFoundException(String id) {
		super(id);
	}
	
}
