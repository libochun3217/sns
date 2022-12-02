package com.charlee.sns.manager;

import android.content.Context;

/**
 */
public interface ISnsNetworkParams {
    String getVersionCode(Context var1);

    String getChannel(Context var1);

    String getChannelId(Context var1);

    String getVersion(Context var1);

    String getPushToken();

    String getIMEI();

    String getCuid(Context context);

    boolean getQAMode();

    String getAndroidId();

    String getGoogleId();

    String getLanguage(Context context);

    String getNetworkType(Context context);

    String getMac(Context var1);
}