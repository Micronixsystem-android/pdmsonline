package com.fleetshipdigitalboard.model;



public class StatusRequest {
    private String authorised_key;
    private String device_code;

    public StatusRequest(String authorised_key, String device_code) {
        this.authorised_key = authorised_key;
        this.device_code = device_code;
    }
}
