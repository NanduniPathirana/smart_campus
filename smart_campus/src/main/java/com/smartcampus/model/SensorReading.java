package com.smartcampus.model;

import java.util.UUID;

//Represents a single historical reading captured by a sensor.

public class SensorReading {

    private String id;          // Unique reading event ID (UUID)
    private long timestamp;     // Epoch time in milliseconds when reading was captured
    private double value;       // The actual metric value recorded by the hardware

    //  Constructors 

    public SensorReading() {
    }

    public SensorReading(double value) {
        this.id = UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
        this.value = value;
    }

    public SensorReading(String id, long timestamp, double value) {
        this.id = id;
        this.timestamp = timestamp;
        this.value = value;
    }

    // Getters & Setters 

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
