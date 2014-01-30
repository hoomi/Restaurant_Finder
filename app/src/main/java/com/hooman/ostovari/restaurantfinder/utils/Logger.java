package com.hooman.ostovari.restaurantfinder.utils;

import android.util.Log;

import com.hooman.ostovari.android.restaurantfinder.BuildConfig;


public class Logger {

    protected final static boolean SHOW_LINE_NUMBER = true;
    protected final static boolean DEBUG = true;

    public static void i(Object object, String message) {
        if (BuildConfig.DEBUG && DEBUG) {
            String debugMessage = message;
            if (SHOW_LINE_NUMBER) {
                debugMessage = addAdditionalInformation() + message;
            }
            Log.i(object.getClass().getSimpleName(), debugMessage);
        }
    }

    public static void d(Object object, String message) {
        if (BuildConfig.DEBUG && DEBUG) {
            String debugMessage = message;
            if (SHOW_LINE_NUMBER) {
                debugMessage = addAdditionalInformation() + message;
            }
            Log.d(object.getClass().getSimpleName(), debugMessage);
        }
    }

    public static void w(Object object, String message) {
        if (BuildConfig.DEBUG && DEBUG) {
            String debugMessage = message;
            if (SHOW_LINE_NUMBER) {
                debugMessage = addAdditionalInformation() + message;
            }
            Log.w(object.getClass().getSimpleName(), debugMessage);
        }
    }

    public static void e(Object object, String message) {
        if (BuildConfig.DEBUG && DEBUG) {
            String debugMessage = message;
            if (SHOW_LINE_NUMBER) {
                debugMessage = addAdditionalInformation() + message;
            }
            Log.e(object.getClass().getSimpleName(), debugMessage);
        }
    }

    public static void v(Object object, String message) {
        if (BuildConfig.DEBUG && DEBUG) {
            String debugMessage = message;
            if (SHOW_LINE_NUMBER) {
                debugMessage = addAdditionalInformation() + message;
            }
            Log.v(object.getClass().getSimpleName(), debugMessage);
        }
    }

    private static String addAdditionalInformation() {
        StackTraceElement trace = Thread.currentThread().getStackTrace()[4];
        return trace.getFileName() + " ===> " + trace.getClassName() + " ===> " + trace.getMethodName() + " ===> " + trace.getLineNumber() + "\n";
    }
}
