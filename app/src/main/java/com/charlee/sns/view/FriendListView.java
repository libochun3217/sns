package com.charlee.sns.view;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.charlee.sns.R;
import com.charlee.sns.adapter.FriendListAdapter;
import com.charlee.sns.helper.ErrorHandler;
import com.charlee.sns.model.ICollectionObserver;
import com.charlee.sns.model.IObservable;
import com.charlee.sns.model.IPageableList;
import com.charlee.sns.model.SnsUser;
import com.charlee.sns.widget.SwipeRefreshLayoutEx;
import com.charlee.sns.widget.VerticalListView;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;

import java.util.List;

import bolts.Continuation;
import bolts.Task;

/**
 * TAG标签页的自定义视图
 */
public class FriendListView extends FrameLayout {

    // 错误信息显示
    private EmptyPlaceholderView placeholderView;

    // 下拉刷新/上拉加载下一页的控件
    private SwipeRefreshLayoutEx swipeRefreshLayout;

    // TAG列表控件
    private VerticalListView itemListView;

    private FriendListAdapter friendListAdapter;

    IPageableList<SnsUser> userList;

    private ICollectionObserver recommendObserver = new ICollectionObserver() {
        @Override
        public void update(IObservable<ICollectionObserver> observable,
                           Action action, Object item, List<Object> range) {
            IPageableList<SnsUser> userList = (IPageableList<SnsUser>) observable;
            if (userList != null) {
                if (userList.size() == 0) {
                    placeholderView.setVisibility(VISIBLE);
                    itemListView.setVisibility(INVISIBLE);
                    initPlaceHolderView();
                } else {
                    placeholderView.setVisibility(GONE);
                }
            }
        }
    };

    public FriendListView(Context context) {
        super(context);
        init(null, 0);
    }

    public FriendListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public void setUserList(IPageableList<SnsUser> userList) {
        this.userList = userList;
        this.userList.addObserver(recommendObserver);

        friendListAdapter = new FriendListAdapter(this.userList);
        itemListView.setAdapter(friendListAdapter);

        if (userList.isEmpty()) {
            swipeRefreshLayout.setRefreshing(true);
            onRefresh();
        }
    }

    private void init(AttributeSet attrs, int defStyle) {
        View.inflate(getContext(), R.layout.view_friend_list, this);

        placeholderView = (EmptyPlaceholderView) findViewById(R.id.empty_placeholder);

        swipeRefreshLayout = (SwipeRefreshLayoutEx) findViewById(R.id.swipe_refresh_layout);

        itemListView = (VerticalListView) findViewById(R.id.message_list);

        swipeRefreshLayout.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(SwipyRefreshLayoutDirection direction) {
                if (direction == SwipyRefreshLayoutDirection.TOP) {
                    FriendListView.this.onRefresh();
                } else {
                    friendListAdapter.loadNextPage().continueWith(new Continuation<Boolean, Object>() {
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

    private void initPlaceHolderView() {
        placeholderView.setHintImage(R.drawable.ic_no_recommend);
        placeholderView.setHintString(R.string.no_users_hint);
        placeholderView.setActionButtonVisibility(true);
        placeholderView.setActionString(R.string.action_refresh);
        placeholderView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (friendListAdapter != null) {
                    onRefresh();
                }
            }
        });
    }

    private void onRefresh() {
        friendListAdapter.refresh().continueWith(new Continuation<Boolean, Object>() {
            @Override
            public Object then(Task<Boolean> task) throws Exception {
                if (task.isFaulted()) {
                    placeholderView.setVisibility(VISIBLE);
                    itemListView.setVisibility(INVISIBLE);
                    ErrorHandler.handleExceptionWithPlaceholder((Activity) getContext(),
                            task.getError(), placeholderView, "",
                            new ErrorHandler.Callback() {
                                @Override
                                public void onEvent() {
                                    onRefresh();
                                }
                            });
                } else {
                    if (friendListAdapter.getItemCount() > 0) {
                        placeholderView.setVisibility(GONE);
                        itemListView.setVisibility(VISIBLE);
                        itemListView.scrollToPosition(0);
                    }
                }

                swipeRefreshLayout.setRefreshing(false);
                return null;
            }
        }, Task.UI_THREAD_EXECUTOR);
    }

    public void scrollToPosition(int i) {
        itemListView.scrollToPosition(i);
    }
}
