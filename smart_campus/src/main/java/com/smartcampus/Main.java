package com.smartcampus;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;

// Main class that bootstraps the Grizzly embedded HTTP server.

public class Main {

    // Base URI the Grizzly HTTP server will listen on
    public static final String BASE_URI = "http://localhost:8080/api/v1/";

    // Starts the Grizzly HTTP server exposing the JAX-RS resources.

    public static HttpServer startServer() {
        // Create a ResourceConfig that scans for JAX-RS resources in com.smartcampus package
        // JacksonFeature must be explicitly registered to enable JSON serialization
        final ResourceConfig rc = new ResourceConfig()
                .packages("com.smartcampus")
                .register(JacksonFeature.class);

        // Create and start the Grizzly HTTP server
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
    }

    /**
     * Main method - entry point of the application.
     */
    public static void main(String[] args) throws IOException {
        final HttpServer server = startServer();

        System.out.println("----------------------------------------------");
        System.out.println("  Smart Campus API is running!");
        System.out.println("  Base URL : " + BASE_URI);
        System.out.println("  API Root : " + BASE_URI + "api/v1");
        System.out.println("  Press ENTER to stop the server...");
        System.out.println("----------------------------------------------");

        // Keep the server running until ENTER is pressed
        System.in.read();
        server.stop();

        System.out.println("Server stopped.");
    }
}
