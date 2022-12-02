package com.charlee.sns.helper;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.charlee.sns.BuildConfig;
import com.charlee.sns.manager.SnsEnvController;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;

import bolts.Task;

/**
 * 累积延时上报的工具类，超过MAX_ITEM条时上报
 */
public class SectionReport {
    private static final String TAG = SectionReport.class.getName();
    private static final int MIN_ITEM = 2;
    private static final int MAX_ITEM = 60;
    private static final long CHECK_INTERVAL = 2 * 60 * 1000;
    private static final String SNS_REPORT_SETTING = "sns_report_setting";
    private static final String SHOW_CONTENT = "show_content_";
    private static final String SHOW_COUNT = "show_count_";

    private String[] pageArray = {"community", "challenge", "personal", "tag", "feed"};

    // 生产者队列，写入打点事件，理论上不会引发阻塞
    private ConcurrentLinkedQueue<ReportItem> itemQueue = new ConcurrentLinkedQueue<>();

    private static SectionReport instance = null;

    public static synchronized SectionReport getInstance() {
        if (instance == null) {
            instance = new SectionReport();
        }
        return instance;
    }

    public void showMessage(ReportHelper.MessageScene scene, String id) {
        int pageIndex = 0;
        switch (scene) {
            case community:
                pageIndex = 0;
                break;
            case challenge:
                pageIndex = 1;
                break;
            case personal:
                pageIndex = 2;
                break;
            case tag:
                pageIndex = 3;
                break;
            case feed:
                pageIndex = 4;
                break;
            default:
                break;
        }

        itemQueue.add(new ReportItem(pageArray[pageIndex], id));
    }

    /**
     * 首次启动，先上报之前累计的打点
     */
    public void start() {
        Task.callInBackground(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                for (String page : pageArray) {
                    String pageKey = SHOW_CONTENT + page;
                    String countKey = SHOW_COUNT + page;

                    int count = getCount(countKey);
                    if (count >= MIN_ITEM) {
                        if (BuildConfig.DEBUG) {
                            Log.d(TAG, "启动程序时的打点：" + page + "," + getItems(pageKey));
                        }
                        reportShowedMessages(SnsEnvController.getInstance().getAppContext(), page, getItems(pageKey));
                        clearItems(pageKey);
                        setCount(countKey, 0);
                    }
                }

                // 然后启动后台轮询线程
                restart();

                return null;
            }
        });
    }

    public void stop() {

    }

    /**
     * 消费者线程，将打点事件序列化到本地，如果打点事件大于MAX_ITEM则上报
     */
    public void restart() {
        Task.callInBackground(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                while (true) {
                    while (!itemQueue.isEmpty()) {
                        ReportItem item = itemQueue.poll();
                        String pageKey = SHOW_CONTENT + item.page;
                        String countKey = SHOW_COUNT + item.page;

                        int count = getCount(countKey);
                        addItem(pageKey, item.content + ",");
                        setCount(countKey, ++count);
                        if (count >= MAX_ITEM) {
                            if (BuildConfig.DEBUG) {
                                Log.d(TAG, "运行时的打点：" + item.page + "," + getItems(pageKey));
                            }
                            reportShowedMessages(SnsEnvController.getInstance().getAppContext(), item.page, getItems(pageKey));
                            clearItems(pageKey);
                            setCount(countKey, 0);
                        }
                    }

                    Thread.sleep(CHECK_INTERVAL);
                }
            }
        });
    }

    class ReportItem {
        String page;
        String content;

        ReportItem(String page, String content) {
            this.page = page;
            this.content = content;
        }
    }

    private static void reportShowedMessages(final Context context,
                                             final String page, final String content) {
        Task.callInBackground(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                ReportHelper.showMessage(context, page, content);
                return null;
            }
        });
    }

    private void addItem(String key, String content) {
        SharedPreferences settings = getSettingsSharedPref();
        String itemsContent = settings.getString(key, "");
        itemsContent += content;
        settings.edit().putString(key, itemsContent).commit();
    }

    private void clearItems(String key) {
        SharedPreferences settings = getSettingsSharedPref();
        settings.edit().remove(key).commit();
    }

    private String getItems(String key) {
        SharedPreferences settings = getSettingsSharedPref();
        return settings.getString(key, "");
    }

    private void setCount(String key, int count) {
        SharedPreferences settings = getSettingsSharedPref();
        settings.edit().putInt(key, count).commit();
    }

    private int getCount(String key) {
        SharedPreferences settings = getSettingsSharedPref();
        return settings.getInt(key, 0);
    }

    private SharedPreferences getSettingsSharedPref() {
        return SnsEnvController.getInstance().getAppContext().getSharedPreferences(SNS_REPORT_SETTING,
                Activity.MODE_APPEND);
    }

}
