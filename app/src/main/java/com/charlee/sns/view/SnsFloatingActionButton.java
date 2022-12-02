package com.charlee.sns.view;


import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.charlee.sns.R;
import com.charlee.sns.helper.NavigationHelper;
import com.charlee.sns.helper.NetworkMonitor;
import com.charlee.sns.model.IObservable;
import com.charlee.sns.model.IObserver;
import com.charlee.sns.widget.floatingmenu.FloatingActionMenu;
import com.charlee.sns.widget.floatingmenu.SubActionButton;

/**
 */
public class SnsFloatingActionButton {

    private final Context context;
    private final FrameLayout overlayContainer;
    private final View buttonContainer;
    private final View buttonForeground;
    private String eventKey;

    // 按钮菜单控件
    private FloatingActionMenu actionMenu;

    // 按钮菜单控件子按钮
    private SubActionButton subActionButtonCamera;
    private SubActionButton subActionButtonPicture;
//    private SubActionButton subActionButtonBeautify;

    private boolean isNetworkAvailable;

    private IObserver networkObserver = new IObserver() {
        @Override
        public void update(IObservable<IObserver> observable, Object data) {
            NetworkMonitor monitor = (NetworkMonitor) observable;
            isNetworkAvailable = monitor.isNetworkAvailable();
            boolean btnShown = buttonContainer.getVisibility() == View.VISIBLE;
            if (isNetworkAvailable) {
                if (!btnShown) {
                    buttonContainer.setVisibility(View.VISIBLE);
                }
            } else {
                if (btnShown) {
                    if (isOpen()) {
                        close(true);
                    }
                    buttonContainer.setVisibility(View.GONE);
                }
            }
        }
    };

    private View.OnClickListener onCameraClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            NavigationHelper.navigateToMainProjectCameraPage(v.getContext());
            actionMenu.close(true);
        }
    };

    private View.OnClickListener onPictureClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            NavigationHelper.navigateToMainProjectPickPicturePage(v.getContext());
            actionMenu.close(true);
        }
    };


