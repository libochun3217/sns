package com.charlee.sns.helper;

import java.sql.Timestamp;
import java.util.Date;


import android.content.Context;

import androidx.annotation.NonNull;

import com.charlee.sns.R;
import com.charlee.sns.model.SnsModel;

/**
 * 用于时间的格式化
 */
public class TimeFormatter {
    private static final long MILLI_SECOND = 1000; // 服务器传回的时间以秒为单位，本地时间需换算为秒

    private static final long ONE_MINUTE = 60;
    private static final long ONE_HOUR = 60 * ONE_MINUTE;
    private static final long ONE_DAY = 24 * ONE_HOUR;
    private static final long ONE_MONTH = 30 * ONE_DAY;
    private static final long ONE_YEAR = 12 * ONE_MONTH;

    public static String getPublishTime(@NonNull Context context, long timeStampInSeconds) {
        long offset = SnsModel.getInstance().getTimeOffsetInSeconds();
        long elapsedSeconds = System.currentTimeMillis() / MILLI_SECOND - timeStampInSeconds + offset;
        if (elapsedSeconds > 0) {
            if (elapsedSeconds < ONE_MINUTE) {
                int minute = 1;
                return context.getResources().getString(R.string.time_one_minute, minute);
            } else if (elapsedSeconds < ONE_HOUR) {
                int minutes = (int) (elapsedSeconds / ONE_MINUTE);
                return context.getResources().getQuantityString(R.plurals.time_format_minutes, minutes, minutes);
            } else if (elapsedSeconds < ONE_DAY) {
                int hours = (int) (elapsedSeconds / ONE_HOUR);
                return context.getResources().getQuantityString(R.plurals.time_format_hours, hours, hours);
            } else if (elapsedSeconds < ONE_MONTH) {
                int days = (int) (elapsedSeconds / ONE_DAY);
                return context.getResources().getQuantityString(R.plurals.time_format_days, days, days);
            } else if (elapsedSeconds < ONE_YEAR) {
                int months = (int) (elapsedSeconds / ONE_MONTH);
                return context.getResources().getQuantityString(R.plurals.time_format_months, months, months);
            } else {
                int years = (int) (elapsedSeconds / ONE_YEAR);
                return context.getResources().getQuantityString(R.plurals.time_format_years, years, years);
            }
        }

        return context.getResources().getString(R.string.time_jsut_now);
    }
}
