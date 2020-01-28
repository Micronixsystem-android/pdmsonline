package com.fleetshipdigitalboard.model;



public class ContentStatusRequest {
    private String authorised_key;
    private String device_id;

    public ContentStatusRequest(String authorised_key, String device_id) {
        this.authorised_key = authorised_key;
        this.device_id = device_id;
    }
}
