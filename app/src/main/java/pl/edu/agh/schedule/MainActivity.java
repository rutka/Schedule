package pl.edu.agh.schedule;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.SystemRequirementsChecker;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private final static int MAJOR_MINT = 1148;
    private final static int MINOR_MINT = 14561;
    private final static String MINT = "Mint";

    private final static int MAJOR_BLUEBERRY = 64343;
    private final static int MINOR_BLUEBERRY = 34791;
    private static final String BLUEBERRY = "Blueberry";

    private final static String UUID_STRING = "B9407F30-F5F8-466E-AFF9-25556B57FE6D";
    private static final Map<String, String> BEACONS_MAP;

    private Button downloadButton;

    static {
        Map<String, String> map = new HashMap<>();
        map.put(String.format("%d:%d", MAJOR_BLUEBERRY, MINOR_BLUEBERRY), BLUEBERRY);
        map.put(String.format("%d:%d", MAJOR_MINT, MINOR_MINT), MINT);
        BEACONS_MAP = Collections.unmodifiableMap(map);
    }

    private BeaconManager beaconManager;
    private Region region;
    private NetworkReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        receiver = new NetworkReceiver();
        this.registerReceiver(receiver, filter);

        downloadButton = (Button) findViewById(R.id.downloadButton);
        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                new DownloadTask(MainActivity.this).execute();
            }
        });
        final CalendarParser calendarParser = new CalendarParser(MainActivity.this);

        beaconManager = new BeaconManager(this);
        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, List<Beacon> list) {
                if (!list.isEmpty()) {
                    Beacon nearestBeacon = list.get(0);
                    String colour = getColour(nearestBeacon);
//                    showNotification(colour, "beacon");

                    List<EventDTO> eventsById = calendarParser.getEventsByBeacon(UUID_STRING + String.format(":%d:%d", nearestBeacon.getMajor(), nearestBeacon.getMinor()));
                    Log.d("DEBUG", "size: " + String.valueOf(eventsById.size()));

                }

                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                Date date = new Date();
                try {
                     date = sdf.parse("16-10-2015");
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                List<EventDTO> eventsById = calendarParser.getEventsByBeacon("B9407F30-F5F8-466E-AFF9-25556B57FE6D:1148:14561");
                List<EventDTO> eventsByIdAndDate = calendarParser.getEventsByBeaconAndDate("B9407F30-F5F8-466E-AFF9-25556B57FE6D:1148:14561", date);
                Log.d("DEBUG", "size: " + String.valueOf(eventsById.size()));
                Log.d("DEBUG", "size beacon date: " + String.valueOf(eventsByIdAndDate.size()));
            }
        });
        region = new Region("ranged region", UUID.fromString(UUID_STRING), null, null);

    }

    @Override
    protected void onResume() {
        super.onResume();
        SystemRequirementsChecker.checkWithDefaultDialogs(this);
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startRanging(region);
            }
        });
    }

    @Override
    protected void onPause() {
        beaconManager.stopRanging(region);

        super.onPause();
    }

    private String getColour(Beacon beacon) {
        String beaconKey = String.format("%d:%d", beacon.getMajor(), beacon.getMinor());
        if (BEACONS_MAP.containsKey(beaconKey)) {
            return BEACONS_MAP.get(beaconKey);
        }
        return beaconKey;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (receiver != null) {
            this.unregisterReceiver(receiver);
        }
    }

    public void showNotification(String title, String message) {
        Intent notifyIntent = new Intent(this, MainActivity.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivities(this, 0,
                new Intent[] { notifyIntent }, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setContentIntent(pendingIntent)
                .build();
        notification.defaults |= Notification.DEFAULT_SOUND;
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);
    }
}