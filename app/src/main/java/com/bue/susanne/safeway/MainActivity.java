package com.bue.susanne.safeway;

import android.app.Activity;
import android.app.usage.UsageEvents;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.PointF;
import android.hardware.SensorEventListener;
import android.location.Address;
import android.location.Geocoder;

import com.google.gson.stream.JsonReader;

import com.here.android.mpa.mapping.MapObject;
import com.here.android.mpa.mapping.MapRoute;

import com.here.android.mpa.search.AutoSuggest;
import  com.here.android.mpa.search.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.common.Image;
import com.here.android.mpa.common.OnEngineInitListener;
import com.here.android.mpa.common.ViewObject;
import com.here.android.mpa.mapping.Map;
import com.here.android.mpa.mapping.MapFragment;
import com.here.android.mpa.mapping.MapGesture;
import com.here.android.mpa.mapping.MapMarker;
import com.here.android.mpa.search.ErrorCode;
import com.here.android.mpa.search.GeocodeRequest;
import com.here.android.mpa.search.ResultListener;
import com.here.android.mpa.search.TextAutoSuggestionRequest;

import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.Manifest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends AppCompatActivity {
    public final static String EXTRA_MESSAGE = "com.bue.susanne.safeway.MESSAGE";

    protected static MainActivity instance;

    // map embedded in the map fragment
    private Map map = null;
    private Activity activity;

    // map fragment embedded in this activity
    private MapFragment mapFragment = null;

    private LocationManager locationManager;
    private android.location.Location currentLocation;

    private SensorEventListener listener;
    private LocationListener locationListener;

    private boolean useCurrentLocation = false;

    private String currentLocationString;

    private SafeRouting routing;

    MyDialog dialog;
    RouteDialog routeDialog;

    PointF pointA;

    private EventListPOJO events;

    /**
     * permissions request code
     */
    private final static int REQUEST_CODE_ASK_PERMISSIONS = 1;

    /**
     * Permissions that need to be explicitly requested from end user.
     */
    private static final String[] REQUIRED_SDK_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.INTERNET};


    /** Get singleton instance of activity **/
    public static MainActivity getInstance() {
        return instance;
    }

    /** Returns context of this activity **/
    public static Context getContext(){
        return instance.getApplicationContext();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        activity = this;
        instance = this;
        super.onCreate(savedInstanceState);
        checkPermissions();
        initializeRouting();
        events = new EventListPOJO();
    }

    public void onDestroy() {
        super.onDestroy();
    }

    private void initializeRouting() {
        final EditText startLocation = (EditText) findViewById(R.id.editStart);
        final EditText endLocation = (EditText) findViewById(R.id.editEnd);

        Button routeButton = (Button) findViewById(R.id.routingButton);

        routeButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println(v);
                String start = startLocation.getText().toString();
                if (start.equals("Your Location")) {
                    if (currentLocationString != null) {
                        start = currentLocationString;
                        useCurrentLocation = true;
                    } else {
                        start = "Andreasstr. 10, Berlin";
                        useCurrentLocation = false;
                    }
                }
                String end = endLocation.getText().toString();

                System.out.println(start + " - " + end);
                geoCodeLocation(start, end);
            }
        });
    }

    private void initializeGPS() {

        locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(android.location.Location loc) {
                currentLocation = loc;
                if (loc != null) {
                    updateLocation(loc);
                }
            }

            @Override
            public void onProviderDisabled(String provider) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }
        };

        checkCallingPermission(ACCESS_FINE_LOCATION);
        currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0, locationListener);
