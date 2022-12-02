package com.charlee.sns.activity;

import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.charlee.sns.R;
import com.charlee.sns.adapter.SimpleMessageViewHolder;
import com.charlee.sns.helper.NavigationHelper;
import com.charlee.sns.helper.SnsImageLoader;
import com.charlee.sns.model.Constants;
import com.charlee.sns.model.ISnsModel;
import com.charlee.sns.model.MessageTag;
import com.charlee.sns.model.SnsModel;
import com.charlee.sns.model.UserMessage;
import com.charlee.sns.view.SimpleMessageListView;
import com.charlee.sns.view.TopBarLayout;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import bolts.Continuation;
import bolts.Task;

/**
 * 简单消息列表页，用瀑布流展示消息
 */
public class SimpleMessageListActivity extends Activity {

    private ISnsModel model;

    private TopBarLayout topBar;

    private SimpleMessageListView messageListView;

    // 分享按钮所需参数
    private String shareUrl;
    private String imageUrlForShare;
    private String shareDesc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_simple_message_list);

        model = SnsModel.getInstance();

        initViews();

        handleIntent();
    }


    private void initViews() {
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

        messageListView = (SimpleMessageListView) findViewById(R.id.tag_message_list);
    }

    private void setTopBarRightView() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.top_bar_share_button, null);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareMessageTag();
            }
        });
        topBar.setRightView(view);
    }

    private void handleIntent() {
        NavigationHelper.NavUriParts navUri = NavigationHelper.getValidNavUri(SimpleMessageListActivity.this);
        if (navUri.pathNoLeadingSlash == null) {
            return;
        }

        if (navUri.pathNoLeadingSlash.startsWith(NavigationHelper.PATH_TAG_MESSAGE_LIST)) {
            showTagMessageList(navUri);
        } else if (navUri.pathNoLeadingSlash.startsWith(NavigationHelper.PATH_LATEST_MESSAGE_LIST)) {
            showLatestMessageList(navUri);
        }

        String posStr = navUri.uri.getQueryParameter(NavigationHelper.QUERY_POS);
        if (posStr != null) {
            try {
                int pos = Integer.parseInt(posStr);
                messageListView.scrollToPosition(pos);
            } catch (NumberFormatException e) {
                // 忽略异常
            }
        }
    }

    private void showTagMessageList(NavigationHelper.NavUriParts navUri) {
        model.getTagByNavUri(navUri.uri).continueWith(new Continuation<MessageTag, Object>() {
            @Override
            public Object then(Task<MessageTag> task) throws Exception {
                if (!task.isFaulted()) {
                    final MessageTag messageTag = task.getResult();
                    if (messageTag != null) {
                        SimpleMessageViewHolder.IImageClickedCallback itemClickCallback =
                                new SimpleMessageViewHolder.IImageClickedCallback() {
                                    @Override
                                    public void onClick(View view, int index, UserMessage item) {
                                        HashMap<String, String> queries = new HashMap<>();
                                        messageTag.getNavQuery(queries);
                                        queries.put(NavigationHelper.QUERY_POS, String.valueOf(index));
                                        NavigationHelper.navigateToPath(SimpleMessageListActivity.this,
                                                NavigationHelper.PATH_SNS_TAG_MSGS, queries);
                                    }
                                };
                        messageListView.setMessagesAndCallback(messageTag.getMessages(), itemClickCallback);
                        topBar.setTitle(messageTag.getName());

                        if (messageTag.canBeShared()) {
                            shareUrl = messageTag.getShareUrl();
                            imageUrlForShare = messageTag.getTagImage().getSquareUrlForShare();
                            shareDesc = messageTag.getName();
                            if (TextUtils.isEmpty(imageUrlForShare)) {
                                imageUrlForShare = messageTag.getTagImage().getSquareUrl(Constants.THUMB_SIZE);
                            }

                            SnsImageLoader.isImageInDiskCache(imageUrlForShare).continueWith(
                                    new Continuation<Boolean, Object>() {
                                        @Override
                                        public Object then(Task<Boolean> task) {
                                            if (!task.isFaulted() && Boolean.TRUE.equals(task.getResult())) {
                                                setTopBarRightView();
                                            } else {
                                                SnsImageLoader.preloadImage(SimpleMessageListActivity.this,
                                                        imageUrlForShare,
                                                        new SimpleImageLoadingListener() {
                                                            @Override
                                                            public void onLoadingComplete(String imageUri, View view,
                                                                                          Bitmap loadedImage) {
                                                                if (SimpleMessageListActivity.this != null) {
                                                                    Handler mainHandler = new Handler(getMainLooper());
                                                                    mainHandler.post(new Runnable() {
                                                                        @Override
                                                                        public void run() {
                                                                            setTopBarRightView();
                                                                        }
                                                                    });
                                                                }
                                                            }
                                                        });
                                            }

                                            return null;
                                        }
                                    }, Task.UI_THREAD_EXECUTOR);
                        }
                    }
                }
                return null;
            }
        }, Task.UI_THREAD_EXECUTOR);
    }

    private void showLatestMessageList(NavigationHelper.NavUriParts navUri) {
        topBar.setTitle(R.string.title_latest_msg);
        SimpleMessageViewHolder.IImageClickedCallback itemClickCallback =
                new SimpleMessageViewHolder.IImageClickedCallback() {
                    @Override
                    public void onClick(View view, int index, UserMessage item) {
                        HashMap<String, String> queries = new HashMap<>();
                        queries.put(NavigationHelper.QUERY_POS, String.valueOf(index));
                        NavigationHelper.navigateToPath(
                                SimpleMessageListActivity.this, NavigationHelper.PATH_SNS_LATEST_MSGS_FULL, queries);
                    }
                };
        messageListView.setMessagesAndCallback(model.getLatestMessages(), itemClickCallback);
    }

    private void shareMessageTag() {
        SnsImageLoader.createThumbnailForCachedImage(this, imageUrlForShare)
                .continueWith(new Continuation<Uri, Object>() {
                    @Override
                    public Object then(Task<Uri> task) throws Exception {
                        if (!task.isFaulted() && task.getResult() != null) {
                            NavigationHelper.navigateToShareUrlPage(SimpleMessageListActivity.this,
                                    shareUrl, task.getResult().toString(),
                                    getResources().getString(R.string.sns_share_title),
                                    shareDesc);
                        } else {
                            Toast toast = Toast.makeText(SimpleMessageListActivity.this,
                                    R.string.notification_share_failed, Toast.LENGTH_SHORT);
                            toast.show();
                        }
                        return null;
                    }
                }, Task.UI_THREAD_EXECUTOR);
    }

}
