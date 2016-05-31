package pl.edu.agh.schedule;

import android.app.Application;

/**
 * Code initialized in
 * Application classes is rare since this code will be run any time a ContentProvider, Activity,
 * or Service is used by the user or system. Dependency injection, and multi-dex
 * frameworks are in this very small set of use cases.
 */
public class AppApplication extends Application {
//TODO: 31/05/16 add which room is the nereast one and checking if ble is on

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
