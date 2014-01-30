package com.hooman.ostovari.restaurantfinder.utils;

import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;

/**
 * Created by hostova1 on 18/10/2013.
 */
public class LocationProvider {
    private static LocationProvider locationProvider;
    private LocationManager locationManager;
    private LocationClient locationClient;
    private int numberOfListeners = 0;
    private final Context context;

    private final int minMillisecond;
    private final int fasterMillisecond;
    private final int minMeters;
    private boolean connected;

    private LocationProvider(final Context context) {
        this.context = context;
        minMillisecond = 10 * 1000;
        fasterMillisecond = 5 * 1000;
        minMeters =  20;
        connected = false;
    }

    public synchronized static void initialize(final Context context) {
        if (LocationProvider.locationProvider == null) {
            LocationProvider.locationProvider = new LocationProvider(context);
        }
        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS && locationProvider.locationClient == null) {
            locationProvider.locationClient = new LocationClient(context, new GooglePlayServicesClient.ConnectionCallbacks() {
                @Override
                public void onConnected(Bundle bundle) {
                    locationProvider.connected = true;
                }

                @Override
                public void onDisconnected() {
                    locationProvider.locationClient = null;
                    locationProvider.connected = false;
                }
            }, new GooglePlayServicesClient.OnConnectionFailedListener() {
                @Override
                public void onConnectionFailed(ConnectionResult connectionResult) {
                    locationProvider.locationClient = null;
                    locationProvider.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                    locationProvider.connected = true;

                }
            });
            locationProvider.locationClient.connect();
        } else {
            locationProvider.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            locationProvider.connected = true;
        }
    }


    public synchronized  static LocationProvider getInstance() {
        return locationProvider;
    }

    public synchronized void requestLocationUpdates() {
        if (!isLocationEnabled()) {
            return;
        }
        if (numberOfListeners == 0) {
            if (locationClient == null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                    Criteria criteria = new Criteria();
                    criteria.setAccuracy(Criteria.ACCURACY_FINE);
                    criteria.setCostAllowed(true);
                    locationManager.requestLocationUpdates(minMillisecond, minMeters, criteria, GeneralUtils.getLocationIntent(context));
                } else {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minMillisecond, minMeters, GeneralUtils.getLocationIntent(context));
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minMillisecond, minMeters, GeneralUtils.getLocationIntent(context));
                    locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, minMillisecond, minMeters, GeneralUtils.getLocationIntent(context));
                }
            } else if (locationClient.isConnected()) {
                LocationRequest locationRequest = new LocationRequest();
                locationRequest.setInterval(minMillisecond);
                locationRequest.setSmallestDisplacement(minMeters);
                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                locationRequest.setFastestInterval(fasterMillisecond);
                locationClient.requestLocationUpdates(locationRequest, GeneralUtils.getLocationIntent(context));
            } else {
                return;
            }
        }
        numberOfListeners++;
    }

    public boolean isConnected() {
        return connected;
    }

    public void removeLocationUpdates() {
        if (locationClient == null && locationManager == null) {
            numberOfListeners = 0;
            return;
        }
        if (numberOfListeners == 1) {
            if (locationClient == null) {
                locationManager.removeUpdates(GeneralUtils.getLocationIntent(context));
            } else if (locationClient != null && locationClient.isConnected()) {
                locationClient.removeLocationUpdates(GeneralUtils.getLocationIntent(context));
            }
        }
        numberOfListeners --;
        numberOfListeners= numberOfListeners < 0 ? 0 : numberOfListeners;
    }

    public void destroy() {
        if (locationClient == null) {
            locationManager.removeUpdates(GeneralUtils.getLocationIntent(context));
        } else if (locationClient != null && locationClient.isConnected()) {
            locationClient.removeLocationUpdates(GeneralUtils.getLocationIntent(context));
            locationClient.disconnect();
        }
        numberOfListeners = 0;
        locationProvider = null;

    }

    public Location getLastKnowLocation() {
        Location bestLastLocation;
        if (locationClient == null) {
            Location gpsLastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Location netLastLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            Location passiveLastLocation = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
            bestLastLocation = gpsLastLocation;
            if (GeneralUtils.isBetterLocation(netLastLocation, bestLastLocation)) {
                bestLastLocation = netLastLocation;
            }
            if (GeneralUtils.isBetterLocation(passiveLastLocation, bestLastLocation)) {
                bestLastLocation = passiveLastLocation;
            }
        } else if (locationClient.isConnected()) {
            bestLastLocation = locationClient.getLastLocation();
        } else {
            return null;
        }
        return bestLastLocation;
    }

    public  boolean isLocationEnabled() {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean isWifi = false;
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (connectivityManager.getActiveNetworkInfo() != null) {
            isWifi =  connectivityManager.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_WIFI;
        }
        return gpsEnabled || (networkEnabled && isWifi);
    }

    public static Location getLocationFromIntent(Intent intent) {
        Location location = null;
        if (intent.hasExtra(LocationManager.KEY_LOCATION_CHANGED)) {
            location = (Location)intent.getExtras().get(LocationManager.KEY_LOCATION_CHANGED);
            Logger.i(LocationProvider.class, String.format("Got location from LocationManager: %s", location.toString()));
        } else if (intent.hasExtra(LocationClient.KEY_LOCATION_CHANGED)) {
            location = (Location)intent.getExtras().get(LocationClient.KEY_LOCATION_CHANGED);
            Logger.i(LocationProvider.class, String.format("Got location from Google services: %s", location.toString()));
        }
    return location;

    }
}
