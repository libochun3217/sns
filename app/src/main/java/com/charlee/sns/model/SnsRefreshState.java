package com.charlee.sns.model;

/**
 * 标记刷新状态，供Feed流页面按需刷新
 */
public class SnsRefreshState {
    private static SnsRefreshState instance;
    private boolean refreshState;

    // 标记首页Tab是否位于FollowFragment页面
    // 为了不在层级较深的View级别多处写入标记，所以用全局状态来标记
    // 这个标记为完成一个特殊需求，当从首页的Follow页面发起登录请求后，不刷新Follow页面，反之刷新
    private boolean isFollowFragmentTab;

    public static synchronized SnsRefreshState getInstance() {
        if (instance == null) {
            instance = new SnsRefreshState();
        }
        return instance;
    }

    public void setRefreshState(boolean state) {
        refreshState = state;
    }

    public boolean getRefreshState() {
        return refreshState;
    }

    public void setFollowFragmentTab(boolean state) {
        isFollowFragmentTab = state;
    }

    public boolean isFollowFragmentTab() {
        return isFollowFragmentTab;
    }

}
