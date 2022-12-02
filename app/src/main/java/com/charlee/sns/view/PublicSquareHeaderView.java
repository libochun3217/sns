package com.charlee.sns.view;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;


import androidx.fragment.app.FragmentActivity;

import com.charlee.sns.R;
import com.charlee.sns.adapter.LatestMsgListAdapter;
import com.charlee.sns.adapter.TagListAdapter;
import com.charlee.sns.helper.NavigationHelper;
import com.charlee.sns.model.IPageableList;
import com.charlee.sns.model.ISnsModel;
import com.charlee.sns.model.SnsCampaign;
import com.charlee.sns.model.SnsModel;
import com.charlee.sns.widget.HorizontalListView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import bolts.Continuation;
import bolts.Task;

/**
 */
public class PublicSquareHeaderView extends LinearLayout {

    private final Context context;

    private ISnsModel model;
    private IPageableList<SnsCampaign> campaignList;

    private CampaignView campaignView;

    // 开关，分别控制TAG列表和最新消息列表是否显示
    private boolean latestSwitch = false;
    private boolean tagSwitch = false;

    // 标签列表
    private ViewGroup tagsListSection;
    private HorizontalListView tagsListView;
    private TagListAdapter tagListAdapter;

    // 最新消息列表
    private ViewGroup latestMsgListSection;
    private HorizontalListView latestMsgListView;
    private LatestMsgListAdapter latestMsgListAdapter;

    // 屏幕尺寸，用于布局计算
    private DisplayMetrics metrics = new DisplayMetrics();

    public PublicSquareHeaderView(Context context) {
        super(context);
        this.context = context;
        init(null, 0);
    }

    public PublicSquareHeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init(attrs, 0);
    }

    public PublicSquareHeaderView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        init(attrs, defStyle);
    }

    public Task<Void> refresh() {
        Collection<Task<?>> tasks = new ArrayList<>();
        if (tagSwitch) {
            tasks.add(tagListAdapter.refresh().continueWith(new Continuation<Boolean, Object>() {
                @Override
                public Object then(Task<Boolean> task) throws Exception {
                    if ((task.isFaulted() || !task.getResult()) && tagListAdapter.getItemCount() == 0) {
                        tagsListSection.setVisibility(GONE);
                    } else {
                        tagsListSection.setVisibility(VISIBLE);
                    }

                    return null;
                }
            }, Task.UI_THREAD_EXECUTOR));
        }

        if (latestSwitch) {
            tasks.add(latestMsgListAdapter.refresh().continueWith(new Continuation<Boolean, Object>() {
                @Override
                public Object then(Task<Boolean> task) throws Exception {
                    if ((task.isFaulted() || !task.getResult()) && latestMsgListAdapter.getItemCount() == 0) {
                        latestMsgListSection.setVisibility(GONE);
                    } else {
                        latestMsgListSection.setVisibility(VISIBLE);
                    }

                    return null;
                }
            }, Task.UI_THREAD_EXECUTOR));
        }

        try {
            tasks.add(campaignList.refresh().continueWith(new Continuation<Boolean, Object>() {
                @Override
                public Object then(Task<Boolean> task) throws Exception {
                    if (!task.isFaulted() && campaignList.size() > 0) {
                        campaignView.setVisibility(VISIBLE);
                        campaignView.setCampaign(campaignList.get(0), CampaignView.CampaignPosition.HomePage);
                    }
                    return null;
                }
            }, Task.UI_THREAD_EXECUTOR));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Task.whenAll(tasks);
    }

    public boolean areAllListEmpty() {
        return (tagSwitch ? tagListAdapter.getItemCount() == 0 : true)
                && (latestSwitch ? latestMsgListAdapter.getItemCount() == 0 : true);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    private void init(AttributeSet attrs, int defStyle) {
        View.inflate(getContext(), R.layout.view_public_square_header, this);
        initMetrics();
        initWidgets();
    }

    private void initWidgets() {
        model = SnsModel.getInstance();

        initCampaignView();
        initTagListSection();
        initLatestMsgsSection();

        refresh(); // 获取数据
    }

    private void initCampaignView() {
        campaignList = model.getCampaignActiveList();

        campaignView = (CampaignView) findViewById(R.id.view_campaign_header);
        campaignView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (campaignList == null || campaignList.size() == 0) {
                    return;
                }
                HashMap<String, String> queries = new HashMap<>();
                campaignList.get(0).getNavQuery(queries);
                NavigationHelper.navigateToPath(context, NavigationHelper.PATH_CAMPAIGN_DETAILS, queries);
            }
        });
        campaignView.setCampaignDefault(CampaignView.CampaignPosition.HomePage);
    }

    private void initTagListSection() {
        tagsListSection = (ViewGroup) findViewById(R.id.tag_list_section);
        if (tagSwitch) {
            tagsListView = (HorizontalListView) findViewById(R.id.tag_list);
            tagListAdapter = new TagListAdapter(model.getHotTags());
            // 初始化标签列表宽度
            tagListAdapter.init(getContext(), metrics.widthPixels); // 两侧没有边距
            tagsListView.setAdapter(tagListAdapter);
            ViewGroup.LayoutParams tagsListLayout = tagsListView.getLayoutParams();
            tagsListLayout.height = tagListAdapter.getListHeight();
            tagsListView.setLayoutParams(tagsListLayout);

            findViewById(R.id.tag_more).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    NavigationHelper.navigateToPath(context, NavigationHelper.PATH_FULL_TAG_LIST, null);
                }
            });
        } else {
            tagsListSection.setVisibility(GONE);
        }
    }

    private void initLatestMsgsSection() {
        latestMsgListSection = (ViewGroup) findViewById(R.id.latest_msg_list_section);
        if (latestSwitch) {
            latestMsgListView = (HorizontalListView) findViewById(R.id.latest_msg_list);
            latestMsgListAdapter = new LatestMsgListAdapter(model.getLatestMessages());
            // 初始化最新消息列表宽度
            latestMsgListAdapter.init(getContext(), metrics.widthPixels); // 两侧没有边距
            latestMsgListView.setAdapter(latestMsgListAdapter);
            ViewGroup.LayoutParams latestMsgListLayout = latestMsgListView.getLayoutParams();
            latestMsgListLayout.height = latestMsgListAdapter.getListHeight();
            latestMsgListView.setLayoutParams(latestMsgListLayout);

            findViewById(R.id.latest_msg_more).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    NavigationHelper.navigateToPath(context, NavigationHelper.PATH_LATEST_MESSAGE_LIST, null);
                }
            });
        } else {
            latestMsgListSection.setVisibility(GONE);
        }
    }

    private void initMetrics() {
        Activity activity;
        if (context instanceof FragmentActivity) {
            activity = (Activity) context;
        } else {
            return;
        }
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
    }
}
