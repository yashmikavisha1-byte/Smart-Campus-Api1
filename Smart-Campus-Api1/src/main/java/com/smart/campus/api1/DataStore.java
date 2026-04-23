package com.smart.campus.api1;

import com.smart.campus.api1.model.Room;
import com.smart.campus.api1.model.Sensor;
import com.smart.campus.api1.model.SensorReading;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DataStore {

    public static final Map<String, Room> rooms = new ConcurrentHashMap<>();
    public static final Map<String, Sensor> sensors = new ConcurrentHashMap<>();
    public static final Map<String, List<SensorReading>> readings = new ConcurrentHashMap<>();

    static {
        // Sample Room 1
        Room r1 = new Room("LIB-301", "Library Quiet Study", 50);
        rooms.put(r1.getId(), r1);

        // Sample Room 2
        Room r2 = new Room("LAB-101", "Computer Lab", 30);
        rooms.put(r2.getId(), r2);

        // Sample Sensor 1
        Sensor s1 = new Sensor("TEMP-001", "Temperature", "ACTIVE", 22.5, "LIB-301");
        sensors.put(s1.getId(), s1);
        r1.getSensorIds().add("TEMP-001");
        readings.put("TEMP-001", new ArrayList<>());

        // Sample Sensor 2
        Sensor s2 = new Sensor("CO2-001", "CO2", "ACTIVE", 400.0, "LAB-101");
        sensors.put(s2.getId(), s2);
        r2.getSensorIds().add("CO2-001");
        readings.put("CO2-001", new ArrayList<>());

        // Sample Sensor 3 — MAINTENANCE for testing 403
        Sensor s3 = new Sensor("OCC-001", "Occupancy", "MAINTENANCE", 0.0, "LIB-301");
        sensors.put(s3.getId(), s3);
        r1.getSensorIds().add("OCC-001");
        readings.put("OCC-001", new ArrayList<>());
    }
}