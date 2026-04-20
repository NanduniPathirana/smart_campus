package com.smartcampus;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * JAX-RS Application configuration class. 
 * The @ApplicationPath annotation sets the base URI for all REST resources.

 */
@ApplicationPath("api/v1")
public class SmartCampusApplication extends Application {
    
}
