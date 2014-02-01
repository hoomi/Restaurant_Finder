package com.hooman.ostovari.restaurantfinder.utils;

/**
 * Created by hoomi on 28/01/2014.
 */
public class Constants {

    public static final String BASE = "com.hooman.ostovari.restaurant";

    public class GooglePlaceConstant {
        public final static String SERVER_URL = "https://maps.googleapis.com/maps/api/place/textsearch/json";
    }

    public class Intents {
        public static final String DOWNLOAD_NEAR_BY_RESTAURANTS = BASE + ".DOWNLOAD_NEAR_BY_RESTAURANTS";
        public static final String LOCATION_CHANGED = BASE + ".LOCATION_CHANGED";
    }

    public class Loaders {
        public static final int RESTAURANT_ID = 1;

    }
}
