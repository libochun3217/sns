package com.charlee.sns.storage;


import androidx.annotation.Nullable;

/**
 */
public abstract class IStorage {
    protected static final String SNS_SETTING = "sns_setting";
    protected static final String SNS_LOGIN_COOKIE = "sns_login_cookie";
    protected static final String SNS_USER_DETAILS = "sns_user_info";
    protected static final String SNS_PUSH_TOKEN = "sns_push_token";
    protected static final String SNS_LOGIN_TYPE = "sns_login_type";
    protected static final String SNS_USER_NOTIFICATION_UPDATE_ID_INT = "sns_user_notification_update_id_string";
    protected static final String FOLLOW_LIST_UPDATE_ID_INT = "follow_list_update_id_string";
    protected static final String FOLLOW_NEW_STATUS = "follow_new_status";
    protected static final String SNS_CONFIG_NOTIFICATION_REFRESH_PERIOD_INT =
            "sns_config_notification_refresh_period_int";

    // 没有被查看过的社区推送数量
    protected static final String SNS_NOTIFICATION_NUM = "sns_notification_num";
    protected static final String SNS_NEW_NOTIFICATION_STATUS = "sns_new_notification_status";

    // 推荐卡片页上一次的展示时间
    public static final String SNS_RECOMMEND_SHOW_TIME = "sns_recommend_show_time";

    public abstract void setLoginCookie(String cookie);

    @Nullable
    public abstract String getLoginCookie();

    public abstract void removeLoginCookie();

    public abstract void setUserInfo(String userDetails);

    public abstract String getUserInfo();

    public abstract void removeUserInfo();

    public abstract void setPushToken(String pushToken);

    public abstract String getPushToken();

    public abstract void setLoginType(int type);

    public abstract int getLoginType();

    public abstract void setSnsNotificationNum(int num);

    public abstract int getSnsNotificationNum();

    public abstract void setNewNotificationStatus(boolean status);

    public abstract boolean getNewNotificationStatus();

    public abstract String getNotificationUpdateId();

    public abstract void setNotificationUpdateId(String id);

    public abstract boolean getNewFollowStatus();

    public abstract void setNewFollowStatus(boolean status);

    public abstract String getFollowListUpdateId();

    public abstract void setFollowListUpdateId(String id);

    public abstract long getRecommendShowTime();

    public abstract void setRecommendShowTime(long time);

    /**
     * 获取全局客户端配置中通知中心刷新的时间，单位为秒
     *
     * @return
     */
    public abstract int getConfigNotificationRefreshPeriod();

    /**
     * 保存全局客户端配置中通知中心刷新的时间，单位为秒
     *
     * @return
     */
    public abstract void setConfigNotificationRefreshPeriod(int period);
}
