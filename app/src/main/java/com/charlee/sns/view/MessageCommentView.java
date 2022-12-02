package com.charlee.sns.view;

import android.app.Activity;
import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.charlee.sns.R;
import com.charlee.sns.adapter.MessageCommentListAdapter;
import com.charlee.sns.adapter.OnRecycleViewItemClickListener;
import com.charlee.sns.helper.ErrorHandler;
import com.charlee.sns.helper.ReportHelper;
import com.charlee.sns.manager.SnsEnvController;
import com.charlee.sns.model.Comment;
import com.charlee.sns.model.UserMessage;
import com.charlee.sns.widget.SwipeRefreshLayoutEx;
import com.charlee.sns.widget.VerticalListView;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;

import bolts.Continuation;
import bolts.Task;

/**
 * 消息评论页的自定义视图
 */
public class MessageCommentView extends FrameLayout {
    private static final int MAX_LINES = 10;

    // 错误信息显示
    private EmptyPlaceholderView placeholderView;

    // 下拉刷新/上拉加载下一页的控件
    private SwipeRefreshLayoutEx swipeRefreshLayout;

    // 正在发布的评论列表
    private VerticalListView publishListView;
    private MessageCommentListAdapter publishCommentListAdapter;

    // 评论列表控件
    private VerticalListView commentListView;
    private MessageCommentListAdapter commentListAdapter;

    private EditText contentEditView;
    private Button publishButton;

    private UserMessage userMessage;
    private Comment fatherComment;

    private boolean hasShowKeyboard;

    private ILoginBehavior loginBehavior;

    public MessageCommentView(Context context) {
        super(context);
        init(null, 0);
    }

    public MessageCommentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public void setUserMessage(UserMessage userMessage, boolean isAddComment, boolean forceRefresh) {
        this.userMessage = userMessage;

        if (forceRefresh) {
            userMessage.getComments().clear();
        }

        publishCommentListAdapter = new MessageCommentListAdapter(userMessage.getPublishComments());
        publishCommentListAdapter.setUserMessage(userMessage);
        publishCommentListAdapter.setOnItemClickListener(onItemClickListener);
        publishListView.setAdapter(publishCommentListAdapter);

        commentListAdapter = new MessageCommentListAdapter(userMessage.getComments());
        commentListAdapter.setUserMessage(userMessage);
        commentListAdapter.setOnItemClickListener(onItemClickListener);
        commentListView.setAdapter(commentListAdapter);

        if (userMessage.getComments().isEmpty()
                || userMessage.getComments().size() < userMessage.getComments().getTotalSize()) {
            swipeRefreshLayout.setRefreshing(true);
            onRefresh();
        }

        if (isAddComment) {
            if (loginBehavior.isLogged()) {
                contentEditView.requestFocus();
                hasShowKeyboard = true;
            }
        }

    }

    public void setLoginBehavior(ILoginBehavior loginBehavior) {
        this.loginBehavior = loginBehavior;
    }

    public void postComment() {
        String content = contentEditView.getText().toString();
        if (!TextUtils.isEmpty(content)) {
            userMessage.postComment(content, fatherComment);
            fatherComment = null;
            hideIMM();
            contentEditView.getText().clear();
            contentEditView.setHint(R.string.edit_comment_hint);

            ReportHelper.publishComment(getContext(), userMessage.getId());
        }
    }

    private void init(AttributeSet attrs, int defStyle) {
        View.inflate(getContext(), R.layout.view_message_comment, this);

        placeholderView = (EmptyPlaceholderView) findViewById(R.id.empty_placeholder);

        swipeRefreshLayout = (SwipeRefreshLayoutEx) findViewById(R.id.swipe_refresh_layout);

        publishListView = (VerticalListView) findViewById(R.id.publish_list);
        publishListView.setHasFixedSize(false);

        commentListView = (VerticalListView) findViewById(R.id.comment_list);

        swipeRefreshLayout.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(SwipyRefreshLayoutDirection direction) {
                if (direction == SwipyRefreshLayoutDirection.TOP) {
                    MessageCommentView.this.onRefresh();
                } else {
                    commentListAdapter.loadNextPage().continueWith(new Continuation<Boolean, Object>() {
                        @Override
                        public Object then(Task<Boolean> task) throws Exception {
                            swipeRefreshLayout.setRefreshing(false);
                            return null;
                        }
                    });
                }
            }
        });

        initEditView();
        initPublishButton();

    }

    private void initEditView() {
        contentEditView = (EditText) findViewById(R.id.et_edit_content);
        contentEditView.addTextChangedListener(new TextWatcher() {
            private String preText;
            private int preCursorPos = 0;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                preText = s.toString();
                preCursorPos = contentEditView.getSelectionStart();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                publishButton.setEnabled(!TextUtils.isEmpty(s.toString()));
                checkTextLength(s.toString());

                // 添加行数限制
                contentEditView.removeTextChangedListener(this);
                if (contentEditView.getLineCount() > MAX_LINES) {
                    contentEditView.setText(preText);
                    contentEditView.setSelection(preCursorPos);
                }
                contentEditView.addTextChangedListener(this);
            }
        });

    }

    private void initPublishButton() {
        publishButton = (Button) findViewById(R.id.btn_publish_comment);
        publishButton.setEnabled(false);
        publishButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (loginBehavior.isLogged()) {
                    postComment();
                } else {
                    loginBehavior.onLogin();
                }
            }
        });
    }

    private void checkTextLength(String content) {
        int length = getContext().getResources().getInteger(R.integer.text_max_length);
        if (content.length() >= length) {
            Toast.makeText(SnsEnvController.getInstance().getAppContext(),
                    "too long",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void onRefresh() {
        commentListAdapter.refresh().continueWith(new Continuation<Boolean, Object>() {
            @Override
            public Object then(Task<Boolean> task) throws Exception {
                if (task.isFaulted()) {
                    placeholderView.setVisibility(VISIBLE);
                    publishListView.setVisibility(GONE);
                    commentListView.setVisibility(GONE);
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
                    publishListView.setVisibility(VISIBLE);
                    commentListView.setVisibility(VISIBLE);
                    commentListView.scrollToPosition(0);
                }

                swipeRefreshLayout.setRefreshing(false);
                return null;
            }
        }, Task.UI_THREAD_EXECUTOR);
    }

    public void scrollToPosition(int i) {
        commentListView.scrollToPosition(i);
    }

    private OnRecycleViewItemClickListener onItemClickListener =
            new OnRecycleViewItemClickListener() {
                @Override
                public void onItemClick(Object param) {
                    if (param == null) {
                        return;
                    }
                    contentEditView.requestFocus();

                    showIMM();

                    if (param instanceof Comment) {
                        fatherComment = (Comment) param;
                        // 回复评论
                        contentEditView.setHint("@" + fatherComment.getUser().getNickName());
                    }
                }

                @Override
                public boolean onIntercept() {
                    if (hasShowKeyboard) {
                        hideIMM();
                        return true;
                    } else {
                        return false;
                    }
                }
            };

    private void hideIMM() {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        boolean isOpen = imm.isActive();
        if (isOpen) {
            imm.hideSoftInputFromWindow(getWindowToken(), 0);
        }
        hasShowKeyboard = false;
    }

    private void showIMM() {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
        hasShowKeyboard = true;
    }

    public interface ILoginBehavior {
        boolean isLogged();

        void onLogin();
    }

}
