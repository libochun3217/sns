package com.charlee.sns.helper;

import java.lang.ref.SoftReference;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.charlee.sns.model.ObjectObservableBase;

/**
 */
public class NetworkMonitor extends ObjectObservableBase {

    // region Private Static Fields

    private BroadcastReceiver mNetworkStateChangeListener;

    private boolean isNetworkAvailable;
    private boolean isBroadband;

    private int netType;
    private String netTypeString;

    // endregion

    // region Private Fields

    private Context appContext;

    // endregion

    // region Weak Singleton

    private static SoftReference<NetworkMonitor> instance = null;

    /**
     * 获取魔图模型的单例。
     * 注意该单例为SoftReference，所以请注意生命周期的管理。
     * 只应该在视图层调用。模型层对象如果需要应该从构造函数注入。
     */
    public static synchronized NetworkMonitor getInstance(final Context context) {
        NetworkMonitor inst = instance == null ? null : instance.get();
        if (inst == null) {
            instance = new SoftReference<>(inst = new NetworkMonitor(context));
        }

        return inst;
    }

    // endregion

    // region Constructors

    private NetworkMonitor(final Context context) {
        if (appContext == null) {
            appContext = context.getApplicationContext();
        }

        initNetworkStateListener();
    }

    // endregion

    // region Public Methods

    public boolean isNetworkAvailable() {
        return isNetworkAvailable;
    }

    public boolean isBroadband() {
        return isBroadband;
    }

    public int getNetType() {
        return netType;
    }

    public String getNetTypeString() {
        return netTypeString;
    }

    // endregion

    // region Private Methods

    /**
     * 初始化网络状态监听
     */
    private void initNetworkStateListener() {
        if (mNetworkStateChangeListener == null) {
            updateNetworkState(appContext);

            mNetworkStateChangeListener = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    updateNetworkState(context);
                }
            };

            appContext.registerReceiver(mNetworkStateChangeListener,
                    new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
        }
    }

    private void updateNetworkState(Context context) {
            setChanged();
            notifyObservers();
    }
}
