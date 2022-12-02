package com.charlee.sns.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.charlee.sns.BuildConfig;
import com.charlee.sns.R;
import com.charlee.sns.helper.ErrorHandler;
import com.charlee.sns.helper.NavigationHelper;
import com.charlee.sns.helper.ReportHelper;
import com.charlee.sns.helper.SnsImageLoader;
import com.charlee.sns.model.SnsModel;
import com.charlee.sns.model.UserMessage;

import java.security.InvalidParameterException;

import bolts.Continuation;
import bolts.Task;

/**
 */
public class SimpleMessageViewHolder extends RecyclerView.ViewHolder {
    private static final String LOG_TAG = "SimpleMessageViewHolder";

    private int imageWidth = 0; // 图片宽度
    private int heightAdjust = 0; // 图片高度调整值

    private View container;
    private boolean voteIndicator;

    private UserMessage userMessage;
    private ImageView image;
    private ImageView btnLike;
    private ImageView imageGifFlag;
    private ImageView imageVideoFlag;
    private TextView txtLikesNum;

    public interface IImageClickedCallback {
        void onClick(View view, int index, UserMessage item);
    }

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
                    btnLike.setImageResource(R.drawable.ic_campaign_like);
                } else {
                    userMessage.like();
                    txtLikesNum.setText(String.valueOf(userMessage.getLikeNum()));
                    btnLike.setImageResource(R.drawable.ic_campaign_like_pressed);

                    ReportHelper.clickLike(container.getContext(), userMessage.getId());
                }
            }
        }
    };

    public SimpleMessageViewHolder(final View container,
                                   final IImageClickedCallback imageClickedCallback,
                                   final boolean listIsHeadered, int itemWidth, int heightAdjust,
                                   boolean voteIndicator) {
        super(container);
        this.container = container;
        this.voteIndicator = voteIndicator;
        image = (ImageView) container.findViewById(R.id.image);
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int index = SimpleMessageViewHolder.this.getLayoutPosition();
                imageClickedCallback.onClick(v, listIsHeadered ? index - 1 : index, userMessage);
            }
        });

        if (voteIndicator) {
            container.findViewById(R.id.vote_layout).setVisibility(View.VISIBLE);
            btnLike = (ImageView) container.findViewById(R.id.img_like);
            btnLike.setVisibility(View.VISIBLE);
            btnLike.setOnClickListener(onLikeClickListener);
            txtLikesNum = (TextView) container.findViewById(R.id.txt_likes_num);
            txtLikesNum.setVisibility(View.VISIBLE);
        }

        imageGifFlag = (ImageView) container.findViewById(R.id.gif_flag);
        imageVideoFlag = (ImageView) container.findViewById(R.id.video_flag);

        imageWidth = itemWidth;
        this.heightAdjust = heightAdjust;

        // 不要删除！检查图片宽度，避免布局和代码修改不同步导致的宽度不一致
        if (BuildConfig.DEBUG) {
            int width = itemView.getWidth();
            if (width > 0 && imageWidth != width) {
                throw new InvalidParameterException("Incorrect imageWidth!");
            }
        }
    }

    /**
     * 绑定Model
     *
     * @param item 用户消息Model
     */
    public void bind(@NonNull final UserMessage item) {
        userMessage = item;
        SnsImageLoader.loadImage(userMessage.getImage(), image, imageWidth, heightAdjust);
        if (voteIndicator) {
            resetLikeStatus(item);
        }

        if (item.getImage().isAnimatable()) {
            imageGifFlag.setVisibility(View.VISIBLE);
        } else {
            imageGifFlag.setVisibility(View.GONE);
        }

        if (item.getVideo() != null) {
            imageVideoFlag.setVisibility(View.VISIBLE);
        } else {
            imageVideoFlag.setVisibility(View.GONE);
        }

    }

    private void resetLikeStatus(@NonNull UserMessage item) {
        txtLikesNum.setText(String.valueOf(item.getLikeNum()));
        btnLike.setImageResource(item.isLiked()
                ? R.drawable.ic_campaign_like_pressed : R.drawable.ic_campaign_like);
    }

    private boolean checkLogin() {
        if (SnsModel.getInstance().isUserLoggedIn()) {
            return true;
        }
        NavigationHelper.navigateToLoginPageForResult(btnLike.getContext());
        return false;
    }

}