package pl.edu.agh.schedule;

import java.util.Map;

public class Event {
    private Map<String, String> propertiesMap;

    public Event(Map<String, String> map) {
        this.propertiesMap = map;
    }

    public Map<String, String> getPropertiesMap() {
        return propertiesMap;
    }

    public void setPropertiesMap(Map<String, String> propertiesMap) {
        this.propertiesMap = propertiesMap;
    }

}
