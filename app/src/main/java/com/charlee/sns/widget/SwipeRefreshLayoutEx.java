package com.charlee.sns.widget;

import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;

import android.content.Context;
import android.util.AttributeSet;

/**
 * 扩展SwipyRefreshLayout以便支持非ListView的内容
 */
public class SwipeRefreshLayoutEx extends SwipyRefreshLayout {

    private ICanChildScrollCallback canChildScrollCallback;

    /**
     * 回调接口，用于返回内容是否可以滚动的状态
     */
    public interface ICanChildScrollCallback {
        int DOWN = 1;
        int UP = -1;

        /**
         * 是否能够滚动
         * @param direction >0：向下滚动; < 0 向上滚动
         * @return          true: 可以滚动; false: 不能滚动
         */
        boolean canChildScroll(int direction);
    }

    public SwipeRefreshLayoutEx(Context context) {
        super(context);
    }

    public SwipeRefreshLayoutEx(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setCanChildScrollCallback(ICanChildScrollCallback callback) {
        canChildScrollCallback = callback;
    }

    @Override
    public boolean canChildScrollDown() {
        if (canChildScrollCallback != null) {
            return canChildScrollCallback.canChildScroll(ICanChildScrollCallback.DOWN);
        }

        return super.canChildScrollDown();
    }

    @Override
    public boolean canChildScrollUp() {
        if (canChildScrollCallback != null) {
            return canChildScrollCallback.canChildScroll(ICanChildScrollCallback.UP);
        }

        return super.canChildScrollUp();
    }
}
