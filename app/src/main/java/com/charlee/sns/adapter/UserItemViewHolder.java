package com.charlee.sns.adapter;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.charlee.sns.R;
import com.charlee.sns.helper.ErrorHandler;
import com.charlee.sns.helper.EventConstant;
import com.charlee.sns.helper.NavigationHelper;
import com.charlee.sns.helper.SnsImageLoader;
import com.charlee.sns.model.SnsModel;
import com.charlee.sns.model.SnsUser;
import com.charlee.sns.R;
import com.charlee.sns.helper.NavigationHelper;
import com.charlee.sns.helper.SnsImageLoader;
import com.charlee.sns.model.SnsUser;

import bolts.Continuation;
import bolts.Task;

/**
 * 展示一个用户消息的ViewHolder
 */
public class UserItemViewHolder extends RecyclerView.ViewHolder {
    private static final String LOG_TAG = "UserItemViewHolder";

    private SnsUser snsUser;

    // 发布者
    private ImageView imgUserAvatar;
    private TextView txtUserName;
    private ImageView btnFollow;
    private ImageView imgFollowing;

    private Animation animation;

    public UserItemViewHolder(final View container) {
        super(container);

        imgUserAvatar = (ImageView) container.findViewById(R.id.img_user_avatar);
        txtUserName = (TextView) container.findViewById(R.id.txt_user_name);
        btnFollow = (ImageView) container.findViewById(R.id.img_follow);
        imgFollowing = (ImageView) container.findViewById(R.id.img_following);

        animation = AnimationUtils.loadAnimation(container.getContext(), R.anim.refresh);
        animation.setRepeatMode(Animation.RESTART);
        animation.setRepeatCount(Animation.INFINITE);

    }

    public void bind(final SnsUser item) {
        this.snsUser = item;

        imgUserAvatar.setImageResource(R.drawable.shape_avatar_default_bg);
        SnsImageLoader.loadAvatar(snsUser.getPortraitUri(), imgUserAvatar, false);
        txtUserName.setText(snsUser.getNickName());

        imgUserAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavigationHelper.navigateToUserProfilePage(v.getContext(), snsUser);
            }
        });

        txtUserName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavigationHelper.navigateToUserProfilePage(v.getContext(), snsUser);
            }
        });

        if (snsUser.isLoginUser()) {
            btnFollow.setVisibility(View.GONE);
            return;
        }

        btnFollow.setOnClickListener(onFollowClickListener);
        if (snsUser.isFollowed()) {
            btnFollow.setImageResource(R.drawable.selector_ic_followed);
            btnFollow.setBackgroundResource(R.drawable.selector_button_followed);
        } else {
            btnFollow.setImageResource(R.drawable.selector_ic_to_follow);
            btnFollow.setBackgroundResource(R.drawable.selector_button_to_follow);
        }

    }

    private boolean checkLogin() {
        if (SnsModel.getInstance().isUserLoggedIn()) {
            return true;
        }

        NavigationHelper.navigateToLoginPageForResult(btnFollow.getContext());
        return false;
    }

    private View.OnClickListener onFollowClickListener = new View.OnClickListener() {

        private void onFollowSucceeded() {
            btnFollow.setImageResource(R.drawable.ic_followed);
            btnFollow.setBackgroundResource(R.drawable.shape_button_followed);
        }

        private void onUnFollowSucceeded() {
            btnFollow.setImageResource(R.drawable.selector_ic_to_follow);
            btnFollow.setBackgroundResource(R.drawable.selector_button_to_follow);
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
            if (snsUser == null) {
                return;
            }

            if (checkLogin()) {
                showProgressAnimation();
                if (snsUser.isFollowed()) {
                    snsUser.unfollow().continueWith(new Continuation<Boolean, Object>() {
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
                    snsUser.follow().continueWith(new Continuation<Boolean, Object>() {
                        @Override
                        public Object then(Task<Boolean> task) throws Exception {
                            stopProgressAnimation();
                            if (!task.isFaulted()) {
                                onFollowSucceeded();
                            } else {
                                onError(task);
                            }

                            return null;
                        }
                    }, Task.UI_THREAD_EXECUTOR);
                }
            }
        }
    };

}
