package com.androidhive.openhourgoogle;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import com.androidhive.openhourgoogle.adapters.GoogleCustomListViewAdapter;
import com.androidhive.openhourgoogle.api.ConnectionDetector;
import com.androidhive.openhourgoogle.api.GooglePlaces;
import com.androidhive.openhourgoogle.api.GoogleServiceErrorMessages;
import com.androidhive.openhourgoogle.util.LocationUtils;
import com.androidhive.openhourgoogle.components.Place;
import com.androidhive.openhourgoogle.components.PlacesList;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Author: JessicaC
 * Date: 9/12/13
 *
 * The main activity of this application.
 * It gets the user current location, and display a list of nearby restaurants.
 *
 */
public class MainActivity extends FragmentActivity implements
        LocationListener,
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {

    // KEY Strings
    public static String KEY_REFERENCE = "reference"; // id of the place
    public static String KEY_NAME = "name"; // name of the place
    public static String KEY_VICINITY = "vicinity"; // Place area name

    // A request to connect to Location Services
    private LocationRequest mLocationRequest;

    // Stores the current instantiation of the location client in this object
    private LocationClient mLocationClient;

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

    // ListItems data
    ArrayList<HashMap<String, String>> placesListItems = new ArrayList<HashMap<String,String>>();

    // Hold the coordinate of the location
    Coordinate coordinate;

    // Hold the search terms
    String user_search = "";
    String user_near = "";

    // Hold the search preference
    SearchPreference preference;

    /* Search term preference */
    class SearchPreference {
        String types;
        double radius;

        public SearchPreference(String types, double radius) {
            this.types = types;
            this.radius = radius;
        }
    }

    /* Coordinate of a location */
    private class Coordinate {
        Double latitude;
        Double longitude;
    }

	@Override
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

        // Create a new global location parameters object
        mLocationRequest = LocationRequest.create();
        // Set the update interval
        mLocationRequest.setInterval(LocationUtils.UPDATE_INTERVAL_IN_MILLISECONDS);
        // Use high accuracy
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        // Set the interval ceiling to one minute
        mLocationRequest.setFastestInterval(LocationUtils.FAST_INTERVAL_CEILING_IN_MILLISECONDS);

        // Create a new location client, using the enclosing class to handle callbacks.
        mLocationClient = new LocationClient(this, this, this);

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

        /*
		lv.setOnItemClickListener(new OnItemClickListener() {
 
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
            	// getting values from selected ListItem
                String reference = ((TextView) view.findViewById(R.id.reference)).getText().toString();
                
                // Starting new intent
                Intent in = new Intent(getApplicationContext(),
                        SinglePlaceActivity.class);
                
                // Sending place refrence id to single place activity
                // place refrence id used to get "Place full details"
                in.putExtra(KEY_REFERENCE, reference);
                startActivity(in);
            }
        });
        */
	}

    /**
     * Invoked by the "Search" button.
     * @param v The view object associated with this method, in this case a Button.
     */
    public void getSearch(View v) {
        user_search = etSearch.getText().toString();
        user_near = etNear.getText().toString();

        if (!user_near.equals("") && !user_near.equalsIgnoreCase(getString(R.string.msg_current_location))) {
            // if near term is not empty, then search restaurant at the specify location
            // Launch SearchActivity
            Intent i = new Intent(getApplicationContext(), SearchActivity.class);
            i.putExtra("user_search", user_search);
            i.putExtra("user_near", user_near);
            startActivity(i);
        } else {
            // if near term is empty, then search for specify search term at current location.
            if (servicesConnected()) {
                Location currentLocation = mLocationClient.getLastLocation();
                coordinate.latitude = currentLocation.getLatitude();
                coordinate.longitude = currentLocation.getLongitude();
                (new MainActivity.LoadPlaces()).execute(coordinate);
            }
        }
    }

    /**
     * Invoked by the "Map" button.
     * Calls getLastLocation() to get the current location
     * @param v The view object associated with this method, in this case a Button.
     */
    public void getMap(View v) {
        if (nearPlaces == null || nearPlaces.results == null || nearPlaces.results.size() == 0) {
            // if there is no nearest places found, do nothing
        } else {
            Intent i = new Intent(getApplicationContext(), MarkerActivity.class);
            i.putExtra("user_search", user_search);
            i.putExtra("user_near", getString(R.string.msg_current_location));
            i.putExtra("near_places", nearPlaces);
            startActivity(i);
        }
    }

    @Override
    public void onBackPressed() {
         //do nothing
    }

    /*
     * Called when the Activity is no longer visible at all.
     * Disconnect the location client.
     */
    @Override
    public void onStop() {
        mLocationClient.disconnect();
        super.onStop();
    }

    /*
     * Called when the Activity is restarted, even before it becomes visible.
     */
    @Override
    public void onStart() {
        super.onStart();
        // Connect the client. Don't re-start any requests here; instead, wait for onResume()
        mLocationClient.connect();
    }

    /*
     * Reserve for future use.
     * Called when the Activity is going into the background.
     * Parts of the UI may be visible, but the Activity is inactive.
     */
    @Override
    public void onPause() {
        super.onPause();
    }

    /*
     * Reserve for future use.
     * Called when the system detects that this Activity is now visible.
     */
    @Override
    public void onResume() {
        super.onResume();
    }

    /*
     * Called by Location Services when the request to connect the
     * client finishes successfully. At this point, you can
     * request the current location or start periodic updates
     */
    @Override
    public void onConnected(Bundle bundle) {
        // If Google Play Services is available
        if (servicesConnected()) {
            Location currentLocation = mLocationClient.getLastLocation();
            coordinate.latitude = currentLocation.getLatitude();
            coordinate.longitude = currentLocation.getLongitude();
            (new MainActivity.LoadPlaces()).execute(coordinate);
        }
    }

    /*
     * Called by Location Services if the connection to the
     * location client drops because of an error.
     */
    @Override
    public void onDisconnected() {}

    /**
     * Report location updates to the UI.
     *
     * @param location The updated location.
     */
    @Override
    public void onLocationChanged(Location location) {
        //TODO testing
        Toast.makeText(getApplicationContext(),"LocationChanged", Toast.LENGTH_LONG);

        if (servicesConnected()) {
            Location currentLocation = mLocationClient.getLastLocation();
            coordinate.latitude = currentLocation.getLatitude();
            coordinate.longitude = currentLocation.getLongitude();
            (new MainActivity.LoadPlaces()).execute(coordinate);
        }
    }

    /*
     * Called by Location Services if the attempt to
     * Location Services fails.
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this,
                        LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                // Thrown if Google Play services canceled the original PendingIntent
                e.printStackTrace();
            }
        } else {
            // If no resolution is available, display a dialog to the user with the error.
            showErrorDialog(connectionResult.getErrorCode());
        }
    }

    /**
     * Verify that Google Play services is available before making a request.
     *
     * @return true if Google Play services is available, otherwise false
     */
    private boolean servicesConnected() {
        // Check that Google Play services is available
        int resultCode =
                GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Log.d(LocationUtils.APPTAG, getString(R.string.play_services_available));

            // Continue
            return true;
            // Google Play services was not available for some reason
        } else {
            // Display an error dialog
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0);
            if (dialog != null) {
                ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                errorFragment.setDialog(dialog);
                errorFragment.show(getSupportFragmentManager(), LocationUtils.APPTAG);
            }
            return false;
        }
    }

    /**
     * Show a dialog returned by Google Play services for the
     * connection error code
     *
     * @param errorCode An error code returned from onConnectionFailed
     */
    private void showErrorDialog(int errorCode) {
        // Get the error dialog from Google Play services
        Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
                errorCode,
                this,
                LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);

        // If Google Play services can provide an error dialog
        if (errorDialog != null) {

            // Create a new DialogFragment in which to show the error dialog
            ErrorDialogFragment errorFragment = new ErrorDialogFragment();

            // Set the dialog in the DialogFragment
            errorFragment.setDialog(errorDialog);

            // Show the error dialog in the DialogFragment
            errorFragment.show(getSupportFragmentManager(), LocationUtils.APPTAG);
        }
    }


	/**
	 * Inner class.
     * Background Async Task to Load Google places.
	 * */
	private class LoadPlaces extends AsyncTask<Coordinate, Void, Void> {

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

			pDialog = new ProgressDialog(MainActivity.this);
			pDialog.setMessage(Html.fromHtml("<b>Search</b><br/>Loading Places..."));
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
		}

		/**
		 * getting Places JSON
		 * */
		protected Void doInBackground(Coordinate... params) {
			try {
                // Get the current location from the input parameter list
                Coordinate currentLocation = params[0];
                if (currentLocation != null) {
				    nearPlaces = GooglePlaces.get().search(currentLocation.latitude,
                            currentLocation.longitude, preference.radius, preference.types, user_search);
                }
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		/**
		 * After completing background task Dismiss the progress dialog
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
					// Get json response status
					String status = nearPlaces.status;
					
					// Check for all possible status
					if(status.equals(getString(R.string.status_ok))){
						// Successfully got places details
						if (nearPlaces.results != null) {
                            List<Place> placeList = new ArrayList<Place>();
							// loop through each place
							for (final Place p : nearPlaces.results) {
                                /*
								HashMap<String, String> map = new HashMap<String, String>();
								// Place reference won't display in listview - it will be hidden
								// Place reference is used to get "place full details"
								map.put(KEY_REFERENCE, p.reference);
								// Place name
								map.put(KEY_NAME, p.name);
								// adding HashMap to ArrayList
								placesListItems.add(map);
                                */

                                /** Yelp business request */
                                /*
                                final String searchTerm = p.name;
                                final Double searchLat = p.geometry.location.lat;
                                final Double searchLng = p.geometry.location.lng;

                                new AsyncTask<Void, Void, List<Business>>() {
                                    @Override
                                    protected List<Business> doInBackground(Void... params) {
                                        String businesses = Yelp.getYelp(MainActivity.this).search(searchTerm, searchLat, searchLng);
                                        try {
                                            return processJson(businesses);
                                        } catch (JSONException e) {
                                            return Collections.<Business>emptyList();
                                        }
                                    }

                                    @Override
                                    protected void onPostExecute(List<Business> businesses) {
                                        super.onPostExecute(businesses);
                                        if (businesses != null && !businesses.isEmpty()) {
                                            p.setImageUrl(businesses.get(0).getImageUrl());
                                        }
                                    }
                                }.execute();
                                */

                                //TODO for custom list adapter
                                // add each place into the list
                                placeList.add(p);
							}

                            /*
							// list adapter
							ListAdapter adapter = new SimpleAdapter(MainActivity.this, placesListItems,
					                R.layout.list_item,
					                new String[] { KEY_REFERENCE, KEY_NAME}, new int[] {
					                        R.id.reference, R.id.name });
							
							// Adding data into listview
							lv.setAdapter(adapter);
							*/

                            //TODO for custom list adapter
                            GoogleCustomListViewAdapter adapter = new GoogleCustomListViewAdapter(MainActivity.this, R.layout.custom_list_item, placeList);
                            lv.setAdapter(adapter);
						}
					} else {
                        // Empty the list view
                        GoogleCustomListViewAdapter adapter = new GoogleCustomListViewAdapter(
                                MainActivity.this, R.layout.custom_list_item,  new ArrayList<Place>());
                        lv.setAdapter(adapter);

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
                        Log.e(LocationUtils.APPTAG,errorMsg);
                    }
				}
			});
		}
	}

    /**
     * Define a DialogFragment to display the error dialog generated in
     * showErrorDialog.
     */
    public static class ErrorDialogFragment extends DialogFragment {

        // Global field to contain the error dialog
        private Dialog mDialog;

        /**
         * Default constructor. Sets the dialog field to null
         */
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }

        /**
         * Set the dialog to display
         *
         * @param dialog An error dialog
         */
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }

        /*
         * This method must return a Dialog to the DialogFragment.
         */
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }
}
