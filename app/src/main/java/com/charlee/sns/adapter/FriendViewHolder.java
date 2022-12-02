package com.charlee.sns.adapter;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import androidx.recyclerview.widget.RecyclerView;

import com.charlee.sns.R;
import com.charlee.sns.helper.ErrorHandler;
import com.charlee.sns.helper.NavigationHelper;
import com.charlee.sns.helper.SnsImageLoader;
import com.charlee.sns.model.IPageableList;
import com.charlee.sns.model.SnsModel;
import com.charlee.sns.model.SnsUser;
import com.charlee.sns.model.UserMessage;

import java.util.ArrayList;
import java.util.HashMap;

import bolts.Continuation;
import bolts.Task;

/**
 * Friend用户对象的ViewHolder
 */
public class FriendViewHolder extends RecyclerView.ViewHolder {
    private static final String LOG_TAG = "FriendViewHolder";
    private static final int SPAN_COUNT = 4;

    private ImageView imgUserAvatar;
    private TextView txtUserName;
    private TextView txtReason;
    private ImageView btnFollow;
    private ImageView imgFollowing;
    private LinearLayout imageContainer;

    private Animation animation;

    private int itemSpacing;
    private int imageWidth;

    private ArrayList<ImageView> imageArray = new ArrayList<>();
    private View noPhotoContainer;

    private SnsUser snsUser;

    public FriendViewHolder(final View container) {
        super(container);

        imgUserAvatar = (ImageView) container.findViewById(R.id.img_user_avatar);
        txtUserName = (TextView) container.findViewById(R.id.txt_user_name);
        txtReason = (TextView) container.findViewById(R.id.txt_reason);
        btnFollow = (ImageView) container.findViewById(R.id.img_follow);
        imgFollowing = (ImageView) container.findViewById(R.id.img_following);

        imageArray.add((ImageView) container.findViewById(R.id.image_first));
        imageArray.add((ImageView) container.findViewById(R.id.image_second));
        imageArray.add((ImageView) container.findViewById(R.id.image_third));
        imageArray.add((ImageView) container.findViewById(R.id.image_forth));

        imageContainer = (LinearLayout) container.findViewById(R.id.image_container);
        noPhotoContainer = container.findViewById(R.id.container_no_photo);

        DisplayMetrics metrics = new DisplayMetrics();
        ((Activity) (container.getContext())).getWindowManager().getDefaultDisplay().getMetrics(metrics);

        itemSpacing = container.getContext().getResources().getDimensionPixelSize(R.dimen.large_item_spacing);
        int marginCount = SPAN_COUNT + 1;
        imageWidth = (metrics.widthPixels - marginCount * itemSpacing) / SPAN_COUNT;

        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) imageContainer.getLayoutParams();
        params.height = imageWidth;
        imageContainer.setLayoutParams(params);

        for (ImageView iv : imageArray) {
            LinearLayout.LayoutParams imageParams = (LinearLayout.LayoutParams) iv.getLayoutParams();
            imageParams.width = imageWidth;
            imageParams.height = imageWidth;
            imageParams.setMargins(itemSpacing, 0, 0, 0);
            iv.setLayoutParams(imageParams);
        }

        animation = AnimationUtils.loadAnimation(container.getContext(), R.anim.refresh);
        animation.setRepeatMode(Animation.RESTART);
        animation.setRepeatCount(Animation.INFINITE);
    }

    public void bind(final SnsUser item) {
        snsUser = item;

        imgUserAvatar.setImageResource(R.drawable.shape_avatar_default_bg);
        SnsImageLoader.loadAvatar(snsUser.getPortraitUri(), imgUserAvatar, false);
        txtUserName.setText(snsUser.getNickName());
        txtReason.setText(snsUser.getRecommendReason());

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

        btnFollow.setOnClickListener(onFollowClickListener);
        if (snsUser.isFollowed()) {
            btnFollow.setImageResource(R.drawable.selector_ic_followed);
            btnFollow.setBackgroundResource(R.drawable.selector_button_followed);
        } else {
            btnFollow.setImageResource(R.drawable.selector_ic_to_follow);
            btnFollow.setBackgroundResource(R.drawable.selector_button_to_follow);
        }

        for (ImageView iv : imageArray) {
            iv.setVisibility(View.INVISIBLE);
        }

        refresh();
    }

    private void refresh() {
        if (snsUser.getMessages().size() > 0) {
            fillViews();
            return;
        }

        try {
            snsUser.getMessages().refresh().onSuccess(new Continuation<Boolean, Object>() {
                @Override
                public Object then(Task<Boolean> task) throws Exception {
                    fillViews();
                    return null;
                }
            }, Task.UI_THREAD_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void fillViews() {
        IPageableList<UserMessage> messages = snsUser.getMessages();
        if (messages != null && messages.size() == 0) {
            noPhotoContainer.setVisibility(View.VISIBLE);
            return;
        } else {
            noPhotoContainer.setVisibility(View.GONE);
        }

        for (int index = 0; index < SPAN_COUNT; index++) {
            if (messages.size() <= index) {
                break;
            }
            UserMessage message = messages.get(index);
            final ImageView iv = imageArray.get(index);
            final int position = index;
            if (message != null) {
                iv.setVisibility(View.VISIBLE);
                SnsImageLoader.loadSquareImage(message.getImage(), iv, imageWidth);
                iv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        HashMap<String, String> queries = new HashMap<>();
                        snsUser.getNavQuery(queries);
                        queries.put(NavigationHelper.QUERY_POS, String.valueOf(position));
                        NavigationHelper.navigateToPath(iv.getContext(), NavigationHelper.PATH_SNS_USER_MSGS, queries);
                    }
                });
            }
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

            snsUser.setFiltered();

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
