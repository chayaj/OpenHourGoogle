package com.androidhive.openhourgoogle.components;

import java.io.Serializable;

import android.content.res.Resources;
import com.androidhive.openhourgoogle.R;
import com.google.api.client.util.Key;

/**
 * Represents the place details from Google Place Result
 */
public class Place implements Serializable {

	@Key
	public String id;
	
	@Key
	public String name;
	
	@Key
	public String reference;
	
	@Key
	public String icon;
	
	@Key
	public String vicinity;
	
	@Key
	public Geometry geometry;
	
	@Key
	public String formatted_address;
	
	@Key
	public String formatted_phone_number;

    @Key
    public OpeningHours opening_hours;

    @Key
    public Photos[] photos;

    //Url of image
    public String imageUrl = null;

	@Override
	public String toString() {
		return name + " - " + id + " - " + reference;
	}
	
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

    public static class OpeningHours implements Serializable
    {
        @Key
        public boolean open_now;
    }

    public static class Photos implements Serializable
    {
        @Key
        public String photo_reference;
    }



    /**
     Getter and Setter
     */
    public String getImageUrl() {
        return imageUrl;
    }
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Boolean isOpen() {
        if (opening_hours != null) {
            return opening_hours.open_now;
        }
        return null;
    }

    public String getOpenNow() {
        if (isOpen() == null) {
            return "N/A";
        } else if (isOpen()) {
            return "Open";
        } else {
            return "Close";
        }
    }
	
}
