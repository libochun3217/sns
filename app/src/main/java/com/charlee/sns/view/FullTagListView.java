package com.charlee.sns.view;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.charlee.sns.R;
import com.charlee.sns.adapter.FullTagListAdapter;
import com.charlee.sns.helper.ErrorHandler;
import com.charlee.sns.model.IPageableList;
import com.charlee.sns.model.MessageTag;
import com.charlee.sns.widget.SwipeRefreshLayoutEx;
import com.charlee.sns.widget.VerticalListView;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;

import java.io.IOException;

import bolts.Continuation;
import bolts.Task;

/**
 * TAG标签页的自定义视图
 */
public class FullTagListView extends FrameLayout {

    // 错误信息显示
    private EmptyPlaceholderView placeholderView;

    // 下拉刷新/上拉加载下一页的控件
    private SwipeRefreshLayoutEx swipeRefreshLayout;

    // TAG列表控件
    private VerticalListView tagListView;

    private FullTagListAdapter tagListAdapter;

    public FullTagListView(Context context) {
        super(context);
        init(null, 0);
    }

    public FullTagListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public void setTagList(IPageableList<MessageTag> tagList) {
        tagListAdapter = new FullTagListAdapter(tagList);
        tagListView.setAdapter(tagListAdapter);

        if (tagList.isEmpty()) {
            swipeRefreshLayout.setRefreshing(true);
            onRefresh();
        }

    }

    private void init(AttributeSet attrs, int defStyle) {
        View.inflate(getContext(), R.layout.view_full_tag_list, this);

        placeholderView = (EmptyPlaceholderView) findViewById(R.id.empty_placeholder);

        swipeRefreshLayout = (SwipeRefreshLayoutEx) findViewById(R.id.swipe_refresh_layout);

        tagListView = (VerticalListView) findViewById(R.id.message_list);

        swipeRefreshLayout.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(SwipyRefreshLayoutDirection direction) {
                if (direction == SwipyRefreshLayoutDirection.TOP) {
                    FullTagListView.this.onRefresh();
                } else {
                    tagListAdapter.loadNextPage().continueWith(new Continuation<Boolean, Object>() {
                        @Override
                        public Object then(Task<Boolean> task) throws Exception {
                            swipeRefreshLayout.setRefreshing(false);
                            return null;
                        }
                    });
                }
            }
        });

    }

    private void onRefresh() {
        tagListAdapter.refresh().continueWith(new Continuation<Boolean, Object>() {
            @Override
            public Object then(Task<Boolean> task) throws Exception {
                if (task.isFaulted()) {
                    placeholderView.setVisibility(VISIBLE);
                    tagListView.setVisibility(INVISIBLE);
                    ErrorHandler.handleExceptionWithPlaceholder((Activity) getContext(),
                            task.getError(), placeholderView, "",
                            new ErrorHandler.Callback() {
                                @Override
                                public void onEvent() {
                                    onRefresh();
                                }
                            });
                } else {
                    placeholderView.setVisibility(GONE);
                    tagListView.setVisibility(VISIBLE);
                    tagListView.scrollToPosition(0);
                }

                swipeRefreshLayout.setRefreshing(false);
                return null;
            }
        }, Task.UI_THREAD_EXECUTOR);
    }

    public void scrollToPosition(int i) {
        tagListView.scrollToPosition(i);
    }
}
