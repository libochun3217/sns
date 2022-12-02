package com.charlee.sns.widget;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.charlee.sns.R;

/**
 * 水平滑动列表控件，根据指定的列数计算相应的子项目大小。
 * 和HorizontalImageWithTitleListAdapter使用时实际高度在HorizontalImageWithTitleListAdapter中计算确定。
 */
public class HorizontalListView extends RecyclerView {

    public static final int DEFAULT_ITEM_SPACING = 10;

    private int spacing = DEFAULT_ITEM_SPACING;
    private int paddingStart = DEFAULT_ITEM_SPACING;
    private int paddingEnd = DEFAULT_ITEM_SPACING;

    private HorizontalSrollController horizontalSrollController;

    // 用于给子控件留出间隔
    private class SpacesItemDecoration extends RecyclerView.ItemDecoration {

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int space = spacing / 2;
            boolean isRtl = ViewCompat.getLayoutDirection(view) == ViewCompat.LAYOUT_DIRECTION_RTL;
            int position = parent.getChildAdapterPosition(view);
            if (position == 0) {
                if (isRtl) {
                    outRect.right = paddingStart;
                    outRect.left = space;
                } else {
                    outRect.left = paddingStart;
                    outRect.right = space;
                }
            } else {
                boolean isLast = position == parent.getAdapter().getItemCount() - 1;
                if (isLast) {
                    if (isRtl) {
                        outRect.left = paddingEnd;
                        outRect.right = space;
                    } else {
                        outRect.right = paddingEnd;
                        outRect.left = space;
                    }
                } else {
                    outRect.left = space;
                    outRect.right = space;
                }
            }

            outRect.bottom = 0;
            outRect.top = 0;
        }
    }

    public HorizontalListView(Context context) {
        super(context);
        init(context, null, 0);
    }

    public HorizontalListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public HorizontalListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        setHasFixedSize(true);

        if (attrs != null) {
            TypedArray typeArray = context.obtainStyledAttributes(attrs, R.styleable.HorizontalListView, defStyle, 0);

            int n = typeArray.getIndexCount();
            for (int i = 0; i < n; i++) {
                int attr = typeArray.getIndex(i);
                if (attr == R.styleable.HorizontalListView_hl_spacing) {
                    spacing = typeArray.getDimensionPixelSize(
                            R.styleable.HorizontalListView_hl_spacing, DEFAULT_ITEM_SPACING);
                } else if (attr == R.styleable.HorizontalListView_hl_paddingStart) {
                    paddingStart = typeArray.getDimensionPixelSize(
                            R.styleable.HorizontalListView_hl_paddingStart, DEFAULT_ITEM_SPACING);
                } else if (attr == R.styleable.HorizontalListView_hl_paddingEnd) {
                    paddingEnd = typeArray.getDimensionPixelSize(
                            R.styleable.HorizontalListView_hl_paddingEnd, DEFAULT_ITEM_SPACING);
                }
            }

            typeArray.recycle();
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        setLayoutManager(layoutManager);
        addItemDecoration(new SpacesItemDecoration());
        horizontalSrollController = new HorizontalSrollController(this, SwipeRefreshLayout.class);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        horizontalSrollController.dispatchTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }
}
