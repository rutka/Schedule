package pl.edu.agh.schedule;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * Created by anna on 16.05.16.
 */
public class PageParser {
    private static String SCHEDULE_PREFIX = "schedule-v";

    public String getLatestFileName(String url) {
        Map<Integer, String> versionFileNameMap = getFileNamesMap(url);
        Integer max = Collections.max(versionFileNameMap.keySet());
        return versionFileNameMap.get(max);
    }


    private Map<Integer, String> getFileNamesMap(String url){
        try {
            return createVersionFileNameMap(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Collections.emptyMap();
    }

    private Map<Integer, String> createVersionFileNameMap(String url) throws IOException {
        Map<Integer, String> versionMap = new HashMap<>();
        Document doc = Jsoup.connect(url).get();
        for (Element file : doc.select("td a")) {
            String fileName = file.attr("href");
            if(fileName.startsWith(SCHEDULE_PREFIX)) {
                versionMap.put(getVersion(fileName), fileName);
            }
        }
        return versionMap;
    }

    private Integer getVersion(String fileName) {
        String version = fileName.substring(fileName.indexOf("-v") + 2, fileName.indexOf(".ics"));
        return Integer.valueOf(version);
    }
}
