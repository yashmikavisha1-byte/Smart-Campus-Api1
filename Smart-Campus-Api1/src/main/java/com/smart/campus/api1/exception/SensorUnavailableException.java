package com.smart.campus.api1.exception;

public class SensorUnavailableException extends RuntimeException {

    public SensorUnavailableException(String sensorId) {
        super("Sensor " + sensorId + " is currently under MAINTENANCE and cannot accept new readings.");
    }
}