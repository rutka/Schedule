package pl.edu.agh.schedule.sync;

import android.content.Context;
import android.content.SyncStatusObserver;
import android.net.ConnectivityManager;
import android.util.Log;

import pl.edu.agh.schedule.AsyncTaskResult;
import pl.edu.agh.schedule.BuildConfig;
import pl.edu.agh.schedule.DownloadTask;

import static pl.edu.agh.schedule.util.LogUtils.makeLogTag;

/**
 * A helper class for dealing with data synchronization.
 */
public class SyncHelper {

    private static final String TAG = makeLogTag(SyncHelper.class);

    private Context mContext;
    private SyncStatusObserver syncStatusObserver;

    /**
     * @param context            Can be Application, Activity or Service context.
     * @param syncStatusObserver spinning widget to notify
     */
    public SyncHelper(Context context, SyncStatusObserver syncStatusObserver) {
        this.mContext = context;
        this.syncStatusObserver = syncStatusObserver;
        AsyncTaskResult.setObserver(syncStatusObserver);
        this.performSync();
    }

    /**
     * Attempts to perform data synchronization.
     */
    public void performSync() {
        try {
            if (!isOnline()) {
                Log.d(TAG, "Not attempting remote sync because device is OFFLINE");
                syncStatusObserver.onStatusChanged(0);
                return;
            }
            Log.d(TAG, "Starting remote sync.");
            new DownloadTask(mContext, BuildConfig.BEACONS).execute();
            new DownloadTask(mContext, BuildConfig.SCHEDULE).execute();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            Log.e(TAG, "Error performing remote sync.");
        }
        Log.i(TAG, "End of sync.");
    }

    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(
                Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null &&
                cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }
}
