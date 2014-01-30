package com.hooman.ostovari.restaurantfinder.utils;

import android.app.ActivityManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;

import java.io.PrintWriter;
import java.io.StringWriter;

public final class GeneralUtils {

    private static final int TWO_MINUTES = 1000 * 60 * 2;

    private GeneralUtils() {
    }

    public static boolean isServiceRunning(Context context, Class<?> myService) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (myService.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static int getDrawable(Context context, String name) {
        Resources res = context.getResources();
        String packageName = context.getPackageName();
        String resourceType = "drawable";
        return res.getIdentifier(name, resourceType, packageName);
    }


    private static String stackTrace(Throwable ex) {
        if (null == ex) {
            return "";
        }
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        String stackTrace = sw.toString();
        pw.close();
        return stackTrace;
    }

    public static boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (null == location) {
            // No location is not a better location
            return false;
        }

        if (null == currentBestLocation) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    public static boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    public static boolean insideGeofence(Location currentLocation, double latitude, double longitude, float radius) {
        float[] results = new float[2];
        Location.distanceBetween(currentLocation.getLatitude(), currentLocation.getLongitude(),
                latitude, longitude, results);

        return results[0] <= radius;
    }

    public static boolean isLocationEnabled(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean isWifi = false;
        boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (connectivityManager.getActiveNetworkInfo() != null) {
            isWifi = connectivityManager.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_WIFI;
        }
        return gpsEnabled || (networkEnabled && isWifi);
    }

    public static PendingIntent getLocationIntent(Context context) {
        Intent i = new Intent(Constants.Intents.LOCATION_CHANGED);
        return PendingIntent.getService(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static void disableBroadcastReceiver(Context context, Class<? extends BroadcastReceiver> receiverClass) {
        ComponentName receiver = new ComponentName(context, receiverClass);
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }

    public static void enableBroadcastReceiver(Context context, Class<? extends BroadcastReceiver> receiverClass) {
        ComponentName receiver = new ComponentName(context, receiverClass);
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }
}

