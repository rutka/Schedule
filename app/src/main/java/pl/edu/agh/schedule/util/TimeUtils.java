package pl.edu.agh.schedule.util;

import org.joda.time.DateTime;

import java.util.Date;

public class TimeUtils {

    public static Date toShortDate(Date date) {
        DateTime dateTime = new DateTime(date);
        return dateTime.toLocalDate().toDate();
    }

}
