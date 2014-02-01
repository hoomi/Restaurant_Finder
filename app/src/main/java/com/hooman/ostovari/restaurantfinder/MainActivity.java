package com.hooman.ostovari.restaurantfinder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.MapsInitializer;
import com.hooman.ostovari.android.restaurantfinder.R;
import com.hooman.ostovari.restaurantfinder.communication.CommunicationService;
import com.hooman.ostovari.restaurantfinder.ui.MapFragment;
import com.hooman.ostovari.restaurantfinder.ui.RestaurantListFragment;
import com.hooman.ostovari.restaurantfinder.utils.Constants;
import com.hooman.ostovari.restaurantfinder.utils.LocationProvider;

import java.util.Locale;

public class MainActivity extends ActionBarActivity implements ActionBar.TabListener {


    SectionsPagerAdapter mSectionsPagerAdapter;
    private LocationProvider locationProvider;
    private OnLocationChangeListener onLocationChangeListener;
    private Handler handler = new Handler();
    private Runnable locationRunnable = new Runnable() {
        @Override
        public void run() {
            if (!locationProvider.isConnected() && locationProvider.isLocationEnabled()) {
                handler.postDelayed(locationRunnable, 1000);
            } else {
                locationProvider.requestLocationUpdates();
                startService(new Intent(MainActivity.this, CommunicationService.class).setAction(Constants.Intents.DOWNLOAD_NEAR_BY_RESTAURANTS).putExtra(LocationClient.KEY_LOCATION_CHANGED,locationProvider.getLastKnowLocation()));
            }
        }
    };


    private BroadcastReceiver locationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Constants.Intents.LOCATION_CHANGED.equals(intent.getAction())) {
                Location location = LocationProvider.getLocationFromIntent(intent);
                startService(new Intent(MainActivity.this, CommunicationService.class).setAction(Constants.Intents.DOWNLOAD_NEAR_BY_RESTAURANTS).putExtra(LocationClient.KEY_LOCATION_CHANGED,location));
                if (onLocationChangeListener != null) {
                    onLocationChangeListener.onLocationChanged(location);
                }
            }
        }
    };

    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }
        LocationProvider.initialize(this);
        locationProvider = LocationProvider.getInstance();

        try {
            MapsInitializer.initialize(this);
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        locationProvider.removeLocationUpdates();
        handler.removeCallbacks(locationRunnable);
        unregisterReceiver(locationReceiver);
    }

    public void setOnLocationChangeListener(OnLocationChangeListener onLocationChangeListener) {
        this.onLocationChangeListener = onLocationChangeListener;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!locationProvider.isConnected() && locationProvider.isLocationEnabled()) {
            handler.postDelayed(locationRunnable, 1000);
        } else if (locationProvider.isConnected()) {
            locationProvider.requestLocationUpdates();
            startService(new Intent(this, CommunicationService.class).setAction(Constants.Intents.DOWNLOAD_NEAR_BY_RESTAURANTS).putExtra(LocationClient.KEY_LOCATION_CHANGED, locationProvider.getLastKnowLocation()));
        }
        registerReceiver(locationReceiver,new IntentFilter("com.hooman.ostovari.restaurant.LOCATION_CHANGED"));
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return MapFragment.newInstance();
            } else {
                return new RestaurantListFragment();
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.on_the_map).toUpperCase(l);
                case 1:
                    return getString(R.string.restaurants_list).toUpperCase(l);

            }
            return null;
        }
    }

    public interface OnLocationChangeListener {
        public void onLocationChanged(Location newLocation);
    }
}
