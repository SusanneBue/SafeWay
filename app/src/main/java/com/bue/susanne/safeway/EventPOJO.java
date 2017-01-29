package com.bue.susanne.safeway;

/**
 * Created by abaumgra on 28/01/2017.
 */

public class EventPOJO {

    private Double longitude;
    private Double latitude;
    private int iconID;

    // the higher the better
    private int safetyValue;

    private String title;

    private String description;

    private int rating;

    private int confirmationCount;

    public int getSafetyValue() {
        return safetyValue;
    }

    public void setSafetyValue(int safetyValue) {
        this.safetyValue = safetyValue;
    }

    public EventPOJO (Double latitude, Double longitude){
        setLatitude(latitude);
        setLongitude(longitude);
    }

    public EventPOJO(double latitude, double longitude){
        this.latitude = Double.valueOf(String.valueOf(latitude));
        this.longitude = Double.valueOf(String.valueOf(longitude));
    }

    public void setTitle(String title) { this.title = title;}

    public void setDescription(String description) { this.description = description;}

    public void setRating(int rating) {this.rating = rating;}

    public int getRating(){return rating;}

    public void setConfirmationCount(int confirmationCount) { this.confirmationCount = confirmationCount;}

    public int getConfirmationCount(){return confirmationCount;}

    public String getDescription(){return description;}

    public String getTitle(){return title;}

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public void setIconID(int iconID) {
        this.iconID = iconID;
    }

    public int getIconID() {
        return iconID;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }


}
