package com.charlee.sns.data;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 通知中心返回的结果
 * 注意：成员变量命名要和JSON对应。GSON转换规则为FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES
 * */
public class NotificationResult extends ResultBase {
    private final PagedList<NotificationItem> notifications;

    public NotificationResult(int errCode, @Nullable String errMsg, Boolean hasMore,
                              @NonNull PagedList<NotificationItem> notifications) {
        super(errCode, errMsg, hasMore);
        this.notifications = notifications;
    }

    @NonNull
    public PagedList<NotificationItem> getItems() {
        return notifications;
    }

    @Override
    public boolean isValid() {
        return notifications != null && notifications.isValid();
    }

    @Override
    public void setServerTimeStamp(long serverTimeStamp) {
        super.setServerTimeStamp(serverTimeStamp);
        if (notifications != null) {
            notifications.setUpdateTime(serverTimeStamp);
        }
    }
}
