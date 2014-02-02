package com.hooman.ostovari.restaurantfinder.ui;

import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hooman.ostovari.android.restaurantfinder.R;
import com.hooman.ostovari.restaurantfinder.MainActivity;
import com.hooman.ostovari.restaurantfinder.db.tables.RestaurantTable;
import com.hooman.ostovari.restaurantfinder.utils.Constants;
import com.hooman.ostovari.restaurantfinder.utils.LocationProvider;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Created by hoomi on 28/01/2014.
 */
public class RestaurantListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, MainActivity.OnLocationChangeListener {

    private ExpandableListView restaurantList;
    private ProgressBar progressBar;
    private Location myLocation = null;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.list_fragment, container, false);
        restaurantList = (ExpandableListView) v.findViewById(R.id.v_restaurant_list);
        restaurantList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (restaurantList.isGroupExpanded(position)) {
                    restaurantList.collapseGroup(position);
                } else {
                    restaurantList.expandGroup(position);
                }
            }
        });
        progressBar = (ProgressBar) v.findViewById(R.id.v_progress);
        Location location;
        if (savedInstanceState != null) {
            location = savedInstanceState.getParcelable("location");
        } else {
            location = LocationProvider.getInstance().getLastKnowLocation();
        }
        requestRestaurants(location);
        return v;
    }

    private void requestRestaurants(Location location) {
            if (location != null) {
                Bundle b = new Bundle();
                b.putParcelable("location", location);
                getLoaderManager().restartLoader(Constants.Loaders.RESTAURANT_ID, b, this);
            }
        myLocation = location;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setListShown(false);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (myLocation != null) {
            outState.putParcelable("location", myLocation);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) getActivity()).setOnLocationChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        ((MainActivity) getActivity()).setOnLocationChangeListener(null);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        if (id == Constants.Loaders.RESTAURANT_ID) {
            Location location = bundle.getParcelable("location");
            String latString = location.getLatitude() < 0 ? "+" + (-location.getLatitude()) : -location.getLatitude() + "";
            String lonString = location.getLongitude() < 0 ? "+" + (-location.getLongitude()) : -location.getLongitude() + "";
            // The where clause is just an approximation. Since we do not need a very high accuracy this would work
            return new CursorLoader(getActivity(), RestaurantTable.CONTENT_URI, RestaurantTable.PROJECTION,
                    "(" + RestaurantTable.Cols.LAT + latString + ") * (" + RestaurantTable.Cols.LAT + latString + ")" +
                            " + (" + RestaurantTable.Cols.LNG + lonString + ")*(" + RestaurantTable.Cols.LNG + lonString + ") * 13800000000<=" + Constants.MILE * Constants.MILE, null, RestaurantTable.Cols.RATING + " DESC");
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (cursorLoader.getId() == Constants.Loaders.RESTAURANT_ID) {
            setListShown(true);
            if (restaurantList.getExpandableListAdapter() == null) {
                restaurantList.setAdapter(new RestaurantAdapter(cursor));
            } else {
                ((RestaurantAdapter) restaurantList.getExpandableListAdapter()).swapCursor(cursor);
            }
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        if (cursorLoader.getId() == Constants.Loaders.RESTAURANT_ID) {
            setListShown(false);
            if (restaurantList.getExpandableListAdapter() != null) {
                ((RestaurantAdapter) restaurantList.getExpandableListAdapter()).swapCursor(null);

            }
        }
    }

    public void setListShown(boolean show) {
        progressBar.setVisibility(show ? View.GONE : View.VISIBLE);
        restaurantList.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onLocationChanged(Location newLocation) {
       requestRestaurants(newLocation);
    }

    class RestaurantAdapter extends BaseExpandableListAdapter {
        private Cursor mCursor;
        private int addressIndex = -1;
        private int nameIndex = -1;
        private int ratingIndex = -1;
        private int iconIndex = -1;
        private int typesIndex = -1;

        RestaurantAdapter(Cursor mCursor) {
            this.mCursor = mCursor;
        }

        @Override
        public int getGroupCount() {
            if (mCursor != null && addressIndex < 0) {
                addressIndex = mCursor.getColumnIndex(RestaurantTable.Cols.FORMATTED_ADDRESS);
                nameIndex = mCursor.getColumnIndex(RestaurantTable.Cols.NAME);
                iconIndex = mCursor.getColumnIndex(RestaurantTable.Cols.ICON);
                typesIndex = mCursor.getColumnIndex(RestaurantTable.Cols.TYPES);
                ratingIndex = mCursor.getColumnIndex(RestaurantTable.Cols.RATING);
            }
            return mCursor == null ? 0 : mCursor.getCount();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return 1;
        }

        @Override
        public Object getGroup(int groupPosition) {
            return null;
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return null;
        }

        @Override
        public long getGroupId(int groupPosition) {
            mCursor.moveToPosition(groupPosition);
            return mCursor.getLong(mCursor.getColumnIndex(RestaurantTable.Cols._ID));
        }


        @Override
        public long getChildId(int groupPosition, int childPosition) {
            mCursor.moveToPosition(groupPosition);
            return mCursor.getLong(mCursor.getColumnIndex(RestaurantTable.Cols._ID));
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }


        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            if (!mCursor.moveToPosition(groupPosition)) {
                return null;
            }
            ParentViewHolder viewHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.restaurant_item, parent, false);
                viewHolder = new ParentViewHolder(convertView);
                convertView.setTag(viewHolder);
            }
            viewHolder = (ParentViewHolder) convertView.getTag();
            viewHolder.restaurantNameTextView.setText(mCursor.getString(nameIndex));
            viewHolder.ratingTextView.setText("Rating: " + mCursor.getFloat(ratingIndex));
            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            if (!mCursor.moveToPosition(groupPosition)) {
                return null;
            }
            ChildViewHolder childViewHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.restaurant_details, parent, false);
                childViewHolder = new ChildViewHolder(convertView);
                convertView.setTag(childViewHolder);
            }
            childViewHolder = (ChildViewHolder) convertView.getTag();
            childViewHolder.typesTextView.setText(mCursor.getString(typesIndex));
            childViewHolder.addressTextView.setText(mCursor.getString(addressIndex));
            ImageLoader.getInstance().displayImage(mCursor.getString(iconIndex), childViewHolder.iconImageView);
            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return false;
        }

        public Cursor swapCursor(Cursor cursor) {
            if (cursor == mCursor) return mCursor;
            if (mCursor != null && !mCursor.isClosed()) {
                mCursor.close();
            }
            mCursor = cursor;
            notifyDataSetChanged();
            return cursor;
        }
    }

    class ParentViewHolder {
        TextView restaurantNameTextView;
        TextView ratingTextView;

        ParentViewHolder(View v) {
            restaurantNameTextView = (TextView) v.findViewById(R.id.v_restaurant_name);
            ratingTextView = (TextView) v.findViewById(R.id.v_rating);
        }
    }

    class ChildViewHolder {
        TextView addressTextView;
        TextView typesTextView;
        ImageView iconImageView;

        ChildViewHolder(View v) {
            addressTextView = (TextView) v.findViewById(R.id.v_address);
            typesTextView = (TextView) v.findViewById(R.id.v_types);
            iconImageView = (ImageView) v.findViewById(R.id.v_icon);
        }
    }
}
