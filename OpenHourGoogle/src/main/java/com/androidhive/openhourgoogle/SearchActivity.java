package com.androidhive.openhourgoogle;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import com.androidhive.openhourgoogle.adapters.GoogleCustomListViewAdapter;
import com.androidhive.openhourgoogle.api.*;
import com.androidhive.openhourgoogle.components.GeocodeList;
import com.androidhive.openhourgoogle.components.Place;
import com.androidhive.openhourgoogle.components.PlacesList;
import com.androidhive.openhourgoogle.util.LocationUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Author: JessicaC
 * Date: 9/12/13
 *
 * The search activity searches for nearby restaurant,
 * with a given search and location parameters.
 *
 */
public class SearchActivity extends Activity {

    // flag for Internet connection status
    Boolean isInternetPresent = false;

    // Connection detector class
    ConnectionDetector cd;

    // Handles to UI widgets
    ProgressDialog pDialog;
    Button btnShowOnMap;
    EditText etSearch;
    EditText etNear;
    Button btnSearch;
    ListView lv;

    // Places List
    PlacesList nearPlaces;

    // Geocode List
    GeocodeList geocodeResults;

    // Hold the search terms
    String user_search;
    String user_near;

    // Hold the coordinate of the location
    Coordinate coordinate = null;

    // Hold the search preference
    SearchPreference preference;

    /* Search term preference */
    private class SearchPreference {
        String types;
        double radius;

        public SearchPreference(String types, double radius) {
            this.types = types;
            this.radius = radius;
        }
    }

