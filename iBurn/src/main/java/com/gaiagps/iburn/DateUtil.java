package com.gaiagps.iburn;

import android.content.Context;
import android.text.format.DateUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by davidbrodsky on 8/6/14.
 */
public class DateUtil {

    private static SimpleDateFormat timeFormatter = new SimpleDateFormat("h:mm a", Locale.US);

    /**
     * Get a human description of an event's state
     * (e.g: Starts in XX, Ends in XX)
     *
     * @param context            The application {@link android.content.Context}
     * @param nowDate            The date to treat as 'now'
     * @param startDate          The start Date
     * @param prettyStartDateStr A 'prettified' start date string
     * @param endDate            The end Date
     * @param prettyEndDateStr   A 'prettified' end date string
     */
    public static String getDateString(Context context, Date nowDate, Date startDate, String prettyStartDateStr, Date endDate, String prettyEndDateStr) {
            // The date before which to use relative date descriptors. e.g: (in 2 minutes)
            Calendar relativeTimeCutoff = Calendar.getInstance();
            relativeTimeCutoff.setTime(nowDate);
            relativeTimeCutoff.add(Calendar.HOUR, 1);

            if (nowDate.before(startDate)) {
                // Has not yet started
                if (relativeTimeCutoff.after(startDate)) {
                    return context.getString(R.string.starts) + " " + DateUtils.getRelativeTimeSpanString(startDate.getTime(), nowDate.getTime(), DateUtils.MINUTE_IN_MILLIS).toString();
                }
                return context.getString(R.string.starts) + " " + prettyStartDateStr;
            } else {
                // Already started
                if (endDate.before(nowDate)) {
                    if (relativeTimeCutoff.after(endDate)) {
                        return context.getString(R.string.ended) + " " + DateUtils.getRelativeTimeSpanString(endDate.getTime(), nowDate.getTime(), DateUtils.MINUTE_IN_MILLIS).toString();
                    }
                    return context.getString(R.string.ended) + " " + prettyEndDateStr;
                } else {
                    if (relativeTimeCutoff.after(endDate)) {
                        return context.getString(R.string.ends) + " " + DateUtils.getRelativeTimeSpanString(endDate.getTime(), nowDate.getTime(), DateUtils.MINUTE_IN_MILLIS).toString();
                    }
                    return context.getString(R.string.ends) + " " + prettyEndDateStr;
                }

            }
    }

    public static String getStartDateString(Date startDate, Date nowDate) {

        if (Math.abs(startDate.getTime() - nowDate.getTime()) < DateUtils.MINUTE_IN_MILLIS) {
            return "Starting now";

        } else {
            long deltaTime = startDate.getTime() - nowDate.getTime();
            int hours = (int) (deltaTime / DateUtils.HOUR_IN_MILLIS);
            int minutes = (int) ((deltaTime - (hours * DateUtils.HOUR_IN_MILLIS)) / DateUtils.MINUTE_IN_MILLIS);

            String relativeSpan = null;
            if (hours > 0)
                relativeSpan = "at " + timeFormatter.format(startDate);
            else
                relativeSpan = String.format("in %d minute%s", minutes, minutes > 1 ? 's' : "");

            return (startDate.before(nowDate) ? "Started " : "Starts ") + relativeSpan;
        }
    }

    public static String getEndDateString(Date endDate, Date nowDate) {

        if (Math.abs(endDate.getTime() - nowDate.getTime()) < DateUtils.MINUTE_IN_MILLIS) {
            return "Ending now";

        } else {

            String relativeSpan = DateUtils.getRelativeTimeSpanString(
                    endDate.getTime(),
                    nowDate.getTime(),
                    DateUtils.MINUTE_IN_MILLIS).toString();
            return (endDate.before(nowDate) ? "Ended " : "Ends ") + relativeSpan;
        }
    }
}
