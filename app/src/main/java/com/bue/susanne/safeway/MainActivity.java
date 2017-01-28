package com.bue.susanne.safeway;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;

import com.here.android.mpa.search.AutoSuggest;
import  com.here.android.mpa.search.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.common.OnEngineInitListener;
import com.here.android.mpa.mapping.Map;
import com.here.android.mpa.mapping.MapFragment;
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
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.provider.AlarmClock.EXTRA_MESSAGE;

public class MainActivity extends AppCompatActivity {
    public final static String EXTRA_MESSAGE = "com.bue.susanne.safeway.MESSAGE";

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

    /**
     * permissions request code
     */
    private final static int REQUEST_CODE_ASK_PERMISSIONS = 1;

    /**
     * Permissions that need to be explicitly requested from end user.
     */
    private static final String[] REQUIRED_SDK_PERMISSIONS = new String[] {
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.INTERNET };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        activity = this;
        super.onCreate(savedInstanceState);
        checkPermissions();
        initializeRouting();
    }

    public void onDestroy(){
        super.onDestroy();
    }

    private void initializeRouting(){
        final EditText startLocation = (EditText) findViewById(R.id.editStart);
        final EditText endLocation = (EditText) findViewById(R.id.editEnd);

        Button routeButton = (Button) findViewById(R.id.routingButton);

        routeButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println(v);
                String start = startLocation.getText().toString();
                if (start.equals("Your Location")){
                    if (currentLocationString != null){
                        start = currentLocationString;
                        useCurrentLocation = true;
                    }else{
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

    private void initializeGPS(){

        locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener(){
            @Override
            public void onLocationChanged(android.location.Location loc) {
                currentLocation = loc;
                if (loc != null){
                    updateLocation(loc);
                }
            }

            @Override
            public void onProviderDisabled(String provider) {}

            @Override
            public void onProviderEnabled(String provider) {}

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}
        };

        checkCallingPermission(ACCESS_FINE_LOCATION);
        currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0, locationListener);
        System.out.println(currentLocation);
        if (currentLocation != null) {
            updateLocation(currentLocation);
        }
    }



    private void geoCodeLocation(String start, String end){


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
                    if (data.size() > 0){
                        System.out.println(data.get(0).getCoordinate().getLatitude() + "," + data.get(0).getCoordinate().getLongitude());
                    }
                    System.out.println(endString);
                    if (start == null){
                        if (currentLocation != null && useCurrentLocation){
                            start = new Location(new GeoCoordinate(currentLocation.getLatitude(), currentLocation.getLongitude()));
                        }else{
                            start = data.get(0);
                        }
                        System.out.println("Now geocode second location");
                        GeocodeRequest request = new GeocodeRequest(endString).setSearchArea(searchLocation,5000);
                        request.execute(this);
                    }else{
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

    private void updateLocation(android.location.Location loc){
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
        }
        catch (IOException e) {
            e.printStackTrace();
        }

       // locationText.setText(loc.getLatitude() + "," + loc.getLongitude() + " - " + cityName);
    }

    private void initializeAutoComplete(){

        class AutoSuggester implements View.OnFocusChangeListener {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    System.out.println("Focus left " + v.getId());
                    final MultiAutoCompleteTextView autoCompleteTextEdit = (MultiAutoCompleteTextView) v;
                    String incompleteLocation = autoCompleteTextEdit.getText().toString();
                    System.out.println(incompleteLocation);
                    TextAutoSuggestionRequest request = null;
                    request = new TextAutoSuggestionRequest(incompleteLocation).setSearchCenter(map.getCenter());
                    class AutoSuggestionQueryListener implements ResultListener<List<AutoSuggest>> {

                        @Override
                        public void onCompleted(List<AutoSuggest> data, ErrorCode error) {
                            if (error != ErrorCode.NONE) {
                                System.err.println(error);
                            } else {
                                final String[] suggestions = new String[data.size()];


                                for (int i = 0; i < data.size() && i < 5; i++) {
                                    suggestions[i] = data.get(i).getTitle();
                                    System.out.println(suggestions[i]);
                                }
                                ArrayAdapter<String> adapter = new ArrayAdapter<String>(activity,
                                        android.R.layout.simple_dropdown_item_1line, suggestions);
                                autoCompleteTextEdit.setAdapter(adapter);

                                autoCompleteTextEdit.showDropDown();
                            }
                        }
                    }
                    ;

                    request.execute(new AutoSuggestionQueryListener());
                }
                ;
            }
        }

        final MultiAutoCompleteTextView startLocation = (MultiAutoCompleteTextView) findViewById(R.id.editStart);
        final EditText endLocation = (EditText) findViewById(R.id.editEnd);

            startLocation.setOnFocusChangeListener(new AutoSuggester());
        endLocation.setOnFocusChangeListener(new AutoSuggester());

    }

    private void initialize() {
        setContentView(R.layout.activity_main);
        initializeGPS();

        // Search for the map fragment to finish setup by calling init().
        mapFragment = (MapFragment)getFragmentManager().findFragmentById(
                R.id.mapfragment);
        mapFragment.init(new OnEngineInitListener() {
            @Override
            public void onEngineInitializationCompleted(
                    OnEngineInitListener.Error error)
            {
                if (error == OnEngineInitListener.Error.NONE) {
                    // retrieve a reference of the map from the map fragment
                    map = mapFragment.getMap();
                    routing = new SafeRouting(map);
                    initializeAutoComplete();

                    // Set the map center to the Vancouver region (no animation)
                    if (currentLocation != null){
                        map.setCenter(new GeoCoordinate(currentLocation.getLatitude(), currentLocation.getLongitude(), 0.0),
                                Map.Animation.NONE);
                    }else{
                        map.setCenter(new GeoCoordinate(52.511390, 13.400027, 0.0), Map.Animation.NONE);
                    }
                    // Set the zoom level to the average between min and max
                    map.setZoomLevel(
                            (map.getMaxZoomLevel() + map.getMinZoomLevel()) / 2);

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
}
