package com.smartcampus.resource;

import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.model.Sensor;
import com.smartcampus.store.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

/**
 * Sensor Resource - Part 3.1 & 3.2
 *
 * Manages the /api/v1/sensors collection.
 *
 * Endpoints:
 *   POST /api/v1/sensors              - Register a new sensor (validates roomId exists)
 *   GET  /api/v1/sensors              - List all sensors
 *   GET  /api/v1/sensors?type=CO2     - Filter sensors by type (Part 3.2)
 *   GET  /api/v1/sensors/{sensorId}   - Get a specific sensor by ID
 */
@Path("sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    private final DataStore store = DataStore.getInstance();

    // POST /api/v1/sensors 
    /**
     * Part 3.1 - Registers a new sensor.
     *
     * Integrity Check: Validates that the roomId in the request body
     * actually exists in the system before registering the sensor.
     * If the roomId does not exist, throws LinkedResourceNotFoundException -> HTTP 422.
     
     * HTTP 415 Unsupported Media Type without reaching this method.
     */
    @POST
    public Response registerSensor(Sensor sensor) {
        // Validate required fields
        if (sensor.getId() == null || sensor.getId().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Sensor ID is required\"}")
                    .build();
        }

        // Validate that the referenced roomId actually exists - Part 5.2
        // Throws LinkedResourceNotFoundException -> mapped to HTTP 422 by ExceptionMapper
        if (sensor.getRoomId() == null || !store.roomExists(sensor.getRoomId())) {
            throw new LinkedResourceNotFoundException(
                "Cannot register sensor '" + sensor.getId() +
                "'. The referenced roomId '" + sensor.getRoomId() +
                "' does not exist in the system."
            );
        }

        // Check for duplicate sensor ID
        if (store.sensorExists(sensor.getId())) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("{\"error\": \"Sensor with ID '" + sensor.getId() + "' already exists\"}")
                    .build();
        }

        // Set default status if not provided
        if (sensor.getStatus() == null || sensor.getStatus().isEmpty()) {
            sensor.setStatus("ACTIVE");
        }

        // Register sensor and link it to the room's sensorIds list
        store.addSensor(sensor);
        store.getRoom(sensor.getRoomId()).getSensorIds().add(sensor.getId());

        return Response.status(Response.Status.CREATED).entity(sensor).build();
    }

    // GET /api/v1/sensors
    /**
     * Part 3.2 - Returns all sensors with optional @QueryParam type filtering.
     *
     * Usage:
     *   GET /api/v1/sensors           -> returns ALL sensors
     *   GET /api/v1/sensors?type=CO2  -> returns only CO2 type sensors
     */
    @GET
    public Response getAllSensors(@QueryParam("type") String type) {
        List<Sensor> sensorList = new ArrayList<>(store.getSensors().values());

        // Apply optional type filter - Part 3.2
        if (type != null && !type.isEmpty()) {
            List<Sensor> filtered = new ArrayList<>();
            for (Sensor sensor : sensorList) {
                if (sensor.getType().equalsIgnoreCase(type)) {
                    filtered.add(sensor);
                }
            }
            return Response.ok(filtered).build();
        }

        return Response.ok(sensorList).build();
    }

    // GET /api/v1/sensors/{sensorId} 
    /**
     * Returns a specific sensor by ID.
     * Returns HTTP 404 if the sensor does not exist.
     */
    @GET
    @Path("{sensorId}")
    public Response getSensorById(@PathParam("sensorId") String sensorId) {
        Sensor sensor = store.getSensor(sensorId);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Sensor not found with ID: " + sensorId + "\"}")
                    .build();
        }
        return Response.ok(sensor).build();
    }

    // Sub-Resource Locator - Part 4.1 
    // Part 4.1 - Sub-Resource Locator for {sensorId}/readings.
    
    @Path("{sensorId}/readings")
    public SensorReadingResource getReadingsResource(@PathParam("sensorId") String sensorId) {
        return new SensorReadingResource(sensorId);
    }
}
