package com.bue.susanne.safeway;

/**
 * Created by abaumgra on 28/01/2017.
 */

public class EventPOJO {

    private Double longitude;
    private Double latitude;
    private String icon_path;



    public EventPOJO (Double latitude, Double longitude){
        setLatitude(latitude);
        setLongitude(longitude);
    }


    public EventPOJO(float latitude, float longitude){
        this.latitude = Double.valueOf(String.valueOf(latitude));
        this.longitude = Double.valueOf(String.valueOf(longitude));
        System.out.println(this.latitude);
        System.out.println(this.longitude);
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public void setIconPath(String icon_path) {
        this.icon_path = icon_path;
    }

    public String getIconPath() {
        return icon_path;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }


}
