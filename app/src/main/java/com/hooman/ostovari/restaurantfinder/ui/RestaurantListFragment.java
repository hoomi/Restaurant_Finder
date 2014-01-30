package com.hooman.ostovari.restaurantfinder.ui;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hooman.ostovari.restaurantfinder.db.tables.RestaurantTable;

/**
 * Created by hoomi on 28/01/2014.
 */
public class RestaurantListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>{
    private final int RESTAURANT_ID = 1;


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setListShown(false);
        setListAdapter(new RestaurantAdapter(getActivity()));
        getLoaderManager().initLoader(RESTAURANT_ID,null,this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        if (id == RESTAURANT_ID) {
            return new CursorLoader(getActivity(), RestaurantTable.CONTENT_URI,RestaurantTable.PROJECTION,null,null,null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (cursorLoader.getId() == RESTAURANT_ID) {
            setListShown(true);
            ((RestaurantAdapter)getListAdapter()).swapCursor(cursor);
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        if (cursorLoader.getId() == RESTAURANT_ID) {
            setListShown(true);
            ((RestaurantAdapter)getListAdapter()).swapCursor(null);
        }
    }

    class RestaurantAdapter extends CursorAdapter {

        public RestaurantAdapter(Context context) {
            super(context, null, false);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
            return new TextView(context);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ((TextView)view).setText(cursor.getString(cursor.getColumnIndex(RestaurantTable.Cols.NAME)));
        }
    }
}
