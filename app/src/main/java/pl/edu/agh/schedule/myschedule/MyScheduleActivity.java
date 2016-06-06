/*
 * Copyright 2014 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.edu.agh.schedule.myschedule;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ListFragment;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.SystemRequirementsChecker;

import org.joda.time.DateTime;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import pl.edu.agh.schedule.BuildConfig;
import pl.edu.agh.schedule.R;
import pl.edu.agh.schedule.model.ScheduleHelper;
import pl.edu.agh.schedule.ui.BaseActivity;
import pl.edu.agh.schedule.util.BeaconUtils;
import pl.edu.agh.schedule.util.TimeUtils;

import static pl.edu.agh.schedule.util.LogUtils.makeLogTag;

public class MyScheduleActivity extends BaseActivity implements MyScheduleFragment.Listener,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = makeLogTag(MyScheduleActivity.class);

    private static final int DAYS_RANGE = 15;
    private static final int TODAY = 7;
    private static final String TITLE = "pl.edu.agh.schedule.myschedule.TITLE";
    private static final String LOCATION = "pl.edu.agh.schedule.myschedule.LOCATION";
    private static final String DAY_INDEX = "pl.edu.agh.schedule.myschedule.DAY_INDEX";

    // The adapters that serves as the source of data for the UI, indicating the available
    // items. We have one adapter per day of the conference. When we push new data into these
    // adapters, the corresponding UIs update automatically.
    private MyScheduleAdapter[] mScheduleAdapters = new MyScheduleAdapter[DAYS_RANGE];

    // The ScheduleHelper is responsible for feeding data in a format suitable to the Adapter.
    private ScheduleHelper mDataHelper;
    // View pager and adapter
    ViewPager mViewPager = null;
    OurViewPagerAdapter mViewPagerAdapter = null;

    TabLayout mTabLayout = null;

    boolean mDestroyed = false;

    private Set<MyScheduleFragment> mMyScheduleFragments = new HashSet<>();

    private int baseTabViewId = 12345;

    private BeaconManager beaconManager;
    private Region region;
    private String location;

    @Override
    protected int getSelfNavDrawerItem() {
        return NAVDRAWER_ITEM_MY_SCHEDULE;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_schedule);
        if (mDataHelper == null) {
            mDataHelper = new ScheduleHelper(this);
        }
        mViewPager = (ViewPager) findViewById(R.id.view_pager);

        for (int i = 0; i < DAYS_RANGE; i++) {
            mScheduleAdapters[i] = new MyScheduleAdapter(this, getLUtils());
        }

        mViewPagerAdapter = new OurViewPagerAdapter(getFragmentManager());
        mViewPager.setAdapter(mViewPagerAdapter);

        mTabLayout = (TabLayout) findViewById(R.id.sliding_tabs);

        if (mTabLayout != null) {
            mTabLayout.setTabsFromPagerAdapter(mViewPagerAdapter);
        }

        mTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition(), true);
                TextView view = (TextView) findViewById(baseTabViewId + tab.getPosition());
                if (view != null) {
                    view.setContentDescription(
                            getString(R.string.talkback_selected,
                                    getString(R.string.a11y_button, tab.getText())));
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                TextView view = (TextView) findViewById(baseTabViewId + tab.getPosition());
                if (view != null) {
                    view.setContentDescription(
                            getString(R.string.a11y_button, tab.getText()));
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Do nothing
            }
        });
        mViewPager.addOnPageChangeListener(
                new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        // When swiping between pages, select the corresponding tab.
                        securelySelectTab(position);
                    }
                });

        mViewPager.setPageMargin(getResources()
                .getDimensionPixelSize(R.dimen.my_schedule_page_margin));
        mViewPager.setPageMarginDrawable(R.drawable.page_margin);
        setTabLayoutContentDescriptions();

        overridePendingTransition(0, 0);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        sp.registerOnSharedPreferenceChangeListener(this);

        // FIXME adjust settings
        beaconManager = new BeaconManager(this);
        beaconManager.setForegroundScanPeriod(1000, 5000);
        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, List<Beacon> list) {
                if (!list.isEmpty()) {
                    Beacon nearestBeacon = list.get(0);
                    String id = String.format("%s:%d:%d",
                            nearestBeacon.getProximityUUID().toString(),
                            nearestBeacon.getMajor(),
                            nearestBeacon.getMinor());
                    if (!id.equals(BeaconUtils.nearestBeacon())) {
                        setTitle(mDataHelper.getLocationForBeacon(id));
                        BeaconUtils.nearestBeacon(id);
                        updateData();
                    }
                }
            }
        });
        region = new Region("ranged region", null, null, null);
        SystemRequirementsChecker.checkWithDefaultDialogs(this);
        if (mViewPager != null) {
            selectDay(TODAY);
        }
        if (savedInstanceState == null) {
            new Handler().postDelayed(
                    new Runnable() {
                        @Override
                        public void run() {
                            securelySelectTab(TODAY);
                        }
                    }, 100);
        }
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startRanging(region);
            }
        });
    }

    private void securelySelectTab(int position) {
        TabLayout.Tab tab = mTabLayout.getTabAt(position);
        if (tab != null) {
            tab.select();
        }
    }

    private void setTabLayoutContentDescriptions() {
        LayoutInflater inflater = getLayoutInflater();
        int gap = 0;
        for (int i = 0, count = mTabLayout.getTabCount(); i < count; i++) {
            TabLayout.Tab tab = mTabLayout.getTabAt(i);
            if (tab != null) {
                TextView view = (TextView) inflater.inflate(R.layout.tab_my_schedule, mTabLayout, false);
                view.setId(baseTabViewId + i);
                view.setText(tab.getText());
                if (i == 0) {
                    view.setContentDescription(
                            getString(R.string.talkback_selected,
                                    getString(R.string.a11y_button, tab.getText())));
                } else {
                    view.setContentDescription(
                            getString(R.string.a11y_button, tab.getText()));
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    view.announceForAccessibility(
                            getString(R.string.my_schedule_tab_desc_a11y, getDayName(i - gap)));
                }
                tab.setCustomView(view);
            } else {
                Log.e(TAG, "Tab is null.");
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(TITLE, getTitle().toString());
        outState.putInt(DAY_INDEX, mViewPager.getCurrentItem());
        outState.putString(LOCATION, location);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        setTitle(savedInstanceState.getString(TITLE));
        location = savedInstanceState.getString(LOCATION);
        final int day = savedInstanceState.getInt(DAY_INDEX);
        System.out.println(getDayName(day));
        new Handler().postDelayed(
                new Runnable() {
                    @Override
                    public void run() {
                        securelySelectTab(day);
                    }
                }, 100);
        selectDay(day);
        updateData();
        super.onRestoreInstanceState(savedInstanceState);
    }

    private void selectDay(int day) {
        mViewPager.setCurrentItem(day);
    }

    @Override
    public boolean canSwipeRefreshChildScrollUp() {
        for (MyScheduleFragment fragment : mMyScheduleFragments) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                if (!fragment.getUserVisibleHint()) {
                    continue;
                }
            }
            return ViewCompat.canScrollVertically(fragment.getListView(), -1);
        }
        return false;
    }

    protected void updateData() {
        Log.d(TAG, "Filling adapters with data.");
        for (int i = 0; i < DAYS_RANGE; i++) {
            mDataHelper.getScheduleDataAsync(mScheduleAdapters[i], getDayAtPosition(i), location);
        }
    }

    private Date getDayAtPosition(int position) {
        DateTime dateTime = new DateTime(new Date());
        int shift = position - TODAY;
        dateTime = dateTime.plusDays(shift);
        return dateTime.toDate();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDestroyed = true;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        sp.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onFragmentViewCreated(ListFragment fragment) {
        int dayIndex = fragment.getArguments().getInt(DAY_INDEX, 0);
        fragment.setListAdapter(mScheduleAdapters[dayIndex]);
        fragment.getListView().setRecyclerListener(mScheduleAdapters[dayIndex]);

    }

    @Override
    public void onFragmentAttached(MyScheduleFragment fragment) {
        mMyScheduleFragments.add(fragment);
    }

    @Override
    public void onFragmentDetached(MyScheduleFragment fragment) {
        mMyScheduleFragments.remove(fragment);
    }

    private class OurViewPagerAdapter extends FragmentPagerAdapter {

        public OurViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Log.d(TAG, "Creating fragment #" + position);
            MyScheduleFragment frag = new MyScheduleFragment();
            Bundle args = new Bundle();
            args.putInt(DAY_INDEX, position);
            frag.setArguments(args);
            return frag;
        }

        @Override
        public int getCount() {
            return DAYS_RANGE;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return getDayName(position);
        }

    }

    private String getDayName(int position) {
        return TimeUtils.formatShortDate(this, getDayAtPosition(position));
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        for (String location : mDataHelper.getLocations()) {
            menu.add(location);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String newLocation = item.getTitle().toString();
        location = newLocation;
        updateData();
        setTitle(newLocation);
        // TODO TURN OFF BEACON SCAN AND ZERO LOCATION VARIABLE AFTER TURNING ON BEACON SCAN
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(BuildConfig.BEACONS) || key.equals(BuildConfig.SCHEDULE)) {
            mDataHelper.refreshCalendar();
            Log.d(TAG, "Refreshing calendar.");
            updateData();
        }
    }
}