    private class Coordinate {
        Double latitude;
        Double longitude;
        String address;
    }

    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check if Internet present
        cd = new ConnectionDetector(getApplicationContext());
        isInternetPresent = cd.isConnectingToInternet();
        if (!isInternetPresent) {
            // Internet Connection is not present
            Toast.makeText(getApplicationContext(),
                    getString(R.string.internet_connection_error),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Get handles to the UI widgets.
        lv = (ListView) findViewById(R.id.list);
        btnShowOnMap = (Button) findViewById(R.id.btn_show_map);
        etSearch = (EditText) findViewById(R.id.et_search);
        etNear = (EditText) findViewById(R.id.et_near);
        btnSearch = (Button) findViewById(R.id.btn_search);

        // Instantiate member variables
        preference = new SearchPreference(getString(R.string.restaurant), LocationUtils.RADIUS);
        coordinate = new Coordinate();

        // Getting intent data
        Intent i = getIntent();
        user_search = i.getStringExtra("user_search");
        user_near = i.getStringExtra("user_near");

        // Set the text field in the search area.
        etSearch.setText(user_search);
        etNear.setText(user_near);

        // calling background Async task to load Google Places
        // After getting places from Google all the data is shown in listview
        (new LoadGeocode(this)).execute();
    }

    /**
     * Invoked by the "Search" button.
     * @param v The view object associated with this method, in this case a Button.
     */
    public void getSearch(View v) {
        user_near = etNear.getText().toString();
        user_search = etSearch.getText().toString();

        if (!user_near.equals("") && !user_near.equalsIgnoreCase(getString(R.string.msg_current_location))) {
            // if location term is not empty, then search restaurant at the specify location
            // call async task to get coordinate of the specify location
            (new LoadGeocode(this)).execute();
        } else {
            // if near term is empty, search for specify search term at current location.
            // Launch MainActivity
            Intent i = new Intent(getApplicationContext(), MainActivity.class);
            i.putExtra("user_search", user_search);
            i.putExtra("user_near", user_near);
            startActivity(i);
        }
    }

    /**
     * Invoked by the "Map" button.
     * Calls getLastLocation() to get the current location
     * @param v The view object associated with this method, in this case a Button.
     */
    public void getMap(View v) {
        if (nearPlaces == null || nearPlaces.results == null || nearPlaces.results.size() == 0) {
           //do nothing
        } else {
            Intent i = new Intent(getApplicationContext(), MarkerActivity.class);
            i.putExtra("user_search", user_search);
            i.putExtra("user_near", user_near);
            i.putExtra("near_places", nearPlaces);
            startActivity(i);
        }
    }

    @Override
    public void onBackPressed() {
        //do nothing
    }

    /**
     * Background Async Task to Load Google places
     * */
    private class LoadPlaces extends AsyncTask<Void, Void, Void> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Hide the soft keyboard
            InputMethodManager imm = (InputMethodManager)getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
            imm.hideSoftInputFromWindow(etNear.getWindowToken(), 0);

            pDialog = new ProgressDialog(SearchActivity.this);
            pDialog.setMessage(Html.fromHtml(getString(R.string.msg_loading)));
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        /**
         * getting Places JSON
         * */
        protected Void doInBackground(Void... args) {
            try {
                // get nearest places
                nearPlaces = GooglePlaces.get().search(coordinate.latitude, coordinate.longitude,
                        preference.radius, preference.types, user_search);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog                                      s
         * and show the data in UI
         * Always use runOnUiThread(new Runnable()) to update UI from background
         * thread, otherwise you will get error
         * **/
        protected void onPostExecute(Void arg) {
            // dismiss the dialog after getting all products
            pDialog.dismiss();
            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                public void run() {
                    /**
                     * Updating parsed Places into LISTVIEW
                     * */
                     if (nearPlaces == null) {
                         Toast.makeText(getApplicationContext(),getString(R.string.msg_no_location_found),Toast.LENGTH_SHORT).show();
                         Log.e(LocationUtils.APPTAG,getString(R.string.google_api_connection_error));
                     } else {
                        // Get json response status
                        String status = nearPlaces.status;

                        // Check for all possible status
                        if(status.equals(getString(R.string.status_ok))) {
                            if (nearPlaces.results != null) {
                                List<Place> placeList = new ArrayList<Place>();
                                for (Place p : nearPlaces.results) {
                                    // add each place into the list
                                    placeList.add(p);
                                }
                                GoogleCustomListViewAdapter adapter = new GoogleCustomListViewAdapter(
                                        SearchActivity.this, R.layout.custom_list_item, placeList);
                                lv.setAdapter(adapter);
                            }
                        } else {
                            // Empty the list view
                            GoogleCustomListViewAdapter adapter = new GoogleCustomListViewAdapter(
                                    SearchActivity.this, R.layout.custom_list_item,  new ArrayList<Place>());
                            lv.setAdapter(adapter);

                            // Log the error
                            String errorMsg = GoogleServiceErrorMessages.getErrorString(getApplicationContext(), status);
                            if (errorMsg.equals(getString(R.string.status_zero_results))) {
                                Toast.makeText(getApplicationContext(),
                                        getString(R.string.msg_no_result_found),
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getApplicationContext(),
                                        getString((R.string.msg_google_place_api_error)),
                                        Toast.LENGTH_SHORT).show();
                            }
                            Log.e(LocationUtils.APPTAG, errorMsg);
                        }
                    }
                }
            });
        }
    }


    /**
     * An AsyncTask that calls getFromLocationName() in the background.
     * Get a geocoding of the specified location
     */
    protected class LoadGeocode extends AsyncTask<Void, Void, Void> {

        // Store the context passed to the AsyncTask when the system instantiates it.
        Context localContext;

        // Constructor called by the system to instantiate the task
        public LoadGeocode(Context context) {
            // Required by the semantics of AsyncTask
            super();
            // Set a Context for the background task
            localContext = context;
        }

