package pl.edu.agh.schedule.util;

import android.content.Context;
import android.text.TextUtils;
import android.text.format.DateUtils;

import org.joda.time.DateTime;
import org.joda.time.DateTimeComparator;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;
import java.util.Locale;
import java.util.TimeZone;

public class TimeUtils {

    private static final SimpleDateFormat[] ACCEPTED_TIMESTAMP_FORMATS = {
            new SimpleDateFormat("yyyyMMdd'T'HHmmss", Locale.US),
            new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US),
            new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy", Locale.US),
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US),
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z", Locale.US),
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US),
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US),
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US),
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss Z", Locale.US)
    };

    public static Date parseTimestamp(String timestamp) {
        for (SimpleDateFormat format : ACCEPTED_TIMESTAMP_FORMATS) {
            format.setTimeZone(TimeZone.getTimeZone("GMT"));
            try {
                return format.parse(timestamp);
            } catch (ParseException ex) {
                // NO OP
            }
        }

        // All attempts to parse have failed
        return null;
    }

    public static long timestampToMillis(String timestamp) throws TimestampException {
        TimestampException timestampException = new TimestampException("Wrong timestamp");
        if (TextUtils.isEmpty(timestamp)) {
            throw timestampException;
        }
        Date d = parseTimestamp(timestamp);
        if (d == null) {
            throw timestampException;
        }
        return d.getTime();
    }

    public static class TimestampException extends Throwable {
        public TimestampException(String s) {
            super(s);
        }
    }

    public static Date toShortDate(Date date) {
        DateTime dateTime = new DateTime(date);
        return dateTime.toLocalDate().toDate();
    }

    /**
     * Format a {@code date} honoring the app preference for using app or device timezone.
     * {@code Context} is used to lookup the shared preference settings.
     */
    public static String formatShortDate(Context context, Date date) {
        StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter(sb);
        return DateUtils.formatDateRange(context, formatter, date.getTime(), date.getTime(),
                DateUtils.FORMAT_ABBREV_ALL | DateUtils.FORMAT_NO_YEAR).toString();
    }

    public static String formatShortTime(Context context, Date time) {
        // Android DateFormatter will honor the user's current settings.
        DateFormat format = android.text.format.DateFormat.getTimeFormat(context);
        return format.format(time);
    }

    public static int compareHours(long day1, long day2) {
        return DateTimeComparator.getTimeOnlyInstance().compare(day1, day2);
    }

}
