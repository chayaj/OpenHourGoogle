package com.androidhive.openhourgoogle.components;

import java.io.Serializable;

import com.google.api.client.util.Key;

/**
 * Represents the place result and status from Google Place Result
 */
public class PlaceDetails implements Serializable {

	@Key
	public String status;
	
	@Key
	public Place result;

	@Override
	public String toString() {
		if (result!=null) {
			return result.toString();
		}
		return super.toString();
	}
}
