package com.charlee.sns.activity;

import java.util.concurrent.Callable;


import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.charlee.sns.R;
import com.charlee.sns.exception.RequestFailedException;
import com.charlee.sns.helper.ErrorHandler;
import com.charlee.sns.helper.EventConstant;
import com.charlee.sns.helper.NavigationHelper;
import com.charlee.sns.model.CollectionObservableBase;
import com.charlee.sns.model.ICollectionObserver;
import com.charlee.sns.model.IObservable;
import com.charlee.sns.model.IObserver;
import com.charlee.sns.model.IPageableList;
import com.charlee.sns.model.ISnsModel;
import com.charlee.sns.model.MessageTag;
import com.charlee.sns.model.SnsCampaign;
import com.charlee.sns.model.SnsModel;
import com.charlee.sns.model.SnsUser;
import com.charlee.sns.model.UserMessage;
import com.charlee.sns.view.EmptyPlaceholderView;
import com.charlee.sns.view.FullMessageListView;
import com.charlee.sns.view.TopBarLayout;

import bolts.Continuation;
import bolts.Task;


/**
 * 消息详细列表页面.
 */
public class FullMessageListActivity extends Activity {
    private static final String TAG = "FullMessageListActivity";

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;

    // 错误信息显示
    private EmptyPlaceholderView placeholderView;

    private TopBarLayout topBar;
    private FullMessageListView messageListView;
    private boolean visible;

    private ISnsModel model;

    private static class SingleMessageList extends CollectionObservableBase implements IPageableList<UserMessage> {

        private final UserMessage userMessage;

        private IObserver itemObserver = new IObserver() {
            @Override
            public void update(final IObservable observable, Object data) {
                Task.call(new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {
                        UserMessage msg = (UserMessage) observable;
                        if (msg != null) {
                            setChanged();
                            notifyObservers(msg.isRemoved() ? ICollectionObserver.Action.RemoveItem :
                                    ICollectionObserver.Action.UpdateItem, msg, null);
                        }

                        return null;
                    }
                });
            }
        };

        public SingleMessageList(@NonNull UserMessage message) {
            userMessage = message;
            userMessage.addObserver(itemObserver);
        }

        @Nullable
        @Override
        public UserMessage get(int location) {
            return userMessage;
        }

        @Override
        public void add(UserMessage userMessage) {
        }

        @Override
        public boolean remove(UserMessage userMessage) {
            return false;
        }

        @Override
        public boolean filter(@Nullable UserMessage userMessage) {
            return false;
        }

        @Override
        public void clear() {

        }

        @Override
        public int indexOf(UserMessage userMessage) {
            return this.userMessage == userMessage ? 0 : -1;
        }

        @Override
        public boolean isEmpty() {
            return userMessage.isRemoved();
        }

        @Override
        public int size() {
            return userMessage.isRemoved() ? 0 : 1;
        }

        @Override
        public int getTotalSize() {
            return size();
        }

        @Override
        public Task<Boolean> refresh() throws Exception {
            return Task.forResult(false);
        }

        @Override
        public boolean hasNextPage() {
            return false;
        }

        @Override
        public Task<Boolean> loadNextPage() throws Exception {
            return Task.forResult(false);
        }

        @Override
        public String getLastId() {
            return "";
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_full_message_list);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        model = SnsModel.getInstance();

        visible = true;

        initTitleBar();

        placeholderView = (EmptyPlaceholderView) findViewById(R.id.empty_placeholder);

