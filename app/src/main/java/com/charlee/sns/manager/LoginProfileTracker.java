package com.charlee.sns.manager;


import com.charlee.sns.model.SnsModel;
import com.charlee.sns.storage.Storage;

import java.util.concurrent.Callable;

import bolts.Task;

/**
 * 跟踪并同步登录相关的配置信息
 * 1，pushToken是否发生变化，如果有变化则进行同步
 */
public class LoginProfileTracker {

    private static LoginProfileTracker instance = null;

    public static synchronized LoginProfileTracker getInstance() {
        if (instance == null) {
            instance = new LoginProfileTracker();
        }
        return instance;
    }

    // 启动跟踪，检查profile是否发生变化
    public void startTracker() {
        if (SnsModel.getInstance().isUserLoggedIn() == false) {
            return;
        }

        Task.callInBackground(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                startPushTokenTracker();
                return null;
            }
        });
    }

    private void startPushTokenTracker() {
        String currentPushToken = SnsEnvController.getInstance().getNetworkParams().getPushToken();
        String serialPushToken = Storage.getInstance().getPushToken();

        if (currentPushToken == null) {
            return;
        }

        if (currentPushToken.equals(serialPushToken)) {
            return;
        }

        updatePushToken(currentPushToken);
    }

    public void updatePushToken(String pushToken) {
        SnsModel.getInstance().updateLoginProfile(0, null, pushToken, null, null, null);
    }

    public Task<Boolean> updatePortrait(String portraitUri) {
        return SnsModel.getInstance().updateLoginProfile(0, null, null, portraitUri, null, null);
    }

    public Task<Boolean> updateNickname(String nickName) {
        return SnsModel.getInstance().updateLoginProfile(0, null, null, null, nickName, null);
    }

    public Task<Boolean> updateFcmToken(String fcmToken) {
        return SnsModel.getInstance().updateLoginProfile(0, null, null, null, null, fcmToken);
    }

}
