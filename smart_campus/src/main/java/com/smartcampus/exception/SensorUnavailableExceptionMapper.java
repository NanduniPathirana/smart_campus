package com.smartcampus.exception;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Exception Mapper for SensorUnavailableException - Part 5.3
 * Maps SensorUnavailableException to HTTP 403 Forbidden with a structured JSON body.
 * Returned when a POST reading is attempted on a sensor in MAINTENANCE status.
 */
@Provider
public class SensorUnavailableExceptionMapper
        implements ExceptionMapper<SensorUnavailableException> {

    @Override
    public Response toResponse(SensorUnavailableException exception) {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("status", 403);
        error.put("error", "Forbidden");
        error.put("message", exception.getMessage());
        error.put("hint", "Wait until the sensor status is set back to ACTIVE before posting new readings.");

        return Response.status(Response.Status.FORBIDDEN)
                .type(MediaType.APPLICATION_JSON)
                .entity(error)
                .build();
    }
}
