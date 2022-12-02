package com.charlee.sns.adapter;

import android.app.Activity;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.charlee.sns.R;
import com.charlee.sns.helper.NavigationHelper;
import com.charlee.sns.helper.SnsImageLoader;
import com.charlee.sns.model.IPageableList;
import com.charlee.sns.model.MessageTag;
import com.charlee.sns.model.UserMessage;

import java.util.ArrayList;
import java.util.HashMap;

import bolts.Continuation;
import bolts.Task;

/**
 * 每个TAG标签元素的ViewHolder
 */
public class FullTagViewHolder extends RecyclerView.ViewHolder {
    private static final int SPAN_COUNT = 3;

    private TextView tvTagTitle;
    private TextView tvTagMore;
    private LinearLayout imageContainer;

    private int itemSpacing;
    private int imageWidth;

    private ArrayList<ImageView> imageArray = new ArrayList<>();

    private MessageTag messageTag;

    public FullTagViewHolder(final View container) {
        super(container);

        tvTagTitle = (TextView) container.findViewById(R.id.tv_tag_title);
        tvTagMore = (TextView) container.findViewById(R.id.btn_tag_more);

        imageArray.add((ImageView) container.findViewById(R.id.image_first));
        imageArray.add((ImageView) container.findViewById(R.id.image_second));
        imageArray.add((ImageView) container.findViewById(R.id.image_third));

        imageContainer = (LinearLayout) container.findViewById(R.id.image_container);

        DisplayMetrics metrics = new DisplayMetrics();
        ((Activity) (container.getContext())).getWindowManager().getDefaultDisplay().getMetrics(metrics);

        itemSpacing = container.getContext().getResources().getDimensionPixelSize(R.dimen.large_item_spacing);
        int marginCount = SPAN_COUNT + 1;
        imageWidth = (metrics.widthPixels - marginCount * itemSpacing) / SPAN_COUNT;

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) imageContainer.getLayoutParams();
        params.height = imageWidth;
        imageContainer.setLayoutParams(params);

        for (ImageView iv : imageArray) {
            params = (LinearLayout.LayoutParams) iv.getLayoutParams();
            params.width = imageWidth;
            params.height = imageWidth;
            params.setMargins(itemSpacing, 0, 0, 0);
            iv.setLayoutParams(params);
        }

        tvTagTitle.setPadding(itemSpacing, 0, 0, 0);
    }

    public void bind(final MessageTag item) {
        messageTag = item;

        tvTagTitle.setText(item.getName());
        tvTagMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String, String> queries = new HashMap<>();
                item.getNavQuery(queries);
                NavigationHelper.navigateToPath(v.getContext(), NavigationHelper.PATH_TAG_MESSAGE_LIST, queries);
            }
        });

        for (ImageView iv : imageArray) {
            iv.setVisibility(View.INVISIBLE);
        }

        refresh();

    }

    private void refresh() {
        if (messageTag.getMessages().size() > 0) {
            fillViews();
            return;
        }

        try {
            messageTag.getMessages().refresh().onSuccess(new Continuation<Boolean, Object>() {
                @Override
                public Object then(Task<Boolean> task) throws Exception {
                    fillViews();
                    return null;
                }
            }, Task.UI_THREAD_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void fillViews() {
        IPageableList<UserMessage> messages = messageTag.getMessages();
        if (messages != null && messages.size() == 0) {
            return;
        }

        for (int index = 0; index < SPAN_COUNT; index++) {
            if (messages.size() <= index) {
                break;
            }
            UserMessage message = messages.get(index);
            final ImageView iv = imageArray.get(index);
            final int position = index;
            if (message != null) {
                iv.setVisibility(View.VISIBLE);
                SnsImageLoader.loadSquareImage(message.getImage(), iv, imageWidth);
                iv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        HashMap<String, String> queries = new HashMap<>();
                        messageTag.getNavQuery(queries);
                        queries.put(NavigationHelper.QUERY_POS, String.valueOf(position));
                        NavigationHelper.navigateToPath(iv.getContext(), NavigationHelper.PATH_SNS_TAG_MSGS, queries);
                    }
                });
            }
        }

    }

}
