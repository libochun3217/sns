package com.charlee.sns.adapter;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Rect;
import android.net.Uri;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.charlee.sns.R;
import com.charlee.sns.helper.ErrorHandler;
import com.charlee.sns.helper.NavigationHelper;
import com.charlee.sns.helper.ReportHelper;
import com.charlee.sns.helper.SectionReport;
import com.charlee.sns.helper.SnsImageLoader;
import com.charlee.sns.helper.TimeFormatter;
import com.charlee.sns.model.Card;
import com.charlee.sns.model.IObservable;
import com.charlee.sns.model.IObserver;
import com.charlee.sns.model.ModelBase;
import com.charlee.sns.model.SnsImage;
import com.charlee.sns.model.SnsModel;
import com.charlee.sns.model.SnsUser;
import com.charlee.sns.model.UserMessage;
import com.charlee.sns.view.TextWithNameAndTags;
import com.charlee.sns.widget.CustomContextMenu;
import com.charlee.sns.widget.VerticalListView;
import com.facebook.animated.gif.BuildConfig;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.concurrent.Callable;

import bolts.Continuation;
import bolts.Task;

/**
 * 消息完整详情的ViewHolder
 */
public class FullMessageViewHolder extends RecyclerView.ViewHolder implements CustomContextMenu.OnItemSelectedListener {

    private static final String LOG_TAG = "FullMessageViewHolder";
    private static final int MAX_SHARE_DESC_LENGTH = 30;

    private static int imageWidth = 0; // 图片宽度。前提：所有图片宽度相同。

