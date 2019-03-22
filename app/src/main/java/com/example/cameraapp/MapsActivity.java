package com.example.cameraapp;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    // Request codes
    static final int REQUEST_SHOW_IMAGES_CODE = 1;

    // Saving state keys
    static final String MARKERS_STATE_KEY = "MARKERS";

    // Passing data to another activity keys
    static final String MARKER_ID_KEY = "MARKER_ID";
    static final String IMAGES_KEY = "IMAGES";

    private GoogleMap map;
    private HashMap<String, Place> placeByMarkerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        restoreInstanceState(savedInstanceState);

        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    protected void restoreInstanceState(Bundle savedInstanceState){
        if (savedInstanceState != null){
            placeByMarkerId = (HashMap<String, Place>)savedInstanceState.getSerializable(MARKERS_STATE_KEY);
        } else {
            placeByMarkerId = new HashMap<>();
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        map.setMyLocationEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(true);
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                MarkerOptions markerOptions = new MarkerOptions().position(point);
                Marker newMarker = map.addMarker(markerOptions);
                String uuid = UUID.randomUUID().toString();
                newMarker.setTag(uuid);

                Place newPlace = new Place(point, new ArrayList<String>());
                placeByMarkerId.put(uuid, newPlace);
            }
        });
        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Intent intent = new Intent(MapsActivity.this, GalleryActivity.class);
                String markerUuid = (String)marker.getTag();
                intent.putExtra(MARKER_ID_KEY, markerUuid);
                intent.putStringArrayListExtra(IMAGES_KEY, placeByMarkerId.get(markerUuid).getImages());
                startActivityForResult(intent, REQUEST_SHOW_IMAGES_CODE);
                return false;
            }
        });

        map.clear();
        Iterator it = placeByMarkerId.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            String uuid = (String)pair.getKey();
            Place place = (Place)pair.getValue();

            MarkerOptions markerOptions = new MarkerOptions().position(place.getPosition());
            Marker marker = map.addMarker(markerOptions);
            marker.setTag(uuid);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SHOW_IMAGES_CODE && resultCode == RESULT_OK) {
            String markerId = data.getStringExtra(MARKER_ID_KEY);
            ArrayList<String> images = data.getStringArrayListExtra(IMAGES_KEY);
            Place place = placeByMarkerId.get(markerId);
            placeByMarkerId.put(markerId, new Place(place.getPosition(), images));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putSerializable(MARKERS_STATE_KEY, placeByMarkerId);
    }
}
