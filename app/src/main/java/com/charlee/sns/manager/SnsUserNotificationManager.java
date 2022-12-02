package com.charlee.sns.manager;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.charlee.sns.BuildConfig;
import com.charlee.sns.model.IPageableList;
import com.charlee.sns.model.NotificationList;
import com.charlee.sns.model.SnsModel;
import com.charlee.sns.model.UserNotification;
import com.charlee.sns.storage.Storage;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import bolts.Continuation;
import bolts.Task;

public class SnsUserNotificationManager {
    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = SnsUserNotificationManager.class.getSimpleName();

    private List<UserNotificationListener> listenerList;
    private List<Integer> listenerHashcodeListAlerted;
    private final Object locker = new Object();

    private String updateId;
    private String updateIdInited;

    private long intervalTimeMsCheckUpdate = 120 * 1000; // 120*1000 ms default
    private long lastCheckUpdateTimeMs;

    private static final int MSG_CHECK_NOTIFICATION_UPDATE = 0;
    private static final int MSG_REMOVE_ALL_NOTIFICATIONS = 1;

    private static MyHandler sHandler;

    private static SnsUserNotificationManager instance;

    private SnsUserNotificationManager() {
        listenerList = new ArrayList<>();
        listenerHashcodeListAlerted = new ArrayList<>();
        updateId = Storage.getInstance().getNotificationUpdateId();
        updateIdInited = updateId;
        // 先直接读取设置中的
        intervalTimeMsCheckUpdate = Storage.getInstance().getConfigNotificationRefreshPeriod() * 1000;
        // 然后去服务器同步
        SnsModel.getInstance().refreshSnsConfiguration().continueWith(new Continuation<Void, Void>() {
            @Override
            public Void then(Task<Void> task) throws Exception {
                intervalTimeMsCheckUpdate = Storage.getInstance().getConfigNotificationRefreshPeriod() * 1000;
                return null;
            }
        }, Task.UI_THREAD_EXECUTOR);
    }

    public static synchronized SnsUserNotificationManager getInstance() {
        if (instance == null) {
            if (DEBUG) {
                Log.d(TAG, "getInstance().init");
            }
            instance = new SnsUserNotificationManager();
            sHandler = new MyHandler(instance);
        }

        return instance;
    }

    public void register(UserNotificationListener listener) {
        if (DEBUG) {
            Log.d(TAG, "register(), listener=" + listener);
        }

        if (listener == null) {
            return;
        }

        synchronized (locker) {
            if (!listenerList.contains(listener)) {
                listenerList.add(listener);
            }

            if (updateId != updateIdInited && !listenerHashcodeListAlerted.contains(listener.hashCode())) {
                listener.onNewNotification();
                listenerHashcodeListAlerted.add(listener.hashCode());
            }
        }

        if (listenerList.size() == 1 && !sHandler.hasMessages(MSG_CHECK_NOTIFICATION_UPDATE)) {
            restart();
        }
    }

    public void unRegister(UserNotificationListener listener) {

        if (listener == null) {
            return;
        }

        synchronized (locker) {
            if (listenerList.contains(listener)) {
                listenerList.remove(listener);
            }
        }
    }

    public interface UserNotificationListener {
        void onNewNotification();

        void onRemoveNotification();
    }

    private static class MyHandler extends Handler {
        private WeakReference<SnsUserNotificationManager> instanceWeakRef;

        public MyHandler(SnsUserNotificationManager snm) {
            super(Looper.getMainLooper());
            instanceWeakRef = new WeakReference<>(snm);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_CHECK_NOTIFICATION_UPDATE: {
                    SnsUserNotificationManager snm = instanceWeakRef.get();
                    if (snm == null) {
                        return;
                    }

                    if (snm.isNeedCheckUpdate()) {
                        snm.doCheckHasUpdate();
                        snm.sendCheckUpdateTick();
                    }
                }
                    break;
                case MSG_REMOVE_ALL_NOTIFICATIONS: {
                    SnsUserNotificationManager snm = instanceWeakRef.get();
                    if (snm == null) {
                        return;
                    }

                    snm.doRemoveAllNotifications();
                }
                    break;

                default:
                    break;
            }
        }
    }

    public void restart() {
        sHandler.removeMessages(MSG_CHECK_NOTIFICATION_UPDATE);
        long now = System.currentTimeMillis();
        long distance = now - lastCheckUpdateTimeMs;
        if (distance > 0 && distance < intervalTimeMsCheckUpdate) {
            sHandler.sendEmptyMessageDelayed(MSG_CHECK_NOTIFICATION_UPDATE, distance);
        } else {
            sHandler.sendEmptyMessage(MSG_CHECK_NOTIFICATION_UPDATE);
        }
    }

    public void stop() {
        sHandler.removeMessages(MSG_CHECK_NOTIFICATION_UPDATE);
        sHandler.sendEmptyMessage(MSG_REMOVE_ALL_NOTIFICATIONS);

        Storage.getInstance().setNotificationUpdateId("");
    }

    public void saveNotificationUpdateId() {
        if (isNeedCheckUpdate()) {
            Storage.getInstance().setNotificationUpdateId(updateId);
        }
    }

    public void removeAllNotifications() {
        updateIdInited = updateId;
        Storage.getInstance().setNewNotificationStatus(false);
        sHandler.sendEmptyMessage(MSG_REMOVE_ALL_NOTIFICATIONS);
    }

    private void doRemoveAllNotifications() {
        synchronized (locker) {
            for (UserNotificationListener listener : listenerList) {
                listener.onRemoveNotification();
            }
        }
    }

    private void sendCheckUpdateTick() {
        synchronized (locker) {
            if (listenerList.size() > 0) {
                sHandler.sendEmptyMessageDelayed(
                        MSG_CHECK_NOTIFICATION_UPDATE, intervalTimeMsCheckUpdate);
            }
        }
    }

    private boolean isNeedCheckUpdate() {
        return SnsModel.getInstance().isUserLoggedIn();
    }

    private void doCheckHasUpdate() {
        lastCheckUpdateTimeMs = System.currentTimeMillis();

        final IPageableList<UserNotification> list = SnsModel.getInstance().getUserNotificationList();
        if (list == null) {
            return;
        }

        if (!(list instanceof NotificationList)) {
            return;
        }

        try {
            list.refresh().continueWith(new Continuation<Boolean, Boolean>() {
                @Override
                public Boolean then(Task<Boolean> task) throws Exception {
                    if (task != null && task.getResult()) {
                        String newLastId = list.getLastId();
                        boolean isHasUpdate = !TextUtils.isEmpty(newLastId) && !newLastId.equals(updateId);
                        if (isHasUpdate) {
                            updateId = newLastId;
                            listenerHashcodeListAlerted.clear(); // 当updatedId改变后，通知列表需要被清空。
                            Storage.getInstance().setNewNotificationStatus(true);
                            doNotify();
                        }
                        saveNotificationUpdateId();
                        return isHasUpdate;
                    } else {
                        return false;
                    }
                }
            }, Task.UI_THREAD_EXECUTOR);

        } catch (Exception e) {
            // TODO
        }
    }

    private void doNotify() {
        synchronized (locker) {
            if (listenerList.size() == 0) {
                return;
            }

            for (UserNotificationListener listener : listenerList) {
                listener.onNewNotification();
                listenerHashcodeListAlerted.add(listener.hashCode());
            }
        }
    }
}
