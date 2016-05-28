package pl.edu.agh.schedule.sync;

import android.content.Context;
import android.content.SyncStatusObserver;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.util.Log;

import pl.edu.agh.schedule.AsyncTaskResult;
import pl.edu.agh.schedule.DownloadTask;
import pl.edu.agh.schedule.util.ConstUtils;

import static pl.edu.agh.schedule.util.LogUtils.makeLogTag;

/**
 * A helper class for dealing with data synchronization.
 */
public class SyncHelper {

    private static final long INTERVAL_TO_AUTO_REFRESH = 60000L;

    private static final String TAG = makeLogTag(SyncHelper.class);

    private Context mContext;

    /**
     * @param context            Can be Application, Activity or Service context.
     * @param syncStatusObserver spinning widget to notify
     */
    public SyncHelper(Context context, SyncStatusObserver syncStatusObserver) {
        this.mContext = context;
        AsyncTaskResult.setObserver(syncStatusObserver);
        new UpdateScheduler(this).run();
    }

    /**
     * Attempts to perform data synchronization.
     */
    public void performSync() {
        try {
            if (!isOnline()) {
                Log.d(TAG, "Not attempting remote sync because device is OFFLINE");
            }
            Log.d(TAG, "Starting remote sync.");
            // FIXME
            new DownloadTask(mContext, ConstUtils.BEACON).execute();
            new DownloadTask(mContext, ConstUtils.SCHEDULE).execute();
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

    final class UpdateScheduler implements Runnable {
        private final Handler handler = new Handler();
        private SyncHelper syncHelper;

        public UpdateScheduler(SyncHelper syncHelper) {
            this.syncHelper = syncHelper;
        }

        public void scheduleNextRun() {
            handler.postDelayed(this, INTERVAL_TO_AUTO_REFRESH);
        }

        @Override
        public void run() {
            Log.d(TAG, "Running auto update.");
            try {
                syncHelper.performSync();
            } finally {
                this.scheduleNextRun();
            }
        }
    }
}
