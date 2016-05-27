package pl.edu.agh.schedule.model;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import pl.edu.agh.schedule.util.LogUtils;

public class ScheduleItemHelper {

    private static final String TAG = LogUtils.makeLogTag(ScheduleItem.class);

    /**
     * Sort items to be sure that are ordered properly
     **/
    public static ArrayList<ScheduleItem> processItems(List<ScheduleItem> items) {
        ArrayList<ScheduleItem> result = new ArrayList<>();
        result.addAll(items);
        Log.d(TAG, "Results: " + result.toString());

        Collections.sort(result, new Comparator<ScheduleItem>() {
            @Override
            public int compare(ScheduleItem lhs, ScheduleItem rhs) {
                return lhs.startTime < rhs.startTime ? -1 : 1;
            }
        });

        return result;
    }

}
