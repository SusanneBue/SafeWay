package com.bue.susanne.safeway;

import android.graphics.Color;

/**
 * Created by Susanne on 28.01.2017.
 */

public class SafeRouteInfos {

    private int dangerLevel = 0;

    private int numberOfDangerEvents = 0;

    private int numberOfSafetyEvents = 0;

    public int getNumberOfDangerEvents() {
        return numberOfDangerEvents;
    }

    public int getNumberOfSafetyEvents() {
        return numberOfSafetyEvents;
    }

    public void setNumberOfDangerEvents(int numberOfDangerEvents) {
        this.numberOfDangerEvents = numberOfDangerEvents;
    }

    public void setNumberOfSafetyEvents(int numberOfSafetyEvents) {
        this.numberOfSafetyEvents = numberOfSafetyEvents;
    }


    public SafeRouteInfos(int dangerLevel) {
        this.dangerLevel = dangerLevel;
    }

    public int getDangerLevel(){
        return this.dangerLevel;
    }

    public int getColor(){
        if (dangerLevel > 7){
            return Color.parseColor("#FF0000");
        }else{
            if (dangerLevel > 4){
                return Color.parseColor("#FF9500");
            }else{
                return Color.parseColor("#1CA800");
            }

        }
    }

    public String toString(){
        return "Route Safety: " + this.dangerLevel + "\n" + "Safety points: " + this.numberOfSafetyEvents +"\n" + "Danger points: " + this.numberOfSafetyEvents;
    }

}
