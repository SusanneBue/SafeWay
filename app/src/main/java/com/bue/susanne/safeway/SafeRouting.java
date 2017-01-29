package com.bue.susanne.safeway;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.common.GeoBoundingBox;
import com.here.android.mpa.mapping.Map;
import com.here.android.mpa.mapping.MapMarker;
import com.here.android.mpa.mapping.MapRoute;
import com.here.android.mpa.routing.Route;
import com.here.android.mpa.routing.RouteManager;
import com.here.android.mpa.routing.RouteOptions;
import com.here.android.mpa.routing.RoutePlan;
import com.here.android.mpa.routing.RouteResult;

import org.locationtech.spatial4j.context.SpatialContext;
import org.locationtech.spatial4j.context.SpatialContextFactory;
import org.locationtech.spatial4j.shape.Point;
import org.locationtech.spatial4j.shape.Shape;
import org.locationtech.spatial4j.shape.SpatialRelation;
import org.locationtech.spatial4j.shape.impl.PointImpl;
import org.locationtech.spatial4j.shape.impl.ShapeFactoryImpl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * Created by Susanne on 28.01.2017.
 */

public class SafeRouting {

    private Map map = null;

    private HashMap<Route, SafeRouteInfos> safeRouteInfos = new HashMap();

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
        routeOptions.setRouteCount(10);
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

                //always render first route
                MapRoute mapRoute = new MapRoute(routeResult.get(0).getRoute());
                GeoBoundingBox routeBox = mapRoute.getRoute().getBoundingBox();
                map.setCenter(routeBox.getCenter(), Map.Animation.NONE);
                if (!map.getBoundingBox().contains(routeBox)) {
                    while (!map.getBoundingBox().contains(routeBox)) {
                        map.setZoomLevel(map.getZoomLevel() - 1);
                    }
                }else {
                    //zoom into map
                    while (map.getBoundingBox().contains(routeBox.getBottomRight()) && map.getBoundingBox().contains(routeBox.getTopLeft()) && (map.getZoomLevel() + 1) < map.getMaxZoomLevel()) {
                        map.setZoomLevel(map.getZoomLevel() + 1);
                        System.out.println(map.getZoomLevel());
                    }
                    if (!(map.getBoundingBox().contains(routeBox.getBottomRight())) || !map.getBoundingBox().contains(routeBox.getTopLeft())) {
                        map.setZoomLevel(map.getZoomLevel() - 1);
                    }
                }

                map.addMapObject(mapRoute);

                SafeRouteInfos infos1 = new SafeRouteInfos();
                int tta =  mapRoute.getRoute().getTta(Route.TrafficPenaltyMode.DISABLED, mapRoute.getRoute().getSublegCount()-1).getDuration() / 60;
                infos1.minutes = tta;
                matchEvents(mapRoute,infos1);
                safeRouteInfos.put(mapRoute.getRoute(), infos1);
                mapRoute.setColor(infos1.getColor());

                int safest = infos1.getSafetyLevel();
                MapRoute safestRoute = mapRoute;

                for (int i = 1; i <routeResult.size(); i++){
                    mapRoute = new MapRoute(routeResult.get(i).getRoute());

                    SafeRouteInfos infos = new SafeRouteInfos();
                    tta =  mapRoute.getRoute().getTta(Route.TrafficPenaltyMode.DISABLED, mapRoute.getRoute().getSublegCount()-1).getDuration() / 60;
                    infos.minutes = tta;
                    matchEvents(mapRoute,infos);
                    safeRouteInfos.put(mapRoute.getRoute(), infos);
                    mapRoute.setColor(infos.getColor());
                    if (safest <= infos.getSafetyLevel()){
                        safest = infos.getSafetyLevel();
                        safestRoute = mapRoute;
                    }
                }

                map.addMapObject(safestRoute);

            }
            else {
                // Display a message indicating route calculation failure
            }
        }
    }




    public void matchEvents(MapRoute route, SafeRouteInfos infos) {
        Gson gson = new GsonBuilder().create();

        Context context = MainActivity.getContext();

        InputStream is = context.getResources().openRawResource(R.raw.events);

        //String file = "/res/raw/events2.json";
        //Resources res = this.context.getResources();

        //InputStream is = getClass().getClassLoader().getResourceAsStream(file);
        int size = 0;
        try {
            size = is.available();

            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json_string = new String(buffer, "UTF-8");
            EventListPOJO eventListPOJO = gson.fromJson(json_string, EventListPOJO.class);
            int safetyValue = 0;
            for (EventPOJO event : eventListPOJO.getEvents()) {
                boolean onRoute = isOnRoute(createRouteFromCoordinates(route), event.getLatitude(),event.getLongitude());
                if (onRoute){
                    if (event.getSafetyValue()>0) {
                        infos.setNumberOfSafetyEvents(infos.getNumberOfSafetyEvents() + 1);
                    }else {
                        infos.setNumberOfDangerEvents(infos.getNumberOfDangerEvents() + 1);
                    }
                }
            }
           // System.out.println("Safe: " + infos.getNumberOfSafetyEvents());

        }catch (IOException e){
        }
    }

    private Shape createRouteFromCoordinates(MapRoute mapRoute) {
        SpatialContextFactory ctxFactory = new SpatialContextFactory();
        // ctxFactory.geo = true;
        SpatialContext ctx = ctxFactory.newSpatialContext();
        ArrayList<Point> points = new ArrayList<Point>();

        for (GeoCoordinate coordinate : mapRoute.getRoute().getRouteGeometry()) {

            // prepare points for line in spatial4j
            points.add(new PointImpl(coordinate.getLatitude(), coordinate.getLongitude(), ctx));
        }

        ShapeFactoryImpl sfImpl = new ShapeFactoryImpl(ctx, ctxFactory);
        double buffer = 0.000899320368; //100 meters
        Shape shapeOfRoute = sfImpl.lineString(points, buffer);
        return shapeOfRoute;
    }

    private boolean isOnRoute(Shape shapeOfRoute, double latitude, double longitude) {
        SpatialContextFactory ctxFactory = new SpatialContextFactory();
        // ctxFactory.geo = true;
        SpatialContext ctx = ctxFactory.newSpatialContext();
        PointImpl point = new PointImpl(latitude, longitude, ctx);

        SpatialRelation relation = shapeOfRoute.relate(point);
        if (relation != SpatialRelation.CONTAINS) {
            return false;
        }
        return true;
    }

}