//        if (currentLocation == null) {
//            currentLocation = new Location("");
//            currentLocation.setLatitude(52.522101);
//            currentLocation.setLongitude(13.413215);
//        }
        System.out.println(currentLocation);
        if (currentLocation != null) {
            updateLocation(currentLocation);
        }
    }


    private void geoCodeLocation(String start, String end) {


        final GeoCoordinate searchLocation = new GeoCoordinate(52.511390, 13.400027);
        GeocodeRequest request = new GeocodeRequest(start).setSearchArea(searchLocation, 5000);

        Location returnLocation;

        class GeocodeListener implements ResultListener<List<Location>> {

            public String endString;
            Location start;
            Location end;

            @Override
            public void onCompleted(List<Location> data, ErrorCode error) {

                if (error != ErrorCode.NONE) {
                    System.err.println(error);
                } else {
                    if (data.size() > 0) {
                        System.out.println(data.get(0).getCoordinate().getLatitude() + "," + data.get(0).getCoordinate().getLongitude());
                    }
                    System.out.println(endString);
                    if (start == null) {
                        if (currentLocation != null && useCurrentLocation) {
                            start = new Location(new GeoCoordinate(currentLocation.getLatitude(), currentLocation.getLongitude()));
                        } else {
                            start = data.get(0);
                        }
                        System.out.println("Now geocode second location");
                        GeocodeRequest request = new GeocodeRequest(endString).setSearchArea(searchLocation, 5000);
                        request.execute(this);
                    } else {
                        end = data.get(0);
                        routing.calculateRoute(start.getCoordinate(), end.getCoordinate());
                    }
                }
            }
        }
        GeocodeListener listener = new GeocodeListener();
        listener.endString = end;
        request.execute(listener);


    }

    private void updateLocation(android.location.Location loc) {
        /*------- To get city name from coordinates -------- */
        Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = gcd.getFromLocation(loc.getLatitude(),
                    loc.getLongitude(), 1);
            if (addresses.size() > 0) {
                System.out.println(addresses.get(0).getLocality());
                currentLocationString = addresses.get(0).toString();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // locationText.setText(loc.getLatitude() + "," + loc.getLongitude() + " - " + cityName);
    }

    private void initializeAutoComplete() {

        class AutoSuggester implements TextWatcher {

            AutoCompleteTextView view;


            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1,
                                          int arg2, int arg3) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onTextChanged(CharSequence cs, int start, int before,
                                      int count) {
            }

            @Override
            public void afterTextChanged(Editable e) {
                if (view.isPerformingCompletion()) {
                    // An item has been selected from the list. Ignore.
                    view.clearFocus();
                    return;
                }

                final String incompleteLocation = view.getText().toString();
                System.out.println(incompleteLocation);
                TextAutoSuggestionRequest request = null;
                if (incompleteLocation.isEmpty()) {
                    view.dismissDropDown();
                    return;
                }
                request = new TextAutoSuggestionRequest(incompleteLocation).setSearchCenter(map.getCenter());
                class AutoSuggestionQueryListener implements ResultListener<List<AutoSuggest>> {

                    @Override
                    public void onCompleted(List<AutoSuggest> data, ErrorCode error) {
                        if (error != ErrorCode.NONE) {
                            System.err.println(error);
                        } else {
                            final String[] suggestions = new String[data.size() + 1];
                            suggestions[0] = incompleteLocation;

                            for (int i = 0; i < data.size(); i++) {
                                suggestions[i + 1] = data.get(i).getTitle();
                            }
                            ArrayAdapter<String> adapter = new ArrayAdapter<String>(activity,
                                    android.R.layout.simple_dropdown_item_1line, suggestions);
                            view.setAdapter(adapter);
                            view.showDropDown();
                        }
                    }
                }
                ;

                request.execute(new AutoSuggestionQueryListener());
            }

            ;
        }

        final AutoCompleteTextView startLocation = (AutoCompleteTextView) findViewById(R.id.editStart);
        final AutoCompleteTextView endLocation = (AutoCompleteTextView) findViewById(R.id.editEnd);

        final AutoSuggester startSuggester = new AutoSuggester();
        startSuggester.view = startLocation;
        startLocation.addTextChangedListener(startSuggester);
        startLocation.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (parent != null) {
                    String text = (String) parent.getItemAtPosition(position);
                    System.out.println("SelectedItem: " + text);
                }

            }
        });

        AutoSuggester endSuggester = new AutoSuggester();
        endSuggester.view = endLocation;
        endLocation.addTextChangedListener(endSuggester);

    }

    private void initialize() {
        setContentView(R.layout.activity_main);
        initializeGPS();
        //addListenerOnButton();

        // Search for the map fragment to finish setup by calling init().
        mapFragment = (MapFragment) getFragmentManager().findFragmentById(
                R.id.mapfragment);
        mapFragment.init(new OnEngineInitListener() {
            @Override
            public void onEngineInitializationCompleted(
                    OnEngineInitListener.Error error) {


                if (error == OnEngineInitListener.Error.NONE) {
                    // retrieve a reference of the map from the map fragment
                    map = mapFragment.getMap();
                    routing = new SafeRouting(map);
                    initializeAutoComplete();

                    // Set the map center to the Vancouver region (no animation)
                    if (currentLocation != null) {
                        map.setCenter(new GeoCoordinate(currentLocation.getLatitude(), currentLocation.getLongitude(), 0.0),
                                Map.Animation.NONE);
                    } else {
                        map.setCenter(new GeoCoordinate(52.511390, 13.400027, 0.0), Map.Animation.NONE);
                    }
                    // Set the zoom level to pretty close (max = 18)
                    map.setZoomLevel(15);
                    loadMarkers();

                    mapFragment.getMapGesture().addOnGestureListener(new MapGesture.OnGestureListener() {

                        @Override
                        public void onPanStart() {

                        }

                        @Override
                        public void onPanEnd() {

                        }

                        @Override
                        public void onMultiFingerManipulationStart() {

                        }

                        @Override
                        public void onMultiFingerManipulationEnd() {

                        }

                        @Override
                        public boolean onMapObjectsSelected(List<ViewObject> list) {
                            if (list.size() > 0){
                                ViewObject selectedObject = list.get(0);
                                if (((MapObject)selectedObject).getType() == MapObject.Type.ROUTE){
                                    //get it to top
                                    ((MapRoute) selectedObject).setZIndex(1000);
                                    String routeInfo = routing.getSafeRouteInfo((MapRoute) selectedObject);
                                    routeDialog = RouteDialog.newInstance(routeInfo);

                                     routeDialog.show(getSupportFragmentManager(),"Route Info");
                                }
                            }
                            return false;
                        }

                        @Override
                        public boolean onTapEvent(PointF pointF) {
                            return false;
                        }

                        @Override
                        public boolean onDoubleTapEvent(PointF pointF) {
                            return false;
                        }

                        @Override
                        public void onPinchLocked() {

                        }

                        @Override
                        public boolean onPinchZoomEvent(float v, PointF pointF) {
                            return false;
                        }

                        @Override
                        public void onRotateLocked() {

                        }

                        @Override
                        public boolean onRotateEvent(float v) {
                            return false;
                        }

                        @Override
                        public boolean onTiltEvent(float v) {
                            return false;
                        }

                        @Override
                        public boolean onLongPressEvent(PointF pointF) {
                            dialog = new MyDialog();
                            dialog.show(getSupportFragmentManager(), "Dialog");
                            pointA = pointF;
                            return true;
                        }

                        @Override
                        public void onLongPressRelease() {

                        }

                        @Override
                        public boolean onTwoFingerTapEvent(PointF pointF) {
                            return false;
                        }
                    });
                } else {
                    System.out.println("ERROR: Cannot initialize Map Fragment");
                }
            }
        });

    }

    /**
     * Checks the dynamically-controlled permissions and requests missing permissions from end user.
     */
    protected void checkPermissions() {
        final List<String> missingPermissions = new ArrayList<String>();
        // check all required dynamic permissions
        for (final String permission : REQUIRED_SDK_PERMISSIONS) {
            final int result = ContextCompat.checkSelfPermission(this, permission);
            if (result != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission);
            }
        }
        if (!missingPermissions.isEmpty()) {
            // request all missing permissions
            final String[] permissions = missingPermissions
                    .toArray(new String[missingPermissions.size()]);
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_ASK_PERMISSIONS);
        } else {
            final int[] grantResults = new int[REQUIRED_SDK_PERMISSIONS.length];
            Arrays.fill(grantResults, PackageManager.PERMISSION_GRANTED);
            onRequestPermissionsResult(REQUEST_CODE_ASK_PERMISSIONS, REQUIRED_SDK_PERMISSIONS,
                    grantResults);
        }
    }

    public void mapEvent(View view) throws IOException {
        dialog.dismiss();
        int resource = 0;
        String title = "";
        String description = "";
        int safetyValue = 0;
        switch (view.getId()) {
            case R.id.imageButtonSafety:
                resource = R.drawable.safety;
                title = "Safety House";
                description = "Navigate here to find a secure place.";
                safetyValue = 1;
                break;
            case R.id.imageButtonLights:
                resource = R.drawable.streetlights;
                title = "Missing Streetlights";
                description = "On this street the lights are missing.";
                safetyValue = -1;
                break;
            case R.id.imageButtonPerson:
                resource = R.drawable.person;
                title = "Safety Person";
                description = "At this place you can find a SafeWay person.";
                safetyValue = 1;
                break;
            case R.id.imageButtonHotel:
                resource = R.drawable.hotel;
                title = "Hotel";
                description = "Here you find a hotel as safe point.";
                safetyValue = 1;
                break;
            case R.id.imageButtonCaution:
                resource = R.drawable.caution;
                title = "Caution";
                description = "Please be careful, a lot of danger here!";
                safetyValue = -1;
                break;
            case R.id.imageButtonGasStation:
                resource = R.drawable.gas_station;
                title = "Gas station";
                description = "Here you find a gas station as safe point.";
                safetyValue = 1;
                break;
        }
        storeMarkers(resource,title, description, safetyValue);
        Toast.makeText(MainActivity.this,
                "Thanks for your contribution, event added to the database!", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                for (int index = permissions.length - 1; index >= 0; --index) {
                    if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                        // exit the app if one permission is not granted
                        Toast.makeText(this, "Required permission '" + permissions[index]
                                + "' not granted, exiting", Toast.LENGTH_LONG).show();
                        finish();
                        return;
                    }
                }
                // all permissions were granted
                initialize();

                break;
        }
    }


    public void storeMarkers(int resource, String title, String description, int safetyValue) throws IOException {

        com.here.android.mpa.common.Image img =
                new com.here.android.mpa.common.Image();

        img.setImageResource(resource);

        GeoCoordinate location = map.pixelToGeo(pointA);
        EventPOJO event = new EventPOJO(location.getLatitude(), location.getLongitude());
        event.setIconID(resource);
        event.setDescription(description);
        event.setTitle(title);
        event.setSafetyValue(safetyValue);

         events.addEvent(event);
        Gson gson = new GsonBuilder().create();
        String events_string = gson.toJson(events);
        System.out.println(events_string);

        MapMarker marker = new MapMarker();
        marker.setIcon(img);
        marker.setTitle(title);
        marker.setDescription(description);
        marker.setDraggable (true);
        marker.setZIndex(1001);
        marker.setCoordinate(new GeoCoordinate(event.getLatitude(), event.getLongitude()));
        map.addMapObject(marker);
    }

    public void loadMarkers() {
        Gson gson = new GsonBuilder().create();

        InputStream is = getResources().openRawResource(R.raw.events);
        int size = 0;
        try {
            size = is.available();

        byte[] buffer = new byte[size];
        is.read(buffer);
        is.close();
        String json_string = new String(buffer, "UTF-8");
        EventListPOJO eventListPOJO = gson.fromJson(json_string, EventListPOJO.class);

        for (EventPOJO event : eventListPOJO.getEvents()){
            MapMarker marker = new MapMarker();
            com.here.android.mpa.common.Image img =
                    new com.here.android.mpa.common.Image();
            img.setImageResource(event.getIconID());
            marker.setIcon(img);
            marker.setDraggable (true);
            marker.setZIndex(1001);
            marker.setCoordinate(new GeoCoordinate(event.getLatitude(), event.getLongitude()));
            map.addMapObject(marker);

        }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
