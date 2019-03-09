package com.cdps.proxifood;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatAutoCompleteTextView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.Normalizer;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;


import org.osmdroid.config.Configuration;
import org.osmdroid.events.DelayedMapListener;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import java.text.Normalizer.Form;
import android.app.AlertDialog;

public class SearchFragment extends Fragment {

    private static final String TAG = "[SearchFragment] ";

    private MapView map = null;
    private FloatingActionButton relocateBtn;
    LocationManager lm;
    private AddressListAdapter autoSuggestAdapter;
    private Handler handler;

    private static final int MY_PERMISSIONS_REQUEST_COARSE_LOCATION = 1;
    private static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 2;
    private static final long MINIMUM_DISTANCE_CHANGE_FOR_UPDATES = 1; // in Meters
    private static final long MINIMUM_TIME_BETWEEN_UPDATES = 1000; // in Milliseconds
    private static final double DEFAULT_LATITUDE = 48.8534;
    private static final double DEFAULT_LONGITUDE = 2.3488 ;
    private static final int TRIGGER_AUTO_COMPLETE = 100;
    private static final long AUTO_COMPLETE_DELAY = 300;
    private static final double DEFAULT_ZOOM = 16.0;

    private RequestQueue queue;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_research, container, false);
        ViewTreeObserver vto = view.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

            }
        });

        return view;
    }





    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((MainActivity) getActivity()).setActionBarTitle(getString(R.string.title_research));


        //handle permissions first, before map is created. not depicted here

        //load/initialize the osmdroid configuration, this can be done
        Context ctx = getActivity().getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        //setting this before the layout is inflated is a good idea
        //it 'should' ensure that the map has a writable location for the map cache, even without permissions
        //if no tiles are displayed, you can try overriding the cache path using Configuration.getInstance().setCachePath
        //see also StorageUtils
        //note, the load method also sets the HTTP User Agent to your application's package name, abusing osm's tile servers will get you banned based on this string

        //inflate and create the map
        initializeComponents();

        lm = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        if ( ContextCompat.checkSelfPermission( getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
            Log.i("PERMISSION", "REQUEST PERMISSION create");

            requestPermissions(new String[] {  android.Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_COARSE_LOCATION );
        }

        else {

            setUpMap();
            map.getController().setZoom(DEFAULT_ZOOM);
        }

    }


    private void setUpMap() {
        queue = Volley.newRequestQueue(getActivity().getApplicationContext());

        if ( ContextCompat.checkSelfPermission( getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION ) == PackageManager.PERMISSION_GRANTED ) {
            Log.i("ICI", "YEAh");
            lm.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    MINIMUM_TIME_BETWEEN_UPDATES,
                    MINIMUM_DISTANCE_CHANGE_FOR_UPDATES,
                    new MyLocationListener()
            );
        }
        lm.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                MINIMUM_TIME_BETWEEN_UPDATES,
                MINIMUM_DISTANCE_CHANGE_FOR_UPDATES,
                new MyLocationListener()
        );
        map.setMultiTouchControls(true);
        showCurrentLocation();

        final AppCompatAutoCompleteTextView autoCompleteTextView =
                getActivity().findViewById(R.id.auto_complete_address);

        //Setting up the adapter for AutoSuggest
        autoSuggestAdapter = new AddressListAdapter(getActivity(),
                android.R.layout.simple_dropdown_item_1line, new ArrayList<Address>());
        autoCompleteTextView.setThreshold(2);
        autoCompleteTextView.setAdapter(autoSuggestAdapter);
        autoCompleteTextView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        Address a = autoSuggestAdapter.getObject(position);
                        autoCompleteTextView.setText(a.getFormatted());
                        map.getController().setCenter(new GeoPoint(a.getLatitude(), a.getLongitude()));
                        map.getController().setZoom(DEFAULT_ZOOM);
                    }
                });

        autoCompleteTextView.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int before,
                                          int count) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                handler.removeMessages(TRIGGER_AUTO_COMPLETE);
                handler.sendEmptyMessageDelayed(TRIGGER_AUTO_COMPLETE, AUTO_COMPLETE_DELAY);
            }

            @Override
            public void afterTextChanged(Editable e) {
            }
        });

        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == TRIGGER_AUTO_COMPLETE) {
                    if (!TextUtils.isEmpty(autoCompleteTextView.getText())) {
                        makeApiCall(autoCompleteTextView.getText().toString());
                    }
                }
                return false;
            }
        });


        map.setMapListener(new DelayedMapListener(new MapListener() {
            public boolean onZoom(final ZoomEvent e) {
                //do something
                Log.i("ZOOM", "ZOom !");
                if (map.getZoomLevel() > 10.0)
                    displayOffers(map.getBoundingBox());
                return true;
            }

            public boolean onScroll(final ScrollEvent e) {
                Log.i("NEW BOUNDING BOX", map.getBoundingBox().toString());

                if (map.getZoomLevel() > 10.0)
                    displayOffers(map.getBoundingBox());

                return true;
            }
        }, 1000));
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        Log.i("REQUEST CODE", Integer.toString(requestCode));
        Log.i("GRANTRESULTS", grantResults.toString());
        Log.i("O", Integer.toString(grantResults[0]));        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_COARSE_LOCATION: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i("PERMISSION", "GRANTED");

                    setUpMap();
                } else {
                    Log.i("PERMISSION", "DENIED");

                    getActivity().finish();

                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void displayOffers(BoundingBox box) {
        Log.i("ACTION", "Requesting offers...");
        final double minLongitude = box.getLonWest();
        final double minLatitude = box.getLatSouth();
        final double maxLongitude = box.getLonEast();
        final double maxLatitude = box.getLatNorth();

        final HashMap<Long, ArrayList<JSONObject>> hashmap = new HashMap<Long, ArrayList<JSONObject>>();

        String url = Globals.SERVER_ADDRESS + "/offers";
        Log.i(Globals.TAG_REQUEST, url);
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.i(TAG + Globals.TAG_SERVER_RESPONSE, response.toString());
                        for(int i=0; i<response.length(); i++) {
                            try {
                                JSONObject offer = response.getJSONObject(i);
                                String end = offer.getString("endOfInscription");
                                TimeZone tz = TimeZone.getTimeZone("UTC");
                                DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");

                                df.setTimeZone(tz);

                                Log.i("Date", df.parse(end).toString());
                                Date endOfInscription =  df.parse(end);

                                Date currentTime = Calendar.getInstance().getTime();
                                // Display only still available offers
                                if(currentTime.getTime() < endOfInscription.getTime()) {
                                    JSONObject creator = offer.getJSONObject("creator");
                                    // Display only other users offers
                                    if(creator.getLong("id") != Globals.getUserId()) {
                                        ArrayList<JSONObject> offers;
                                        if (hashmap.containsKey(creator.getLong("id"))) {
                                            offers = hashmap.get((creator.getLong("id")));
                                        } else {
                                            offers = new ArrayList<JSONObject>();
                                        }
                                        offers.add(offer);
                                        hashmap.put(((Long) creator.getLong("id")), offers);
                                    }
                                }
                            } catch (JSONException | ParseException e) {
                                e.printStackTrace();
                            }
                        }

                        addMarkers(hashmap);

                    }




                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }) {

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("latitudeMin", Double.toString(minLatitude));
                params.put("longitudeMin", Double.toString(minLongitude));
                params.put("latitudeMax", Double.toString(maxLatitude));
                params.put("longitudeMax", Double.toString(maxLongitude));
                params.put("token", Globals.getSessionToken());

                Log.i("PARAMS", params.toString());
                return params;
            }
        };
        request.setTag("MAP");

        queue.add(request);
    }


    private void addMarkers(HashMap<Long, ArrayList<JSONObject>> hashmap) {
        for (Map.Entry<Long, ArrayList<JSONObject>> entry : hashmap.entrySet()) {
            Long key = entry.getKey();
            ArrayList<JSONObject> offers = entry.getValue();
            JSONObject first = offers.get(0);

            try {
                JSONObject creator = first.getJSONObject("creator");
                JSONObject address = creator.getJSONObject("homeAddress");
                Log.i("ADDRESS", address.toString());
                Double longitude = address.getDouble("longitude");
                Double latitude = address.getDouble("latitude");

                GeoPoint location = new GeoPoint(latitude, longitude);

                if(getActivity()!=null) {
                    Marker marker = new Marker(map);
                    marker.setPosition(location);
                    marker.setIcon(getResources().getDrawable(R.drawable.ic_map_marker));
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                    marker.setInfoWindow(new MapCustomInfoBubble(this.getContext(), map, offers));
                    map.getOverlays().add(marker);

                }
            } catch(JSONException e) {
                e.printStackTrace();
            }

            // ...
        }

    }

    protected void showCurrentLocation() {

        if ( ContextCompat.checkSelfPermission( getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
            Log.i("PERMISSION", "REQUEST PERMISSION current Pos");
            requestPermissions(new String[] {  android.Manifest.permission.ACCESS_COARSE_LOCATION  },
                    MY_PERMISSIONS_REQUEST_COARSE_LOCATION );
        }
        Location location = getLastBestLocation();

        if (location != null && location.getLatitude() !=0 && location.getLongitude()!=0) {
            map.getController().setZoom(DEFAULT_ZOOM);
            map.getController().setCenter(new GeoPoint(location.getLatitude(), location.getLongitude()));
        }
        else {

            RequestQueue queue = Volley.newRequestQueue(getActivity());
            String url = Globals.SERVER_ADDRESS + "/users/" + Globals.getUserId();
            Log.i("TOKEN", Globals.getSessionToken());
            Log.i(Globals.TAG_REQUEST, url);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            double longitude;
                            double latitude;
                            try {
                                JSONObject address = response.getJSONObject("homeAddress");
                                if(address.get("longitude") != null && address.get("latitude") != null) {
                                    longitude = address.getDouble("longitude");
                                    latitude = address.getDouble("latitude");
                                }
                                else {
                                    longitude = DEFAULT_LONGITUDE;
                                    latitude = DEFAULT_LATITUDE;
                                }
                                Log.i("LONGITUDE", Double.toString(longitude));
                                Log.i("LATITUDE", Double.toString(latitude));
                                map.getController().setZoom(DEFAULT_ZOOM);
                                map.getController().setCenter(new GeoPoint(latitude, longitude));


                            } catch(JSONException e) {
                                e.printStackTrace();
                            }
                        }

                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(Globals.TAG_SERVER_ERROR, error.getMessage());
                }
            }) {

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("token", Globals.getSessionToken());
                    return params;
                }
            };
            queue.add(request);
    /*        location = new Location("");
            location.setLatitude(DEFAULT_LATITUDE);
            location.setLongitude(DEFAULT_LONGITUDE);
            map.getController().setZoom(DEFAULT_ZOOM);*/

        }

 //

    }

    private class MyLocationListener implements LocationListener {

        public void onLocationChanged(Location location) {
            String message = String.format(
                    "New Location \n Longitude: %1$s \n Latitude: %2$s",
                    location.getLongitude(), location.getLatitude()
            );
           Log.i("MESSAGE", message);
        }

        public void onStatusChanged(String s, int i, Bundle b) {
            Log.i("MESSAGE", "Provider status changed");
        }

        public void onProviderDisabled(String s) {
            Log.i("MESSAGE", "Provider disabled by the user. GPS turned off");
        }

        public void onProviderEnabled(String s) {
            Log.i("MESSAGE", "Provider enabled by the user. GPS turned on");
        }

    }

    public void onResume(){
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        map.onResume(); //needed for compass, my location overlays, v6.0.0 and up
        Log.i("STATUS", "RESUME");
    }

    public void onPause(){
        super.onPause();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        map.onPause();  //needed for compass, my location overlays, v6.0.0 and up
        Log.i("STATUS", "PAUSE");
        queue.cancelAll("MAP");

    }

    private void makeApiCall(final String text) {
        RequestQueue queue = Volley.newRequestQueue(getActivity());

        Log.i("TEXT", text);
        JSONObject json = new JSONObject();
        try {
            json.put("q", "Moabit%2C%20Berlin");
            json.put("key", "cb8aa02f6c78442282a92fa33760ef96");
            json.put("language", "fr");
            Log.i(Globals.TAG_REQUEST, "Create user");
        }
        catch(JSONException e) {
            Log.i("EXCEPTION","exception");
        }

        try {
            String url = "https://www.mapquestapi.com/geocoding/v1/address?key=0UCY2i85UiMToB5kBqcLZ5upu6t3oQoO&inFormat=kvp&outFormat=json&location="+ text+ "&thumbMaps=false";
            Log.i("INFO", url);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.i(Globals.TAG_SERVER_RESPONSE, response.toString());
                            List<Address> addressList = new ArrayList<>();
                            String searchText = text.toLowerCase();
                            searchText = Normalizer.normalize(searchText, Form.NFD)
                                    .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
                            try {
                                JSONArray array = response.getJSONArray("results");
                                for (int i = 0; i < array.length(); i++) {
                                    JSONObject row = array.getJSONObject(i);
                                    JSONArray locations = row.getJSONArray("locations");
                                    for (int j = 0; j < locations.length(); j++) {

                                        JSONObject loc = locations.getJSONObject(j);
                                        Log.i("JSON", loc.toString());
                                        String postcode = null;
                                        String country = null;
                                        String formatted = "";

                                        if (loc.has("street") && loc.getString("street").equals("") && loc.has("adminArea5") && loc.has("geocodeQuality") && loc.getString("geocodeQuality").equals("CITY")) {
                                            String city = Normalizer.normalize(loc.getString("adminArea5"), Form.NFD)
                                                    .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
                                            city = city.toLowerCase();
                                            if (searchText.contains(city) || city.contains(searchText)) {
                                                String town = loc.getString("adminArea5");
                                                formatted += town + " ";

                                                if (loc.has("postalCode") && !loc.getString("postalCode").equals("")) {
                                                    postcode = loc.getString("postalCode");
                                                    formatted += ", " + postcode + " ";
                                                }

                                                if (loc.has("adminArea3") && !loc.getString("adminArea3").equals("")) {
                                                    String county = loc.getString("adminArea3");
                                                    formatted += ", " + county + " ";
                                                }

                                                if (loc.has("adminArea1")) {
                                                    country = loc.getString("adminArea1");
                                                    formatted += ", " + country;
                                                }

                                                double longitude = 0;
                                                double latitude = 0;

                                                if (loc.has("latLng")) {
                                                    JSONObject coordinates = loc.getJSONObject("latLng");
                                                    longitude = coordinates.getDouble("lng");

                                                    latitude = coordinates.getDouble("lat");
                                                    country = loc.getString("adminArea1");
                                                }

                                                addressList.add(new Address(postcode, town, country, null, null, formatted, longitude, latitude));

                                            }
                                        }
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            //IMPORTANT: set data here and notify
                            autoSuggestAdapter.setData(addressList);
                            autoSuggestAdapter.notifyDataSetChanged();
                        }
                        },
                                new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.i(Globals.TAG_FAILURE, "ERROR API");
                            }
                        });
            queue.add(request);

        } catch (Exception e)  {
            e.printStackTrace();
        }
    }

    private void initializeComponents() {
        map = getView().findViewById(R.id.map);
        relocateBtn = getView().findViewById(R.id.relocate_button);

        relocateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                relocateUser();
            }
        });
    }

    private void relocateUser() {
        try {
            boolean gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if(!gps_enabled) {
                AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
                alertDialog.setTitle("Géolocalisation");
                alertDialog.setMessage("Vous devez activer votre localisation pour utiliser cette fonctionnalité");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
            }
            else {
                Log.i("INFO", "ELSE");
                if ( ContextCompat.checkSelfPermission( getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {

                    requestPermissions(new String[] {  android.Manifest.permission.ACCESS_COARSE_LOCATION  },
                            MY_PERMISSIONS_REQUEST_COARSE_LOCATION );
                }

                Location location = getLastBestLocation();

                if (location != null && location.getLatitude() !=0 && location.getLongitude()!=0) {
                    Log.i("LOCATION", Double.toString(location.getLatitude()));
                    map.getController().setCenter(new GeoPoint(location.getLatitude(), location.getLongitude()));
                    map.getController().setZoom(DEFAULT_ZOOM);
                }
            }

        } catch(Exception ex) {
            ex.printStackTrace();
        }

    }

    private Location getLastBestLocation() {
        if ( ContextCompat.checkSelfPermission( getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {

            requestPermissions(new String[] {  android.Manifest.permission.ACCESS_COARSE_LOCATION  },
                    MY_PERMISSIONS_REQUEST_COARSE_LOCATION );
        }
        Location locationGPS = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Location locationNet = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        long GPSLocationTime = 0;
        if (null != locationGPS) { GPSLocationTime = locationGPS.getTime(); }

        long NetLocationTime = 0;

        if (null != locationNet) {
            NetLocationTime = locationNet.getTime();
        }

        if ( 0 < GPSLocationTime - NetLocationTime ) {
            return locationGPS;
        }
        else {
            return locationNet;
        }
    }

}
