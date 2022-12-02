package com.charlee.sns.storage;

import android.content.SharedPreferences;

import com.charlee.sns.manager.SnsEnvController;


/**
 */
public class Storage extends IStorage {
    private static IStorage mStorage;

    public static synchronized IStorage getInstance() {
        if (mStorage == null) {
            mStorage = new Storage();
        }
        return mStorage;
    }

    public void setLoginCookie(String cookie) {
        if (cookie != null && !cookie.isEmpty()) {
            SharedPreferences settings = getSettingsSharedPref();
            settings.edit().putString(SNS_LOGIN_COOKIE, cookie).commit();
        }
    }

    public String getLoginCookie() {
        SharedPreferences settings = getSettingsSharedPref();
        return settings.getString(SNS_LOGIN_COOKIE, null);
    }

    public void removeLoginCookie() {
        SharedPreferences settings = getSettingsSharedPref();
        settings.edit().remove(SNS_LOGIN_COOKIE).commit();
    }

    @Override
    public void setUserInfo(String userDetails) {
        SharedPreferences settings = getSettingsSharedPref();
        settings.edit().putString(SNS_USER_DETAILS, userDetails).commit();
    }

    @Override
    public String getUserInfo() {
        SharedPreferences settings = getSettingsSharedPref();
        return settings.getString(SNS_USER_DETAILS, null);
    }

    @Override
    public void removeUserInfo() {
        SharedPreferences settings = getSettingsSharedPref();
        settings.edit().remove(SNS_USER_DETAILS).commit();
    }

    @Override
    public void setPushToken(String pushToken) {
        SharedPreferences settings = getSettingsSharedPref();
        settings.edit().putString(SNS_PUSH_TOKEN, pushToken).commit();
    }

    @Override
    public String getPushToken() {
        SharedPreferences settings = getSettingsSharedPref();
        return settings.getString(SNS_PUSH_TOKEN, null);
    }

    @Override
    public void setLoginType(int type) {
        SharedPreferences settings = getSettingsSharedPref();
        settings.edit().putInt(SNS_LOGIN_TYPE, type).commit();
    }

    @Override
    public int getLoginType() {
        SharedPreferences settings = getSettingsSharedPref();
        return settings.getInt(SNS_LOGIN_TYPE, 0);
    }

    public void setSnsNotificationNum(int num) {
        SharedPreferences settings = getSettingsSharedPref();
        settings.edit().putInt(SNS_NOTIFICATION_NUM, num).commit();
    }

    public int getSnsNotificationNum() {
        SharedPreferences settings = getSettingsSharedPref();
        return settings.getInt(SNS_NOTIFICATION_NUM, 0);
    }

    @Override
    public void setNewNotificationStatus(boolean status) {
        SharedPreferences settings = getSettingsSharedPref();
        settings.edit().putBoolean(SNS_NEW_NOTIFICATION_STATUS, status).commit();
    }

    @Override
    public boolean getNewNotificationStatus() {
        SharedPreferences settings = getSettingsSharedPref();
        return settings.getBoolean(SNS_NEW_NOTIFICATION_STATUS, false);
    }

    @Override
    public String getNotificationUpdateId() {
        SharedPreferences settings = getSettingsSharedPref();
        return settings.getString(SNS_USER_NOTIFICATION_UPDATE_ID_INT, "");
    }

    @Override
    public void setNotificationUpdateId(String id) {
        SharedPreferences settings = getSettingsSharedPref();
        settings.edit().putString(SNS_USER_NOTIFICATION_UPDATE_ID_INT, id).commit();
    }

    @Override
    public String getFollowListUpdateId() {
        SharedPreferences settings = getSettingsSharedPref();
        return settings.getString(FOLLOW_LIST_UPDATE_ID_INT, "");
    }

    @Override
    public void setFollowListUpdateId(String id) {
        SharedPreferences settings = getSettingsSharedPref();
        settings.edit().putString(FOLLOW_LIST_UPDATE_ID_INT, id).commit();
    }

    @Override
    public boolean getNewFollowStatus() {
        SharedPreferences settings = getSettingsSharedPref();
        return settings.getBoolean(FOLLOW_NEW_STATUS, false);
    }

    @Override
    public void setNewFollowStatus(boolean status) {
        SharedPreferences settings = getSettingsSharedPref();
        settings.edit().putBoolean(FOLLOW_NEW_STATUS, status).commit();
    }

    @Override
    public long getRecommendShowTime() {
        SharedPreferences settings = getSettingsSharedPref();
        return settings.getLong(SNS_RECOMMEND_SHOW_TIME, 0);
    }

    @Override
    public void setRecommendShowTime(long time) {
        SharedPreferences settings = getSettingsSharedPref();
        settings.edit().putLong(SNS_RECOMMEND_SHOW_TIME, time).commit();
    }

    /**
     * 获取全局客户端配置中通知中心刷新的时间，单位为秒
     *
     * @return
     */
    @Override
    public int getConfigNotificationRefreshPeriod() {
        SharedPreferences settings = getSettingsSharedPref();
        return settings.getInt(SNS_CONFIG_NOTIFICATION_REFRESH_PERIOD_INT, 120); // 默认120秒
    }

    /**
     * 保存全局客户端配置中通知中心刷新的时间，单位为秒
     *
     * @return
     */
    @Override
    public void setConfigNotificationRefreshPeriod(int period) {
        SharedPreferences settings = getSettingsSharedPref();
        settings.edit().putInt(SNS_CONFIG_NOTIFICATION_REFRESH_PERIOD_INT, period).commit();
    }

    // region private methods
    private SharedPreferences getSettingsSharedPref() {
        return SnsEnvController.getInstance().getAppContext().getSharedPreferences(SNS_SETTING, 0);
    }
    // endregion
}
