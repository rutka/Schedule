package pl.edu.agh.schedule;
import java.util.Map;

public class EventDTO {

    private String startTime; // 20150925T130537
    private String endTime;
    private String summary;
    private String location;
    private Map<String, String> propertiesMap;

    public EventDTO(Map<String, String> map) {
        this.propertiesMap = map;
        setProperties();
    }

    private void setProperties() {
        startTime = propertiesMap.get("DTSTART");
        endTime = propertiesMap.get("DTEND");
        summary = propertiesMap.get("SUMMARY");
        location = propertiesMap.get("LOCATION");
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
                "location='" + location + '\'' +
                ", summary='" + summary + '\'' +
                ", endDate=" + endTime +
                ", startDate=" + startTime +
                '}';
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
}