        messageListView = (FullMessageListView) findViewById(R.id.full_message_list);
        // Set up the user interaction to manually show or hide the system UI.
        messageListView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        messageListView.addOnScollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0 && visible) {
                    // 向下滑动
                    hide();
                } else if (dy < 0 && !visible) {
                    // 向上滑动
                    show();
                }
            }
        });

        handleIntent();

    }

    private void initTitleBar() {
        topBar = (TopBarLayout) findViewById(R.id.title_bar);

        topBar.setOnBackClickListener(new TopBarLayout.OnBackClickListener() {
            @Override
            public void onBack() {
                finish();
            }
        });

        topBar.setOnTitleClickListener(new TopBarLayout.OnTitleClickListener() {
            @Override
            public void onTitleClick() {
                // 点击标题栏回到页面顶部
                messageListView.scrollToPosition(0);
            }
        });

        View rightView = topBar.createButton(R.string.welcome);
        topBar.setRightView(rightView);
        rightView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavigationHelper.navigateToHomePage(FullMessageListActivity.this);
            }
        });
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        new Handler(getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if (messageListView != null) {
                    messageListView.onResume();
                }
            }
        }, 1000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        messageListView.onPause();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (messageListView != null) {
            messageListView.onRestart();
        }
    }

    private View.OnClickListener placeHolderActionClickListenerForUserMessage =
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FullMessageListActivity.this.onBackPressed();
                }
            };

    private void handleIntent() {
        NavigationHelper.NavUriParts navUri = NavigationHelper.getValidNavUri(FullMessageListActivity.this);
        if (navUri.pathNoLeadingSlash != null) {
            if (navUri.pathNoLeadingSlash.startsWith(NavigationHelper.PATH_SNS_HOT_MSGS_FULL)) {
                topBar.setTitle(R.string.title_hot_messages);
                messageListView.setMessageList(model.getHotMessages());
            } else if (navUri.pathNoLeadingSlash.startsWith(NavigationHelper.PATH_SNS_LATEST_MSGS_FULL)) {
                topBar.setTitle(R.string.title_latest_msg);
                messageListView.setMessageList(model.getLatestMessages());
            } else if (navUri.pathNoLeadingSlash.startsWith(NavigationHelper.PATH_SNS_USER_MSGS)) {
                onHandleUserMessages(navUri.uri);
                return;
            } else if (navUri.pathNoLeadingSlash.startsWith(NavigationHelper.PATH_SNS_TAG_MSGS)) {
                onHandleTagMessages(navUri.uri);
            } else if (navUri.pathNoLeadingSlash.startsWith(NavigationHelper.PATH_SNS_MESSAGE_DETAILS)) {
                onHandleMessageDetail(navUri.uri);
            } else if (navUri.pathNoLeadingSlash.startsWith(NavigationHelper.PATH_SNS_CAMPAIGN_MSGS)) {
                onHandleCampaignMessages(navUri.uri);
            }

            String posStr = navUri.uri.getQueryParameter(NavigationHelper.QUERY_POS);
            if (posStr != null && !navUri.pathNoLeadingSlash.startsWith(NavigationHelper.PATH_SNS_USER_MSGS)) {
                try {
                    int pos = Integer.parseInt(posStr);
                    messageListView.scrollToPosition(pos);
                } catch (NumberFormatException e) {
                    // 忽略异常
                }
            }
        }
    }

    private void toggle() {
        if (visible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        visible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    private void onHandleUserMessages(final Uri uri) {
        EmptyPlaceholderView.PlaceHolder placeHolder = new EmptyPlaceholderView.PlaceHolder(
                R.string.user_detail_empty_view_no_pictures,
                R.drawable.ic_no_photo,
                R.string.action_back_to_user_center_page,
                0,
                placeHolderActionClickListenerForUserMessage);
        messageListView.setEmptyListPlaceHolder(placeHolder);
        model.getUserByNavUri(uri).continueWith(new Continuation<SnsUser, Object>() {
            @Override
            public Object then(Task<SnsUser> task) throws Exception {
                if (!task.isFaulted()) {
                    SnsUser user = task.getResult();
                    topBar.setTitle(user.getNickName());
                    messageListView.setMessageList(user.getMessages());
                    String posStr = uri.getQueryParameter(NavigationHelper.QUERY_POS);
                    if (!TextUtils.isEmpty(posStr)) {
                        final String posFinal = posStr;
                        try {
                            int pos = Integer.parseInt(posFinal);
                            messageListView.scrollToPosition(pos);
                        } catch (NumberFormatException e) {
                            // 忽略异常
                        }
                    } else {
                        messageListView.scrollToPosition(0);
                    }
                } else {
                    onErrorHandle(task.getError());
                }
                return null;
            }
        }, Task.UI_THREAD_EXECUTOR);
    }

    private void onHandleTagMessages(Uri uri) {
        model.getTagByNavUri(uri).continueWith(new Continuation<MessageTag, Object>() {
            @Override
            public Object then(Task<MessageTag> task) throws Exception {
                if (!task.isFaulted()) {
                    MessageTag tag = task.getResult();
                    if (tag != null) {
                        topBar.setTitle(tag.getName());
                        messageListView.setMessageList(tag.getMessages());
                    }
                } else {
                    onErrorHandle(task.getError());
                }
                return null;
            }
        }, Task.UI_THREAD_EXECUTOR);
    }

    private void onHandleMessageDetail(Uri uri) {
        model.getMessageByNavUri(uri, true).continueWith(new Continuation<UserMessage, Object>() {
            @Override
            public Object then(Task<UserMessage> task) throws Exception {
                if (!task.isFaulted()) {
                    UserMessage userMessage = task.getResult();
                    if (userMessage != null
                            && userMessage.getPublisher() != null
                            && !TextUtils.isEmpty(userMessage.getPublisher().getNickName())) {
                        topBar.setTitle(userMessage.getPublisher().getNickName());
                    } else {
                        topBar.setTitle(R.string.title_hot_messages);
                    }
                    messageListView.setMessageList(new SingleMessageList(task.getResult()));
                    messageListView.setGoGackWhenListEmpty();
                } else {
                    onErrorHandle(task.getError());
                }
                return null;
            }
        }, Task.UI_THREAD_EXECUTOR);
    }

    private void onHandleCampaignMessages(Uri uri) {
        model.getCampaignByNavUri(uri, false).continueWith(new Continuation<SnsCampaign, Object>() {
            @Override
            public Object then(Task<SnsCampaign> task) throws Exception {
                if (!task.isFaulted()) {
                    final SnsCampaign campaign = task.getResult();
                    if (campaign != null) {
                        messageListView.setMessageList(campaign.getMessages());
                        messageListView.setGoGackWhenListEmpty();
                        topBar.setTitle(campaign.getTitle());
                    }
                }
                return null;
            }
        }, Task.UI_THREAD_EXECUTOR);
    }

    private void onErrorHandle(Exception e) {
        messageListView.setVisibility(View.GONE);
        placeholderView.setVisibility(View.VISIBLE);
        if (e instanceof RequestFailedException) {
            RequestFailedException requestFailedException = (RequestFailedException) e;
            int errCode = requestFailedException.getErrCode();
            if (errCode == RequestFailedException.ERR_NOT_FOUND) {
                placeholderView.setHintImage(R.drawable.ic_no_photo);
                placeholderView.setHintString(R.string.hint_message_removed);
                placeholderView.setActionString(R.string.action_go_back);
                placeholderView.setActionClickedListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                onBackPressed();
                            }
                        }
                );
                return;
            }
        }

        ErrorHandler.handleExceptionWithPlaceholder(FullMessageListActivity.this, e, placeholderView, TAG,
                new ErrorHandler.Callback() {
                    @Override
                    public void onEvent() {
                        handleIntent();
                    }
                });
    }

    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            messageListView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };

    @SuppressLint("InlinedApi")
    private void show() {
        // 显示导航栏（但不显示状态栏）
        messageListView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        visible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
        }
    };

    private final Handler mHideHandler = new Handler();
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
}
