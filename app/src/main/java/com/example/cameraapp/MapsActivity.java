package com.example.cameraapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;

import com.example.cameraapp.Realm.PlaceRealm;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.realm.Realm;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    // Request codes
    static final int REQUEST_SHOW_IMAGES_CODE = 1;

    // Passing data to another activity keys
    static final String MARKER_ID_KEY = "MARKER_ID";

    // Permission request codes
    static final int PERMISSIONS_REQUEST_LOCATION = 1;

    private GoogleMap map;
    private List<PlaceRealm> places;
    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        realm = Realm.getDefaultInstance();
        restoreInstanceState(savedInstanceState);

        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    protected void restoreInstanceState(Bundle savedInstanceState) {
        places = realm.where(PlaceRealm.class).findAll();
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

        requestLocationPermission();
        map.getUiSettings().setMyLocationButtonEnabled(true);
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                addPlace(point);
            }
        });
        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                startGalleryActivity(marker);
                return false;
            }
        });
        map.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(final Marker marker) {
                final String markerUuid = (String) marker.getTag();

                realm.executeTransactionAsync(new Realm.Transaction() {
                    @Override
                    public void execute(Realm bgRealm) {
                        bgRealm.where(PlaceRealm.class).equalTo("id", markerUuid).findFirst().deleteFromRealm();
                    }
                }, new Realm.Transaction.OnSuccess() {
                    @Override
                    public void onSuccess() {
                        marker.remove();
                    }
                });
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onMarkerDrag(Marker marker) {
                // TODO Auto-generated method stub

            }
        });

        map.clear();
        for (PlaceRealm place : places) {
            MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(place.getLatitude(), place.getLongitude()));
            Marker marker = map.addMarker(markerOptions);
            marker.setTag(place.getId());
            marker.setDraggable(true);
        }
    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(MapsActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(MapsActivity.this,
                    Manifest.permission.READ_CONTACTS)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(MapsActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSIONS_REQUEST_LOCATION);
            }
        } else {
            enableMyLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the task you need to do.
                    enableMyLocation();
                } else {
                    // permission denied, boo! Disable the functionality that depends on this permission.
                }
                return;
            }
        }
    }

    private void enableMyLocation() {
        map.setMyLocationEnabled(true);
    }

    private void addPlace(LatLng point) {
        MarkerOptions markerOptions = new MarkerOptions().position(point);
        Marker newMarker = map.addMarker(markerOptions);
        String uuid = UUID.randomUUID().toString();
        newMarker.setTag(uuid);
        newMarker.setDraggable(true);

        final PlaceRealm newPlace = new PlaceRealm(point.latitude, point.longitude);
        newPlace.setId(uuid);

        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm bgRealm) {
                bgRealm.copyToRealm(newPlace);
            }
        });
    }

    private void startGalleryActivity(Marker marker) {
        Intent intent = new Intent(MapsActivity.this, GalleryActivity.class);
        String markerUuid = (String) marker.getTag();
        intent.putExtra(MARKER_ID_KEY, markerUuid);
        startActivityForResult(intent, REQUEST_SHOW_IMAGES_CODE);
    }
}
