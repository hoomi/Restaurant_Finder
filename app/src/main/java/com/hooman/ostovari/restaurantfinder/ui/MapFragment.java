package com.hooman.ostovari.restaurantfinder.ui;

import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.hooman.ostovari.android.restaurantfinder.R;
import com.hooman.ostovari.restaurantfinder.db.tables.RestaurantTable;
import com.hooman.ostovari.restaurantfinder.model.Restaurant;
import com.hooman.ostovari.restaurantfinder.utils.Constants;
import com.hooman.ostovari.restaurantfinder.utils.RestaurantMarkerUtils;

import java.util.ArrayList;

/**
 * Created by hoomi on 28/01/2014.
 */
public class MapFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private MapView mapView;
    private ArrayList<Restaurant> restaurantMarkers =  new ArrayList<Restaurant>();


    public static MapFragment newInstance() {
        return new MapFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.map_fragment,container,false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mapView = (MapView) view.findViewById(R.id.v_map);
        mapView.onCreate(savedInstanceState);
        mapView.getMap().setMyLocationEnabled(true);
        getLoaderManager().initLoader(Constants.Loaders.RESTAURANT_ID,null,this);
    }

    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
    }

    @Override
    public void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        if (id == Constants.Loaders.RESTAURANT_ID) {
            return new CursorLoader(getActivity(), RestaurantTable.CONTENT_URI, RestaurantTable.PROJECTION, null, null, null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (cursorLoader.getId() == Constants.Loaders.RESTAURANT_ID) {
            if (cursor != null) {
                addRestaurantsToTheMap(cursor);
            }
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        if (cursorLoader.getId() == Constants.Loaders.RESTAURANT_ID) {

        }
    }

    private AsyncTask<Cursor, Restaurant, ArrayList<Restaurant>>  restaurantArrayListAsyncTask;



    private void addRestaurantsToTheMap(Cursor cursor) {
        if (restaurantArrayListAsyncTask == null) {
            restaurantArrayListAsyncTask = new AsyncTask<Cursor, Restaurant, ArrayList<Restaurant>>() {
                @Override
                protected void onPreExecute() {
                    restaurantMarkers.clear();
                    mapView.getMap().clear();
                    super.onPreExecute();
                }

                @Override
                protected ArrayList<Restaurant> doInBackground(Cursor... params) {
                    Cursor tempCursor = params[0];
                    Restaurant restaurant;
                    while (tempCursor.moveToNext()) {
                        restaurant = new Restaurant();
                        restaurant.setAddress(tempCursor.getString(tempCursor.getColumnIndex(RestaurantTable.Cols.FORMATTED_ADDRESS)));
                        restaurant.setName(tempCursor.getString(tempCursor.getColumnIndex(RestaurantTable.Cols.NAME)));
                        restaurant.setRating(tempCursor.getFloat(tempCursor.getColumnIndex(RestaurantTable.Cols.RATING)));
                        restaurant.setIcon(tempCursor.getString(tempCursor.getColumnIndex(RestaurantTable.Cols.ICON)));
                        restaurant.setLocation(new LatLng(tempCursor.getDouble(tempCursor.getColumnIndex(RestaurantTable.Cols.LAT)),tempCursor.getDouble(tempCursor.getColumnIndex(RestaurantTable.Cols.LNG))));
                        publishProgress(restaurant);
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(ArrayList<Restaurant> restaurants) {
                    restaurantMarkers = restaurants;
                    restaurantArrayListAsyncTask = null;
                }

                @Override
                protected void onProgressUpdate(Restaurant... values) {
                    Marker marker = mapView.getMap().addMarker(RestaurantMarkerUtils.createMarker(values[0].getLocation(),values[0].getName()));
                    values[0].setMarker(marker);
                    restaurantMarkers.add(values[0]);
                    super.onProgressUpdate(values);
                }
            };
            restaurantArrayListAsyncTask.execute(cursor)
;        }

    }
}
