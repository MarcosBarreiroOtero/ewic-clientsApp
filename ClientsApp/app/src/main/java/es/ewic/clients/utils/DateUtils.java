package es.ewic.clients.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class DateUtils {

    public static SimpleDateFormat sdfLong = new SimpleDateFormat("HH:mm dd/MM/yyyy");
    public static SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy");
    public static SimpleDateFormat sdfHour = new SimpleDateFormat("HH:mm");

    public static Calendar parseDateLong(String dateString) {
        Calendar cal = new GregorianCalendar();
        try {
            synchronized (sdfLong) {
                cal.setTime(sdfLong.parse(dateString));
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

}
