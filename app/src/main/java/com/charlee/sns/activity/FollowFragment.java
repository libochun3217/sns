package com.charlee.sns.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.charlee.sns.R;
import com.charlee.sns.helper.NavigationHelper;
import com.charlee.sns.view.FeedsView;
import com.charlee.sns.view.SnsFloatingActionButton;


/**
 * 关注流页面
 */
public class FollowFragment extends Fragment {
    private static final String PAGE_NAME = "FollowFragment";

    private FrameLayout feedsContainer;
    private FeedsView feedsView;

    // 右下方浮动按钮控件
    private View bottomActionButton;
    private View bottomActionForegroundButton;
    private SnsFloatingActionButton bottomMenu;

    public static FollowFragment newInstance() {
        FollowFragment fragment = new FollowFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_follow, container, false);
        feedsContainer = (FrameLayout) rootView.findViewById(R.id.feeds_container);
        feedsView = (FeedsView) rootView.findViewById(R.id.feeds_view);

        // 初始化浮动按钮
        bottomActionButton = rootView.findViewById(R.id.feeds_floating_action_button);
        bottomActionForegroundButton = rootView.findViewById(R.id.floating_action_foreground);
        bottomMenu = new SnsFloatingActionButton(getActivity(), feedsContainer,
                bottomActionButton, bottomActionForegroundButton);
        feedsView.addOnScollListener(bottomMenu.getOnScrollListener());
        feedsView.addFloatButtonShowListener(new SnsFloatingActionButton.IShowListener() {
            @Override
            public void onShow(boolean show) {
                if (show) {
                    bottomMenu.setVisible(show);
                }
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        onResumePlayer();
    }

    @Override
    public void onPause() {
        super.onPause();

        onPausePlayer();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == NavigationHelper.LOGIN_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                feedsView.update();
            }
        }
    }

    public void onResumePlayer() {
        if (feedsView != null) {
            feedsView.onResume();
        }
    }

    public void onPausePlayer() {
        if (feedsView != null) {
            feedsView.onPause();
        }
    }

    public void scrollToHead() {
        if (feedsView != null) {
            feedsView.scrollToPosition(0);
        }
    }
}
