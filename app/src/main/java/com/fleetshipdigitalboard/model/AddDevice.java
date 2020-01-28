package com.fleetshipdigitalboard.model;



public class AddDevice {
    private String authorised_key;
    private String device_code;
    private String sharing_code;

    public AddDevice(String authorised_key, String device_code, String sharing_code) {
        this.authorised_key = authorised_key;
        this.device_code = device_code;
        this.sharing_code = sharing_code;
    }
}
