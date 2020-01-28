package com.fleetshipdigitalboard.model;



public class UpdateStatusRequest {
    private String authorised_key;
    private String device_id;
    private String status;

    public UpdateStatusRequest(String authorised_key, String device_id, String status) {
        this.authorised_key = authorised_key;
        this.device_id = device_id;
        this.status = status;
    }
}
