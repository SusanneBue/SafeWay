package com.bue.susanne.safeway;

import android.graphics.Color;

/**
 * Created by Susanne on 28.01.2017.
 */

public class SafeRouteInfos {

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


    public SafeRouteInfos() {
    }

    public int getSafetyLevel(){
        System.out.println( "safety pionts: " + getNumberOfSafetyEvents());
        System.out.println(" danger points: " + getNumberOfDangerEvents());
        return (getNumberOfSafetyEvents() - getNumberOfDangerEvents());
    }

    public int getColor(){

        if (getSafetyLevel() < 0){
            return Color.parseColor("#FF0000");
        }else{
            if (getSafetyLevel() == 0){
                return Color.parseColor("#FF9500");
            }else{
                return Color.parseColor("#1CA800");
            }

        }
    }

    public String toString(){
        return "Route Safety: " + getSafetyLevel() + "\n" + "Safety points: " + this.numberOfSafetyEvents +"\n" + "Danger points: " + this.numberOfDangerEvents;
    }

}
