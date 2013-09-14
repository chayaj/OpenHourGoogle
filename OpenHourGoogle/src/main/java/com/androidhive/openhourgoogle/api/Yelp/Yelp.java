package com.androidhive.openhourgoogle.api.Yelp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.scribe.builder.ServiceBuilder;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;
import com.androidhive.openhourgoogle.util.LocationUtils;

import java.util.ArrayList;
import java.util.List;

public class Yelp {

    OAuthService service;
    Token accessToken;
    private static final String API_URL = "http://api.yelp.com/v2/search";

    // class instance
    private static Yelp instance = new Yelp(LocationUtils.YELP_CONSUMER_KEY, LocationUtils.YELP_CONSUMER_SECRET,
            LocationUtils.YELP_TOKEN, LocationUtils.YELP_TOKEN_SECRET);

    public static Yelp get() {
        return instance;
    }

    /**
     * Setup the Yelp API OAuth credentials.
     *
     * OAuth credentials are available from the developer site, under Manage API access (version 2 API).
     *
     * @param consumerKey Consumer key
     * @param consumerSecret Consumer secret
     * @param token Token
     * @param tokenSecret Token secret
     */
    public Yelp(String consumerKey, String consumerSecret, String token, String tokenSecret) {
        this.service = new ServiceBuilder().provider(YelpApi2.class).apiKey(consumerKey).apiSecret(consumerSecret).build();
        this.accessToken = new Token(token, tokenSecret);
    }

    /**
     * Search with term and location.
     *
     * @param term Search term
     * @param latitude Latitude
     * @param longitude Longitude
     * @return JSON string response
     */
    public String search(String term, double latitude, double longitude) {
        OAuthRequest request = new OAuthRequest(Verb.GET, API_URL);
        request.addQuerystringParameter("term", term);
        request.addQuerystringParameter("ll", latitude + "," + longitude);
        this.service.signRequest(this.accessToken, request);
        Response response = request.send();
        return response.getBody();
    }

    /**
     * Search with term string location.
     *
     * @param term Search term
     * @param location Latitude and longitude
     * @return JSON string response
     */
    public String search(String term, String location) {
        OAuthRequest request = new OAuthRequest(Verb.GET, API_URL);
        request.addQuerystringParameter("term", "musashi");
        request.addQuerystringParameter("location", location);

        this.service.signRequest(this.accessToken, request);
        Response response = request.send();
        return response.getBody();
    }

    /**
     * Process Json response from Yelp api call.
     * @param response
     * @return
     * @throws JSONException
     */
    public static List<Business> processJson(String response) throws JSONException {
        JSONObject json = new JSONObject(response);
        JSONArray businesses = json.getJSONArray("businesses");
        ArrayList<Business> businessObjs = new ArrayList<Business>(businesses.length());
        for (int i = 0; i < businesses.length(); i++) {
            JSONObject business = businesses.getJSONObject(i);
            businessObjs.add(new Business(business.optString("name"), business.optString("mobile_url"),
                    business.optString("image_url"), business.optString("display_phone")));
        }
        return businessObjs;
    }
}
