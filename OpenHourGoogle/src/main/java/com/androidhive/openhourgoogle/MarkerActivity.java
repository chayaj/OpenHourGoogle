/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.androidhive.openhourgoogle;

import android.content.Intent;
import android.location.Location;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import com.androidhive.openhourgoogle.components.Place;
import com.androidhive.openhourgoogle.components.PlacesList;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.TextView;
import android.widget.Toast;

import java.util.*;

/**
 * Author: JessicaC
 * Date: 9/12/2013
 * Activity with map, markers on a map, and user current location.
 */
public class MarkerActivity extends FragmentActivity
        implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener,
        LocationListener,
        GoogleMap.OnMyLocationButtonClickListener {

    /** Customize info window and its contents. */
    class CustomInfoWindowAdapter implements InfoWindowAdapter {

        // This viewgroups containing an ImageView with id "badge" and two TextViews with id
        private final View mWindow;

        CustomInfoWindowAdapter() {
            mWindow = getLayoutInflater().inflate(R.layout.custom_info_window, null);
        }

        @Override
        public View getInfoWindow(Marker marker) {
            render(marker, mWindow);
            return mWindow;
        }

        @Override
        public View getInfoContents(Marker marker) {
            return null;
        }

        private void render(Marker marker, View view) {
            // set title
            String title = marker.getTitle();
            TextView titleUi = ((TextView) view.findViewById(R.id.title));
            if (title != null) {
                // Spannable string allows us to edit the formatting of the text.
                SpannableString titleText = new SpannableString(title);
                titleText.setSpan(new ForegroundColorSpan(Color.BLACK), 0, titleText.length(), 0);
                titleUi.setText(titleText);
            } else {
                titleUi.setText("");
            }

            // set snippet
            String snippet = marker.getSnippet();
            TextView snippetUi = ((TextView) view.findViewById(R.id.snippet));
            if (snippet != null) {
                SpannableString snippetText = new SpannableString(snippet);
                if (!snippet.equals("Open")) {
                    snippetText.setSpan(new ForegroundColorSpan(Color.RED), 0, snippet.length(), 0);
                }
                snippetUi.setText(snippetText);
            } else {
                snippetUi.setText("");
            }
        }
    }

    // Google Map instance
    private GoogleMap mMap;

    // Location client
    private LocationClient mLocationClient;

    // Nearest places
    PlacesList nearPlaces;

    // Stores LatLng as key that corresponds to the Place.
    Map<LatLng, Place> latLngPlaceMap;

    // User search term
    String user_search;
    String user_near;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.marker_activity);

        // Getting intent data
        Intent i = getIntent();
        nearPlaces = (PlacesList) i.getSerializableExtra("near_places");
        user_search = i.getStringExtra("user_search");
        user_near = i.getStringExtra("user_near");

        // List of LatLng
        latLngPlaceMap = new HashMap<LatLng, Place>();

        // loop through all the places, add each place to hashmap.
        if (nearPlaces != null && nearPlaces.results != null && nearPlaces.status.equals(getString(R.string.status_ok))) {
            double latitude;
            double longitude;
            LatLng latlng;
            for (Place place : nearPlaces.results) {
                latitude = place.geometry.location.lat;
                longitude = place.geometry.location.lng;

                // Geopoint to place on map
                latlng = new LatLng(latitude, longitude);

                // Add to hashmap
                latLngPlaceMap.put(latlng, place);
            }
        }
        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        setUpLocationClientIfNeeded();
        mLocationClient.connect();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mLocationClient != null) {
            mLocationClient.disconnect();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    /**
     * Implementation of {@link LocationListener}.
     */
    @Override
    public void onLocationChanged(Location location) {

    }

    /**
     * Callback called when connected to GCore. Implementation of {@link com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks}.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        Toast.makeText(getApplicationContext(),"Map Connect",Toast.LENGTH_SHORT);
    }

    /**
     * Callback called when disconnected from GCore. Implementation of {@link com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks}.
     */
    @Override
    public void onDisconnected() {
        // Do nothing
        Toast.makeText(getApplicationContext(),"Map Disconnect",Toast.LENGTH_SHORT);
    }

    /**
     * Implementation of {@link com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener}.
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Do nothing
    }

    @Override
    public boolean onMyLocationButtonClick() {
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    /**
     * Button click to get list view
     * @param v
     */
    public void getList(View v) {
        Intent i = new Intent(getApplicationContext(), MainActivity.class);
        i.putExtra("user_search", user_search);
        i.putExtra("user_near", user_near);
        i.putExtra("near_places", nearPlaces);
        startActivity(i);
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                mMap.setMyLocationEnabled(true);
                mMap.setOnMyLocationButtonClickListener(this);
                setUpMap();
            }
        }
    }

    private void setUpLocationClientIfNeeded() {
        if (mLocationClient == null) {
            mLocationClient = new LocationClient(
                    getApplicationContext(),
                    this,  // ConnectionCallbacks
                    this); // OnConnectionFailedListener
        }
    }

    private void setUpMap() {
        // Add lots of markers to the map.
        addMarkersToMap();

        // Setting an info window adapter allows us to change the both the contents and look of the
        // info window.
        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());

        // Pan to see all markers in view.
        // Cannot zoom to bounds until the map has a size.
        final View mapView = getSupportFragmentManager().findFragmentById(R.id.map).getView();
        if (mapView.getViewTreeObserver().isAlive()) {
            mapView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
                @SuppressWarnings("deprecation") // We use the new method when supported
                @SuppressLint("NewApi") // We check which build version we are using.
                @Override
                public void onGlobalLayout() {

                    if (latLngPlaceMap.size() > 0) {
                        // a list of LatLng
                        List<LatLng> latlngList = new ArrayList<LatLng>();
                        for (LatLng latlng: latLngPlaceMap.keySet()) {
                            latlngList.add(latlng);
                        }

                        // create boundary for the map
                        LatLngBounds bounds;
                        if (latlngList.size() < 2) {
                            bounds = new LatLngBounds.Builder()
                                    .include(new LatLng(getLatLngFromList(latlngList, 0).latitude, getLatLngFromList(latlngList, 0).longitude))
                                    .build();
                        } else {
                            bounds = new LatLngBounds(latlngList.get(0), latlngList.get(1));
                            for (LatLng latlng: latlngList) {
                                bounds = bounds.including(latlng);
                            }
                        }


                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                          mapView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        } else {
                          mapView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));

                    }
                }
            });
        }
    }

    private LatLng getLatLngFromList(List<LatLng> latlngList, int index) {
        return latlngList.get(index);
    }

    private void addMarkersToMap() {
        for (LatLng latlng: latLngPlaceMap.keySet()) {
            Place place = latLngPlaceMap.get(latlng);
            addMarker(latlng, place.name, place.getOpenNow());
        }
    }

    private Marker addMarker(LatLng latlng, String title, String snippet) {
        return mMap.addMarker(new MarkerOptions()
                .position(latlng)
                .title(title)
                .snippet(snippet)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
    }
}
