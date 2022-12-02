package com.charlee.sns.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.charlee.sns.R;
import com.charlee.sns.model.IPageableList;
import com.charlee.sns.model.MessageTag;


/**
 * Tag标签页的垂直列表Adapter，显示所有的Tag列表。
 */
public class FullTagListAdapter extends PageableListAdapter<MessageTag, FullTagViewHolder> {

    public FullTagListAdapter(IPageableList<MessageTag> tags) {
        super(tags);
    }

    @Override
    public void onBindViewHolder(final FullTagViewHolder holder, int position) {
        MessageTag item = itemList.get(position);
        holder.bind(item);
    }

    @Override
    public FullTagViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tag_full, parent, false);
        return new FullTagViewHolder(v);
    }

}
