package com.smartcampus.resource;

import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;
import com.smartcampus.store.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;

/**
 * SensorReading Resource - Part 4.2
 *
 * Handles historical readings for a specific sensor.
 * Endpoints (relative to /api/v1/sensors/{sensorId}/readings):
 *   GET  / -> Fetch full reading history for the sensor
 *   POST / -> Append a new reading and update sensor's currentValue
 */
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final String sensorId;
    private final DataStore store = DataStore.getInstance();

    /**
     * Constructor called by the sub-resource locator in SensorResource.
     * Receives the sensorId from the URL path.
     */
    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    // GET /api/v1/sensors/{sensorId}/readings 
    /**
     * Part 4.2 - Returns the full historical reading log for this sensor.
     * Returns 404 if the sensor does not exist.
     */
    @GET
    public Response getReadings() {
        // Validate sensor exists
        Sensor sensor = store.getSensor(sensorId);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Sensor not found with ID: " + sensorId + "\"}")
                    .build();
        }

        List<SensorReading> readings = store.getReadings(sensorId);
        return Response.ok(readings).build();
    }

    // POST /api/v1/sensors/{sensorId}/readings 
    //Part 4.2 - Appends a new reading to this sensor's history.
     
    @POST
    public Response addReading(SensorReading reading) {
        // Validate sensor exists
        Sensor sensor = store.getSensor(sensorId);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Sensor not found with ID: " + sensorId + "\"}")
                    .build();
        }

        // State Constraint: Block readings for MAINTENANCE sensors
        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"error\": \"Sensor '" + sensorId +
                            "' is currently in MAINTENANCE and cannot accept new readings.\"}")
                    .build();
        }

        // Auto-generate ID and timestamp if not provided
        if (reading.getId() == null || reading.getId().isEmpty()) {
            reading.setId(UUID.randomUUID().toString());
        }
        if (reading.getTimestamp() == 0) {
            reading.setTimestamp(System.currentTimeMillis());
        }

        // Save the new reading
        store.addReading(sensorId, reading);

        // Update the parent Sensor's currentValue to keep data consistent
        sensor.setCurrentValue(reading.getValue());

        return Response.status(Response.Status.CREATED).entity(reading).build();
    }
}
