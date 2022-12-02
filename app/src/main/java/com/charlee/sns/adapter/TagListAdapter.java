package com.charlee.sns.adapter;

import java.util.HashMap;

import com.charlee.sns.R;
import com.charlee.sns.helper.NavigationHelper;
import com.charlee.sns.helper.SnsImageLoader;
import com.charlee.sns.model.IPageableList;
import com.charlee.sns.model.MessageTag;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;

import androidx.annotation.NonNull;

/**
 * 标签列表Adapter
 */
public class TagListAdapter extends HorizontaMessageListAdapter<MessageTag> {

    private static final float TEXT_REAL_HEIGHT_FACTOR = 1.2f; // 文字真实高度与指定高度的倍数

    public TagListAdapter(@NonNull IPageableList<MessageTag> messages) {
        super(messages, R.layout.item_image_with_title);
    }

    @Override
    protected int calculateItemHeight(Context context, int width, int itemSpacing) {
        Resources resources = context.getResources();
        int textHeight = resources.getDimensionPixelSize(R.dimen.sns_text_small);
        return width + itemSpacing + (int) (textHeight * TEXT_REAL_HEIGHT_FACTOR);
    }

    @Override
    public void onBindViewHolder(final TagListAdapter.ViewHolder holder, int position) {
        final MessageTag item = itemList.get(position);
        holder.text.setText("#" + item.getName());
        SnsImageLoader.loadSquareImage(item.getTagImage(), holder.image, itemWidth);

        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String, String> queries = new HashMap<>();
                item.getNavQuery(queries);
                NavigationHelper.navigateToPath(holder.image.getContext(),
                        NavigationHelper.PATH_TAG_MESSAGE_LIST, queries);
            }
        });
    }
}