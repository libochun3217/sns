package com.charlee.sns.manager;

import android.content.Context;

import com.charlee.sns.helper.SnsImageLoader;
import com.facebook.animated.gif.BuildConfig;


/**
 * Sns整体环境初始化
 */
public class SnsEnvController {
    private static SnsEnvController instance;
    private static Context appContext;

    private ISnsNetworkParams networkParams;
    private IAdBridge adBridge;
    private INavigationBridge navigationBridge;

    public static final boolean isGooglePlayChannel = BuildConfig.FLAVOR.equals("googleplay");

    private SnsEnvController() {

    }

    public static synchronized SnsEnvController getInstance() {
        if (instance == null) {
            instance = new SnsEnvController();
        }

        return instance;
    }

    public void setAppContext(Context appContext) {
        this.appContext = appContext;
    }

    public Context getAppContext() {
        return appContext;
    }

    public void setNetworkParams(ISnsNetworkParams networkParams) {
        this.networkParams = networkParams;
    }

    public ISnsNetworkParams getNetworkParams() {
        return networkParams;
    }

    public void setAdBridge(IAdBridge adBridge) {
        this.adBridge = adBridge;
    }

    public IAdBridge getAdBridge() {
        return adBridge;
    }

    public void setNavigationBridge(INavigationBridge navigationBridge) {
        this.navigationBridge = navigationBridge;
    }

    public INavigationBridge getNavigationBridge() {
        return this.navigationBridge;
    }

    public void init() {
        SnsImageLoader.init(appContext);
    }
}
