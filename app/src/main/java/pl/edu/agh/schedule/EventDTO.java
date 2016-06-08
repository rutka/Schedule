package pl.edu.agh.schedule;
import net.fortuna.ical4j.model.DateList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.parameter.Value;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class EventDTO {

    private String startTime; // 20150925T130537
    private String endTime;
    private String summary;
    private String location;
    private String rule;
    private Map<String, String> propertiesMap;
    private List<Date> dateList = Collections.emptyList();
    private String description;

    public EventDTO(Map<String, String> map) {
        this.propertiesMap = map;
        setProperties();
    }

    private void setProperties() {
        startTime = propertiesMap.get("DTSTART");
        endTime = propertiesMap.get("DTEND");
        summary = propertiesMap.get("SUMMARY");
        location = propertiesMap.get("LOCATION");
        rule = propertiesMap.get("RRULE");
        description = propertiesMap.get("DESCRIPTION");
        try {
            Recur recur = new Recur(rule);
            net.fortuna.ical4j.model.Date untilDate = recur.getUntil();
            if (untilDate == null) {
                org.joda.time.DateTime dateTime = new org.joda.time.DateTime();
                dateTime = dateTime.plusMonths(1);
                untilDate = new DateTime(dateTime.toDate());
            }
            DateList dateList = recur.getDates(new DateTime(startTime), new DateTime(startTime), untilDate, Value.DATE_TIME);
            this.dateList = convert(dateList);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public Map<String, String> getPropertiesMap() {
        return propertiesMap;
    }

    public void setPropertiesMap(Map<String, String> propertiesMap) {
        this.propertiesMap = propertiesMap;
        setProperties();

    }

    @Override
    public String toString() {
        return "EventDTO{" +
                "startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                ", summary='" + summary + '\'' +
                ", location='" + location + '\'' +
                ", description='" + description + '\'' +
                ", rule='" + rule;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getRule() {
        return rule;
    }

    public void setRule(String rule) {
        this.rule = rule;
    }

    public List<Date> getDateList() {
        return dateList;
    }

    public void setDateList(List<Date> dateList) {
        this.dateList = dateList;
    }

    private List<Date> convert(DateList dateList) {
        List<Date> list = new LinkedList<>();
        Iterator iterator = dateList.iterator();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");

        while (iterator.hasNext()){
            DateTime element = (DateTime) iterator.next();
            try {
                list.add(format.parse(element.toString().substring(0, 8)));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
