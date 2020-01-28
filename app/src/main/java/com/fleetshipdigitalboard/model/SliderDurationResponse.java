package com.fleetshipdigitalboard.model;


public class SliderDurationResponse {
    private String slider_duration;
    private String status;

    public SliderDurationResponse(String slider_duration, String status) {
        this.slider_duration = slider_duration;
        this.status = status;
    }

    public String getSlider_duration() {
        return slider_duration;
    }

    public String getStatus() {
        return status;
    }
}
