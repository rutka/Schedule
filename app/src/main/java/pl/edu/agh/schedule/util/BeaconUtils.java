package pl.edu.agh.schedule.util;

public class BeaconUtils {
    private static String id;

    public static String nearestBeacon() {
        return id;
    }

    public static void nearestBeacon(String id) {
        BeaconUtils.id = id.toLowerCase();
    }
}
