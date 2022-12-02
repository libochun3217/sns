package com.charlee.sns.view;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.charlee.sns.R;
import com.charlee.sns.adapter.FullMessageListAdapter;
import com.charlee.sns.helper.ErrorHandler;
import com.charlee.sns.model.IPageableList;
import com.charlee.sns.model.UserMessage;
import com.charlee.sns.widget.SwipeRefreshLayoutEx;
import com.charlee.sns.widget.VideoPlayerRecyclerView;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;

import bolts.Continuation;
import bolts.Task;

/**
 */
public class FullMessageListView extends FrameLayout {

    private static final String LOG_TAG = "FullMessageListView";

    // 错误信息显示
    private EmptyPlaceholderView placeholderView;
    private EmptyPlaceholderView.PlaceHolder emptyListPlaceHolder;

    // 下拉刷新/上拉加载下一页的控件
    private SwipeRefreshLayoutEx swipeRefreshLayout;

    // 消息列表控件
    private VideoPlayerRecyclerView messageListView;
    private SnsFloatingActionButton.IShowListener showListener;

    // 消息列表
    private IPageableList<UserMessage> messageList;

    private FullMessageListAdapter messageListAdapter;

    private boolean isRefreshing;
    private boolean shouldGoGackWhenListEmpty;

    private Continuation<Boolean, Object> refreshContinuation = new Continuation<Boolean, Object>() {
        @Override
        public Object then(Task<Boolean> task) throws Exception {
            isRefreshing = false;
            if (task.isFaulted()) {
                Exception exception = task.getError();
                if (messageList.isEmpty()) {
                    placeholderView.setVisibility(View.VISIBLE);
                    messageListView.setVisibility(View.GONE);
                    ErrorHandler.handleExceptionWithPlaceholder((Activity) getContext(),
                            exception, placeholderView, LOG_TAG, new ErrorHandler.Callback() {
                                @Override
                                public void onEvent() {
                                    refresh();
                                }
                            });
                } else {
                    ErrorHandler.showError(getContext(), exception, LOG_TAG);
                }
            } else {
                if (messageList.isEmpty()) {
                    if (emptyListPlaceHolder != null) {
                        placeholderView.setPlaceHolder(emptyListPlaceHolder);
                        messageListView.setVisibility(View.GONE);
                        placeholderView.setVisibility(View.VISIBLE);
                    }
                } else {
                    placeholderView.setVisibility(View.GONE);
                    messageListView.setVisibility(View.VISIBLE);
                    messageListView.scrollToPosition(0);
                }
            }

            swipeRefreshLayout.setRefreshing(false);
            return null;
        }
    };

    public FullMessageListView(Context context) {
        super(context);
        init(null, 0);
    }

    public FullMessageListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    public void setMessageList(@NonNull final IPageableList<UserMessage> messageList) {
        this.messageList = messageList;
        messageListAdapter = new FullMessageListAdapter(this, messageList);
        messageListView.setAdapter(messageListAdapter);
        messageListAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                if (!isRefreshing) {
                    if (messageList.isEmpty()) {
                        if (shouldGoGackWhenListEmpty) {
                            Activity activity = (Activity) getContext();
                            if (activity != null && !activity.isFinishing()) {
                                activity.onBackPressed();
                            }
                        } else if (emptyListPlaceHolder != null) {
                            placeholderView.setPlaceHolder(emptyListPlaceHolder);
                            messageListView.setVisibility(View.GONE);
                            placeholderView.setVisibility(View.VISIBLE);

                            if (showListener != null) {
                                showListener.onShow(true);
                            }
                        }
                    } else {
                        placeholderView.setVisibility(View.GONE);
                        messageListView.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        if (messageList.isEmpty()) {
            swipeRefreshLayout.setRefreshing(true);
            refresh(); // 加载第一页
        }
    }

    public void updateView(boolean noPublishedItem) {
        if (messageList.isEmpty() && noPublishedItem) {
            if (emptyListPlaceHolder != null) {
                placeholderView.setPlaceHolder(emptyListPlaceHolder);
                messageListView.setVisibility(View.GONE);
                placeholderView.setVisibility(View.VISIBLE);
            }
        } else {
            placeholderView.setVisibility(View.GONE);
            messageListView.setVisibility(View.VISIBLE);
        }
    }

    public void setEmptyListPlaceHolder(@Nullable EmptyPlaceholderView.PlaceHolder placeHolder) {
        this.emptyListPlaceHolder = placeHolder;
    }

    public void addOnScollListener(RecyclerView.OnScrollListener listener) {
        messageListView.addOnScrollListener(listener);
    }

    public void removeOnScollListener(RecyclerView.OnScrollListener listener) {
        messageListView.removeOnScrollListener(listener);
    }

    public void addFloatButtonShowListener(SnsFloatingActionButton.IShowListener listener) {
        showListener = listener;
    }

    public void scrollToPosition(int pos) {
        messageListView.scrollToPosition(pos);
    }

    /**
     * 设置当列表为空时调用GoBack
     */
    public void setGoGackWhenListEmpty() {
        shouldGoGackWhenListEmpty = true;
    }

    public void refresh() {
        if (isRefreshing || messageListAdapter == null) {
            return;
        }

        isRefreshing = true;
        messageListAdapter.refresh().continueWith(refreshContinuation, Task.UI_THREAD_EXECUTOR);
    }

    public void onResume() {
        if (messageListView != null) {
            messageListView.onStartPlayer();
        }
    }

    public void onPause() {
        if (messageListView != null) {
            messageListView.onPausePlayer();
        }
    }

    public void onRestart() {
        if (messageListView != null) {
            messageListView.onRestartPlayer();
        }
    }

    private void init(AttributeSet attrs, int defStyle) {
        View.inflate(getContext(), R.layout.view_full_message_list, this);

        placeholderView = (EmptyPlaceholderView) findViewById(R.id.empty_placeholder_full_message_list);

        swipeRefreshLayout = (SwipeRefreshLayoutEx) findViewById(R.id.swipe_refresh_layout);

        messageListView = (VideoPlayerRecyclerView) findViewById(R.id.message_list);
        messageListView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        swipeRefreshLayout.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(SwipyRefreshLayoutDirection direction) {
                if (direction == SwipyRefreshLayoutDirection.TOP) {
                    FullMessageListView.this.refresh();
                } else {
                    if (messageListAdapter != null) {
                        messageListAdapter.loadNextPage().continueWith(new Continuation<Boolean, Object>() {
                            @Override
                            public Object then(Task<Boolean> task) throws Exception {
                                swipeRefreshLayout.setRefreshing(false);
                                return null;
                            }
                        });
                    }
                }
            }
        });
        messageListView.addOnScrollListener(swipeRefreshLayout.getOnScrollListener());
    }

}
