package com.hooman.ostovari.restaurantfinder.ui;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import com.hooman.ostovari.android.restaurantfinder.R;
import com.hooman.ostovari.restaurantfinder.db.tables.RestaurantTable;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Created by hoomi on 28/01/2014.
 */
public class RestaurantListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private final int RESTAURANT_ID = 1;
    private ExpandableListView restaurantList;
    private ProgressBar progressBar;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.list_fragment, container, false);
        restaurantList = (ExpandableListView) v.findViewById(R.id.v_restaurant_list);
        restaurantList.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                if (parent.isGroupExpanded(groupPosition)) {
                    parent.collapseGroup(groupPosition);
                } else {
                    parent.expandGroup(groupPosition);
                }
                return false;
            }
        });
        progressBar = (ProgressBar) v.findViewById(R.id.v_progress);
        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setListShown(false);
        getLoaderManager().initLoader(RESTAURANT_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        if (id == RESTAURANT_ID) {
            return new CursorLoader(getActivity(), RestaurantTable.CONTENT_URI, RestaurantTable.PROJECTION, null, null, null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (cursorLoader.getId() == RESTAURANT_ID) {
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
        if (cursorLoader.getId() == RESTAURANT_ID) {
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
            return new Object();
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return new Object();
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return groupPosition;
        }

        @Override
        public boolean hasStableIds() {
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
            viewHolder.ratingBar.setProgress(Math.round(mCursor.getFloat(ratingIndex)));
            viewHolder.restaurantNameTextView.setText(mCursor.getString(nameIndex));
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
            return true;
        }

        public void swapCursor(Cursor cursor) {
            if (mCursor != null && !mCursor.isClosed()) {
                mCursor.close();
            }
            mCursor = cursor;
            notifyDataSetChanged();
        }
    }

    class ParentViewHolder {
        TextView restaurantNameTextView;
        RatingBar ratingBar;

        ParentViewHolder(View v) {
            restaurantNameTextView = (TextView) v.findViewById(R.id.v_restaurant_name);
            ratingBar = (RatingBar) v.findViewById(R.id.v_rating_bar);
            ratingBar.setMax(5);
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
