package com.charlee.sns.widget;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.charlee.sns.R;
import com.charlee.sns.adapter.SimpleMessageListAdapter;

/**
 * 瀑布流列表控件
 */
public class StaggeredListView extends RecyclerView {
    private static final int DEFAULT_COLUMNS = 2;
    private static final int DEFAULT_ITEM_SPACING = 10;

    private int columns;
    private int spacing;
    private StaggeredGridLayoutManager layoutManager;

    // 用于给子控件留出间隔
    private class SpacesItemDecoration extends RecyclerView.ItemDecoration {
        private final int space;

        public SpacesItemDecoration(int space) {
            this.space = space / 2;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            if (getAdapter() instanceof SimpleMessageListAdapter) {
                if (getChildViewHolder(view).getItemViewType() == SimpleMessageListAdapter.TYPE_HEADER) {
                    // 用负值确保列表头左右没有边距
                    outRect.left = -parent.getPaddingLeft();
                    outRect.right = -parent.getPaddingRight();
                    outRect.bottom = 0;
                    outRect.top = 0;
                    return;
                }
            }

            outRect.left = space;
            outRect.right = space;
            outRect.bottom = space;
            outRect.top = space;
        }
    }

    public StaggeredListView(Context context) {
        super(context);
        init(context, null, 0);
    }

    public StaggeredListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public StaggeredListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    /**
     * @param target
     * @param dx
     * @param dy
     * @param consumed
     *
     * @inheritDoc
     */
    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        super.onNestedPreScroll(target, dx, dy, consumed);
    }

    public int getColumns() {
        return columns;
    }

    public int getItemSpacing() {
        return spacing;
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        setHasFixedSize(true);
        columns = DEFAULT_COLUMNS;
        spacing = DEFAULT_ITEM_SPACING;

        if (attrs != null) {
            TypedArray typeArray = context.obtainStyledAttributes(attrs,
                    R.styleable.StaggeredListView, defStyle, 0);

            int n = typeArray.getIndexCount();
            for (int i = 0; i < n; i++) {
                int attr = typeArray.getIndex(i);
                if (attr == R.styleable.StaggeredListView_sl_column) {
                    columns = typeArray.getInt(R.styleable.StaggeredListView_sl_column, DEFAULT_COLUMNS);
                } else if (attr == R.styleable.StaggeredListView_sl_spacing) {
                    spacing = typeArray.getDimensionPixelSize(
                            R.styleable.StaggeredListView_sl_spacing, DEFAULT_ITEM_SPACING);
                }
            }

            typeArray.recycle();
        }

        layoutManager = new StaggeredGridLayoutManager(columns, StaggeredGridLayoutManager.VERTICAL);
        setLayoutManager(layoutManager);
        if (spacing > 0) {
            addItemDecoration(new SpacesItemDecoration(spacing));
        }
    }
}
