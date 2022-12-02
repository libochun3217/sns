package com.charlee.sns.data;

/**
 */
public class Notification {
    private final int refreshPeriod; // 通知前台刷新时间，单位s

    public Notification(int refreshPeriod) {
        this.refreshPeriod = refreshPeriod;
    }

    public int getRefreshPeriod() {
        return this.refreshPeriod;
    }

    public boolean isValid() {
        return this.refreshPeriod >= 0;
    }
}
