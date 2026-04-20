package com.smartcampus.exception;

/**
 * Thrown when a POST reading is attempted on a sensor that is in MAINTENANCE status.
 * Mapped to HTTP 403 Forbidden by SensorUnavailableExceptionMapper.
 * A sensor in MAINTENANCE is physically disconnected and cannot record readings.
 */
public class SensorUnavailableException extends RuntimeException {

    public SensorUnavailableException(String message) {
        super(message);
    }
}
