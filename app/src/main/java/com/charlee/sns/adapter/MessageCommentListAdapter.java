package com.charlee.sns.adapter;

import java.util.List;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.charlee.sns.R;
import com.charlee.sns.model.Comment;
import com.charlee.sns.model.ICollectionObserver;
import com.charlee.sns.model.IPageableList;
import com.charlee.sns.model.UserMessage;


/**
 * 一条消息的所有评论列表的adapter
 */
public class MessageCommentListAdapter extends PageableListAdapter<Comment, MessageCommentViewHolder> {

    private UserMessage userMessage;

    private OnRecycleViewItemClickListener itemClickListener;

    public MessageCommentListAdapter(IPageableList<Comment> comments) {
        super(comments);
    }

    @Override
    public void onBindViewHolder(final MessageCommentViewHolder holder, int position) {
        Comment item = itemList.get(position);
        holder.setOnItemClickListener(itemClickListener);
        holder.bind(item, userMessage);
    }

    @Override
    public MessageCommentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new MessageCommentViewHolder(v);
    }

    public void setUserMessage(UserMessage userMessage) {
        this.userMessage = userMessage;
    }

    public void setOnItemClickListener(OnRecycleViewItemClickListener listener) {
        itemClickListener = listener;
    }

    @Override
    protected boolean onListChanged(final ICollectionObserver.Action action,
                                    final Comment item, final List<Object> range) {
        if (action == ICollectionObserver.Action.AddItemToFront) {
            notifyDataSetChanged(); // 全部刷新以保证新加入的评论能显示出来
        }

        return false;
    }

}
