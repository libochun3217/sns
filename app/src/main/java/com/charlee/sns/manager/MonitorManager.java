package com.charlee.sns.manager;


import com.charlee.sns.model.SnsModel;

/**
 * 管理各个监控器对象
 */
public class MonitorManager {

    private final FollowListMonitor followListMonitor;

    private static MonitorManager instance;

    public static synchronized MonitorManager getInstance() {
        if (instance == null) {
            instance = new MonitorManager();
        }
        return instance;
    }

    public MonitorManager() {
        followListMonitor = new FollowListMonitor(SnsModel.getInstance().getCardList());
    }

    public FollowListMonitor getFollowListMonitor() {
        return followListMonitor;
    }

}
