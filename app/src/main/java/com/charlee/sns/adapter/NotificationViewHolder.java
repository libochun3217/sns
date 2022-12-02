package com.charlee.sns.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.CharacterStyle;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.text.style.TextAppearanceSpan;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.charlee.sns.R;
import com.charlee.sns.helper.EventConstant;
import com.charlee.sns.helper.NavigationHelper;
import com.charlee.sns.helper.SnsImageLoader;
import com.charlee.sns.helper.TimeFormatter;
import com.charlee.sns.model.SnsModel;
import com.charlee.sns.model.SnsUser;
import com.charlee.sns.model.UserMessage;
import com.charlee.sns.model.UserNotification;
import com.charlee.sns.widget.text.VerticalImageSpan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import bolts.Continuation;
import bolts.Task;

/**
 * 通知详情的ViewHolder
 */
public class NotificationViewHolder extends RecyclerView.ViewHolder {
    private Context context;
    // 消息作者
    public ImageView mImgPortrait;
    public ImageView mImgOfficialIndicator;
    public View btnFollowAction;  // 根据消息类型显示
    public ImageView btnFollowActionIcon; // 根据消息类型显示

    // 消息内容
    public ImageView imageMessage;     // 根据消息类型显示
    public TextView txtMessage;        // 根据消息类型显示

    private View stateFollowing;
    private ImageView imgFollowActionAnim;
    private Animation animFollowing;

    private View container;

    private int imageMessageWidth;
    private int imageMessageHeight;

    private NotificationDataChangedListener notificationDataChangedListener;

    private SnsUser publisher;
    private UserMessage userMessage;

    public NotificationViewHolder(final View container, NotificationDataChangedListener listener) {
        super(container);
        this.container = container;

        context = container.getContext();
        // 消息作者
        mImgPortrait = (ImageView) container.findViewById(R.id.img_portrait);
        mImgOfficialIndicator = (ImageView) container.findViewById(R.id.img_official_indicator);
        btnFollowAction = container.findViewById(R.id.btn_follow_action);
        btnFollowActionIcon = (ImageView) container.findViewById(R.id.btn_follow_action_icon);
        // 消息内容
        imageMessage = (ImageView) container.findViewById(R.id.img_message);
        txtMessage = (TextView) container.findViewById(R.id.txt_message);

        stateFollowing = container.findViewById(R.id.state_follow_action_following);
        imgFollowActionAnim = (ImageView) container.findViewById(R.id.img_follow_btn_anim);
        animFollowing = AnimationUtils.loadAnimation(context, R.anim.refresh);
        animFollowing.setRepeatMode(Animation.RESTART);
        animFollowing.setRepeatCount(Animation.INFINITE);

        imageMessageWidth =
                container.getResources().getDimensionPixelSize(R.dimen.notify_item_message_image_width);
        imageMessageHeight =
                container.getResources().getDimensionPixelSize(R.dimen.notify_item_message_image_height);

        notificationDataChangedListener = listener;
    }

