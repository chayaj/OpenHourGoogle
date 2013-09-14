package com.androidhive.openhourgoogle.components;

import com.google.api.client.util.Key;

import java.io.Serializable;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: JessicaC
 * Date: 9/4/13
 * Time: 5:21 PM
 * To change this template use File | Settings | File Templates.
 */
public class GeocodeList implements Serializable {

    @Key
    public String status;

    @Key
    public List<Geocode> results;

}
