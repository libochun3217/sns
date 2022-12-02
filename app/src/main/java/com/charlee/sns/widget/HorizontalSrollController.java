package com.charlee.sns.widget;

import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import androidx.core.view.MotionEventCompat;

/**
 * 帮助有横向滚动效果的View解决与ViewParent的滚动冲突
 * 不实现为OnTouchListener的原因是:
 * 一般需要解决这类问题的几乎是自定义View,而自定义View本身也可能需要被设置OnTouchListener
 */
public class HorizontalSrollController {
    private static final int INVALID_POINTER = -1;

    private double touchSlop;
    private int activePointerId;
    private float initialDownX;
    private float initialDownY;
    /**
     * Used to track if the parent vertically scrollable view has been told to DisallowInterceptTouchEvent
     */
    private boolean isParentVerticiallyScrollableViewDisallowingInterceptTouchEvent = false;
    private View view;
    private Class viewParentToDisallowingInterceptTouchEvent;

    public HorizontalSrollController(View view, Class viewParentToDisallowingInterceptTouchEvent) {
        this.view = view;
        this.viewParentToDisallowingInterceptTouchEvent = viewParentToDisallowingInterceptTouchEvent;
        ViewConfiguration vc = ViewConfiguration.get(view.getContext());
        touchSlop = vc.getScaledTouchSlop() * 2;
    }

    public void dispatchTouchEvent(MotionEvent ev) {
        final int action = MotionEventCompat.getActionMasked(ev);
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                activePointerId = MotionEventCompat.getPointerId(ev, 0);
                final float initialDownX = getMotionEventX(ev, activePointerId);
                final float initialDownY = getMotionEventY(ev, activePointerId);
                if (initialDownY == -1) {
                    return;
                }
                this.initialDownX = initialDownX;
                this.initialDownY = initialDownY;
                requestParentListViewToNotInterceptTouchEvents(true);
            }
                break;

            case MotionEvent.ACTION_MOVE: {
                if (activePointerId == INVALID_POINTER) {
                    return;
                }

                final float x = getMotionEventX(ev, activePointerId);
                final float y = getMotionEventY(ev, activePointerId);
                if (x == -1 || y == -1) {
                    return;
                }

                float xDiffOnDragAbs;
                float yDiffOnDragAbs;
                xDiffOnDragAbs = Math.abs(x - initialDownX);
                yDiffOnDragAbs = Math.abs(y - initialDownY);

                if (xDiffOnDragAbs < yDiffOnDragAbs && yDiffOnDragAbs > touchSlop) {
                    requestParentListViewToNotInterceptTouchEvents(false);
                }
            }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                requestParentListViewToNotInterceptTouchEvents(false);
            }
                break;

            default:
                break;
        }

        return;
    }

    private float getMotionEventX(MotionEvent ev, int activePointerId) {
        final int index = MotionEventCompat.findPointerIndex(ev, activePointerId);
        if (index < 0) {
            return -1;
        }
        return MotionEventCompat.getX(ev, index);
    }

    private float getMotionEventY(MotionEvent ev, int activePointerId) {
        final int index = MotionEventCompat.findPointerIndex(ev, activePointerId);
        if (index < 0) {
            return -1;
        }
        return MotionEventCompat.getY(ev, index);
    }

    /**
     * When this HorizontalListView is embedded within a vertical scrolling view it is important to disable the
     * parent view from interacting with
     * any touch events while the user is scrolling within this HorizontalListView. This will start at this view and
     * go up the view tree looking
     * for a vertical scrolling view. If one is found it will enable or disable parent touch interception.
     *
     * @param disallowIntercept If true the parent will be prevented from intercepting child touch events
     */
    private void requestParentListViewToNotInterceptTouchEvents(Boolean disallowIntercept) {
        // Prevent calling this more than once needlessly
        if (isParentVerticiallyScrollableViewDisallowingInterceptTouchEvent != disallowIntercept) {
            View view = this.view;

            while (view.getParent() instanceof View) {
                // If the parent is a ListView or ScrollView then disallow intercepting of touch events
                if (view.getParent().getClass().equals(viewParentToDisallowingInterceptTouchEvent)) {
                    view.getParent().requestDisallowInterceptTouchEvent(disallowIntercept);
                    isParentVerticiallyScrollableViewDisallowingInterceptTouchEvent = disallowIntercept;
                    return;
                }

                view = (View) view.getParent();
            }
        }
    }
}