    public void bind(final UserNotification item, final int position) {
        View.OnClickListener messageOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (item.getType() == UserNotification.NotificationType.System) {
                    // 如果是系统消息，则跳转到公告板页面
                    NavigationHelper.navigateToSystemNoticePage(context, item.getText());
                    return;
                }
                SnsUser loginUser = SnsModel.getInstance().getLoginUser();
                if (loginUser == null || userMessage == null) {
                    return;
                }
                // 点击图片跳转至消息详情页并定位到对应图片
                HashMap<String, String> queries = new HashMap<>();
                userMessage.getNavQuery(queries);
                NavigationHelper.navigateToPath(v.getContext(), NavigationHelper.PATH_SNS_MESSAGE_DETAILS, queries);
            }
        };

        String publishTime = TimeFormatter.getPublishTime(context, item.getCreateTime());

        if (item.getType() == UserNotification.NotificationType.System) {
            // 先设置显示
            btnFollowAction.setVisibility(View.INVISIBLE);
            imageMessage.setVisibility(View.INVISIBLE);
            txtMessage.setVisibility(View.VISIBLE);

            // 填充
            mImgPortrait.setImageResource(R.drawable.ic_system);

            List<StyledText> styledTexts = parseTextMessages(context, item.getText(), publishTime);
            setStyledTextView(txtMessage, styledTexts, false);
            container.setOnClickListener(messageOnClickListener);
            return;
        }

        // 发布者相关
        publisher = item.getUser();
        userMessage = item.getMessage();
        SnsImageLoader.loadAvatar(publisher.getPortraitUri(), mImgPortrait, false);

        String userName = publisher.getNickName();
        if (item.getType() == UserNotification.NotificationType.Comment) {
            String comment = item.getComment().getContent();
            // 先设置显示
            btnFollowAction.setVisibility(View.INVISIBLE);
            imageMessage.setVisibility(View.VISIBLE);
            txtMessage.setVisibility(View.VISIBLE);
            // 填充
            SnsImageLoader.loadSquareImage(userMessage.getImage(), imageMessage, imageMessageWidth);
            List<StyledText> styledTexts = parseTextMessages(context, userName, comment, publishTime,
                    item.getReplyCommentStatus(), false, false);
            setStyledTextView(txtMessage, styledTexts, true);
        } else if (item.getType() == UserNotification.NotificationType.Follow) {
            // 先设置显示
            btnFollowAction.setVisibility(View.VISIBLE);
            imageMessage.setVisibility(View.INVISIBLE);
            txtMessage.setVisibility(View.VISIBLE);
            // 填充
            btnFollowAction.setBackgroundResource(item.getUser().isFollowed()
                    ? R.drawable.selector_button_followed : R.drawable.selector_button_to_follow);
            btnFollowActionIcon.setImageResource(item.getUser().isFollowed()
                    ? R.drawable.selector_ic_followed : R.drawable.selector_ic_to_follow);
            List<StyledText> styledTexts = parseTextMessages(context, userName, null, publishTime,
                    false, false, true);
            setStyledTextView(txtMessage, styledTexts, true);
        } else if (item.getType() == UserNotification.NotificationType.Like) {
            // 先设置显示
            btnFollowAction.setVisibility(View.INVISIBLE);
            imageMessage.setVisibility(View.VISIBLE);
            txtMessage.setVisibility(View.VISIBLE);
            // 填充
            SnsImageLoader.loadSquareImage(userMessage.getImage(), imageMessage, imageMessageWidth);
            List<StyledText> styledTexts = parseTextMessages(context, userName, null, publishTime,
                    false, true, false);
            setStyledTextView(txtMessage, styledTexts, true);
        } else {
            // 异常显示
            btnFollowAction.setVisibility(View.INVISIBLE);
            imageMessage.setVisibility(View.INVISIBLE);
            txtMessage.setVisibility(View.INVISIBLE);
        }

        mImgPortrait.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (publisher.isOfficial()) {
                    return;
                }
                NavigationHelper.navigateToUserProfilePageForResult((Activity) v.getContext(), publisher);
            }
        });

        imageMessage.setOnClickListener(messageOnClickListener);
        txtMessage.setOnClickListener(messageOnClickListener);
        itemView.setOnClickListener(messageOnClickListener);

        btnFollowAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkLogin()) {
                    return;
                }

                if (!publisher.isFollowed()) {
                    showFollowingAnim(true);
                    publisher.follow().continueWith(new Continuation<Boolean, Object>() {
                        @Override
                        public Object then(Task<Boolean> task) throws Exception {
                            showFollowingAnim(false);
                            if (!task.isFaulted()
                                    && task.getResult() != null
                                    && task.getResult().booleanValue() == true
                                    && task.getError() == null) {
                                btnFollowAction.setBackgroundResource(R.drawable.shape_button_followed);
                                btnFollowActionIcon.setImageResource(R.drawable.ic_followed);
                                if (notificationDataChangedListener != null) {
                                    notificationDataChangedListener.onItemChanged(position);
                                }
                            } else {
                                Toast.makeText(context.getApplicationContext(),
                                        R.string.user_detail_header_toast_follow_failed,
                                        Toast.LENGTH_SHORT).show();
                            }

                            return null;
                        }
                    }, Task.UI_THREAD_EXECUTOR);
                } else {
                    showFollowingAnim(true);
                    publisher.unfollow().continueWith(new Continuation<Boolean, Object>() {
                        @Override
                        public Object then(Task<Boolean> task) throws Exception {
                            showFollowingAnim(false);
                            if (!task.isFaulted()
                                    && task.getResult() != null
                                    && task.getResult().booleanValue() == true
                                    && task.getError() == null) {
                                btnFollowAction.setBackgroundResource(
                                        R.drawable.selector_button_to_follow);
                                btnFollowActionIcon.setImageResource(R.drawable.selector_ic_to_follow);
                                if (notificationDataChangedListener != null) {
                                    notificationDataChangedListener.onItemChanged(position);
                                }
                            } else {
                                Toast.makeText(context.getApplicationContext(),
                                        R.string.user_detail_header_toast_unfollow_failed,
                                        Toast.LENGTH_SHORT).show();
                            }
                            return null;
                        }
                    }, Task.UI_THREAD_EXECUTOR);
                }
            }
        });

        if (publisher.isOfficial()) {
            mImgOfficialIndicator.setVisibility(View.VISIBLE);
        } else {
            mImgOfficialIndicator.setVisibility(View.GONE);
        }

    }

    private boolean checkLogin() {
        if (SnsModel.getInstance().isUserLoggedIn()) {
            return true;
        }

        NavigationHelper.navigateToLoginPageForResult(btnFollowAction.getContext());
        return false;
    }

    private void showFollowingAnim(boolean show) {
        stateFollowing.setVisibility(show ? View.VISIBLE : View.GONE);
        btnFollowAction.setVisibility(show ? View.GONE : View.VISIBLE);

        if (show) {
            imgFollowActionAnim.startAnimation(animFollowing);
        } else {
            imgFollowActionAnim.clearAnimation();
        }
    }

    // region span
    private List<StyledText> parseTextMessages(Context context, String userName,
                                               String comment,
                                               String publishTime,
                                               boolean isReplyComment,
                                               boolean isLike,
                                               boolean isFollow) {
        List<StyledText> styledTexts = new ArrayList<>();
        if (userName != null) {
            if (isReplyComment) {
                userName += " ";
            } else {
                userName = userName + " : ";
            }
            styledTexts.add(new StyledText(userName, TextStyle.NAME));
        }

        if (isLike) {
            String likeIcon = context.getString(R.string.emoj_11);
            String likeStr = " " + context.getString(R.string.notify_center_list_item_love_your_photo);
            styledTexts.add(new StyledText(likeIcon, TextStyle.LIKE_ICON));
            styledTexts.add(new StyledText(likeStr, TextStyle.LIKE_STR));
        }

        if (isFollow) {
            String followStr = context.getString(R.string.notify_center_list_item_follow_you);
            styledTexts.add(new StyledText(followStr, TextStyle.FOLLOW));
        }

        if (comment != null) {
            if (isReplyComment) {
                comment = context.getResources().getString(R.string.notify_center_list_item_reply_comment) + comment;
            }
            styledTexts.add(new StyledText(comment, TextStyle.COMMENT));
        }

        if (publishTime != null) {
            publishTime = " " + publishTime;
            styledTexts.add(new StyledText(publishTime, TextStyle.TIME));
        }

        return styledTexts;
    }

    private List<StyledText> parseTextMessages(Context context, String systemNotice, String publishTime) {
        List<StyledText> styledTexts = new ArrayList<>();

        if (systemNotice != null) {
            styledTexts.add(new StyledText(systemNotice, TextStyle.FOLLOW));
        }

        if (publishTime != null) {
            publishTime = " " + publishTime;
            styledTexts.add(new StyledText(publishTime, TextStyle.TIME));
        }

        return styledTexts;
    }


    private void setStyledTextView(@NonNull final TextView textView,
                                   @NonNull final List<StyledText> styledTexts,
                                   boolean checkTextNeedToWrap) {
        String wholeStr = "";
        for (StyledText text : styledTexts) {
            wholeStr += text.getText();
        }

        boolean needSetLinkMovementMethod = false;
        SpannableString spannableString = new SpannableString(wholeStr);

        int indexTimeStart = 0;
        int indexTimeEnd = 0;
        StyledText styledTextTime = null;

        int index = 0;
        for (StyledText text : styledTexts) {
            if (TextUtils.isEmpty(text.getText())) {
                continue;
            }
            CharacterStyle span = getSpan(text.getStyle());
            if (span == null) {
                index += text.getText().length();
                continue;
            }

            if (text.getStyle() == TextStyle.TIME) {
                indexTimeStart = index;
                indexTimeEnd = index + text.getText().length();
                styledTextTime = text;
            }

            spannableString.setSpan(span, index, index + text.getText().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            if (text.getStyle() == TextStyle.NAME) {
                needSetLinkMovementMethod = true;
            }

            index += text.getText().length();
        }

        textView.setText(spannableString);
        if (needSetLinkMovementMethod) {
            textView.setMovementMethod(LinkMovementMethod.getInstance());
        } else {
            textView.setMovementMethod(null);
        }

        final int indexTimeStartFinal = indexTimeStart;
        final int indexTimeEndFinal = indexTimeEnd;
        final StyledText styledTextTimeFinal = styledTextTime;

        if (checkTextNeedToWrap) {
            textView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    textView.getViewTreeObserver().removeOnPreDrawListener(this);
                    int lineCount = textView.getLineCount();
                    if (lineCount <= 0) {
                        return true;
                    }

                    int lineCountTimeStart  = textView.getLayout().getLineForOffset(indexTimeStartFinal);
                    int lineCountTimeEnd = textView.getLayout().getLineForOffset(indexTimeEndFinal);
                    boolean textChanged = false;
                    if (styledTextTimeFinal != null) {
                        String wrappedTime = "\n" + styledTextTimeFinal.getText();
                        if (lineCount == 1) {
                            styledTextTimeFinal.setText(wrappedTime);
                            textChanged = true;
                        } else if (lineCount > 1 && lineCountTimeStart != lineCountTimeEnd) {
                            styledTextTimeFinal.setText(wrappedTime);
                            textChanged = true;
                        }
                    }

                    if (textChanged) {
                        setStyledTextView(textView, styledTexts, false);
                        textView.invalidate();
                    }

                    return true;
                }
            });
        }
    }

    private ImageSpan getVerticaleImageSpan(Context context, int imageRes, int imageWidth, int imageHeight) {
        Drawable drawable = context.getResources().getDrawable(imageRes);
        // use specific size
        drawable.setBounds(0, 0, imageWidth, imageHeight);
        ImageSpan span = new VerticalImageSpan(drawable);
        return span;
    }

    private ClickableSpan spanName;
    private ImageSpan spanLikeIcon;
    private TextAppearanceSpan spanLikeStr;
    private TextAppearanceSpan spanFollow;
    private TextAppearanceSpan spanComment;
    private TextAppearanceSpan spanTime;

    @NonNull
    private CharacterStyle getSpan(TextStyle style) {
        if (style == TextStyle.NAME) {
            if (spanName == null) {
                spanName = new ClickableSpan() {
                    @Override
                    public void onClick(View widget) {
                        if (publisher.isOfficial()) {
                            return;
                        }
                        NavigationHelper.navigateToUserProfilePageForResult((Activity) context, publisher);
                    }

                    @Override
                    public void updateDrawState(TextPaint ds) {
                        if (publisher.isOfficial()) {
                            ds.setColor(context.getResources().getColor(R.color.sns_pink));
                        } else {
                            ds.setColor(context.getResources().getColor(R.color.notify_item_text_name));
                        }
                        ds.setUnderlineText(false);
                    }
                };
            }
            return spanName;
        } else if (style == TextStyle.LIKE_ICON) {
            if (spanLikeIcon == null) {
                spanLikeIcon = getVerticaleImageSpan(context, R.drawable.ic_notification_zan,
                        context.getResources().getDimensionPixelSize(R.dimen.notify_item_text_love_icon_size_width),
                        context.getResources().getDimensionPixelSize(R.dimen.notify_item_text_love_icon_size_height));
            }
            return spanLikeIcon;
        } else if (style == TextStyle.LIKE_STR) {
            if (spanLikeStr == null) {
                spanLikeStr = new TextAppearanceSpan(context, R.style.NotifyItemSpanLikeString);
            }
            return spanLikeStr;
        } else if (style == TextStyle.FOLLOW) {
            if (spanFollow == null) {
                spanFollow = new TextAppearanceSpan(context, R.style.NotifyItemSpanFollow);
            }
            return spanFollow;
        } else if (style == TextStyle.COMMENT) {
            if (spanComment == null) {
                spanComment = new TextAppearanceSpan(context, R.style.NotifyItemSpanLikeComment);
            }
            return spanComment;
        } else if (style == TextStyle.TIME) {
            if (spanTime == null) {
                spanTime = new TextAppearanceSpan(context, R.style.NotifyItemSpanTime);
            }
            return spanTime;
        }

        return null;
    }

    private enum TextStyle {
        NAME,
        LIKE_ICON,
        LIKE_STR,
        FOLLOW,
        COMMENT,
        TIME
    }

    private class StyledText {
        private String text;
        private final TextStyle style;

        public StyledText(@NonNull String text, @NonNull TextStyle style) {
            this.text = text;
            this.style = style;
        }

        public String getText() {
            return this.text;
        }

        public TextStyle getStyle() {
            return this.style;
        }

        public void setText(String text) {
            this.text = text;
        }
    }

    // endregion
}
