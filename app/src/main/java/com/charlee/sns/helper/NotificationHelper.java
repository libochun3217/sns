package com.charlee.sns.helper;

import android.app.NotificationManager;
import android.content.Context;

/**
 */
public class NotificationHelper {
    public static final int SNS_NOTIFICATION_ID = 0;

    public static void cancelNotification(Context context, int id) {
        NotificationManager manager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.cancel(id);
        }
    }
}
