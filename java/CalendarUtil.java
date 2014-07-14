import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import mynamespace.R;

/**
 * Helper class for getting formatted timestamps
 */
public abstract class CalendarUtil {

    private final static SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
    
    public static String getTimeString(Calendar cal) {
        return getTimeString(cal.getTime());
    }

    public static String getTimeString(Date date) {
        return timeFormat.format(date);
    }

    public static String getTimeRangeString(Calendar cal, int field, int margin){
        if (margin == 0) return getTimeString(cal);

        //else
        Calendar start = (Calendar) cal.clone();
        Calendar stop = (Calendar) cal.clone();
        start.add(field,-1 * margin);
        stop.add(field,margin);

        return getInstance().getContext().getString(
            "between %1$s and %2$s", getTimeString(start), getTimeString(stop));
    }
}
