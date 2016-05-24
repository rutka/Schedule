package pl.edu.agh.schedule;

import android.os.Environment;
import android.util.Log;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Property;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.*;

public class CalendarParser {

    private static final String EVENT_NAME = "VEVENT";
    private static final String PROPERTY_LOCATION_NAME = "LOCATION";
    private String beaconLocationFile;
    private Calendar calendar;
    private Map<String,List<EventDTO>> locationEventsMap;
    private Map<String, String> beaconLocationMap;

    public CalendarParser() {
        beaconLocationFile = Environment.getExternalStorageDirectory() + "/MySchedule/beacons-v1.csv";// + pageParser.getLatestBeaconFileName();
        String scheduleFileName = Environment.getExternalStorageDirectory() + "/MySchedule/schedule-v2.ics";// + pageParser.getLatestScheduleFileName();
        try {
            calendar = createCalendar(scheduleFileName);

            locationEventsMap = getAllEvents();
            beaconLocationMap = createBeaconLocationMap();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserException e) {
            e.printStackTrace();
        }
    }

    public Calendar createCalendar(String calendar) throws IOException, ParserException {
        CalendarBuilder builder = new CalendarBuilder();
        return builder.build(new FileInputStream(calendar));
    }

    public List<EventDTO> getEventsById(String id) {
        return getEventsByLocation(beaconLocationMap.get(id));
    }

    private List<EventDTO> getEventsByLocation(String location) {
        Log.d("DEBUG", "sala: " + location);
        if (location != null) {
            return locationEventsMap.get(location);
        } else {
            return Collections.emptyList();
        }
    }

    private Map<String, List<EventDTO>> getAllEvents() {
        Map<String, List<EventDTO>> locationListOfEventMap = new HashMap<>();
        for (Object o : calendar.getComponents()) {
            Component component = (Component) o;
            if (component.getName().equals(EVENT_NAME)) {
                Map<String, String> propertiesMap = new HashMap<>();
                String location = component.getProperty(PROPERTY_LOCATION_NAME).getValue();
                for (Object o1 : component.getProperties()) {
                    Property property = (Property) o1;
                    propertiesMap.put(property.getName(), property.getValue());
                }
                EventDTO eventDTO = new EventDTO(propertiesMap);
                if (locationListOfEventMap.containsKey(location)) {
                    locationListOfEventMap.get(location).add(eventDTO);
                } else {
                    List<EventDTO> list = new LinkedList<>();
                    list.add(eventDTO);
                    locationListOfEventMap.put(location, list);
                }
            }
        }
        return locationListOfEventMap;
    }

    private Map<String, String> createBeaconLocationMap() {
        Map<String, String> deviceList = new HashMap<>();

        BufferedReader br = null;
        String line = "";
        try {
            br = new BufferedReader(new FileReader(beaconLocationFile));
            while ((line = br.readLine()) != null) {
                String[] splitLine = line.split(",");
                deviceList.put(splitLine[0], splitLine[1]);
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