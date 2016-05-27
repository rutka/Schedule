package pl.edu.agh.schedule.util;

public class BeaconUtils {
    private static String id = "B9407F30-F5F8-466E-AFF9-25556B57FE6D:64343:34791"; //FIXME delete this

    public static String nearestBeacon() {
        return id;
    }

    public static void nearestBeacon(String id) {
        BeaconUtils.id = id;
    }
}
