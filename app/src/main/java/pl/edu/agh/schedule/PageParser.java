package pl.edu.agh.schedule;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class PageParser {

    private static final String SCHEDULE_EXTENSION = ".ics";
    private static final String BEACON_EXTENSION = ".csv";
    private static final String VERSION_SIGN = "_v";

    public String getLatestFileName(String type) {
        Map<Integer, String> versionFileNameMap;
        if(BuildConfig.BEACONS.contains(type)) {
            versionFileNameMap = getFileNamesMap(BuildConfig.URL, BuildConfig.BEACONS, BEACON_EXTENSION);
        } else {
            versionFileNameMap = getFileNamesMap(BuildConfig.URL, BuildConfig.SCHEDULE, SCHEDULE_EXTENSION);
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
        String version = fileName.substring(fileName.indexOf(VERSION_SIGN) + VERSION_SIGN.length(), fileName.indexOf(extension));
        return Integer.valueOf(version);
    }
}
