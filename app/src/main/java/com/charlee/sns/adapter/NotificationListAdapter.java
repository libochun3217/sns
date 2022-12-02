package com.charlee.sns.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.charlee.sns.R;
import com.charlee.sns.model.IPageableList;
import com.charlee.sns.model.UserNotification;

/**
 * 消息的完整形式列表Adapter，显示消息的全部信息。
 */
public class NotificationListAdapter extends PageableListAdapter<UserNotification, NotificationViewHolder>
                                        implements NotificationDataChangedListener {

    public NotificationListAdapter(IPageableList<UserNotification> notification) {
        super(notification);
    }

    @Override
    public void onBindViewHolder(final NotificationViewHolder holder, int position) {
        UserNotification item = itemList.get(position);
        holder.bind(item, position);
    }

    @Override
    public NotificationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(v, this);
    }

    @Override
    public void onDataChanged() {
        this.notifyDataSetChanged();
    }

    @Override
    public void onItemChanged(int position) {
        this.notifyItemChanged(position);
    }
}
