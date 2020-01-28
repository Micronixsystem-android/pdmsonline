package com.fleetshipdigitalboard.model;


public class ContentStatusResponse {
    private String message;
    private String status;
    private String device_id;

    public ContentStatusResponse(String message, String status, String device_id) {
        this.message = message;
        this.status = status;
        this.device_id = device_id;
    }

    public String getMessage() {
        return message;
    }

    public String getStatus() {
        return status;
    }

    public String getDevice_id() {
        return device_id;
    }
}
