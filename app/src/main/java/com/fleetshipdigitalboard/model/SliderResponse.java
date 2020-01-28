package com.fleetshipdigitalboard.model;



public class SliderResponse {
    private String filename;
    private String filetype;

    public SliderResponse(String filename, String filetype) {
        this.filename = filename;
        this.filetype = filetype;
    }

    public String getFilename() {
        return filename;
    }

    public String getFiletype() {
        return filetype;
    }

    public int getInterValurOfFile(String filename){
        return Integer.parseInt(filename.replaceAll("\\D+",""));
    }
}
