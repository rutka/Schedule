package pl.edu.agh.schedule.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import pl.edu.agh.schedule.CalendarParser;
import pl.edu.agh.schedule.EventDTO;
import pl.edu.agh.schedule.myschedule.MyScheduleAdapter;
import pl.edu.agh.schedule.settings.SettingsActivity;
import pl.edu.agh.schedule.util.BeaconUtils;
import pl.edu.agh.schedule.util.TimeUtils;

import static pl.edu.agh.schedule.util.LogUtils.makeLogTag;


public class ScheduleHelper {

    private static final String TAG = makeLogTag(ScheduleHelper.class);
    private final CalendarParser calendarParser;
    private Context context;

    public ScheduleHelper(Context context) {
        this.context = context;
        this.calendarParser = new CalendarParser(context);
    }

    /**
     * Get schedule data for beacon and date
     *
     * @param date of specific day
     * @param location
     * @return list of schedule items
     */
    private ArrayList<ScheduleItem> getScheduleData(Date date, String location) {
        try {
            List<ScheduleItem> data = null;

            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            boolean beaconScanEnabled = sp.getBoolean(SettingsActivity.PREF_BEACON_SCAN_ENABLED, true);
            if (beaconScanEnabled) {
                String beaconId = BeaconUtils.nearestBeacon();
                Log.v(TAG, "Getting schedules for beacon: " + beaconId + " and date: " + date);
                data = toScheduleItems(calendarParser.getEventsByBeaconAndDate(beaconId, date));
            } else {
                Log.v(TAG, "Getting schedules for location: " + location + " and date: " + date);
                data = toScheduleItems(calendarParser.getEventsByLocationAndDate(location, date));
            }
            return ScheduleItemHelper.processItems(data);
        } catch (RuntimeException e) {
            Log.e(TAG, "Exception while getting schedule data.", e);
            throw e;
        }
    }

    private List<ScheduleItem> toScheduleItems(List<EventDTO> events) {
        List<ScheduleItem> scheduleItems = new LinkedList<>();
        for (EventDTO event : events) {
            try {
                ScheduleItem item = new ScheduleItem();
                item.title = event.getSummary();
                item.startTime = TimeUtils.timestampToMillis(event.getStartTime());
                item.endTime = TimeUtils.timestampToMillis(event.getEndTime());
                item.description = event.getDescription();
                scheduleItems.add(item);
            } catch (TimeUtils.TimestampException e) {
                Log.e(TAG, "Wrong timestamp format in event.");
            }
        }
        return scheduleItems;
    }

    public void getScheduleDataAsync(final MyScheduleAdapter adapter, Date date, String location) {
        AsyncTask<Object, Void, ArrayList<ScheduleItem>> task
                = new AsyncTask<Object, Void, ArrayList<ScheduleItem>>() {
            @Override
            protected ArrayList<ScheduleItem> doInBackground(Object... params) {
                Date date = null;
                if (params[0] instanceof Date) {
                    date = (Date) params[0];
                }
                String location = null;
                if (params[1] instanceof String) {
                    location = (String) params[1];
                }
                return getScheduleData(date, location);
            }

            @Override
            protected void onPostExecute(ArrayList<ScheduleItem> scheduleItems) {
                adapter.updateItems(scheduleItems);
            }
        };
        // On honeycomb and above, AsyncTasks are by default executed one by one. We are using a
        // thread pool instead here, because we want this to be executed independently from other
        // AsyncTasks. See the URL below for detail.
        // http://developer.android.com/reference/android/os/AsyncTask.html#execute(Params...)
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, date, location);
    }

    public void refreshCalendar() {
        this.calendarParser.init(context);
    }

    public String getLocationForBeacon(String beaconId) {
        return calendarParser.getLocation(beaconId);
    }

    public Set<String> getLocations() {
        return calendarParser.getLocations();
    }
}
