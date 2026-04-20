package com.smartcampus.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Discovery Resource - Part 1.2
 *
 * Provides a root "Discovery" endpoint at GET /api/v1
 * Returns essential API metadata including:
 *  - API name and version
 *  - Administrative contact details
 *  - A map of primary resource collections (HATEOAS links)
 *
 * This implements HATEOAS (Hypermedia as the Engine of Application State)
 * which is a hallmark of advanced RESTful API design.
 */
@Path("/")
public class DiscoveryResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response discover() {

        // Build the resource links map (HATEOAS)
        Map<String, String> resources = new LinkedHashMap<>();
        resources.put("rooms", "/api/v1/rooms");
        resources.put("sensors", "/api/v1/sensors");

        // Build the full API metadata response
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("apiName", "Smart Campus Sensor & Room Management API");
        response.put("version", "1.0");
        response.put("description", "A RESTful API for managing campus rooms and IoT sensors");
        response.put("contact", "admin@smartcampus.ac.uk");
        response.put("basePath", "/api/v1");
        response.put("resources", resources);

        return Response.ok(response).build();
    }
}
