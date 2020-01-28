package com.fleetshipdigitalboard.model;


public class StatusModel {
    private String device_id;
    private String connection_status;
    private String sharing_code;

    public StatusModel(String device_id, String connection_status, String sharing_code) {
        this.device_id = device_id;
        this.connection_status = connection_status;
        this.sharing_code = sharing_code;
    }

    public String getDevice_id() {
        return device_id;
    }

    public String getConnection_status() {
        return connection_status;
    }

    public String getSharing_code() {
        return sharing_code;
    }
}