        /**
         * Get a geocoding service instance, pass location name to it, format the returned
         * address, and get the address's coordinate.
         */
        @Override
        protected Void doInBackground(Void... params) {
            Geocoder geocoder = new Geocoder(localContext, Locale.getDefault());

            // Create a list to contain the result address
            List<Address> addresses = null;

            // Try to get an address for the current location. Catch IO or network problems.
            try {
                int maxResult = 5;
                addresses = geocoder.getFromLocationName(user_near, maxResult);

            } catch (IOException exception1) {
                // Catch network or other I/O problems.
                Toast.makeText(getApplicationContext(),getString(R.string.msg_google_place_api_error),Toast.LENGTH_SHORT).show();
                Log.e(LocationUtils.APPTAG, getString(R.string.IO_Exception_getFromLocationName));
                exception1.printStackTrace();
            } catch (IllegalArgumentException exception2) {
                // Catch incorrect values
                Toast.makeText(getApplicationContext(),getString(R.string.msg_no_location_found),Toast.LENGTH_SHORT).show();
                String errorString = getString(R.string.illegal_address, user_near);
                Log.e(LocationUtils.APPTAG, errorString);
                exception2.printStackTrace();
            }

            // If the reverse geocode returned an address
            if (addresses != null && addresses.size() > 0) {
                Address address;
                int idx = 0;
                while (idx < addresses.size()) {
                    //get the address that has coordinate.
                    address = addresses.get(idx);
                    if (address.hasLatitude() && address.hasLongitude()) {
                        coordinate.latitude = address.getLatitude();
                        coordinate.longitude = address.getLongitude();
                        coordinate.address = address.getAdminArea();
                        break;
                    }
                    idx++;
                }
            } else {
                Toast.makeText(getApplicationContext(),getString(R.string.msg_no_location_found),Toast.LENGTH_SHORT).show();
            }
            return null;
        }

        /**
         * A method that's called once doInBackground() completes. Set the text of the
         * UI element that displays the address. This method runs on the UI thread.
         */
        @Override
        protected void onPostExecute(Void args) {
            if (coordinate == null) {
                Toast.makeText(getApplicationContext(),getString(R.string.msg_no_location_found),Toast.LENGTH_SHORT).show();
            } else {
                (new SearchActivity.LoadPlaces()).execute();
            }
        }
    }



//    /**
//     * Background Async Task to Load Google Geocode
//     * */
//    private class LoadGeocode extends AsyncTask<String, String, String> {
//
//        /**
//         * getting Geocode JSON
//         * */
//        protected String doInBackground(String... args) {
//            try {
//                // get list of geocode places
//                geocodeResults = GoogleGeocode.get().search(user_near);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            return null;
//        }
//
//        /**
//         * After completing background task Dismiss the progress dialog
//         * and show the data in UI
//         * Always use runOnUiThread(new Runnable()) to update UI from background
//         * thread, otherwise you will get error
//         * **/
//        protected void onPostExecute(String file_url) {
//            // dismiss the dialog after getting all products
//            pDialog.dismiss();
//            /**
//             * Updating parsed Places into LISTVIEW
//             * */
//            // Get json response status
//            String status = geocodeResults.status;
//
//            // Check for all possible status
//            if(status.equals(getString(R.string.status_ok))){
//                // Successfully got places details
//                List<Geocode> geocodes = geocodeResults.results;
//                if (geocodes != null && geocodes.size() > 0) {
//                    /*
//                    // loop through each place
//                    for (Geocode g : geocodeResults.results) {
//                        // add each place into the list
//                        geocodeList.add(g);
//                    }
//
//                    */
//
//                    int idx = 0;
//                    Geocode geo;
//                    while (idx < geocodes.size()) {
//                        //get the address that has coordinate.
//                        geo = geocodes.get(idx);
//                        if (Double.toString(geo.geometry.location.lat) != null) {
//                            coordinate.latitude = geo.geometry.location.lat;
//                            coordinate.longitude = geo.geometry.location.lng;
//                            break;
//                        }
//                        idx++;
//                    }
//
//                    if (coordinate.latitude != null) {
//                        new LoadPlaces().execute();
//                    } else {
//                        alert.showAlertDialog(SearchActivity.this, getString(R.string.error), getString(R.string.no_location_found), false);
//                    }
//                }
//            } else {
//                String errorMsg = GoogleServiceErrorMessages.getErrorString(getApplicationContext(), status);
//                alert.showAlertDialog(SearchActivity.this, getString(R.string.error), errorMsg, false);
//            }
//        }
//    }
}