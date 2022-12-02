package com.charlee.sns.view;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.charlee.sns.R;
import com.charlee.sns.adapter.PageableListAdapter;
import com.charlee.sns.helper.ErrorHandler;
import com.charlee.sns.helper.EventConstant;
import com.charlee.sns.helper.NavigationHelper;
import com.charlee.sns.helper.SnsImageLoader;
import com.charlee.sns.model.Card;
import com.charlee.sns.model.ICollectionObserver;
import com.charlee.sns.model.IPageableList;
import com.charlee.sns.model.SnsModel;
import com.charlee.sns.model.SnsUser;
import com.charlee.sns.widget.HorizontalListView;
import com.charlee.sns.widget.VerticalListView;

import java.util.List;

import bolts.Continuation;
import bolts.Task;

/**
 * 推荐用户的卡片页,包括列表卡片样式和方格卡片样式
 */
public class RecommendUsersCardView extends FrameLayout {

    // 错误信息显示
    private EmptyPlaceholderView placeholderView;

    // 消息列表瀑布流
    private VerticalListView userListView;
    private HorizontalListView horizontalUserListView;
    private RecommendUserListAdapter userListAdapter;
    private int containerWith;
    private int itemSpacing;

    public enum SourceType {
        UserDetails,
        FollowPage
    }

    private SourceType sourceType = SourceType.FollowPage;

    public RecommendUsersCardView(Context context) {
        super(context);
        init(null, 0);
    }

    public RecommendUsersCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public RecommendUsersCardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    public void setSnsUser(IPageableList<SnsUser> snsUserList, int type) {

        userListAdapter = new RecommendUserListAdapter(snsUserList, type);
        if (type == Card.RECOMMEND_GRID) {
            horizontalUserListView.setAdapter(userListAdapter);
        } else {
            userListView.setAdapter(userListAdapter);
        }

        if (snsUserList.size() == 0) {
            onRefresh();
        }
    }

    public void setSrouceType(SourceType type) {
        sourceType = type;
    }

    private void init(AttributeSet attrs, int defStyle) {
        View.inflate(getContext(), R.layout.view_recommend_user_list, this);
        initWidgets();
    }

    private void initWidgets() {
        placeholderView = (EmptyPlaceholderView) findViewById(R.id.empty_placeholder);
        userListView = (VerticalListView) findViewById(R.id.vertical_user_list);
        horizontalUserListView = (HorizontalListView) findViewById(R.id.horizontal_user_list);

        DisplayMetrics metrics = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(metrics);

        itemSpacing = getContext().getResources().getDimensionPixelSize(R.dimen.content_margin);
        int marginCount = 3 + 1;
        containerWith = (metrics.widthPixels - marginCount * itemSpacing) / 3;
    }

    private void onRefresh() {
        userListAdapter.refresh().continueWith(new Continuation<Boolean, Object>() {
            @Override
            public Object then(Task<Boolean> task) throws Exception {
                if (task.isFaulted()) {
                    placeholderView.setVisibility(VISIBLE);
                    userListView.setVisibility(GONE);
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
                    userListView.setVisibility(VISIBLE);
                    userListView.scrollToPosition(0);
                }
                return null;
            }
        }, Task.UI_THREAD_EXECUTOR);
    }

    public void scrollToPosition(int i) {
        userListView.scrollToPosition(i);
    }

    class RecommendUserListAdapter extends PageableListAdapter<SnsUser, RecommendUserItemViewHolder> {
        private int type;

        public RecommendUserListAdapter(IPageableList<SnsUser> users, int type) {
            super(users);
            this.type = type;
        }

        @Override
        public void onBindViewHolder(final RecommendUserItemViewHolder holder, int position) {
            SnsUser item = itemList.get(position);
            holder.bind(item);
        }

        @Override
        public RecommendUserItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == Card.RECOMMEND_GRID) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_with_grid, parent,
                        false);
                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) v.getLayoutParams();
                params.width = containerWith;
                params.setMargins(itemSpacing, 0, 0, 0);
                v.setLayoutParams(params);
                return new RecommendUserItemViewHolder(v);
            } else {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_with_list, parent, false);
                return new RecommendUserItemViewHolder(v);
            }
        }

        @Override
        protected boolean onListChanged(final ICollectionObserver.Action action,
                                        final SnsUser item, final List<Object> range) {
            if (action == ICollectionObserver.Action.AddItemToFront) {
                notifyDataSetChanged(); // 全部刷新以保证新加入的评论能显示出来
            }
            return false;
        }

        @Override
        public int getItemCount() {
            return super.getItemCount() > 3 ? 3 : super.getItemCount();
        }

        @Override
        public int getItemViewType(int position) {
            return type;
        }
    }

    class RecommendUserItemViewHolder extends RecyclerView.ViewHolder {
        private static final String LOG_TAG = "RecommendUserItemViewHolder";

        private SnsUser snsUser;

        // 发布者
        private ImageView imgUserAvatar;
        private TextView txtUserName;
        private TextView txtReason;
        private ImageView btnFollow;
        private ImageView imgFollowing;
        private View closeView;

        private Animation animation;

        public RecommendUserItemViewHolder(final View container) {
            super(container);

            imgUserAvatar = (ImageView) container.findViewById(R.id.img_user_avatar);
            txtUserName = (TextView) container.findViewById(R.id.txt_user_name);
            txtReason = (TextView) container.findViewById(R.id.txt_reason);
            btnFollow = (ImageView) container.findViewById(R.id.img_follow);
            imgFollowing = (ImageView) container.findViewById(R.id.img_following);
            closeView = container.findViewById(R.id.img_close_item);

            animation = AnimationUtils.loadAnimation(container.getContext(), R.anim.refresh);
            animation.setRepeatMode(Animation.RESTART);
            animation.setRepeatCount(Animation.INFINITE);
        }

        public void bind(final SnsUser item) {
            this.snsUser = item;

            imgUserAvatar.setImageResource(R.drawable.shape_avatar_default_bg);
            SnsImageLoader.loadAvatar(snsUser.getPortraitUri(), imgUserAvatar, false);
            txtUserName.setText(snsUser.getNickName());
            txtReason.setText(snsUser.getRecommendReason());

            imgUserAvatar.setOnClickListener(onUserClickListener);
            txtUserName.setOnClickListener(onUserClickListener);

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

            if (closeView != null) {
                closeView.setOnClickListener(onCloseClickListener);
            }
        }

        private boolean checkLogin() {
            if (SnsModel.getInstance().isUserLoggedIn()) {
                return true;
            }

            NavigationHelper.navigateToLoginPageForResult(btnFollow.getContext());
            return false;
        }

        private OnClickListener onFollowClickListener = new OnClickListener() {

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

        private OnClickListener onCloseClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                snsUser.setFiltered();
            }
        };

        private OnClickListener onUserClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                NavigationHelper.navigateToUserProfilePage(v.getContext(), snsUser);
            }
        };
    }

}

