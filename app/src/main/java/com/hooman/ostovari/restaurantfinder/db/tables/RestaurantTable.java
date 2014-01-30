package com.hooman.ostovari.restaurantfinder.db.tables;

import android.net.Uri;

import com.hooman.ostovari.restaurantfinder.db.RestaurantContentProvider;

/**
 * Created by hoomi on 28/01/2014.
 */
public class RestaurantTable {
    public final static String NAME = "restaurant";
    public static final int PATH_TOKEN = 1;
    public static final String PATH_FOR_ID = "england/*";
    public static final int PATH_FOR_ID_TOKEN = PATH_TOKEN * 100;
    public static final Uri CONTENT_URI = Uri.parse("content://" + RestaurantContentProvider.AUTHORITY).buildUpon().appendPath(NAME).build();

    public class Cols {
        public static final String _ID = "_id";
        public static final String LAT = "lat";
        public static final String LNG = "lng";
        public static final String ID = "id";
        public static final String FORMATTED_ADDRESS = "formatted_address";
        public static final String NAME = "name";
        public static final String RATING = "rating";
        public static final String REFERENCE = "reference";
        public static final String ICON = "icon";
        public static final String TYPES = "types";
    }

    public static final String[] PROJECTION = new String[]{
           Cols._ID,Cols.NAME, Cols.LAT, Cols.LNG, Cols.FORMATTED_ADDRESS, Cols.ICON, Cols.RATING
    };
    public static final String CREATE_TABLE = "CREATE TABLE " + NAME + " (" +
            Cols._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            Cols.LAT + " FLOAT NOT NULL, " +
            Cols.LNG + " FLOAT NOT NULL, " +
            Cols.ID + " TEXT NOT NULL, " +
            Cols.FORMATTED_ADDRESS + " TEXT, " +
            Cols.NAME + " TEXT NOT NULL, " +
            Cols.RATING + " FLOAT DEFAULT 0, " +
            Cols.REFERENCE + " TEXT, " +
            Cols.ICON + " TEXT, " +
            Cols.TYPES + " TEXT, " +
            "UNIQUE (" + Cols.ID + ") ON CONFLICT REPLACE);";

    public static final String DROP_TABLE = "DROP TABLE IF EXISTS " + NAME;

}
