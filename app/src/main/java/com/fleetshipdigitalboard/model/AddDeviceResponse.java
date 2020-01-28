package com.fleetshipdigitalboard.model;



public class AddDeviceResponse {
    private String message;
    private String device_id;

    public AddDeviceResponse(String message, String device_id) {
        this.message = message;
        this.device_id = device_id;
    }

    public String getMessage() {
        return message;
    }

    public String getDevice_id() {
        return device_id;
    }
}
