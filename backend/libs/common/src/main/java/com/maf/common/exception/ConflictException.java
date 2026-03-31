package com.maf.common.exception;

public class ConflictException extends RuntimeException {
    
    public ConflictException(String message) {
        super(message);
    }

    public ConflictException(String entityName, String fieldName, Object fieldValue) {
        super(String.format("%s with %s '%s' already exists", entityName, fieldName, fieldValue));
    }
}
