package com.bue.susanne.safeway;

import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.common.GeoBoundingBox;
import com.here.android.mpa.mapping.Map;
import com.here.android.mpa.mapping.MapContainer;
import com.here.android.mpa.mapping.MapMarker;
import com.here.android.mpa.mapping.MapRoute;
import com.here.android.mpa.routing.Route;
import com.here.android.mpa.routing.RouteManager;
import com.here.android.mpa.routing.RouteOptions;
import com.here.android.mpa.routing.RoutePlan;
import com.here.android.mpa.routing.RouteResult;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * Created by Susanne on 28.01.2017.
 */

public class SafeRouting {

    private Map map = null;

    private HashMap<Route, SafeRouteInfos> safeRouteInfos = new HashMap<Route, SafeRouteInfos>();

    public SafeRouting(Map map){
        this.map =  map;
        System.out.println("Map HERE" + map);

    }

    public String getSafeRouteInfo(MapRoute route){
        SafeRouteInfos infos = safeRouteInfos.get(route.getRoute());
        if (infos == null){
            return "Route Safety: medium";
        }else{
            return infos.toString();
        }
    }

    public void calculateRoute(GeoCoordinate start, GeoCoordinate end) {
        RouteManager rm = new RouteManager();

        // Create the RoutePlan and add two waypoints
        RoutePlan routePlan = new RoutePlan();
        routePlan.addWaypoint(start);
        routePlan.addWaypoint(end);

        // Create the RouteOptions and set its transport mode & routing type
        RouteOptions routeOptions = new RouteOptions();
        routeOptions.setTransportMode(RouteOptions.TransportMode.PEDESTRIAN);
        routeOptions.setRouteType(RouteOptions.Type.FASTEST);
        routeOptions.setRouteCount(5);
        //routeOptions.setParksAllowed(false);
        routePlan.setRouteOptions(routeOptions);

        // Calculate the route
        rm.calculateRoute(routePlan, new RouteListener());

        MapMarker startMarker = new MapMarker();
        startMarker.setCoordinate(start);
        map.addMapObject(startMarker);

        MapMarker endMarker = new MapMarker();
        endMarker.setCoordinate(end);
        map.addMapObject(endMarker);

    }

    private class RouteListener implements RouteManager.Listener {

        // Method defined in Listener
        public void onProgress(int percentage) {
            // Display a message indicating calculation progress
        }

        // Method defined in Listener
        public void onCalculateRouteFinished(RouteManager.Error error, List<RouteResult> routeResult) {
            // If the route was calculated successfully
            if (error == RouteManager.Error.NONE) {
                // Render the route on the map
                System.out.println("Found " + routeResult.size() + " routes");
                for (RouteResult routeResultItem : routeResult){
                    System.out.println("Route: " + routeResultItem.getRoute());
                    MapRoute mapRoute = new MapRoute(routeResultItem.getRoute());
                    GeoBoundingBox routeBox = mapRoute.getRoute().getBoundingBox();
                    map.setCenter(routeBox.getCenter(), Map.Animation.NONE);
                    while(!map.getBoundingBox().contains(routeBox)){
                        map.setZoomLevel(map.getZoomLevel() -1);
                    }

                    map.addMapObject(mapRoute);

                    int dangerLevel = new Random().nextInt(11);
                    System.out.println(dangerLevel);
                    SafeRouteInfos infos = new SafeRouteInfos(dangerLevel);
                    safeRouteInfos.put(mapRoute.getRoute(), infos);
                    mapRoute.setColor(infos.getColor());
                }
            }
            else {
                // Display a message indicating route calculation failure
            }
        }
    }


}
