package com.charlee.sns.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.charlee.sns.R;
import com.charlee.sns.model.IPageableList;
import com.charlee.sns.model.SnsUser;


/**
 * 发现朋友页面的垂直列表Adapter
 */
public class FriendListAdapter extends PageableListAdapter<SnsUser, FriendViewHolder> {

    public FriendListAdapter(IPageableList<SnsUser> friends) {
        super(friends);
    }

    @Override
    public void onBindViewHolder(final FriendViewHolder holder, int position) {
        SnsUser item = itemList.get(position);
        holder.bind(item);
    }

    @Override
    public FriendViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend_user, parent, false);
        return new FriendViewHolder(v);
    }

}
