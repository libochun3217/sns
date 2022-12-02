package com.charlee.sns.activity;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.charlee.sns.R;
import com.charlee.sns.helper.NavigationHelper;
import com.charlee.sns.model.ISnsModel;
import com.charlee.sns.model.SnsModel;
import com.charlee.sns.view.EmptyPlaceholderView;
import com.charlee.sns.view.FeedsView;
import com.charlee.sns.view.PublicSquareView;
import com.charlee.sns.view.SnsFloatingActionButton;
import com.google.android.material.tabs.TabLayout;


/**
 * 社区首页
 */
public class SnsHomeFragment extends Fragment {
    private static final String PAGE_NAME = "SnsHomeFragment";

    private static final int PAGE_COUNT = 2;
    private static final int INDEX_FRAGMENT_PUBLIC_SQUARE = 0;
    private static final int INDEX_FRAGMENT_FEEDS = 1;

    private SimpleFragmentPagerAdapter pagerAdapter;

    private ViewPager viewPager;
    private int currentPage; // 记录当前页面

    private TabLayout tabLayout;

    private boolean feedSwitch = false;

    public static class PublicSquareFragment extends Fragment {
        PublicSquareView publicSquareView;
        private boolean isViewCreated = false;

        public static PublicSquareFragment newInstance() {
            PublicSquareFragment fragment = new PublicSquareFragment();
            return fragment;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            publicSquareView = new PublicSquareView(getActivity());
            isViewCreated = true;
            return publicSquareView;
        }

        public void refresh() {
            if (publicSquareView != null) {
                publicSquareView.refresh();
            }
        }

        public void scrollToTop() {
            if (isViewCreated) {
                publicSquareView.scrollToPosition(0);
            }
        }
    }

    public static class FeedsFragment extends Fragment {

        private ISnsModel model;

        private FrameLayout feedsContainer;
        private EmptyPlaceholderView placeholderView;
        private FeedsView feedsView;

        // 右下方浮动按钮控件
        private View bottomActionButton;
        private View bottomActionForegroundButton;
        private SnsFloatingActionButton bottomMenu;

        private boolean isViewCreated = false;

        public static FeedsFragment newInstance() {
            FeedsFragment fragment = new FeedsFragment();
            return fragment;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_feeds, container, false);
            feedsContainer = (FrameLayout) rootView.findViewById(R.id.feeds_container);
            placeholderView = (EmptyPlaceholderView) rootView.findViewById(R.id.empty_placeholder_feeds);
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

            model = SnsModel.getInstance();

            initPlaceholder();
            isViewCreated = true;

            return rootView;
        }

        @Override
        public void onResume() {
            super.onResume();
        }

        private void initPlaceholder() {
            placeholderView.setHintImage(R.drawable.ic_not_login);
            placeholderView.setHintString(R.string.hint_not_logged_in);
            placeholderView.setActionString(R.string.action_login);
            placeholderView.setActionClickedListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    NavigationHelper.navigateToLoginPageForResult(getActivity());
                }
            });
        }

        public void scrollToTop() {
            if (isViewCreated) {
                feedsView.scrollToPosition(0);
            }
        }
    }

    public class SimpleFragmentPagerAdapter extends FragmentPagerAdapter {

        private String[] tabTitles = new String[PAGE_COUNT];
        private Fragment[] fragments = new Fragment[PAGE_COUNT];

        public SimpleFragmentPagerAdapter(FragmentManager fm, Context context) {
            super(fm);
            tabTitles[INDEX_FRAGMENT_PUBLIC_SQUARE] = context.getResources().getString(R.string.home_tab_public_square);
            tabTitles[INDEX_FRAGMENT_FEEDS] = context.getResources().getString(R.string.home_tab_feeds);

            fragments[INDEX_FRAGMENT_PUBLIC_SQUARE] = PublicSquareFragment.newInstance();
            fragments[INDEX_FRAGMENT_FEEDS] = FeedsFragment.newInstance();
        }

        @Override
        public Fragment getItem(int position) {
            return fragments[position];
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabTitles[position];
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.activity_sns_home, container, false);

        pagerAdapter = new SimpleFragmentPagerAdapter(getChildFragmentManager(), getActivity());
        viewPager = (ViewPager) rootView.findViewById(R.id.viewpager);
        viewPager.setAdapter(pagerAdapter);
        tabLayout = (TabLayout) rootView.findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        setupBackToTopWhenClickTabTitle();
        registerViewPagerChangeListener();

        if (feedSwitch) {
            switchToFeeds();
        } else {
            switchToPublicSquare();
        }


        return rootView;
    }

    // 点击当前Tab标题时回到页面顶部。
    private void setupBackToTopWhenClickTabTitle() {
        // 注意：如果使用OnTabSelectedListener会导致ViewPager不能自动切换，
        //      用ViewPager.setCurrentItem切换又会导致触发onTabReselected
        tabLayout.setOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager) {
            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Tab切换时也会进入此回调，需要检查
                int index = tabLayout.getSelectedTabPosition();
                if (index != currentPage) { // viewPager.getCurrentItem已经变成新的Tab了，要和currentPage比
                    return;
                }

                if (index == INDEX_FRAGMENT_FEEDS) {
                    FeedsFragment feedsFragment = (FeedsFragment) pagerAdapter.getItem(INDEX_FRAGMENT_FEEDS);
                    feedsFragment.scrollToTop();
                } else if (index == INDEX_FRAGMENT_PUBLIC_SQUARE) {
                    PublicSquareFragment publicSquareFragment =
                            (PublicSquareFragment) pagerAdapter.getItem(INDEX_FRAGMENT_PUBLIC_SQUARE);
                    publicSquareFragment.scrollToTop();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == NavigationHelper.LOGIN_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                pagerAdapter.notifyDataSetChanged();
            }
        }

    }

    public void setFeedSwitch(boolean status) {
        feedSwitch = status;
        if (viewPager != null) {
            if (feedSwitch) {
                switchToFeeds();
            } else {
                switchToPublicSquare();
            }
        }
    }

    public void switchToPublicSquare() {
        if (viewPager.getCurrentItem() != INDEX_FRAGMENT_PUBLIC_SQUARE) {
            viewPager.setCurrentItem(INDEX_FRAGMENT_PUBLIC_SQUARE);
        } else {
        }
    }

    public void switchToFeeds() {
        if (viewPager.getCurrentItem() != INDEX_FRAGMENT_FEEDS) {
            viewPager.setCurrentItem(INDEX_FRAGMENT_FEEDS);
        } else {
        }
    }

    private void registerViewPagerChangeListener() {
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                currentPage = i; // 此回调比onTabReselected要晚
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
    }

}
