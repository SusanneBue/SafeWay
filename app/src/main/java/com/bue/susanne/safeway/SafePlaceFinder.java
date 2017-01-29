package com.bue.susanne.safeway;

import android.graphics.Color;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.here.android.mpa.common.GeoBoundingBox;
import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.mapping.Map;
import com.here.android.mpa.mapping.MapMarker;
import com.here.android.mpa.mapping.MapRoute;
import com.here.android.mpa.routing.Route;
import com.here.android.mpa.routing.RouteManager;
import com.here.android.mpa.routing.RouteOptions;
import com.here.android.mpa.routing.RoutePlan;
import com.here.android.mpa.routing.RouteResult;
import com.here.android.mpa.search.Location;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Susanne on 29.01.2017.
 */

public class SafePlaceFinder {

    Map map;
    EventListPOJO events;

    public SafePlaceFinder(Map map, EventListPOJO events){
        this.map = map;
        this.events = events;
    }

    public void findSafePlace(android.location.Location location){
        GeoCoordinate currentLocation = new GeoCoordinate(location.getLatitude(), location.getLongitude(),0.0);


        ArrayList<GeoCoordinate> safePlaces = new ArrayList<GeoCoordinate>();
            for (EventPOJO event : events.getEvents()){
                if (event.getSafetyValue() > 0){
                    GeoCoordinate eventCoordinate = new GeoCoordinate(event.getLatitude(), event.getLongitude(), 0.0);
                    safePlaces.add(eventCoordinate);
                }
            }
            calculateRoutesToSafePlaces(currentLocation, safePlaces, 0, null);
    }

    public void calculateRoutesToSafePlaces(GeoCoordinate start, ArrayList<GeoCoordinate> ends, int i, Route best) {
        RouteManager rm = new RouteManager();

        if (ends.size() == 0){
            return;
        }

        // Create the RoutePlan and add two waypoints
        RoutePlan routePlan = new RoutePlan();
        routePlan.addWaypoint(start);
        routePlan.addWaypoint(ends.get(i));

        // Create the RouteOptions and set its transport mode & routing type
        RouteOptions routeOptions = new RouteOptions();
        routeOptions.setTransportMode(RouteOptions.TransportMode.PEDESTRIAN);
        routeOptions.setRouteType(RouteOptions.Type.FASTEST);
        //routeOptions.setParksAllowed(false);
        routePlan.setRouteOptions(routeOptions);

        // Calculate the route
        SafePlaceFinder.RouteListener listener = new SafePlaceFinder.RouteListener();
        listener.start = start;
        listener.ends = ends;
        listener.bestRoute = best;
        listener.i = i;
        rm.calculateRoute(routePlan, listener);

        MapMarker startMarker = new MapMarker();
        startMarker.setCoordinate(start);
        map.addMapObject(startMarker);

    }

    private class RouteListener implements RouteManager.Listener {

        public GeoCoordinate start;
        public Route bestRoute;
        public ArrayList<GeoCoordinate> ends;
        public int i;

        // Method defined in Listener
        public void onProgress(int percentage) {
            // Display a message indicating calculation progress
        }

        // Method defined in Listener
        public void onCalculateRouteFinished(RouteManager.Error error, List<RouteResult> routeResult) {
            // If the route was calculated successfully
            if (error == RouteManager.Error.NONE) {

                if (routeResult.size() != 0){
                    Route safeRoute = routeResult.get(0).getRoute();
                    if (bestRoute == null || bestRoute.getTta(Route.TrafficPenaltyMode.DISABLED, bestRoute.getSublegCount()-1).getDuration() > safeRoute.getTta(Route.TrafficPenaltyMode.DISABLED, safeRoute.getSublegCount()-1).getDuration()){
                        bestRoute = safeRoute;
                        System.out.println("Safe Poiint: " + safeRoute);
                    }
                }

                if (i + 1 >= ends.size()) {
                    MapRoute mapRoute = new MapRoute(bestRoute);
                    GeoBoundingBox routeBox = mapRoute.getRoute().getBoundingBox();
                    map.setCenter(routeBox.getCenter(), Map.Animation.NONE);
                    if (!map.getBoundingBox().contains(routeBox)) {
                        while (!map.getBoundingBox().contains(routeBox)) {
                            map.setZoomLevel(map.getZoomLevel() - 1);
                        }
                    }

                    map.addMapObject(mapRoute);
                    mapRoute.setColor(Color.parseColor("#0508E8"));
                }else{
                    calculateRoutesToSafePlaces(start, ends, i+1, bestRoute);
                }
            }
            else {
                // Display a message indicating route calculation failure
            }
        }
    }

}
