package com.smartcampus.exception;

/**
 * Thrown when a client references a resource (e.g., a roomId) that does not exist.
 * Mapped to HTTP 422 Unprocessable Entity by LinkedResourceNotFoundExceptionMapper.
 
 * A 404 means the requested URL/resource was not found.
 * A 422 means the request was well-formed but contains invalid references
 */
public class LinkedResourceNotFoundException extends RuntimeException {

    public LinkedResourceNotFoundException(String message) {
        super(message);
    }
}
