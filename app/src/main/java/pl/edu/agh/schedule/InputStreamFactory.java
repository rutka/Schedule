package pl.edu.agh.schedule;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class InputStreamFactory {

    private static final String SCHEDULE = "schedule";
    private static final String BEACON = "beacon";

    public InputStream getSchedule(Context context) throws FileNotFoundException {
        String scheduleLatestFileName = getPreferences(context, SCHEDULE);
        if(scheduleLatestFileName == null) {
            return createInputStream(context, R.raw.schedule_v0);
        } else {
            return new FileInputStream(getPath() + scheduleLatestFileName);
        }
    }

    public InputStream getBeacon(Context context) throws FileNotFoundException {
        String beaconLatestFileName = getPreferences(context, BEACON);
        if(beaconLatestFileName == null) {
            return createInputStream(context, R.raw.beacons_v0);
        } else {
            return new FileInputStream(getPath() + beaconLatestFileName);
        }
    }

    private String getPreferences(Context context, String key) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(key, null);
    }

    private InputStream createInputStream(Context context, int id) {
        return context.getResources().openRawResource(id);
    }

    private String getPath() {
        return Environment.getExternalStorageDirectory() + "/MySchedule/";
    }
}
