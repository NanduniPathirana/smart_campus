package com.smartcampus.exception;

import javax.ws.rs.WebApplicationException;
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

        // Pass through WebApplicationExceptions (404, 415 etc.) unchanged.
        // These are deliberate HTTP responses, not unexpected errors.
        if (exception instanceof WebApplicationException) {
            return ((WebApplicationException) exception).getResponse();
        }

        // Log the full exception server-side for developer debugging only
        LOGGER.log(Level.SEVERE, "Unexpected internal server error: " + exception.getMessage(), exception);

        // Return a safe, generic response - NO stack trace exposed to client
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("status", 500);
        error.put("error", "Internal Server Error");
        error.put("message", "An unexpected error occurred. Please contact the system administrator.");

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON)
                .entity(error)
                .build();
    }
}
