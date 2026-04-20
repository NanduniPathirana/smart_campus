package com.smartcampus.exception;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Global Catch-All Exception Mapper - Part 5.4
 *
 * Intercepts ANY unexpected runtime exception (e.g., NullPointerException,
 * IndexOutOfBoundsException, IllegalArgumentException) that is not handled
 * by a more specific ExceptionMapper.
 *
 * Returns a generic HTTP 500 Internal Server Error with a safe JSON body.
 */
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOGGER = Logger.getLogger(GlobalExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable exception) {
        
        exception.printStackTrace();
        // Log the full exception server-side for developer debugging
        LOGGER.log(Level.SEVERE, "Unexpected internal server error: " + exception.getMessage(), exception);

        // Return a safe, generic response to the client - NO stack trace exposed
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("status", 500);
        error.put("error", "Internal Server Error");
        error.put("message", exception.getMessage());

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON)
                .entity(error)
                .build();
    }
}
