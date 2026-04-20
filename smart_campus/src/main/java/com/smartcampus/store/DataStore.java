package com.smartcampus.store;

import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Singleton in-memory data store for the Smart Campus API.
 * This class replaces a database by using HashMaps to store all data.
 */
public class DataStore {

    // Singleton Instance 
    private static DataStore instance;

    // In-Memory Data Structures 
    private final Map<String, Room> rooms = new LinkedHashMap<>();
    private final Map<String, Sensor> sensors = new LinkedHashMap<>();
    private final Map<String, List<SensorReading>> sensorReadings = new LinkedHashMap<>();

    // Private Constructor (Singleton) 
    private DataStore() {
        seedData(); // Pre-populate with sample data for testing
    }

    /**
     * Returns the single shared instance of DataStore.
     * Synchronized to prevent race conditions during initialization.
     */
    public static synchronized DataStore getInstance() {
        if (instance == null) {
            instance = new DataStore();
        }
        return instance;
    }

    // Seed Data 

    /**
     * Pre-populates the store with sample rooms and sensors for demonstration.
     */
    private void seedData() {
        // Sample Rooms
        Room room1 = new Room("1LE - GP", "Tutorial Lab", 50);
        Room room2 = new Room("5LA", "Lecture Hall", 30);
        Room room3 = new Room("1st Floor - SP", "Library", 200);

        rooms.put(room1.getId(), room1);
        rooms.put(room2.getId(), room2);
        rooms.put(room3.getId(), room3);

        // Sample Sensors
        Sensor sensor1 = new Sensor("TEMP-001", "Temperature", "ACTIVE", 22.5, "1LE - GP");
        Sensor sensor2 = new Sensor("CO2-001", "CO2", "ACTIVE", 412.0, "1LE - GP");
        Sensor sensor3 = new Sensor("OCC-001", "Occupancy", "MAINTENANCE", 0.0, "5LA");
        Sensor sensor4 = new Sensor("TEMP-002", "Temperature", "ACTIVE", 19.8, "1st Floor - SP");

        sensors.put(sensor1.getId(), sensor1);
        sensors.put(sensor2.getId(), sensor2);
        sensors.put(sensor3.getId(), sensor3);
        sensors.put(sensor4.getId(), sensor4);

        // Link sensors to rooms via sensorIds list
        room1.getSensorIds().add("TEMP-001");
        room1.getSensorIds().add("CO2-001");
        room2.getSensorIds().add("OCC-001");
        room3.getSensorIds().add("TEMP-002");

        // Sample Sensor Readings
        List<SensorReading> readings1 = new ArrayList<>();
        readings1.add(new SensorReading("READ-001", System.currentTimeMillis() - 60000, 21.0));
        readings1.add(new SensorReading("READ-002", System.currentTimeMillis() - 30000, 22.0));
        readings1.add(new SensorReading("READ-003", System.currentTimeMillis(), 22.5));
        sensorReadings.put("TEMP-001", readings1);

        List<SensorReading> readings2 = new ArrayList<>();
        readings2.add(new SensorReading("READ-004", System.currentTimeMillis() - 60000, 400.0));
        readings2.add(new SensorReading("READ-005", System.currentTimeMillis(), 412.0));
        sensorReadings.put("CO2-001", readings2);
    }

    // Room Operations 

    public synchronized Map<String, Room> getRooms() {
        return rooms;
    }

    public synchronized Room getRoom(String id) {
        return rooms.get(id);
    }

    public synchronized void addRoom(Room room) {
        rooms.put(room.getId(), room);
    }

    public synchronized boolean deleteRoom(String id) {
        return rooms.remove(id) != null;
    }

    public synchronized boolean roomExists(String id) {
        return rooms.containsKey(id);
    }

    // Sensor Operations 

    public synchronized Map<String, Sensor> getSensors() {
        return sensors;
    }

    public synchronized Sensor getSensor(String id) {
        return sensors.get(id);
    }

    public synchronized void addSensor(Sensor sensor) {
        sensors.put(sensor.getId(), sensor);
    }

    public synchronized boolean sensorExists(String id) {
        return sensors.containsKey(id);
    }

    // Sensor Reading Operations 

    public synchronized List<SensorReading> getReadings(String sensorId) {
        return sensorReadings.getOrDefault(sensorId, new ArrayList<>());
    }

    public synchronized void addReading(String sensorId, SensorReading reading) {
        sensorReadings.computeIfAbsent(sensorId, k -> new ArrayList<>()).add(reading);
    }
}
