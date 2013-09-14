package com.androidhive.openhourgoogle.components;

import java.io.Serializable;
import java.util.List;

import com.google.api.client.util.Key;

/**
 * Represents list of places from Google Place Result
 */
public class PlacesList implements Serializable {

	@Key
	public String status;

	@Key
	public List<Place> results;

}