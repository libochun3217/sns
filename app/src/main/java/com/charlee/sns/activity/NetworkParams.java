package com.charlee.sns.activity;


import android.content.Context;

import com.charlee.sns.manager.ISnsNetworkParams;

public class NetworkParams implements ISnsNetworkParams {


    @Override
    public String getLanguage(Context context) {
        return "china";
    }

    @Override
    public String getVersionCode(Context context) {
        return "110";
    }

    @Override
    public String getChannel(Context context) {
        return "china";
    }

    @Override
    public String getChannelId(Context context) {
        return "defaultId";
    }



    @Override
    public String getMac(Context context) {
        return "macxxxxx";
    }

    @Override
    public String getVersion(Context context) {
        return "version 1.0";
    }

    @Override
    public String getCuid(Context context) {
        return "cuid";
    }

    @Override
    public String getNetworkType(Context context) {
        return "mobile";
    }

    @Override
    public String getPushToken() {
        return "push token";
    }

    @Override
    public String getIMEI() {
        return "imei";
    }

    @Override
    public boolean getQAMode() {
        return true;
    }

    @Override
    public String getAndroidId() {
        return "android id";
    }

    @Override
    public String getGoogleId() {
        return "google id";
    }
}
