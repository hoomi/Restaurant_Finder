package com.hooman.ostovari.restaurantfinder.utils;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by hoomi on 28/01/2014.
 */
public class RestaurantMarkerUtils {
    public static MarkerOptions createMarker(LatLng position, String title) {
        return new MarkerOptions()
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                .anchor(0.0f, 0.0f)
                .position(position)
                .title(title);
    }

}
