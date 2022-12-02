package com.charlee.sns.adapter;

import android.app.Activity;
import android.content.Context;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.charlee.sns.R;
import com.charlee.sns.helper.NavigationHelper;
import com.charlee.sns.helper.SnsImageLoader;
import com.charlee.sns.helper.TimeFormatter;
import com.charlee.sns.model.Comment;
import com.charlee.sns.model.SnsModel;
import com.charlee.sns.model.UserMessage;
import com.charlee.sns.view.TextWithFatherName;


/**
 * 消息评论页的每一项评论的ViewHolder
 */
public class MessageCommentViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private View container;

    private ImageView ivUserPortrait;
    private ImageView ivOfficialIndicator;
    private TextView tvUserName;
    private TextView tvPublishedTime;
    private TextView tvCommentContent;

    private View ivUploading;
    private View btnProblem;
    private View btnRetry;
    private View btnDelete;

    private UserMessage userMessage;
    private Comment comment;

    private Animation animation;

    private OnRecycleViewItemClickListener itemClickListener;

    public MessageCommentViewHolder(final View container) {
        super(container);

        this.container = container;

        ivUserPortrait = (ImageView) container.findViewById(R.id.img_user_avatar);
        ivOfficialIndicator = (ImageView) container.findViewById(R.id.img_official_indicator);
        tvUserName = (TextView) container.findViewById(R.id.tv_user_name);
        tvPublishedTime = (TextView) container.findViewById(R.id.tv_published_time);
        tvCommentContent = (TextView) container.findViewById(R.id.tv_comment_content);

        btnProblem = container.findViewById(R.id.btn_problem);
        btnRetry = container.findViewById(R.id.btn_retry);
        btnDelete = container.findViewById(R.id.btn_delete);

        ivUploading = container.findViewById(R.id.iv_uploading);
        btnProblem.setOnClickListener(this);
        btnRetry.setOnClickListener(this);
        btnDelete.setOnClickListener(this);

        animation = AnimationUtils.loadAnimation(container.getContext(), R.anim.refresh);
        animation.setRepeatMode(Animation.RESTART);
        animation.setRepeatCount(Animation.INFINITE);

    }

    public void setOnItemClickListener(OnRecycleViewItemClickListener listener) {
        itemClickListener = listener;
    }

    public void bind(final Comment item, final UserMessage userMessage) {
        this.comment = item;
        this.userMessage = userMessage;

        ivUserPortrait.setImageResource(R.drawable.shape_avatar_default_bg);
        SnsImageLoader.loadAvatar(item.getUser().getPortraitUri(), ivUserPortrait, false);

        tvUserName.setText(item.getUser().getNickName());

        if (item.getFatherUser() == null) {
            tvCommentContent.setText(item.getContent());
        } else {
            final TextWithFatherName text = TextWithFatherName.create(tvUserName.getContext(),
                    comment.getFatherUser(), item.getContent());
            tvCommentContent.setText(text);
            tvCommentContent.setMovementMethod(LinkMovementMethod.getInstance());
        }

        ivUserPortrait.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (comment.getUser().isOfficial()) {
                    return;
                }
                hideSoftInput(v.getContext());
                NavigationHelper.navigateToUserProfilePage(v.getContext(), comment.getUser());
            }
        });

        tvUserName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (comment.getUser().isOfficial()) {
                    return;
                }
                hideSoftInput(v.getContext());
                NavigationHelper.navigateToUserProfilePage(v.getContext(), comment.getUser());
            }
        });

        // 根据发布状态显示界面元素
        if (item.getState() == SnsModel.PublishedState.PUBLISHING) {
            ivUploading.setVisibility(View.VISIBLE);
            ivUploading.startAnimation(animation);

            btnProblem.setVisibility(View.INVISIBLE);
            tvPublishedTime.setVisibility(View.INVISIBLE);
        } else if (item.getState() == SnsModel.PublishedState.FAILED) {
            ivUploading.setVisibility(View.INVISIBLE);
            ivUploading.clearAnimation();

            btnProblem.setVisibility(View.VISIBLE);
            tvPublishedTime.setVisibility(View.INVISIBLE);
        } else {
            ivUploading.setVisibility(View.INVISIBLE);
            ivUploading.clearAnimation();

            btnProblem.setVisibility(View.INVISIBLE);
            tvPublishedTime.setVisibility(View.VISIBLE);

            tvPublishedTime.setText(TimeFormatter.getPublishTime(
                    tvPublishedTime.getContext(), item.getCreateTime()));
            btnProblem.setVisibility(View.INVISIBLE);
        }

        container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!comment.getUser().isLoginUser()) {
                    if (itemClickListener != null) {
                        itemClickListener.onItemClick(comment);
                    }
                    return;
                }

                if (itemClickListener.onIntercept()) {
                    return;
                }

                Context context = v.getContext();
            }
        });

        if (comment.getUser().isOfficial()) {
            ivOfficialIndicator.setVisibility(View.VISIBLE);
            tvUserName.setTextColor(container.getResources().getColor(R.color.sns_pink));
        } else {
            ivOfficialIndicator.setVisibility(View.GONE);
            tvUserName.setTextColor(container.getResources().getColor(R.color.name_text));
        }

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_problem) {
            if (btnRetry.getVisibility() == View.INVISIBLE) {
                showProblemsView(true);
            } else {
                showProblemsView(false);
            }
        } else if (id == R.id.btn_retry) {
            showProblemsView(false);
            userMessage.getPublishComments().remove(comment);
            userMessage.postComment(comment.getContent(), comment.getFatherComment());
        } else if (id == R.id.btn_delete) {
            showProblemsView(false);
            userMessage.getPublishComments().remove(comment);
        }
    }

    private void showProblemsView(boolean visible) {
        if (visible) {
            btnRetry.setVisibility(View.VISIBLE);
            btnDelete.setVisibility(View.VISIBLE);
        } else {
            btnRetry.setVisibility(View.INVISIBLE);
            btnDelete.setVisibility(View.INVISIBLE);
        }
    }

    private void hideSoftInput(Context context) {
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        }
    }

}
