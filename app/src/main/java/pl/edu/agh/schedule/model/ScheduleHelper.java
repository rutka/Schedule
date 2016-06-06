package pl.edu.agh.schedule.model;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import pl.edu.agh.schedule.CalendarParser;
import pl.edu.agh.schedule.EventDTO;
import pl.edu.agh.schedule.myschedule.MyScheduleAdapter;
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
     * @return list of schedule items
     */
    private ArrayList<ScheduleItem> getScheduleData(Date date) {
        try {
            String beaconId = BeaconUtils.nearestBeacon();
            Log.v(TAG, "Getting schedules for beacon: " + beaconId + " and date: " + date);
            List<ScheduleItem> data = toScheduleItems(calendarParser.getEventsByBeaconAndDate(beaconId, date));
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
                // FIXME after hardcoded image
                item.backgroundImageUrl = "https://storage.googleapis.com/io2015-data.appspot.com/images/sessions/__w-200-400-600-800-1000__/11718f8b-b6d4-e411-b87f-00155d5066d7.jpg";
                scheduleItems.add(item);
            } catch (TimeUtils.TimestampException e) {
                Log.e(TAG, "Wrong timestamp format in event.");
            }
        }
        return scheduleItems;
    }

    public void getScheduleDataAsync(final MyScheduleAdapter adapter, Date date) {
        AsyncTask<Date, Void, ArrayList<ScheduleItem>> task
                = new AsyncTask<Date, Void, ArrayList<ScheduleItem>>() {
            @Override
            protected ArrayList<ScheduleItem> doInBackground(Date... params) {
                Date date = params[0];
                return getScheduleData(date);
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
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, date);
    }

    public void refreshCalendar() {
        this.calendarParser.init(context);
    }
}
