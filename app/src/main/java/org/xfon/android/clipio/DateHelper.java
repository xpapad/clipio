package org.xfon.android.clipio;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

/**
 * Created by xenofon on 6/16/15.
 */
public final class DateHelper {
    private static final long MILLIS_IN_DAY = 86400000L;
    private DateHelper() {}

    /* This uses 24 hours rather than a strict "today", ie it doesn't reset at midnight and doesn't respect Daylight Saving */
    private static long getDaysSince(Date date) {
        Date now = GregorianCalendar.getInstance().getTime();
        long diff = now.getTime() - date.getTime();
        return TimeUnit.DAYS.convert(diff,TimeUnit.MILLISECONDS);
    }

    private static long getSecondsSince(Date date) {
        Date now = GregorianCalendar.getInstance().getTime();
        long diff = now.getTime() - date.getTime();
        return TimeUnit.SECONDS.convert(diff, TimeUnit.MILLISECONDS);
    }

    public static String getTimeSinceText(Date date) {
        long secs = getSecondsSince(date);
        if (secs <= 0) return "now";
        if (secs < 60) return secs + "s";
        long mins = secs / 60;
        if (mins < 60) return mins + "m";
        long hours = mins / 60;
        if (hours < 24) return hours + "h";
        long days = hours / 24;
        return days + "d";
    }

    /* This uses 24 hours rather than a strict "today", ie it doesn't reset at midnight and doesn't respect Daylight Saving */
    public static long epochBeforeToday() {
        Date now = GregorianCalendar.getInstance().getTime();
        return (now.getTime() - MILLIS_IN_DAY) / 1000;
    }
}
