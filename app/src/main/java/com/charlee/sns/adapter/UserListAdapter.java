package com.charlee.sns.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.charlee.sns.R;
import com.charlee.sns.model.ICollectionObserver;
import com.charlee.sns.model.IPageableList;
import com.charlee.sns.model.SnsUser;

import java.util.List;

/**
 * 用户列表adapter，用于展示关注用户或粉丝用户
 */
public class UserListAdapter extends PageableListAdapter<SnsUser, UserItemViewHolder> {

    private OnRecycleViewItemClickListener itemClickListener;

    public UserListAdapter(IPageableList<SnsUser> users) {
        super(users);
    }

    @Override
    public void onBindViewHolder(final UserItemViewHolder holder, int position) {
        SnsUser item = itemList.get(position);
        holder.bind(item);
    }

    @Override
    public UserItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserItemViewHolder(v);
    }

    public void setOnItemClickListener(OnRecycleViewItemClickListener listener) {
        itemClickListener = listener;
    }

    @Override
    protected boolean onListChanged(final ICollectionObserver.Action action,
                                    final SnsUser item, final List<Object> range) {
        if (action == ICollectionObserver.Action.AddItemToFront) {
            notifyDataSetChanged();
        } else if (action == ICollectionObserver.Action.RemoveItem) {
            return true;
        }

        return false;
    }

}
