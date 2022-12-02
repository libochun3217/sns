package com.charlee.sns.adapter;

/**
 * 监控数据对象的本地修改
 */
public interface NotificationDataChangedListener {
    void onDataChanged();
    void onItemChanged(int position);
}
