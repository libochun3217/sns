package com.charlee.sns.adapter;

import android.app.Activity;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.charlee.sns.R;
import com.charlee.sns.helper.ReportHelper;
import com.charlee.sns.helper.SectionReport;
import com.charlee.sns.model.IPageableList;
import com.charlee.sns.model.UserMessage;
import com.charlee.sns.widget.StaggeredListView;


/**
 * 消息的（带表头）简单列表Adapter，每条消息只显示一张图片。
 */
public class SimpleMessageListAdapter extends HeaderedListAdapter<UserMessage> {

    private int itemWidth;
    private int itemSpacing;
    private final SimpleMessageViewHolder.IImageClickedCallback imageClickedCallback;
    private boolean voteIndicator;

    ReportHelper.MessageScene scene;

    /**
     * 简单消息列表的Adapter
     * @param messages          消息列表
     * @param headerResId       列表头的Layout ID(<0则无列表头)
     */
    public SimpleMessageListAdapter(@NonNull IPageableList<UserMessage> messages, int headerResId,
                                    HeaderViewListener headerViewListener,
                                    @NonNull SimpleMessageViewHolder.IImageClickedCallback imageClickedCallback,
                                    ReportHelper.MessageScene scene) {
        super(messages, headerResId, headerViewListener);
        this.imageClickedCallback = imageClickedCallback;
        this.scene = scene;
    }

    /**
     * 带投票功能的简单消息列表的Adapter
     * @param messages          消息列表
     * @param headerResId       列表头的Layout ID(<0则无列表头)
     */
    public SimpleMessageListAdapter(@NonNull IPageableList<UserMessage> messages, int headerResId,
                                    HeaderViewListener headerViewListener,
                                    @NonNull SimpleMessageViewHolder.IImageClickedCallback imageClickedCallback,
                                    boolean voteIndicator,
                                    ReportHelper.MessageScene scene) {
        super(messages, headerResId, headerViewListener);
        this.imageClickedCallback = imageClickedCallback;
        this.voteIndicator = voteIndicator;
        this.scene = scene;
    }

    @Override
    public void onBindBodyViewHolder(final RecyclerView.ViewHolder holder, int position) {
        UserMessage item = super.getItem(position);
        if (item != null && holder instanceof SimpleMessageViewHolder) {
            SimpleMessageViewHolder viewHolder = (SimpleMessageViewHolder) holder;
            viewHolder.bind(item);
            SectionReport.getInstance().showMessage(scene, item.getId());
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateBodyViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_simple, parent, false);
        if (itemWidth == 0) {
            // 这里parent.getWidth()有可能为空
            DisplayMetrics metrics = new DisplayMetrics();
            ((Activity) (parent.getContext())).getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int width = metrics.widthPixels - parent.getPaddingLeft() - parent.getPaddingRight();
            if (parent instanceof StaggeredListView) {
                StaggeredListView listView = (StaggeredListView) parent;
                itemSpacing = listView.getItemSpacing();
                width = width / listView.getColumns() - itemSpacing;
            }

            itemWidth = width;
        }

        return new SimpleMessageViewHolder(v, imageClickedCallback, isHeadered, itemWidth, itemSpacing, voteIndicator);
    }

    public IPageableList<UserMessage> getMessage() {
        return this.itemListModel;
    }

    public void clear() {
        // 通过Observer通知机制在某些情况下无法及时清除视图缓存
        // 所以在adapter里直接触发刷法
        if (itemList != null) {
            itemList.clear();
            notifyDataSetChanged();
        }
    }

}
