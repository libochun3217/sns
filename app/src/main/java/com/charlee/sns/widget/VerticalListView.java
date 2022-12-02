package com.charlee.sns.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.charlee.sns.R;


/**
 * 垂直滑动列表控件
 */
public class VerticalListView extends RecyclerView {

    public static final int DEFAULT_ITEM_SPACING = 10;

    private int spacingHorizontal = DEFAULT_ITEM_SPACING;
    private int spacingVertical = DEFAULT_ITEM_SPACING;
    private LinearLayoutManager layoutManager;

    // 用于给子控件留出间隔
    private class SpacesItemDecoration extends ItemDecoration {
        private final int spaceHorizontal;
        private final int spaceVertical;

        public SpacesItemDecoration(int spaceHorizontal, int spaceVertical) {
            this.spaceHorizontal = spaceHorizontal;
            this.spaceVertical = spaceVertical / 2;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, State state) {
            outRect.left = spaceHorizontal;
            outRect.right = spaceHorizontal;
            outRect.bottom = spaceVertical;
            outRect.top = spaceVertical;
        }
    }

    public VerticalListView(Context context) {
        super(context);
        init(context, null, 0);
    }

    public VerticalListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public VerticalListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        setHasFixedSize(true);

        if (attrs != null) {
            TypedArray typeArray = context.obtainStyledAttributes(attrs, R.styleable.VerticalListView, defStyle, 0);

            int n = typeArray.getIndexCount();
            for (int i = 0; i < n; i++) {
                int attr = typeArray.getIndex(i);
                if (attr == R.styleable.VerticalListView_vl_spacingHorizontal) {
                    spacingHorizontal = typeArray.getDimensionPixelSize(
                            R.styleable.VerticalListView_vl_spacingHorizontal, DEFAULT_ITEM_SPACING);
                } else if (attr == R.styleable.VerticalListView_vl_spacingVertical) {
                    spacingVertical = typeArray.getDimensionPixelSize(
                            R.styleable.VerticalListView_vl_spacingVertical, DEFAULT_ITEM_SPACING);
                }
            }

            typeArray.recycle();
        }

        layoutManager = new LinearLayoutManager(context);
        setLayoutManager(layoutManager);
        addItemDecoration(new SpacesItemDecoration(spacingHorizontal, spacingVertical));
    }
}
