package es.ewic.clients.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class DateUtils {

    public static final SimpleDateFormat sdfLong = new SimpleDateFormat("HH:mm dd/MM/yyyy");
    public static final SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy");
    public static final SimpleDateFormat sdfHour = new SimpleDateFormat("HH:mm");

    public static Calendar parseDateLong(String dateString) {
        return parseDate(dateString, sdfLong);
    }

    public static Calendar parseDateHour(String dateString) {
        return parseDate(dateString, sdfHour);
    }

    public static Calendar parseDateDate(String dateString) {
        return parseDate(dateString, sdfDate);
    }

    public static Calendar parseDate(String dateString, SimpleDateFormat sdf) {
        Calendar cal = new GregorianCalendar();
        try {
            synchronized (sdf) {
                cal.setTime(sdf.parse(dateString));
            }
        } catch (ParseException e) {
            return null;
        }
        return cal;
    }


    public static String formatDateLong(Calendar date) {
        synchronized (sdfLong) {
            return sdfLong.format(date.getTime());
        }
    }

    public static String formatDate(Calendar date) {
        synchronized (sdfDate) {
            return sdfDate.format(date.getTime());
        }
    }

    public static String formatHour(Calendar date) {
        synchronized (sdfHour) {
            return sdfHour.format(date.getTime());
        }
    }

    public static Calendar changeCalendarTimezoneFromUTCToDefault(Calendar utcCalendar) {
        long utcTime = utcCalendar.getTime().getTime();

        long timezoneDefaultTime = utcTime + TimeZone.getDefault().getRawOffset();
        Calendar defaultCalendar = Calendar.getInstance(TimeZone.getDefault());
        defaultCalendar.setTimeInMillis(timezoneDefaultTime);

        return defaultCalendar;
    }

    public static Calendar changeCalendarTimezoneFromDefaultToUTC(Calendar defaultCalendar) {
        long defaultTime = defaultCalendar.getTime().getTime();

        long timezoneUTCTime = defaultTime - TimeZone.getDefault().getRawOffset();
        Calendar utcCalendar = Calendar.getInstance(TimeZone.getDefault());
        utcCalendar.setTimeInMillis(timezoneUTCTime);

        return utcCalendar;
    }
}
