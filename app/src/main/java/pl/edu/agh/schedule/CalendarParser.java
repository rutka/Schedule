package pl.edu.agh.schedule;

import android.content.Context;
import android.util.Log;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Property;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import pl.edu.agh.schedule.util.LogUtils;
import pl.edu.agh.schedule.util.TimeUtils;

public class CalendarParser {

    private static final String TAG = LogUtils.makeLogTag(CalendarParser.class);

    private static final String EVENT_NAME = "VEVENT";
    private static final String PROPERTY_LOCATION_NAME = "LOCATION";
    private Calendar calendar;
    private Map<String,List<EventDTO>> locationEventsMap;
    private Map<String, String> beaconLocationMap;

    public CalendarParser(Context context) {
        init(context);
    }

    public void init(Context context) {
        calendar = createCalendar(context);
        BeaconParser beaconParser = new BeaconParser();
        beaconLocationMap = beaconParser.createBeanLocationMap(context);
        locationEventsMap = getAllEvents();
    }

    private Calendar createCalendar(Context context) {
        return buildCalender(context);
    }

    private Calendar buildCalender(Context context) {
        CalendarBuilder builder = new CalendarBuilder();
        try {
            return builder.build(createInputStream(context));
        } catch (IOException | ParserException e) {
            e.printStackTrace();
        }
        return null;
    }

    private InputStream createInputStream(Context context) {
        InputStreamFactory inputStreamFactory = new InputStreamFactory();
        try {
            return inputStreamFactory.getSchedule(context);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<EventDTO> getEventsByBeacon(String id) {
        return getEventsByLocation(beaconLocationMap.get(id));
    }

    public List<EventDTO> getEventsByBeaconAndDate(String id, Date date) {
        date = TimeUtils.toShortDate(date);
        Log.d(TAG, "Looking for events for beacon " + id + " and date " + date);
        List<EventDTO> eventDTOList = new LinkedList<>();
        List<EventDTO> eventsByBeacon = getEventsByBeacon(id);
        if (eventsByBeacon != null) {
            for (EventDTO eventDTO : eventsByBeacon) {
                if (eventDTO.getDateList().contains(date)) {
                    eventDTOList.add(eventDTO);
                }
            }
        }
        return eventDTOList;
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
            if (isEvent(component)) {
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

    private boolean isEvent(Component component) {
        return component.getName().equals(EVENT_NAME);
    }
}
