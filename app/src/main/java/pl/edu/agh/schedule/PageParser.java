package pl.edu.agh.schedule;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class PageParser {
    private static String URL  = "http://www.student.agh.edu.pl/~rutka/";

    private static String SCHEDULE_PREFIX = "schedule_v";
    private static String SCHEDULE_EXTENSION = ".ics";
    private static String BEACON_PREFIX = "beacons_v";
    private static String BEACON_EXTENSION = ".csv";


    public String getLatestFileName(String type) {
        Map<Integer, String> versionFileNameMap;
        if(BEACON_PREFIX.contains(type)) {
            versionFileNameMap = getFileNamesMap(URL, BEACON_PREFIX, BEACON_EXTENSION);
        } else {
            versionFileNameMap = getFileNamesMap(URL, SCHEDULE_PREFIX, SCHEDULE_EXTENSION);
        }
        Integer max = Collections.max(versionFileNameMap.keySet());
        return versionFileNameMap.get(max);
    }

    private Map<Integer, String> getFileNamesMap(String url, String prefix,String extension){
        try {
            return createVersionFileNameMap(url, prefix, extension);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Collections.emptyMap();
    }

    private Map<Integer, String> createVersionFileNameMap(String url, String prefix, String extension) throws IOException {
        Map<Integer, String> versionMap = new HashMap<>();
        Document doc = Jsoup.connect(url).get();
        for (Element file : doc.select("td a")) {
            String fileName = file.attr("href");
            if(fileName.startsWith(prefix)) {
                versionMap.put(getVersion(fileName, extension), fileName);
            }
        }
        return versionMap;
    }

    private Integer getVersion(String fileName, String extension) {
        String version = fileName.substring(fileName.indexOf("_v") + 2, fileName.indexOf(extension));
        return Integer.valueOf(version);
    }
}