//    private View.OnClickListener onBeautifyClickListener = new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            boolean isGooglePlayChannel = BuildConfig.FLAVOR.equals("googleplay");
//            if (isGooglePlayChannel) {
//                NavigationHelper.navigateToMainProjectMV(v.getContext());
//                EventReport.onEvent(context, EventConstant.EVENT_MV_ENTER, EventConstant.LABEL_MV_ENTER_SNS);
//                EventReport.onEvent(context, EventConstant.EVENT_MV_SOURCE, EventConstant.LABEL_MV_ENTER_SNS);
//            } else {
//                NavigationHelper.navigateToMainProjectPickPictureThenBeautify(v.getContext());
//                EventReport.onEvent(context, EventConstant.EVENT_PHOTO_SOURCE, EventConstant.LABEL_FLOAT_BTN_BEAUTY);
//            }
//            actionMenu.close(true);
//        }
//    };

    private static final int LIST_VIEW_SCROLL_DIRECT_UP = -1;
    private static final int LIST_VIEW_SCROLL_DIRECT_NONE = 0;
    private static final int LIST_VIEW_SCROLL_DIRECT_DOWN = 1;
    private int latestListViewScrollDirect = LIST_VIEW_SCROLL_DIRECT_NONE;

    private RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            int scrollDirect;
            if (dy > 0) {
                scrollDirect = LIST_VIEW_SCROLL_DIRECT_UP;
            } else if (dy < 0) {
                scrollDirect = LIST_VIEW_SCROLL_DIRECT_DOWN;
            } else {
                scrollDirect = LIST_VIEW_SCROLL_DIRECT_NONE;
            }

            if (scrollDirect != SnsFloatingActionButton.this.latestListViewScrollDirect) {
                if (scrollDirect == LIST_VIEW_SCROLL_DIRECT_UP
                        && actionMenu.isOpen()) {
                    actionMenu.close(true);
                }

                if (scrollDirect == LIST_VIEW_SCROLL_DIRECT_UP) {
                    buttonContainer.setVisibility(View.GONE);
                } else if (scrollDirect == LIST_VIEW_SCROLL_DIRECT_DOWN) {
                    buttonContainer.setVisibility(View.VISIBLE);
                }

                SnsFloatingActionButton.this.latestListViewScrollDirect = scrollDirect;
            }
        }
    };

    public SnsFloatingActionButton(Context context, FrameLayout overlayContainer,
                                   final View buttonContainer, View buttonForeground) {
        this.context = context;
        this.overlayContainer = overlayContainer;
        this.buttonContainer = buttonContainer;
        this.buttonForeground = buttonForeground;
        build();
    }

    public void setEventKey(String eventkey) {
        this.eventKey = eventkey;
    }

    public boolean isOpen() {
        return actionMenu.isOpen();
    }

    public void close(boolean animated) {
        actionMenu.close(animated);
    }

    public void setVisible(boolean visible) {
        if (buttonContainer != null) {
            buttonContainer.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
        }
    }

    public RecyclerView.OnScrollListener getOnScrollListener() {
        return onScrollListener;
    }

    private void build() {
        Resources resources = context.getResources();
        SubActionButton.Builder rLSubBuilder = new SubActionButton.Builder(context)
                .setBackgroundDrawable(resources.getDrawable(R.drawable.selector_button_sub_action));
        ImageView rlIconCamera = new ImageView(context);
        ImageView rlIconPicture = new ImageView(context);
        // ImageView rlIconBeautify = new ImageView(context);

        rlIconCamera.setImageDrawable(resources.getDrawable(R.drawable.ic_action_camera_light));
        rlIconPicture.setImageDrawable(resources.getDrawable(R.drawable.ic_action_picture_light));
//        boolean isGooglePlayChannel = BuildConfig.FLAVOR.equals("googleplay");
//        if (isGooglePlayChannel) {
//            rlIconBeautify.setImageDrawable(resources.getDrawable(R.drawable.ic_action_share_beautified));
//        } else {
//            rlIconBeautify.setImageDrawable(resources.getDrawable(R.drawable.ic_action_beautified));
//        }

        subActionButtonCamera = rLSubBuilder.setContentView(rlIconCamera).build();
        subActionButtonPicture = rLSubBuilder.setContentView(rlIconPicture).build();
//        subActionButtonBeautify = rLSubBuilder.setContentView(rlIconBeautify).build();

        subActionButtonCamera.setId(R.id.floating_menu_sub_action_camera);
        subActionButtonCamera.setOnClickListener(onCameraClickListener);

        subActionButtonPicture.setId(R.id.floating_menu_sub_action_picture);
        subActionButtonPicture.setOnClickListener(onPictureClickListener);

//        subActionButtonBeautify.setId(R.id.floating_menu_sub_action_beautify);
//        subActionButtonBeautify.setOnClickListener(onBeautifyClickListener);

        actionMenu = new FloatingActionMenu.Builder(context)
                .setSystemOverlay(false)
                .setOverlayContainerInChildView(overlayContainer)
//                .addSubActionView(subActionButtonBeautify)
                .addSubActionView(subActionButtonCamera)
                .addSubActionView(subActionButtonPicture)
                .setStartAngle(-120)
                .setEndAngle(-180)
                .setRadius(resources.getDimensionPixelSize(R.dimen.radius_middle))
                .attachTo(buttonContainer, buttonForeground)
                .build();
        actionMenu.setStateChangeListener(new FloatingActionMenu.MenuStateChangeListener() {
            @Override
            public void onMenuOpened(FloatingActionMenu menu) {
            }

            @Override
            public void onMenuClosed(FloatingActionMenu menu) {

            }
        });
    }

    public interface IShowListener {
        void onShow(boolean show);
    }

}
