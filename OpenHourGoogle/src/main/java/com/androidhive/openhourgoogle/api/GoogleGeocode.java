package com.androidhive.openhourgoogle.api;

import android.util.Log;
import com.androidhive.openhourgoogle.components.GeocodeList;
import com.google.api.client.googleapis.GoogleHeaders;
import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.http.json.JsonHttpParser;
import com.google.api.client.json.jackson.JacksonFactory;
import org.apache.http.client.HttpResponseException;

/**
 * Created with IntelliJ IDEA.
 * User: JessicaC
 * Date: 9/4/13
 * Time: 5:16 PM
 * To change this template use File | Settings | File Templates.
 */
@SuppressWarnings("deprecation")
public class GoogleGeocode {

    /** Global instance of the HTTP transport. */
    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

    // Google Places serach url's
    private static final String GEOCODE_SEARCH_URL = "https://maps.googleapis.com/maps/api/geocode/json?";

    // An instance for this class
    private static GoogleGeocode instance = new GoogleGeocode();

    private String _address;

    /**
     *
     * @return an instance of GoogleGeocode
     */
    public static GoogleGeocode get() {
        return instance;
    }

    /**
     * Searching geocode
     * @params address - address of place
     * @return list of geocode places
     * */
    public GeocodeList search(String address)
            throws Exception {

        this._address = address;

        try {

            HttpRequestFactory httpRequestFactory = createRequestFactory(HTTP_TRANSPORT);
            HttpRequest request = httpRequestFactory
                    .buildGetRequest(new GenericUrl(GEOCODE_SEARCH_URL));
            request.getUrl().put("address", _address);
            request.getUrl().put("sensor", "false");

            GeocodeList list = request.execute().parseAs(GeocodeList.class);
            // Check log cat for places response status
            Log.d("Geocode Status", "" + list.status);
            return list;

        } catch (HttpResponseException e) {
            Log.e("Error:", e.getMessage());
            return null;
        }
    }

    /**
     * Creating http request Factory
     * */
    public static HttpRequestFactory createRequestFactory(
            final HttpTransport transport) {
        return transport.createRequestFactory(new HttpRequestInitializer() {
            public void initialize(HttpRequest request) {
                GoogleHeaders headers = new GoogleHeaders();
                headers.setApplicationName("AndroidHive-Places-Test");
                request.setHeaders(headers);
                JsonHttpParser parser = new JsonHttpParser(new JacksonFactory());
                request.addParser(parser);
            }
        });
    }
}
