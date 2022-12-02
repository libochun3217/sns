package com.charlee.sns.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;

import com.charlee.sns.R;
import com.charlee.sns.helper.EventConstant;
import com.charlee.sns.helper.NavigationHelper;
import com.charlee.sns.model.ISnsModel;
import com.charlee.sns.model.SnsModel;
import com.charlee.sns.model.UserMessage;
import com.charlee.sns.view.MessageCommentView;
import com.charlee.sns.view.TopBarLayout;

import bolts.Continuation;
import bolts.Task;


/**
 * 消息评论页
 */
public class MessageCommentActivity extends Activity {

    private ISnsModel model;

    private TopBarLayout topBar;

    private MessageCommentView commentListView;

    private boolean forceRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_message_comment_list);

        model = SnsModel.getInstance();

        initViews();

        handleIntent();
    }

    private void initViews() {
        initTopBar();

        commentListView = (MessageCommentView) findViewById(R.id.photo_comment_list);
        commentListView.setLoginBehavior(new MessageCommentView.ILoginBehavior() {
            @Override
            public boolean isLogged() {
                return model.isUserLoggedIn();
            }

            @Override
            public void onLogin() {
                NavigationHelper.navigateToLoginPageForResult(MessageCommentActivity.this);
            }
        });

    }

    private void initTopBar() {
        topBar = (TopBarLayout) findViewById(R.id.title_bar);
        topBar.setTitle(R.string.message_comment);

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
                commentListView.scrollToPosition(0);
            }
        });
    }

    private void handleIntent() {
        forceRefresh = getIntent().getBooleanExtra("extra_from_notification", false);

        final NavigationHelper.NavUriParts navUri = NavigationHelper.getValidNavUri(MessageCommentActivity.this);
        if (navUri.pathNoLeadingSlash != null) {
            model.getMessageByNavUri(navUri.uri, false).continueWith(new Continuation<UserMessage, Object>() {
                @Override
                public Object then(Task<UserMessage> task) throws Exception {
                    if (!task.isFaulted()) {
                        UserMessage message = task.getResult();
                        if (message != null) {
                            if (navUri.pathNoLeadingSlash.startsWith(NavigationHelper.PATH_BROWSE_MESSAGE_COMMENT)) {
                                commentListView.setUserMessage(message, false, forceRefresh);
                            } else if (navUri.pathNoLeadingSlash.startsWith(
                                    NavigationHelper.PATH_ADD_MESSAGE_COMMENT)) {
                                commentListView.setUserMessage(message, true, forceRefresh);
                                if (model.isUserLoggedIn() == false) {
                                    NavigationHelper.navigateToLoginPageForResult(MessageCommentActivity.this);
                                } else {
                                    showIMM();
                                }
                            }
                        }
                        setIntent(null);
                    }
                    return null;
                }
            }, Task.UI_THREAD_EXECUTOR);

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == NavigationHelper.LOGIN_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                commentListView.postComment();
            }
        }
    }

    private void showIMM() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
    }
}
