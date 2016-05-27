/*
 * Copyright 2015 Google Inc. All rights reserved.
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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.net.Uri;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import pl.edu.agh.schedule.R;
import pl.edu.agh.schedule.model.ScheduleItem;
import pl.edu.agh.schedule.util.LUtils;
import pl.edu.agh.schedule.util.TimeUtils;
import pl.edu.agh.schedule.util.UIUtils;

import static pl.edu.agh.schedule.util.LogUtils.makeLogTag;

/**
 * Adapter that produces views to render (one day of) the "My Schedule" screen.
 */
public class MyScheduleAdapter implements ListAdapter, AbsListView.RecyclerListener {
    private static final String TAG = makeLogTag(MyScheduleAdapter.class);

    private final Context mContext;
    private final LUtils mLUtils;

    // list of items served by this adapter
    ArrayList<ScheduleItem> mItems = new ArrayList<>();

    // observers to notify about changes in the data
    ArrayList<DataSetObserver> mObservers = new ArrayList<>();

    private final int mHourColorDefault;
    private final int mHourColorPast;
    private final int mTitleColorDefault;
    private final int mTitleColorPast;
    private final int mColorBackgroundDefault;
    private final int mColorBackgroundPast;
    private final int mListSpacing;
    private final int mSelectableItemBackground;
    private final boolean mIsRtl;

