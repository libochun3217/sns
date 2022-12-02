package com.charlee.sns.adapter;

import java.util.HashMap;
import java.util.List;


import android.content.Context;
import android.view.View;

import com.charlee.sns.R;
import com.charlee.sns.helper.NavigationHelper;
import com.charlee.sns.helper.SnsImageLoader;
import com.charlee.sns.model.ICollectionObserver;
import com.charlee.sns.model.IPageableList;
import com.charlee.sns.model.UserMessage;

/**
 */
public class LatestMsgListAdapter extends HorizontaMessageListAdapter<UserMessage> {

    public LatestMsgListAdapter(IPageableList<UserMessage> messages) {
        super(messages, R.layout.item_message_simple);
    }

    @Override
    protected int calculateItemHeight(Context context, int width, int itemSpacing) {
        return width + itemSpacing;
    }

    @Override
    protected boolean onListChanged(final ICollectionObserver.Action action,
                                    final UserMessage item, final List<Object> range) {
        if (action == ICollectionObserver.Action.AddItemToFront) {
            notifyDataSetChanged(); // 全部刷新以保证新加入的图片能显示出来
        }

        return false;
    }

    @Override
    public void onBindViewHolder(final TagListAdapter.ViewHolder holder, int position) {
        final UserMessage item = itemList.get(position);
        SnsImageLoader.loadSquareImage(item.getImage(), holder.image, itemWidth);

        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String, String> queries = new HashMap<>();
                int index = holder.getLayoutPosition();
                queries.put(NavigationHelper.QUERY_POS, String.valueOf(index));
                NavigationHelper.navigateToPath(
                        holder.image.getContext(), NavigationHelper.PATH_SNS_LATEST_MSGS_FULL, queries);
            }
        });
    }
}
