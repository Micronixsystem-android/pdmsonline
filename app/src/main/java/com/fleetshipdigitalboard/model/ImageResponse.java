package com.fleetshipdigitalboard.model;


public class ImageResponse {
    private String filename;
    private String filetype;
    private String urlpath;

    public ImageResponse(String filename, String filetype, String urlpath) {
        this.filename = filename;
        this.filetype = filetype;
        this.urlpath = urlpath;
    }

    public String getFilename() {
        return filename;
    }

    public String getFiletype() {
        return filetype;
    }

    public String getUrlpath() {
        return urlpath;
    }
}
