package pl.edu.agh.schedule;

import android.content.SyncStatusObserver;

public class AsyncTaskResult {
    private static SyncStatusObserver observer;
    private String result;
    private Exception error;

    public static void setObserver(SyncStatusObserver observer) {
        AsyncTaskResult.observer = observer;
    }

    public String getResult() {
        return result;
    }

    public Exception getError() {
        return error;
    }

    public AsyncTaskResult(String result) {
        super();
        this.result = result;
    }

    public AsyncTaskResult(Exception error) {
        super();
        this.error = error;
    }

    public void notifyObserver() {
        if (observer != null) {
            observer.onStatusChanged(0);
        }
    }
}
