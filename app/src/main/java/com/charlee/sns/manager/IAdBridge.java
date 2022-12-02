package com.charlee.sns.manager;


import android.content.Context;

/**
 */
public interface IAdBridge {
    void openGooglePlayMarket(Context context, String url);

    void openByBrowser(Context context, String url);

    void openByWebView(Context context, String title, String desc, String url);

    void openByDownloadDialog(Context context, String title, String url);
}