package com.charlee.sns.manager;

import android.text.TextUtils;
import android.util.Log;


import com.charlee.sns.model.IPageableList;
import com.charlee.sns.model.SnsModel;
import com.charlee.sns.storage.Storage;
import com.facebook.animated.gif.BuildConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import bolts.Continuation;
import bolts.Task;

/**
 * 检测分页列表数据是否有更新
 */
public abstract class PageableListMonitor<ModeTypeT> {
    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = PageableListMonitor.class.getSimpleName();

    private List<ListChangedListener> listenerList;
    private List<Integer> listenerHashcodeListAlerted;
    private final Object locker = new Object();

    private String updateId;
    private String lastUpdateId;

    private IPageableList<ModeTypeT> itemList;

    private long intervalTimeMsCheckUpdate = 120 * 1000; // 120*1000 ms default

    Timer timer = new Timer();
    TimerTask task = new TimerTask() {
        @Override
        public void run() {
            if (isNeedCheckUpdate()) {
                doCheckHasUpdate();
            }
        }
    };

    public interface ListChangedListener {
        void onNewItem();

        void onRemoveItem();
    }

    public PageableListMonitor(IPageableList<ModeTypeT> itemList) {
        this.itemList = itemList;

        listenerList = new ArrayList<>();
        listenerHashcodeListAlerted = new ArrayList<>();

        updateId = getUpdateId();
        lastUpdateId = updateId;

        initUpdateIntervalTime();
    }

    protected void initUpdateIntervalTime() {
        // 先直接读取设置中的
        intervalTimeMsCheckUpdate = Storage.getInstance().getConfigNotificationRefreshPeriod() * 1000;
        // 然后去服务器同步
        SnsModel.getInstance().refreshSnsConfiguration().continueWith(new Continuation<Void, Void>() {
            @Override
            public Void then(Task<Void> task) throws Exception {
                intervalTimeMsCheckUpdate = Storage.getInstance().getConfigNotificationRefreshPeriod() * 1000;
                if (DEBUG) {
                    Log.d(TAG, "SnsModel.getInstance().refreshSnsConfiguration(), intervalTimeMsCheckUpdate="
                            + intervalTimeMsCheckUpdate);
                }
                return null;
            }
        }, Task.UI_THREAD_EXECUTOR);
    }

    public void register(ListChangedListener listener) {

        if (listener == null) {
            return;
        }

        synchronized (locker) {
            if (!listenerList.contains(listener)) {
                listenerList.add(listener);
            }

            if (updateId != lastUpdateId && !listenerHashcodeListAlerted.contains(listener.hashCode())) {
                listener.onNewItem();
                listenerHashcodeListAlerted.add(listener.hashCode());
            }
        }

    }

    public void unRegister(ListChangedListener listener) {

        if (listener == null) {
            return;
        }

        synchronized (locker) {
            if (listenerList.contains(listener)) {
                listenerList.remove(listener);
            }
        }
    }

    public void start() {

        try {
            timer.schedule(task, 1000, intervalTimeMsCheckUpdate);
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    public void stop() {

        timer.cancel();
        removeAllNotifications();
    }

    protected abstract String getUpdateId();

    protected abstract void setUpdateId(String id);

    protected abstract boolean getNewStatus();

    protected abstract void setNewStatus(boolean status);

    public void removeAllNotifications() {
        lastUpdateId = updateId;
        synchronized (locker) {
            for (ListChangedListener listener : listenerList) {
                listener.onRemoveItem();
            }
        }
    }

    protected boolean isNeedCheckUpdate() {
        return SnsModel.getInstance().isUserLoggedIn() && listenerList.size() > 0;
    }

    private void doCheckHasUpdate() {

        if (itemList == null) {
            return;
        }

        try {
            itemList.refresh().continueWith(new Continuation<Boolean, Boolean>() {
                @Override
                public Boolean then(Task<Boolean> task) throws Exception {
                    if (task != null && task.getResult()) {
                        String newLastId = itemList.getLastId();
                        boolean isHasUpdate = !TextUtils.isEmpty(newLastId) && !newLastId.equals(updateId);
                        if (isHasUpdate) {
                            updateId = newLastId;
                            // 当updatedId改变后，通知列表需要被清空。
                            listenerHashcodeListAlerted.clear();
                            setNewStatus(true);
                            doNotify();
                        }
                        setUpdateId(updateId);
                        lastUpdateId = updateId;
                        return isHasUpdate;
                    } else {
                        return false;
                    }
                }
            }, Task.UI_THREAD_EXECUTOR);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doNotify() {
        synchronized (locker) {
            if (listenerList.size() == 0) {
                return;
            }

            for (ListChangedListener listener : listenerList) {
                listener.onNewItem();
                listenerHashcodeListAlerted.add(listener.hashCode());
            }
        }
    }
}
