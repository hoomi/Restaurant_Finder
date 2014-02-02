package com.hooman.ostovari.restaurantfinder.communication;

import android.app.IntentService;
import android.content.ContentProviderOperation;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.location.Location;
import android.os.RemoteException;

import com.hooman.ostovari.android.restaurantfinder.R;
import com.hooman.ostovari.restaurantfinder.db.RestaurantContentProvider;
import com.hooman.ostovari.restaurantfinder.db.tables.RestaurantTable;
import com.hooman.ostovari.restaurantfinder.model.Result;
import com.hooman.ostovari.restaurantfinder.utils.Constants;
import com.hooman.ostovari.restaurantfinder.utils.LocationProvider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
        if (Constants.Intents.DOWNLOAD_NEAR_BY_RESTAURANTS.equals(intent.getAction())) {
            getPlacesNearBy(intent);
        }
    }

    private void getPlacesNearBy(Intent intent) {
        Location location = LocationProvider.getLocationFromIntent(intent);
        try {
            Result result = client.getNearPlaces(location, Constants.MILE);
            insertToDatabase(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void insertToDatabase(Result result) {
        ArrayList<ContentProviderOperation> contentProviderOperations = new ArrayList<ContentProviderOperation>();
        ContentProviderOperation.Builder contentProviderOperation;
        for (Result.Place place : result.places) {
            contentProviderOperation = ContentProviderOperation.newInsert(RestaurantTable.CONTENT_URI)
                    .withValue(RestaurantTable.Cols.FORMATTED_ADDRESS, place.formatted_address)
                    .withValue(RestaurantTable.Cols.ICON, place.icon)
                    .withValue(RestaurantTable.Cols.ID, place.id)
                    .withValue(RestaurantTable.Cols.LAT, place.geometry.location.lat)
                    .withValue(RestaurantTable.Cols.LNG, place.geometry.location.lng)
                    .withValue(RestaurantTable.Cols.NAME, place.name)
                    .withValue(RestaurantTable.Cols.REFERENCE, place.reference)
                    .withValue(RestaurantTable.Cols.RATING, place.rating)
                    .withValue(RestaurantTable.Cols.TYPES, joinArrayString(place.types, ", "))
                    .withYieldAllowed(true);
            contentProviderOperations.add(contentProviderOperation.build());
        }
        try {
            getContentResolver().applyBatch(RestaurantContentProvider.AUTHORITY, contentProviderOperations);
            getContentResolver().notifyChange(RestaurantTable.CONTENT_URI, null);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }
    }

    private String joinArrayString(List<String> array, CharSequence seperator) {
        if (array == null) {
            return "";
        }
        String returnString = "";
        for (int i = array.size() - 1; i >= 0; i--) {
            if (i != 0) {
                returnString += array.get(i) + seperator;
            } else {
                returnString += array.get(i);
            }
        }
        return returnString;
    }
}
