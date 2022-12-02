package com.charlee.sns.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.charlee.sns.model.IPageableList;
import com.charlee.sns.model.ModelBase;


/**
 * 带表头的列表Adapter
 * 可以指定一个layout作为Header实现头部和列表的同步滚动。
 * 支持LinearLayoutManager、GridLayoutManager和StaggeredGridLayoutManager。
 */
public abstract class HeaderedListAdapter<ItemTypeT extends ModelBase>
        extends PageableListAdapter<ItemTypeT, RecyclerView.ViewHolder> {

    public static final int TYPE_HEADER = -1;
    public static final int TYPE_ITEM = -2;

    private final int headerResId;

    protected final boolean isHeadered;
    protected HeaderViewListener headerViewListener;

    class HeaderViewHolder extends RecyclerView.ViewHolder {
        public HeaderViewHolder(View itemView) {
            super(itemView);
        }
    }

    public static final int HEADER_NONE = -1;

    /**
     * 简单消息列表的Adapter
     * @param itemList          项目列表
     * @param headerResId       列表头的Layout ID(<0则无列表头)
     */
    public HeaderedListAdapter(@NonNull IPageableList<ItemTypeT> itemList,
                               int headerResId,
                               @Nullable HeaderViewListener headerViewListener) {
        super(itemList);
        this.headerResId = headerResId;
        isHeadered = headerResId > 0;
        this.headerViewListener = headerViewListener;
    }

    @Override
    public long getItemId(int position) {
        ItemTypeT item = getItem(position);
        return item != null ? item.hashCode() : 0;
    }

    /**
     * 创建除表头外的其他ViewHolder
     * @param parent    父ViewGroup
     * @return          创建好的ViewHolder
     */
    protected abstract RecyclerView.ViewHolder onCreateBodyViewHolder(ViewGroup parent, int viewType);

    @Override
    public final RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View v = LayoutInflater.from(parent.getContext()).inflate(headerResId, parent, false);
            HeaderViewHolder holder = new HeaderViewHolder(v);
            if (parent instanceof RecyclerView) {
                RecyclerView recyclerView = (RecyclerView) parent;
                RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                if (layoutManager instanceof StaggeredGridLayoutManager) {
                    StaggeredGridLayoutManager.LayoutParams layoutParams =
                            new StaggeredGridLayoutManager.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT);
                    layoutParams.setFullSpan(true);
                    holder.itemView.setLayoutParams(layoutParams);
                } else if (layoutManager instanceof GridLayoutManager) {
                    final GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
                    gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                        @Override
                        public int getSpanSize(int i) {
                            return getItemViewType(i) == TYPE_HEADER ? gridLayoutManager.getSpanCount() : 1;
                        }
                    });
                }
            }

            if (headerViewListener != null) {
                headerViewListener.onHeaderViewCreated(holder.itemView);
            }
            return holder;
        }

        return onCreateBodyViewHolder(parent, viewType);
    }

    protected abstract void onBindBodyViewHolder(final RecyclerView.ViewHolder holder, int position);

    @Override
    public final void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        if (isPositionHeader(position)) {
            return;
        }

        onBindBodyViewHolder(holder, position);
    }

    @Override
    public int getItemCount() {
        int count = itemList.size();
        if (isHeadered) {
            count++;
        }

        return count;
    }

    @Override
    public int indexOfItem(ItemTypeT item) {
        int index = itemList.indexOf(item);
        if (index < 0) {
            return index;
        }

        return isHeadered ? index + 1 : index;
    }

    public int getDataItemCount() {
        return itemList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (isPositionHeader(position)) {
            return TYPE_HEADER;
        }

        return TYPE_ITEM;
    }

    public ItemTypeT getItem(int position) {
        if (isHeadered) {
            if (position == 0) {
                return null;
            } else {
                return itemList.get(position - 1);
            }
        } else {
            return itemList.get(position);
        }
    }

    // 表头固定为第一个位置
    protected boolean isPositionHeader(int position) {
        return isHeadered && position == 0;
    }

    public interface HeaderViewListener {
        void onHeaderViewCreated(View header);
    }
}
