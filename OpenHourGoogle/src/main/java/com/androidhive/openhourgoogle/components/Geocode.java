package com.androidhive.openhourgoogle.components;

import com.google.api.client.util.Key;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: JessicaC
 * Date: 9/4/13
 * Time: 5:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class Geocode implements Serializable {

    @Key
    public String formatted_address;

    @Key
    public Geometry geometry;

    public static class Geometry implements Serializable
    {
        @Key
        public Location location;
    }

    public static class Location implements Serializable
    {
        @Key
        public double lat;

        @Key
        public double lng;
    }

    @Override
    public String toString() {
        return formatted_address;
    }
}
