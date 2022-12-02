package com.charlee.sns.data;


import androidx.annotation.NonNull;

/**
 */
public class Configuration {
    private final Notification notification;

    public Configuration(Notification notification) {
        this.notification = notification;
    }

    @NonNull
    public Notification getNotification() {
        return this.notification;
    }

    public boolean isValid() {
        return notification != null && notification.isValid();
    }
}
