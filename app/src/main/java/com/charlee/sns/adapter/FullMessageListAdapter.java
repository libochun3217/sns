package com.charlee.sns.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import androidx.annotation.NonNull;

import com.charlee.sns.R;
import com.charlee.sns.model.ICollectionObserver;
import com.charlee.sns.model.IPageableList;
import com.charlee.sns.model.UserMessage;
import com.charlee.sns.view.FullMessageListView;

import java.util.List;

/**
 * 消息的完整形式列表Adapter，显示消息的全部信息。
 */
public class FullMessageListAdapter extends PageableListAdapter<UserMessage, FullMessageViewHolder> {

    private final FullMessageListView messageListView;

    public FullMessageListAdapter(@NonNull final FullMessageListView messageListView,
                                  @NonNull IPageableList<UserMessage> messages) {
        super(messages);
        this.messageListView = messageListView;
    }

    @Override
    public void onBindViewHolder(final FullMessageViewHolder holder, int position) {
        UserMessage item = itemList.get(position);
        holder.bind(item);
    }

    @Override
    public void onViewRecycled(FullMessageViewHolder holder) {
        holder.recycle();
    }

    @Override
    public FullMessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_full, parent, false);
        int contentWidth = parent.getWidth() - parent.getPaddingLeft() - parent.getPaddingRight();
        return new FullMessageViewHolder(v, messageListView, contentWidth);
    }

    @Override
    protected boolean onListChanged(ICollectionObserver.Action action, UserMessage item, List<Object> range) {
        if (action == ICollectionObserver.Action.Clear) {
            notifyDataSetChanged();
        }
        return super.onListChanged(action, item, range);
    }
}
