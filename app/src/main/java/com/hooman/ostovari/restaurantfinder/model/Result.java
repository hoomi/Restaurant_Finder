package com.hooman.ostovari.restaurantfinder.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by hoomi on 30/01/2014.
 */
public class Result {

    @SerializedName("html_attributions")
    public List<String> html_attributions;

    @SerializedName("results")
    public List<Place> places;

    public String status;

    public class Geometry {
        @SerializedName("location")
        public Location location;
    }

    public class Location {
        public double lat;
        public double lng;
    }

    public class Place {
        public String formatted_address;
        public String icon;
        public String id;
        public String name;
        public float rating;
        public String reference;
        @SerializedName("types")
        public List<String> types;
        @SerializedName("geometry")
        public Geometry geometry;
    }

}
