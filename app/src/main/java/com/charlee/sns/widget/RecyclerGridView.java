package com.charlee.sns.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.charlee.sns.R;

/**
 * GridView控件
 */
public class RecyclerGridView extends RecyclerView {
    private static final int DEFAULT_SPAN_COUNT = 2;
    private static final int DEFAULT_ITEM_SPACING = 10;

    private int spacing = DEFAULT_ITEM_SPACING;
    private int spanCount = DEFAULT_SPAN_COUNT;
    private GridLayoutManager layoutManager;

    // 用于给子控件留出间隔
    private class SpacesItemDecoration extends ItemDecoration {
        private final int space;

        public SpacesItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, State state) {
            outRect.left = space;
            outRect.right = space;
            outRect.bottom = space;
            outRect.top = space;
        }
    }

    public RecyclerGridView(Context context) {
        super(context);
        init(context, null, 0);
    }

    public RecyclerGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public RecyclerGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        setHasFixedSize(true);

        if (attrs != null) {
            TypedArray typeArray = context.obtainStyledAttributes(attrs, R.styleable.RecyclerGridView, defStyle, 0);

            int n = typeArray.getIndexCount();
            for (int i = 0; i < n; i++) {
                int attr = typeArray.getIndex(i);
                if (attr == R.styleable.RecyclerGridView_gl_spanCount) {
                    spanCount = typeArray.getInt(R.styleable.RecyclerGridView_gl_spanCount, DEFAULT_SPAN_COUNT);
                } else if (attr == R.styleable.RecyclerGridView_gl_spacing) {
                    spacing = typeArray.getDimensionPixelSize(
                            R.styleable.RecyclerGridView_gl_spacing, DEFAULT_ITEM_SPACING);
                }
            }
            typeArray.recycle();
        }

        layoutManager = new AutoFitGridLayoutManager(context, spanCount);
        setLayoutManager(layoutManager);
        addItemDecoration(new SpacesItemDecoration(spacing));
    }

    class AutoFitGridLayoutManager extends GridLayoutManager {
        public AutoFitGridLayoutManager(Context context, int spanCount) {
            super(context, spanCount);
        }

        @Override
        public void onMeasure(Recycler recycler, State state, int widthSpec, int heightSpec) {
            super.onMeasure(recycler, state, widthSpec, heightSpec);
        }
    }


}
