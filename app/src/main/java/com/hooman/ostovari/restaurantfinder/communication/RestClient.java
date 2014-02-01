package com.hooman.ostovari.restaurantfinder.communication;

import android.location.Location;
import android.net.http.AndroidHttpClient;

import com.google.gson.Gson;
import com.hooman.ostovari.restaurantfinder.model.Result;
import com.hooman.ostovari.restaurantfinder.utils.Constants;
import com.hooman.ostovari.restaurantfinder.utils.Logger;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

/**
 * Created by hoomi on 30/01/2014.
 */
public class RestClient {
    private String api_key = "";
    private AndroidHttpClient httpClient;
    private Gson GSON;

    public RestClient(String api_key) {
        this.api_key = api_key;
        GSON = new Gson();
    }

    public Result getNearPlaces(Location location, int radius) throws IOException {
        String urlString = Constants.GooglePlaceConstant.SERVER_URL +
                "?location=" + location.getLatitude() + "," + location.getLongitude() +
                "&radius=" + radius +
                "&key=" + api_key +
                "&sensor=" + true +
                "&query=restaurant";
        Result result = null;
        try {
            httpClient = AndroidHttpClient.newInstance("Restaurant_Finder");
            HttpUriRequest getRequest = new HttpGet(urlString);
            HttpResponse httpResponse = httpClient.execute(getRequest);
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String s = EntityUtils.toString(httpResponse.getEntity());
                Logger.d(this, "response: " + s);
                result = GSON.fromJson(s, Result.class);
            }
        } finally {
            httpClient.close();
        }
        return result;
    }
}