    // 容器
    private final View container;
    private final View parentListView;
    private CustomContextMenu settingsMenu;
    private View.OnTouchListener containerOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    isImageTouched = false;
                    int[] containerCoords = new int[2];
                    container.getLocationOnScreen(containerCoords);
                    int x = (int) event.getRawX() - containerCoords[0];
                    int y = (int) event.getRawY() - containerCoords[1];
                    Rect imageRect = new Rect();
                    image.getHitRect(imageRect);
                    if (imageRect.contains(x, y)) {
                        isImageTouched = true;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    isImageTouched = false;
                    break;
                default:
                    break;
            }
            return false;
        }
    };

    // 发布者
    private ImageView imgUserAvatar;
    private ImageView imgOfficialIndicator;
    private TextView txtUserName;
    private TextView txtPublishTime;
    private ImageView btnFollow;
    private ImageView imgFollowing;

    // 内容
    private ImageView image;
    private TextView txtDescription;
    private boolean isImageLoaded;
    private boolean isImageTouched;

    // Video
    private ViewGroup videoContainer;
    private ImageView imgVideoCover;

    // ActionBar
    private ImageView btnLike;
    private TextView txtLikesNum;
    private ImageView btnAddComment;
    private ImageView btnShare;
    private ImageView btnMoreActions;
    private boolean isMoreActionsClicked;

    // 评论
    private VerticalListView listComments;
    private TextView btnAllComments;

    // 遮罩（删除时显示）
    private ViewGroup maskLayer;
    private ImageView imgProgressInMask;

    private Card card;
    private UserMessage userMessage;
    private SimpleCommentListAdapter commentListAdapter;

    private boolean isPostedByLoginUser;
    private boolean canBeShared;

    private Animation animation;

    private ModelBase loginEvent;

    private SnsUser publisher;
    private IObserver publisherObserver = new IObserver() {
        @Override
        public void update(final IObservable observable, final Object data) {
            Task.call(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    SnsUser publisher = (SnsUser) observable;
                    if (publisher != null && publisher == FullMessageViewHolder.this.publisher) {
                        showPublisher();
                    }

                    return null;
                }
            }, Task.UI_THREAD_EXECUTOR);
        }
    };

    private View.OnClickListener onUserClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (userMessage == null) {
                return;
            }
            if (publisher.isOfficial()) {
                return;
            }
            NavigationHelper.navigateToUserProfilePage(v.getContext(), userMessage.getPublisher());
        }
    };

    private IObserver loginToFollowObserver = new IObserver() {
        @Override
        public void update(final IObservable<IObserver> observable, final Object data) {
            Task.call(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    if ((Boolean) data) {
                        onFollowClickListener.onClick(btnFollow);
                    }
                    return null;
                }
            }, Task.UI_THREAD_EXECUTOR);
        }
    };

    private View.OnClickListener onFollowClickListener = new View.OnClickListener() {

        private void onFollowSucceeded() {
            btnFollow.setImageResource(R.drawable.selector_ic_followed);
            btnFollow.setBackgroundResource(R.drawable.selector_button_followed);
            txtPublishTime.setVisibility(View.GONE);
        }

        private void onUnFollowSucceeded() {
            btnFollow.setImageResource(R.drawable.selector_ic_to_follow);
            btnFollow.setBackgroundResource(R.drawable.selector_button_to_follow);
            txtPublishTime.setVisibility(View.GONE);
        }

        private void onError(Task task) {
            Exception exception = task.getError();
            Context context = btnFollow.getContext();
            if (!ErrorHandler.showError(context, exception, LOG_TAG)) {
                Toast toast = Toast.makeText(context,
                        R.string.notification_follow_failed, Toast.LENGTH_SHORT);
                toast.show();
            }
        }

        private void showProgressAnimation() {
            btnFollow.setVisibility(View.GONE);
            imgFollowing.setVisibility(View.VISIBLE);
            imgFollowing.startAnimation(animation);
        }

        private void stopProgressAnimation() {
            imgFollowing.clearAnimation();
            imgFollowing.setVisibility(View.GONE);
            btnFollow.setVisibility(View.VISIBLE);
        }

        @Override
        public void onClick(View v) {
            if (userMessage == null) {
                return;
            }

            if (!SnsModel.getInstance().isUserLoggedIn()) {
                loginEvent.addObserver(loginToFollowObserver);
            }

            if (checkLogin()) {
                showProgressAnimation();
                if (publisher.isFollowed()) {
                    publisher.unfollow().continueWith(new Continuation<Boolean, Object>() {
                        @Override
                        public Object then(Task<Boolean> task) throws Exception {
                            stopProgressAnimation();
                            if (!task.isFaulted()) {
                                onUnFollowSucceeded();
                            } else {
                                onError(task);
                            }

                            return null;
                        }
                    }, Task.UI_THREAD_EXECUTOR);
                } else {
                    publisher.follow().continueWith(new Continuation<Boolean, Object>() {
                        @Override
                        public Object then(Task<Boolean> task) throws Exception {
                            stopProgressAnimation();
                            if (!task.isFaulted()) {
                                onFollowSucceeded();
                                ReportHelper.followUser(container.getContext(), publisher.getId());
                            } else {
                                onError(task);
                            }
                            loginEvent.deleteObserver(loginToFollowObserver);
                            return null;
                        }
                    }, Task.UI_THREAD_EXECUTOR);
                }
            }
        }
    };

    private View.OnClickListener onLikeClickListener = new View.OnClickListener() {

        private Continuation<Boolean, Object> continuation = new Continuation<Boolean, Object>() {
            @Override
            public Object then(Task<Boolean> task) throws Exception {
                if (task.isFaulted()) {
                    // 恢复原来状态
                    resetLikeStatus(userMessage);
                    Exception exception = task.getError();
                    Context context = btnLike.getContext();
                    if (!ErrorHandler.showError(context, exception, LOG_TAG)) {
                        Toast toast = Toast.makeText(context,
                                R.string.notification_like_failed, Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }

                return null;
            }
        };

        @Override
        public void onClick(View v) {
            if (userMessage == null) {
                return;
            }

            if (checkLogin()) {
                if (userMessage.isLiked()) {
                    userMessage.removeLike();
                    txtLikesNum.setText(String.valueOf(userMessage.getLikeNum()));
                    btnLike.setImageResource(R.drawable.ic_like);
                } else {
                    userMessage.like();
                    txtLikesNum.setText(String.valueOf(userMessage.getLikeNum()));
                    btnLike.setImageResource(R.drawable.ic_like_pressed);
                }
                ReportHelper.clickLike(container.getContext(), userMessage.getId());
            }
        }
    };

    private View.OnClickListener onAddCommentClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (userMessage == null) {
                return;
            }

            HashMap<String, String> queries = new HashMap<>();
            userMessage.getNavQuery(queries);
            NavigationHelper.navigateToPath(v.getContext(), NavigationHelper.PATH_ADD_MESSAGE_COMMENT, queries);

            ReportHelper.showCommentPage(container.getContext(), userMessage.getId());
        }
    };

    private View.OnClickListener onShareMessageClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (userMessage == null) {
                return;
            }
            shareMessage();
        }
    };

    private View.OnClickListener onAllCommentClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (userMessage == null) {
                return;
            }

            HashMap<String, String> queries = new HashMap<>();
            userMessage.getNavQuery(queries);
            NavigationHelper.navigateToPath(v.getContext(), NavigationHelper.PATH_BROWSE_MESSAGE_COMMENT, queries);

            ReportHelper.showCommentPage(container.getContext(), userMessage.getId());
        }
    };

    private View.OnClickListener onMoreActionsClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (userMessage == null) {
                return;
            }

            Activity parentActivity = (Activity) btnMoreActions.getContext();
            if (parentActivity != null) {
                isMoreActionsClicked = true;
                showContextMenu(parentActivity, FullMessageViewHolder.this);
            }
        }
    };

    private View.OnClickListener onImageClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (userMessage == null) {
                return;
            }
            if (publisher.isOfficial()) {
                userMessage.onClick(container.getContext());
                return;
            }
        }
    };

    public FullMessageViewHolder(@NonNull final View container,
                                 @NonNull final View parentListView,
                                 int contentWidth) {
        super(container);
        this.container = container;
        this.parentListView = parentListView;

        container.setOnTouchListener(containerOnTouchListener);
        container.setOnClickListener(onImageClickListener);

        if (imageWidth == 0) {
            // 不要删除！检查图片宽度
            if (BuildConfig.DEBUG) {
                if (contentWidth <= 0) {
                    throw new InvalidParameterException("Incorrect contentWidth!");
                }
            }

            ViewGroup contentContainer = (ViewGroup) container.findViewById(R.id.content_container);
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) contentContainer.getLayoutParams();
            imageWidth = contentWidth;
        }

        imgUserAvatar = (ImageView) container.findViewById(R.id.img_user_avatar);
        imgOfficialIndicator = (ImageView) container.findViewById(R.id.img_official_indicator);
        txtUserName = (TextView) container.findViewById(R.id.txt_user_name);
        txtPublishTime = (TextView) container.findViewById(R.id.txt_publish_time);
        btnFollow = (ImageView) container.findViewById(R.id.img_follow);
        imgFollowing = (ImageView) container.findViewById(R.id.img_following);

        imgUserAvatar.setOnClickListener(onUserClickListener);
        txtUserName.setOnClickListener(onUserClickListener);
        btnFollow.setOnClickListener(onFollowClickListener);

        image = (ImageView) container.findViewById(R.id.image);
        image.setOnClickListener(onImageClickListener);

        videoContainer = (ViewGroup) container.findViewById(R.id.layout_video_container);
        imgVideoCover = (ImageView) container.findViewById(R.id.img_cover);

        txtDescription = (TextView) container.findViewById(R.id.txt_desc);

        btnLike = (ImageView) container.findViewById(R.id.img_like);
        txtLikesNum = (TextView) container.findViewById(R.id.txt_likes_num);
        btnAddComment = (ImageView) container.findViewById(R.id.img_add_comment);
        btnShare = (ImageView) container.findViewById(R.id.img_share);
        btnMoreActions = (ImageView) container.findViewById(R.id.img_more_actions);

        btnLike.setOnClickListener(onLikeClickListener);
        btnAddComment.setOnClickListener(onAddCommentClickListener);
        btnShare.setOnClickListener(onShareMessageClickListener);
        btnMoreActions.setOnClickListener(onMoreActionsClickListener);

        listComments = (VerticalListView) container.findViewById(R.id.list_comments);
        btnAllComments = (TextView) container.findViewById(R.id.txt_all_comments);

        btnAllComments.setOnClickListener(onAllCommentClickListener);

        maskLayer = (ViewGroup) container.findViewById(R.id.progress_mask);
        imgProgressInMask = (ImageView) container.findViewById(R.id.progress_icon);

        animation = AnimationUtils.loadAnimation(container.getContext(), R.anim.refresh);
        animation.setRepeatMode(Animation.RESTART);
        animation.setRepeatCount(Animation.INFINITE);

        loginEvent = SnsModel.getInstance().getLoginEvent();
    }

    public void bind(@NonNull final UserMessage item) {
        container.setTag(this);
        userMessage = item;

        // 检查是否由用户本人发布
        isPostedByLoginUser = item.getPublisher() == SnsModel.getInstance().getLoginUser();

        // 检查是否可以分享
        canBeShared = item.canBeShared();
        btnShare.setVisibility(canBeShared ? View.VISIBLE : View.GONE);
        btnShare.setEnabled(false);
        btnShare.setImageResource(R.drawable.ic_share_disable);

        // 发布者相关
        if (publisher != null) {
            publisher.deleteObserver(publisherObserver);
        }

        publisher = item.getPublisher();
        publisher.addObserver(publisherObserver);

        showPublisher();

        // 内容相关
        String description = item.getDescription();
        boolean hasDescription = !TextUtils.isEmpty(description);
        if (hasDescription) {
            TextWithNameAndTags textWithNameAndTags = TextWithNameAndTags.create(
                    txtDescription.getContext(), publisher, description, item.getTags());
            txtDescription.setText(textWithNameAndTags);
            txtDescription.setMovementMethod(LinkMovementMethod.getInstance());
            txtDescription.setVisibility(View.VISIBLE);
        } else {
            txtDescription.setVisibility(View.GONE);
        }

        final ImageView imageView;
        SnsImage imageModel;
        if (userMessage.getVideo() == null) {
            imageView = image;
            imageModel = userMessage.getImage();
            imgVideoCover.setVisibility(View.GONE);
        } else {
            imageView = imgVideoCover;
            imageModel = userMessage.getVideo().getCoverImage();
            image.setVisibility(View.GONE);
        }
        imageView.setVisibility(View.VISIBLE);

        // 计算图片宽度
        int width = imageView.getWidth();
        if (width > 0 && width != imageWidth) {
            imageWidth = width;
        }

        isImageLoaded = false;
        SnsImageLoader.loadImage(imageModel, imageView, imageWidth, 0, new SnsImageLoader.ImageLoadingListener() {
            @Override
            public void onLoadingComplete(String imageUri, View view) {
                isImageLoaded = true;
                btnShare.setEnabled(true);
                btnShare.setImageResource(R.drawable.selector_ic_share);
            }
        });

        // ActionBar
        resetLikeStatus(item);

        // 评论列表
        commentListAdapter = new SimpleCommentListAdapter(userMessage);
        listComments.setAdapter(commentListAdapter);

        // “评论”
        showComments();

        if (userMessage.isRemoving()) {
            showRemovingAnim();
        } else {
            stopRemovingAnim();
        }

        SectionReport.getInstance().showMessage(ReportHelper.MessageScene.feed, userMessage.getId());
    }

    public void bind(@NonNull final Card card) {
        this.card = card;
        bind(card.getMessage());
    }

    public UserMessage getUserMessage() {
        return userMessage;
    }

    public ViewGroup getVideoContainer() {
        return videoContainer;
    }

    public boolean isMuted() {
        if (userMessage != null) {
            return userMessage.isMuted();
        }
        return false;
    }

    public void setMuted(boolean status) {
        if (userMessage != null) {
            userMessage.setMuted(status);
        }
    }

    private boolean showComments() {
        int totalCommentNum = userMessage.getComments().getTotalSize();
        if (totalCommentNum > 0) {
            String allCommentsText = String.format(
                    btnAllComments.getContext().getString(R.string.action_see_all_comments), totalCommentNum);
            btnAllComments.setText(allCommentsText);
            btnAllComments.setVisibility(View.VISIBLE);
            listComments.setVisibility(View.VISIBLE);
            commentListAdapter.notifyDataSetChanged();
            return true;
        } else {
            btnAllComments.setVisibility(View.GONE);
            listComments.setVisibility(View.GONE);
        }

        return false;
    }

    private void showPublisher() {
        txtUserName.setText(publisher.getNickName());

        imgUserAvatar.setImageResource(R.drawable.shape_avatar_default_bg);
        SnsImageLoader.loadAvatar(publisher.getPortraitUri(), imgUserAvatar, false);

        if (publisher.isOfficial()) {
            txtUserName.setTextColor(container.getResources().getColor(R.color.sns_pink));
            imgOfficialIndicator.setVisibility(View.VISIBLE);
        } else {
            txtUserName.setTextColor(container.getResources().getColor(R.color.name_text));
            imgOfficialIndicator.setVisibility(View.INVISIBLE);
        }

        if (isPostedByLoginUser || publisher.isFollowed() || publisher.isOfficial()) {
            String time = TimeFormatter.getPublishTime(txtPublishTime.getContext(), userMessage.getCreateTime());
            txtPublishTime.setText(time);
            txtPublishTime.setVisibility(View.VISIBLE);
            btnFollow.setVisibility(View.GONE);
        } else {
            txtPublishTime.setVisibility(View.GONE);
            btnFollow.setVisibility(View.VISIBLE);
            btnFollow.setImageResource(R.drawable.ic_to_follow);
            btnFollow.setBackgroundResource(R.drawable.selector_button_to_follow);
        }
    }

    public void recycle() {
        if (publisher != null) {
            publisher.deleteObserver(publisherObserver);
        }

        publisher = null;
    }

    private void resetLikeStatus(@NonNull UserMessage item) {
        txtLikesNum.setText(String.valueOf(item.getLikeNum()));
        btnLike.setImageResource(item.isLiked() ? R.drawable.ic_like_pressed : R.drawable.ic_like);
    }

    private void showContextMenu(Activity parentActivity, CustomContextMenu.OnItemSelectedListener listener) {
        if (!isImageTouched && !isMoreActionsClicked) {
            return;
        }

        isMoreActionsClicked = false;

        settingsMenu = new CustomContextMenu(parentActivity);
        settingsMenu.setOnItemSelectedListener(listener);

        if (isPostedByLoginUser) {
            settingsMenu.add(CustomContextMenu.ItemStyle.DIVIDER_BOTTOM,
                    R.id.ctx_menu_delete,
                    R.string.ctx_menu_delete_title,
                    R.drawable.selector_custom_menu_text_color_red,
                    R.drawable.selector_custom_menu_text_bg);
        }

        if (canBeShared) {
            settingsMenu.add(CustomContextMenu.ItemStyle.DIVIDER_BOTTOM,
                    R.id.ctx_menu_copy_url,
                    R.string.ctx_menu_copy_url_title,
                    R.drawable.selector_custom_menu_text_color,
                    R.drawable.selector_custom_menu_text_bg);
        }

        settingsMenu.add(CustomContextMenu.ItemStyle.NONE,
                R.id.ctx_menu_report,
                R.string.ctx_menu_report_title,
                R.drawable.selector_custom_menu_text_color,
                R.drawable.selector_custom_menu_text_bg);

        if (!isImageLoaded) {
            settingsMenu.setMenuItemEnable(R.id.ctx_menu_report, false);
        }

        if (!settingsMenu.isShowing()) {
            settingsMenu.show();
        }

        // 清除标志
        isImageTouched = false;
    }

    @Override
    public void onItemSelected(int itemId, View itemView) {
        if (itemId == R.id.ctx_menu_share) {
            shareMessage();
            return;
        } else if (itemId == R.id.ctx_menu_copy_url) {
            copyMessageUrl();
            return;
        } else if (itemId == R.id.ctx_menu_report) {
            reportMessage();
            return;
        } else if (itemId == R.id.ctx_menu_delete) {
            deleteMessage();
            return;
        }

        return;
    }

    private void shareMessage() {
        if (userMessage.canBeShared()) {
            SnsImageLoader.createThumbnailForCachedImage(container.getContext(),
                    userMessage.getImage().getWidthUrlForShare())
                    .continueWith(new Continuation<Uri, Object>() {
                        @Override
                        public Object then(Task<Uri> task) throws Exception {
                            if (!task.isFaulted() && task.getResult() != null) {
                                NavigationHelper.navigateToShareUrlPage(container.getContext(),
                                        userMessage.getShareUrl(),
                                        task.getResult().toString(),
                                        container.getResources().getString(R.string.sns_share_title),
                                        getShareDescription(userMessage));

                                ReportHelper.shareMessage(container.getContext(), userMessage.getId());
                            } else {
                                Toast toast = Toast.makeText(container.getContext(),
                                        R.string.notification_share_failed, Toast.LENGTH_SHORT);
                                toast.show();
                            }

                            return null;
                        }
                    }, Task.UI_THREAD_EXECUTOR);
        }
    }

    private String getShareDescription(UserMessage userMessage) {
        String description = userMessage.getDescription();
        if (TextUtils.isEmpty(description)) {
            return container.getResources().getString(R.string.sns_share_description);
        }

        int length = description.length();
        if (length < MAX_SHARE_DESC_LENGTH) {
            return description;
        }

        return description.substring(0, MAX_SHARE_DESC_LENGTH) + "...";
    }

    private void copyMessageUrl() {
        if (userMessage.canBeShared()) {
            String shareTitle = userMessage.getDescription();
            String shareUrl = userMessage.getShareUrl();
            ClipboardManager clipboardManager = (ClipboardManager) container.getContext().getSystemService(
                    Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText(shareTitle, shareUrl);
            clipboardManager.setPrimaryClip(clip);
            Toast toast = Toast.makeText(container.getContext(), R.string.notification_url_copied, Toast.LENGTH_SHORT);
            toast.show();

            ReportHelper.copyMessageUrl(container.getContext(), userMessage.getId());
        }
    }

    private void reportMessage() {
        if (!SnsModel.getInstance().isUserLoggedIn()) {
            NavigationHelper.navigateToLoginPageForResult(container.getContext());
        } else {
        }
    }

    private void deleteMessage() {
        if (userMessage.getPublisher().isLoginUser()) {
            final Context context = container.getContext();
        }
    }

    private void showRemovingAnim() {
        maskLayer.setVisibility(View.VISIBLE);
        imgProgressInMask.startAnimation(animation);
    }

    private void stopRemovingAnim() {
        maskLayer.setVisibility(View.GONE);
        imgProgressInMask.clearAnimation();
    }

    private boolean checkLogin() {
        if (SnsModel.getInstance().isUserLoggedIn()) {
            return true;
        }

        NavigationHelper.navigateToLoginPageForResult(btnFollow.getContext());
        return false;
    }
}