    @SuppressWarnings("deprecation")
    public MyScheduleAdapter(Context context, LUtils lUtils) {
        mContext = context;
        mLUtils = lUtils;
        Resources resources = context.getResources();
        mHourColorDefault = resources.getColor(R.color.my_schedule_hour_header_default);
        mHourColorPast = resources.getColor(R.color.my_schedule_hour_header_finished);
        mTitleColorDefault = resources.getColor(R.color.my_schedule_session_title_default);
        mTitleColorPast = resources.getColor(R.color.my_schedule_session_title_finished);
        mColorBackgroundDefault = resources.getColor(android.R.color.white);
        mColorBackgroundPast = resources.getColor(R.color.my_schedule_past_background);
        mListSpacing = resources.getDimensionPixelOffset(R.dimen.element_spacing_normal);
        TypedArray a = context.obtainStyledAttributes(new int[]{R.attr.selectableItemBackground});
        mSelectableItemBackground = a.getResourceId(0, 0);
        a.recycle();
        mIsRtl = UIUtils.isRtl(context);
    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    @Override
    public boolean isEnabled(int position) {
        return true;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        if (!mObservers.contains(observer)) {
            mObservers.add(observer);
        }
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        if (mObservers.contains(observer)) {
            mObservers.remove(observer);
        }
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public Object getItem(int position) {
        return position >= 0 && position < mItems.size() ? mItems.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    private String formatDescription(ScheduleItem item) {
        return item.description.substring(0, 20);
    }

    private View.OnClickListener mUriOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            /*Object tag = v.getTag(R.id.myschedule_uri_tagkey);
            System.out.println(v);
            System.out.println(tag);
            if (tag != null && tag instanceof Uri) {
                Uri uri = (Uri) tag;
                //mContext.startActivity(new Intent(Intent.ACTION_VIEW, uri)); FIXME openning details
            }*/
        }
    };

    private void setUriClickable(View view, Uri uri) {
//        view.setTag(R.id.myschedule_uri_tagkey, uri);
        view.setOnClickListener(mUriOnClickListener);
        view.setBackgroundResource(mSelectableItemBackground);
    }

    private static void clearClickable(View view) {
        view.setOnClickListener(null);
        view.setBackgroundResource(0);
        view.setClickable(false);
    }

    /**
     * Enforces right-alignment to all the TextViews in the {@code holder}. This is not necessary if
     * all the data is localized in the targeted RTL language, but as we will not be able to
     * localize the conference data, we hack it.
     *
     * @param holder The {@link ViewHolder} of the list item.
     */
    @SuppressLint("RtlHardcoded")
    private void adjustForRtl(ViewHolder holder) {
        if (mIsRtl) {
            holder.startTime.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
            holder.title.setGravity(Gravity.RIGHT);
            holder.description.setGravity(Gravity.RIGHT);
            Log.d(TAG, "Gravity right");
        }
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {

        ViewHolder holder;
        // Create a new view if it is not ready yet.
        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.my_schedule_item, parent, false);
            holder = new ViewHolder();
            holder.startTime = (TextView) view.findViewById(R.id.start_time);
            holder.title = (TextView) view.findViewById(R.id.slot_title);
            holder.description = (TextView) view.findViewById(R.id.slot_description);
            holder.separator = view.findViewById(R.id.separator);
            holder.touchArea = view.findViewById(R.id.touch_area);
            view.setTag(holder);
            // Typeface
            mLUtils.setMediumTypeface(holder.startTime);
            mLUtils.setMediumTypeface(holder.title);
            adjustForRtl(holder);
        } else {
            holder = (ViewHolder) view.getTag();
            // Clear event listeners
            clearClickable(view);
            clearClickable(holder.startTime);
            clearClickable(holder.touchArea);
            //Make sure it doesn't retain conflict coloring
            holder.description.setTextColor(mHourColorDefault);
        }

        if (position < 0 || position >= mItems.size()) {
            Log.e(TAG, "Invalid view position passed to MyScheduleAdapter: " + position);
            return view;
        }
        final ScheduleItem item = mItems.get(position);
        ScheduleItem nextItem = position < mItems.size() - 1 ? mItems.get(position + 1) : null;

        long now = UIUtils.getCurrentTime(view.getContext());
        boolean isPastDuringConference = item.endTime <= now;

        if (isPastDuringConference) {
            view.setBackgroundColor(mColorBackgroundPast);
            holder.startTime.setTextColor(mHourColorPast);
            holder.title.setTextColor(mTitleColorPast);
        } else {
            view.setBackgroundColor(mColorBackgroundDefault);
            holder.startTime.setTextColor(mHourColorDefault);
            holder.title.setTextColor(mTitleColorDefault);

        }
        holder.description.setVisibility(View.VISIBLE);
        holder.startTime.setText(TimeUtils.formatShortTime(mContext, new Date(item.startTime)));

//        holder.touchArea.setTag(R.id.myschedule_uri_tagkey, null);
           holder.title.setVisibility(View.VISIBLE);
            holder.title.setText(item.title);

           // Uri sessionUri = ScheduleContract.Sessions.buildSessionUri(item.sessionId); FIXME

                holder.startTime.setVisibility(View.VISIBLE);
                /*setUriClickable(holder.startTime,
                        ScheduleContract.Sessions.buildUnscheduledSessionsInInterval(
                                item.startTime, item.endTime));*/
                // Padding fix needed for KitKat 4.4. (padding gets removed by setting the background)
                holder.startTime.setPadding(
                        (int) mContext.getResources().getDimension(R.dimen.keyline_2), 0,
                        (int) mContext.getResources().getDimension(R.dimen.keyline_2), 0);
                // FIXME setUriClickable(holder.touchArea, sessionUri);

            holder.description.setText(formatDescription(item));


        holder.separator.setVisibility(nextItem == null ? View.GONE : View.VISIBLE);

        if (position == 0) { // First item
            view.setPadding(0, mListSpacing, 0, 0);
        } else if (nextItem == null) { // Last item
            view.setPadding(0, 0, 0, mListSpacing);
        } else {
            view.setPadding(0, 0, 0, 0);
        }

        return view;
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }


    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return mItems.isEmpty();
    }

    private void notifyObservers() {
        for (DataSetObserver observer : mObservers) {
            observer.onChanged();
        }
    }

    public void updateItems(List<ScheduleItem> items) {
        mItems.clear();
        if (items != null) {
            for (ScheduleItem item : items) {
                Log.d(TAG, "Adding schedule item: " + item + " start=" + new Date(item.startTime));
                mItems.add((ScheduleItem) item.clone());
            }
        }
        notifyObservers();
    }

    @Override
    public void onMovedToScrapHeap(View view) {
        // NO OP
    }

    private static class ViewHolder {
        public TextView startTime;
        public TextView title;
        public TextView description;
        public View separator;
        public View touchArea;
    }

}
