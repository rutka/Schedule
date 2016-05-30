package pl.edu.agh.schedule;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class BeaconParser {

    public Map<String, String> createBeanLocationMap(Context context) {
        InputStreamFactory inputStreamFactory = new InputStreamFactory();
        try {
            InputStream inputStream = inputStreamFactory.getBeacon(context);
            return createBeaconLocationMap(inputStream);
        } catch (FileNotFoundException e) {
            Log.e("ERROR", e.getMessage());
            e.printStackTrace();
        }
        return Collections.emptyMap();
    }

    private Map<String, String> createBeaconLocationMap(InputStream inputStream) {
        Map<String, String> deviceList = new HashMap<>();
        BufferedReader br = null;
        String line = "";
        try {
            br = new BufferedReader (new InputStreamReader(inputStream));
            while ((line = br.readLine()) != null) {
                String[] splitLine = line.split(",");
                deviceList.put(splitLine[0].toLowerCase(), splitLine[1]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return deviceList;
    }
}
