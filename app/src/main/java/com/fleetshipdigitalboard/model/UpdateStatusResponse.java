package com.fleetshipdigitalboard.model;


public class UpdateStatusResponse {
    private String message;
    private String device_id;
    private String status;

    public UpdateStatusResponse(String message, String status, String device_id) {
        this.message = message;
        this.device_id = device_id;
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public String getDevice_id() {
        return device_id;
    }

    public String getStatus() {
        return status;
    }
}
