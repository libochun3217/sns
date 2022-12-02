package com.charlee.sns.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import com.charlee.sns.BuildConfig;
import com.charlee.sns.R;
import com.charlee.sns.adapter.SimpleMessageListAdapter;
import com.charlee.sns.adapter.SimpleMessageViewHolder;
import com.charlee.sns.data.Video;
import com.charlee.sns.helper.ErrorHandler;
import com.charlee.sns.helper.NavigationHelper;
import com.charlee.sns.helper.ReportHelper;
import com.charlee.sns.model.ISnsModel;
import com.charlee.sns.model.SnsModel;
import com.charlee.sns.model.SnsVideo;
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
import bolts.Continuation;
import bolts.Task;

/**
 * 广场页面视图类
 */
public class PublicSquareView extends FrameLayout {

    private static final String LOG_TAG = "PublicSquareView";

    private ISnsModel model;

    // 占位错误提示
    private View placeholderRoot;
    private EmptyPlaceholderView emptyPlaceholderView;

    // 消息列表瀑布流
    private StaggeredListView messageListView;
    private SimpleMessageListAdapter messageListAdapter;

    // 把轮播图、热门标签列表作为消息列表的表头
    private PublicSquareHeaderView headerView;

    // 下拉刷新/上拉加载下一页的控件
    private SwipeRefreshLayoutEx swipeRefreshLayout;

    // 右下方按钮菜单控件
    private View bottomActionButton;
    private View bottomActionForegroundButton;
    private SnsFloatingActionButton bottomMenu;

    private Context context;

    public PublicSquareView(Context context) {
        super(context);
        this.context = context;
        init(null, 0);
    }

    public PublicSquareView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init(attrs, 0);
    }

    public PublicSquareView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        init(attrs, defStyle);
    }

    public void refresh() {
        swipeRefreshLayout.setRefreshing(true);
        if (isAllContentEmpty()) {
            placeholderRoot.setVisibility(View.VISIBLE);
            messageListView.setVisibility(View.GONE);
            bottomActionButton.setVisibility(View.GONE);
        }

        Collection<Task<?>> tasks = new ArrayList<>();
        if (getHeaderView() != null) {
            tasks.add(getHeaderView().refresh());
        }

        tasks.add(messageListAdapter.refresh());

        Task.whenAll(tasks).continueWith(new Continuation<Void, Object>() {
            @Override
            public Object then(Task<Void> task) throws Exception {
                if (bottomMenu.isOpen()) {
                    bottomMenu.close(true);
                }

                if (!task.isFaulted()) {
                    messageListView.scrollToPosition(0);
                    messageListView.setVisibility(View.VISIBLE);
                    bottomActionButton.setVisibility(View.VISIBLE);
                    placeholderRoot.setVisibility(View.GONE);
                } else {
                    // 只有所有列表（Header里面的热门标签、最新图片，以及热门图片）都为空才显示错误占位界面
                    Exception exception = task.getError();
                    if (isAllContentEmpty()) {
                        messageListView.setVisibility(View.GONE);
                        bottomActionButton.setVisibility(View.GONE);
                        placeholderRoot.setVisibility(View.VISIBLE);
                        ErrorHandler.handleExceptionWithPlaceholder((Activity) getContext(),
                                exception, emptyPlaceholderView, LOG_TAG, new ErrorHandler.Callback() {
                                    @Override
                                    public void onEvent() {
                                        refresh();
                                    }
                                });
                    } else {
                        ErrorHandler.showError(getContext(), exception, LOG_TAG);
                    }
                }

                swipeRefreshLayout.setRefreshing(false);
                return null;
            }
        }, Task.UI_THREAD_EXECUTOR);
    }

    public void scrollToPosition(int i) {
        messageListView.scrollToPosition(i);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    private void init(AttributeSet attrs, int defStyle) {
        View.inflate(getContext(), R.layout.view_public_square, this);
        initWidgets();
    }

    private void initWidgets() {
        model = SnsModel.getInstance();

        placeholderRoot = findViewById(R.id.public_square_placeholder);
        emptyPlaceholderView = (EmptyPlaceholderView) findViewById(R.id.public_square_empty_placeholder);

        swipeRefreshLayout = (SwipeRefreshLayoutEx) findViewById(R.id.swipe_refresh_layout);

        messageListView = (StaggeredListView) findViewById(R.id.staggered_message_list);
        messageListAdapter = new SimpleMessageListAdapter(
                model.getHotMessages(),
                R.layout.item_public_square_header,
                null,
                new SimpleMessageViewHolder.IImageClickedCallback() {
                    @Override
                    public void onClick(View view, int index, UserMessage item) {
                        // 点击热门消息跳转至消息详情页并定位到对应图片
                        HashMap<String, String> queries = new HashMap<>();
                        queries.put(NavigationHelper.QUERY_POS, String.valueOf(index));
                        NavigationHelper.navigateToPath(getContext(), NavigationHelper.PATH_SNS_HOT_MSGS_FULL, queries);

                        // add video for test
                        if (BuildConfig.DEBUG) {
                            item.setVideo(new SnsVideo(new Video("http://html5demos.com/assets/dizzy.mp4",
                                    item.getImage().getUrl(),
                                    "http://devimages.apple.com/samplecode/adDemo/ad.m3u8",
                                    item.getImage().getWidth(), item.getImage().getHeight())));
                        }

                        ReportHelper.clickMessageImage(getContext(), item.getId(), ReportHelper.MessageScene.community);
                    }
                },
                ReportHelper.MessageScene.community);
        messageListView.setAdapter(messageListAdapter);

        swipeRefreshLayout.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(SwipyRefreshLayoutDirection direction) {
                if (direction == SwipyRefreshLayoutDirection.TOP) {
                    PublicSquareView.this.refresh();
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
        messageListView.addOnScrollListener(swipeRefreshLayout.getOnScrollListener());

        bottomActionButton = findViewById(R.id.bottom_bar_action_button);
        bottomActionForegroundButton = findViewById(R.id.floating_action_foreground);
        bottomMenu = new SnsFloatingActionButton(getContext(),
                this, bottomActionButton, bottomActionForegroundButton);
        messageListView.addOnScrollListener(bottomMenu.getOnScrollListener());

        refresh(); // 获取数据
    }

    private PublicSquareHeaderView getHeaderView() {
        if (headerView == null) {
            View header = messageListView.getChildAt(0);
            if (header instanceof PublicSquareHeaderView) {
                headerView = (PublicSquareHeaderView) header;
            }
        }

        return headerView;
    }

    private boolean isAllContentEmpty() {
        PublicSquareHeaderView headerView = getHeaderView();
        boolean areAllHeaderListEmpty = headerView != null ? headerView.areAllListEmpty() : true;
        return areAllHeaderListEmpty && messageListAdapter.getDataItemCount() == 0;

    }
}
