package com.charlee.sns.view;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.charlee.sns.R;
import com.charlee.sns.adapter.CardListAdapter;
import com.charlee.sns.adapter.HeaderedListAdapter;
import com.charlee.sns.helper.ErrorHandler;
import com.charlee.sns.helper.NavigationHelper;
import com.charlee.sns.model.Card;
import com.charlee.sns.model.IPageableList;
import com.charlee.sns.widget.SwipeRefreshLayoutEx;
import com.charlee.sns.widget.VideoPlayerRecyclerView;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;

import bolts.Continuation;
import bolts.Task;

/**
 */
public class CardListView extends FrameLayout implements HeaderedListAdapter.HeaderViewListener {

    private static final String LOG_TAG = "CardListView";

    // 错误信息显示
    private EmptyPlaceholderView placeholderView;
    private EmptyPlaceholderView.PlaceHolder emptyListPlaceHolder;

    // 下拉刷新/上拉加载下一页的控件
    private SwipeRefreshLayoutEx swipeRefreshLayout;

    // 卡片列表控件
    private VideoPlayerRecyclerView recyclerView;
    private SnsFloatingActionButton.IShowListener showListener;

    // 卡片列表
    private IPageableList<Card> cardList;

    private CardListAdapter cardListAdapter;

    private boolean isRefreshing;

    private Continuation<Boolean, Object> refreshContinuation = new Continuation<Boolean, Object>() {
        @Override
        public Object then(Task<Boolean> task) throws Exception {
            isRefreshing = false;
            if (task.isFaulted()) {
                Exception exception = task.getError();
                if (cardList.isEmpty()) {
                    placeholderView.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
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
                if (cardList.isEmpty()) {
                    if (emptyListPlaceHolder != null) {
                        placeholderView.setPlaceHolder(emptyListPlaceHolder);
                        recyclerView.setVisibility(View.GONE);
                        placeholderView.setVisibility(View.VISIBLE);
                    }
                } else {
                    placeholderView.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    recyclerView.scrollToPosition(0);
                }
            }

            swipeRefreshLayout.setRefreshing(false);
            return null;
        }
    };

    public CardListView(Context context) {
        super(context);
        init(null, 0);
    }

    public CardListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public void setCardList(@NonNull final IPageableList<Card> cardList) {
        this.cardList = cardList;
        cardListAdapter = new CardListAdapter(cardList,
                R.layout.item_follow_header_view,
                this);
        recyclerView.setAdapter(cardListAdapter);
        cardListAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                if (!isRefreshing) {
                    if (cardList.isEmpty()) {
                        if (emptyListPlaceHolder != null) {
                            placeholderView.setPlaceHolder(emptyListPlaceHolder);
                            recyclerView.setVisibility(View.GONE);
                            placeholderView.setVisibility(View.VISIBLE);

                            if (showListener != null) {
                                showListener.onShow(true);
                            }
                        }
                    } else {
                        placeholderView.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        if (cardList.isEmpty()) {
            swipeRefreshLayout.setRefreshing(true);
            refresh(); // 加载第一页
        }
    }

    public void updateView(boolean noPublishedItem) {
        if (cardList.isEmpty() && noPublishedItem) {
            if (emptyListPlaceHolder != null) {
                placeholderView.setPlaceHolder(emptyListPlaceHolder);
                recyclerView.setVisibility(View.GONE);
                placeholderView.setVisibility(View.VISIBLE);
            }
        } else {
            placeholderView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    public void setEmptyListPlaceHolder(@Nullable EmptyPlaceholderView.PlaceHolder placeHolder) {
        this.emptyListPlaceHolder = placeHolder;
    }

    public void addOnScollListener(RecyclerView.OnScrollListener listener) {
        recyclerView.addOnScrollListener(listener);
    }

    public void removeOnScollListener(RecyclerView.OnScrollListener listener) {
        recyclerView.removeOnScrollListener(listener);
    }

    public void addFloatButtonShowListener(SnsFloatingActionButton.IShowListener listener) {
        showListener = listener;
    }

    public void scrollToPosition(int pos) {
        recyclerView.scrollToPosition(pos);
    }

    public void refresh() {
        if (isRefreshing || cardListAdapter == null) {
            return;
        }

        isRefreshing = true;
        cardListAdapter.refresh().continueWith(refreshContinuation, Task.UI_THREAD_EXECUTOR);
    }

    private void init(AttributeSet attrs, int defStyle) {
        View.inflate(getContext(), R.layout.view_card_list, this);

        placeholderView = (EmptyPlaceholderView) findViewById(R.id.empty_placeholder_full_message_list);

        swipeRefreshLayout = (SwipeRefreshLayoutEx) findViewById(R.id.swipe_refresh_layout);

        recyclerView = (VideoPlayerRecyclerView) findViewById(R.id.message_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        swipeRefreshLayout.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(SwipyRefreshLayoutDirection direction) {
                if (direction == SwipyRefreshLayoutDirection.TOP) {
                    CardListView.this.refresh();
                } else {
                    if (cardListAdapter != null) {
                        cardListAdapter.loadNextPage().continueWith(new Continuation<Boolean, Object>() {
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
        recyclerView.addOnScrollListener(swipeRefreshLayout.getOnScrollListener());
    }

    public void onResume() {
        if (recyclerView != null) {
            recyclerView.onStartPlayer();
        }
    }

    public void onPause() {
        if (recyclerView != null) {
            recyclerView.onPausePlayer();
        }
    }

    @Override
    public void onHeaderViewCreated(final View header) {
        if (header == null) {
            return;
        }
        header.findViewById(R.id.btn_switch).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                NavigationHelper.navigateToPath(header.getContext(), NavigationHelper.PATH_FIND_FRIENDS, null);
            }
        });
    }
}
