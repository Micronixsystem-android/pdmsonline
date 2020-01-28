package com.fleetshipdigitalboard.model;



public class SliderDurationRequest {

    private String authorised_key;

    public SliderDurationRequest(String authorised_key) {
        this.authorised_key = authorised_key;
    }

    public String getAuthorised_key() {
        return authorised_key;
    }
}
