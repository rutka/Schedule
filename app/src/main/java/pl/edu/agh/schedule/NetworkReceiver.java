package pl.edu.agh.schedule;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

public class NetworkReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager conn = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = conn.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            Toast.makeText(context, "Network is available", Toast.LENGTH_SHORT).show();

            new DownloadTask(context, "beacon").execute();
            new DownloadTask(context, "schedule").execute();

        } else {
            Toast.makeText(context, "Lost connection", Toast.LENGTH_SHORT).show();
        }
    }
}