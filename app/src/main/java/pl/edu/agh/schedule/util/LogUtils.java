package pl.edu.agh.schedule.util;

public class LogUtils {
    private static final int MAX_LOG_TAG_LENGTH = 23;

    private static String makeLogTag(String str) {
        if (str.length() > MAX_LOG_TAG_LENGTH) {
            return str.substring(0, MAX_LOG_TAG_LENGTH);
        }

        return str;
    }

    public static String makeLogTag(Class cls) {
        return makeLogTag(cls.getSimpleName());
    }

    private LogUtils() {
    }
}
