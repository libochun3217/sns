package com.charlee.sns.view;

import com.charlee.sns.R;
import com.charlee.sns.adapter.SimpleMessageListAdapter;
import com.charlee.sns.adapter.SimpleMessageViewHolder;
import com.charlee.sns.helper.ErrorHandler;
import com.charlee.sns.helper.NetworkMonitor;
import com.charlee.sns.helper.ReportHelper;
import com.charlee.sns.model.IPageableList;
import com.charlee.sns.model.UserMessage;
import com.charlee.sns.widget.StaggeredListView;
import com.charlee.sns.widget.SwipeRefreshLayoutEx;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import bolts.Continuation;
import bolts.Task;

/**
 * 瀑布流消息列表视图
 */
public class SimpleMessageListView extends FrameLayout {

    // 错误信息显示
    private EmptyPlaceholderView placeholderView;

    // 消息列表瀑布流
    private StaggeredListView messageListView;
    private SimpleMessageListAdapter messageListAdapter;

    // 下拉刷新/上拉加载下一页的控件
    private SwipeRefreshLayoutEx swipeRefreshLayout;

    // 右下方浮动按钮控件
    private View bottomActionButton;
    private View bottomActionForegroundButton;
    private SnsFloatingActionButton bottomMenu;

    public SimpleMessageListView(Context context) {
        super(context);
        init(null, 0);
    }

    public SimpleMessageListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public SimpleMessageListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    public void setMessagesAndCallback(@NonNull IPageableList<UserMessage> messageList,
                                       @NonNull SimpleMessageViewHolder.IImageClickedCallback  imageClickedCallback) {
        if (NetworkMonitor.getInstance(getContext()).isNetworkAvailable() == false) {
            bottomActionButton.setVisibility(View.GONE);
        }

        messageListAdapter = new SimpleMessageListAdapter(
                messageList, SimpleMessageListAdapter.HEADER_NONE, null,
                imageClickedCallback, ReportHelper.MessageScene.tag);
        messageListView.setAdapter(messageListAdapter);

        if (messageList.isEmpty()) {
            swipeRefreshLayout.setRefreshing(true);
            onRefresh();
        }

    }

    private void init(AttributeSet attrs, int defStyle) {
        View.inflate(getContext(), R.layout.view_simple_message_list, this);
        initWidgets();
    }

    private void initWidgets() {
        placeholderView = (EmptyPlaceholderView) findViewById(R.id.empty_placeholder);
        swipeRefreshLayout = (SwipeRefreshLayoutEx) findViewById(R.id.swipe_refresh_layout);
        messageListView = (StaggeredListView) findViewById(R.id.staggered_simple_message_list);
        swipeRefreshLayout.setCanChildScrollCallback(new SwipeRefreshLayoutEx.ICanChildScrollCallback() {
            @Override
            public boolean canChildScroll(int direction) {
                return messageListView.canScrollVertically(direction);
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(SwipyRefreshLayoutDirection direction) {
                if (direction == SwipyRefreshLayoutDirection.TOP) {
                    SimpleMessageListView.this.onRefresh();
                } else {
                    messageListAdapter.loadNextPage().continueWith(new Continuation<Boolean, Object>() {
                        @Override
                        public Object then(Task<Boolean> task) throws Exception {
                            swipeRefreshLayout.setRefreshing(false);
                            return null;
                        }
                    });
                }
            }
        });

        // 初始化浮动按钮
        bottomActionButton = findViewById(R.id.feeds_floating_action_button);
        bottomActionForegroundButton = findViewById(R.id.floating_action_foreground);
        bottomMenu = new SnsFloatingActionButton(getContext(), this,
                bottomActionButton, bottomActionForegroundButton);
        messageListView.addOnScrollListener(bottomMenu.getOnScrollListener());
    }

    private void onRefresh() {
        messageListAdapter.refresh().continueWith(new Continuation<Boolean, Object>() {
            @Override
            public Object then(Task<Boolean> task) throws Exception {
                if (bottomMenu.isOpen()) {
                    bottomMenu.close(true);
                }

                if (task.isFaulted()) {
                    placeholderView.setVisibility(VISIBLE);
                    bottomActionButton.setVisibility(View.GONE);
                    messageListView.setVisibility(GONE);
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
                    bottomActionButton.setVisibility(View.VISIBLE);
                    messageListView.setVisibility(VISIBLE);
                    messageListView.scrollToPosition(0);
                }
                swipeRefreshLayout.setRefreshing(false);
                return null;
            }
        }, Task.UI_THREAD_EXECUTOR);
    }

    public void scrollToPosition(int i) {
        messageListView.scrollToPosition(i);
    }
}
