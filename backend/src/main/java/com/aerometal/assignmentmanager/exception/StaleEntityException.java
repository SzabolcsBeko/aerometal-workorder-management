package com.aerometal.assignmentmanager.exception;

public class StaleEntityException extends RuntimeException {

  	private static final long serialVersionUID = 1L;

	public StaleEntityException(String message) {
        super(message);
    }
}