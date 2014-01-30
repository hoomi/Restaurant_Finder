package com.hooman.ostovari.restaurantfinder.communication;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;

import com.hooman.ostovari.android.restaurantfinder.R;
import com.hooman.ostovari.restaurantfinder.model.Result;
import com.hooman.ostovari.restaurantfinder.utils.Constants;
import com.hooman.ostovari.restaurantfinder.utils.LocationProvider;

import java.io.IOException;

/**
 * Created by hoomi on 30/01/2014.
 */
public class CommunicationService extends IntentService {

    private RestClient client;

    public CommunicationService() {
        super("CommunicationService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        client = new RestClient(getString(R.string.places_api_key));
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (Constants.Intents.LOCATION_CHANGED.equals(intent.getAction())) {
            getPlacesNearBy(intent);
        }
    }

    private void getPlacesNearBy(Intent intent) {
        Location location = LocationProvider.getLocationFromIntent(intent);
        try {
            Result result = client.getNearPlaces(location, 1600);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
