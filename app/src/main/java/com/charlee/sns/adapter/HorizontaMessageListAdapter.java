package com.charlee.sns.adapter;

import com.charlee.sns.R;
import com.charlee.sns.model.IPageableList;
import com.charlee.sns.model.ModelBase;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 带标题的图片水平列表Adapter
 * 根据分栏计算列表项目的宽度以及总的列表高度
 */
public abstract class HorizontaMessageListAdapter<ItemTypeT extends ModelBase>
        extends PageableListAdapter<ItemTypeT, HorizontaMessageListAdapter.ViewHolder> {

    private static final float COLUMNS = 3.7f; // 显示3个半
    private final int itemLayoutId;
    protected int itemWidth;
    protected int itemHeight;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView image;
        public TextView text;

        public ViewHolder(final View container, int imageSize) {
            super(container);
            image = (ImageView) container.findViewById(R.id.image);
            text = (TextView) container.findViewById(R.id.text);
            ViewGroup.LayoutParams layoutParams = image.getLayoutParams();
            layoutParams.width = imageSize;
            layoutParams.height = imageSize;
            image.setLayoutParams(layoutParams);
        }
    }

    public HorizontaMessageListAdapter(@NonNull IPageableList<ItemTypeT> items, @LayoutRes int itemLayoutId) {
        super(items);
        this.itemLayoutId = itemLayoutId;
    }

    public void init(Context context, int containerWidth) {
        Resources resources = context.getResources();
        int margin = resources.getDimensionPixelSize(R.dimen.content_margin);
        int itemSpacing = resources.getDimensionPixelSize(R.dimen.default_item_spacing);

        // 设置标签项目的大小
        itemWidth = (int) ((containerWidth - margin - Math.floor(COLUMNS) * itemSpacing) / COLUMNS);
        itemHeight =  calculateItemHeight(context, itemWidth, itemSpacing);
    }

    public int getListHeight() {
        return itemHeight;
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(itemLayoutId, parent, false);

        RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(itemWidth, itemHeight);
        v.setLayoutParams(layoutParams);

        // WORKAROUND: 设置标签列表的高度
        // 原因：LinearLayoutManager实际上不支持wrap_content：https://code.google.com/p/android/issues/detail?id=74772
        ViewGroup.LayoutParams parentLayoutParams = parent.getLayoutParams();
        if (parentLayoutParams.height != itemHeight) {
            parentLayoutParams.height = itemHeight;
            parent.setLayoutParams(parentLayoutParams);
        }

        return new ViewHolder(v, itemWidth);
    }

    protected abstract int calculateItemHeight(Context context, int width, int itemSpacing);
}