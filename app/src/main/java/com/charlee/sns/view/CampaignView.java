package com.charlee.sns.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.charlee.sns.R;
import com.charlee.sns.helper.NavigationHelper;
import com.charlee.sns.helper.ReportHelper;
import com.charlee.sns.model.SnsCampaign;
import com.charlee.sns.model.SnsModel;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.HashMap;


/**
 * 运营活动的头部视图
 */
public class CampaignView extends FrameLayout {
    private static final String DEFAULT_TEXT = "...";

    public enum CampaignPosition {
        HomePage,
        DetailsPage,
        HistoryPage
    }

    private ImageView imgCover;
    private TextView txtParticipant;
    private TextView txtTimeRemain;
    private TextView txtIndicator;
    private TextView txtGiftHint;
    private View btnParticipate;

    private View viewExtra;
    private boolean isOver;

    public CampaignView(Context context) {
        super(context);
        init(null, 0);
    }

    public CampaignView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public CampaignView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    public void setCampaign(final SnsCampaign campaign, CampaignPosition position) {
        if (campaign == null) {
            return;
        }

        ImageLoader.getInstance().displayImage(campaign.getImg(), imgCover);

        // 检查是否过期
        long offset = SnsModel.getInstance().getTimeOffsetInSeconds();
        long currentSeconds = System.currentTimeMillis() / 1000 + offset;
        if (currentSeconds > campaign.getEndTime()) {
            txtTimeRemain.setCompoundDrawables(null, null, null, null);
            txtTimeRemain.setText(R.string.campaign_over);
            isOver = true;
        } else {
            int days = getDays(campaign.getEndTime() - currentSeconds);
            if (days == 0) {
                days = 1;
            }
            txtTimeRemain.setCompoundDrawables(getDrawable(R.drawable.ic_time_remaing), null, null, null);
            String timeDesc = getResources().getQuantityString(R.plurals.campaign_remaining_time, days, days);
            txtTimeRemain.setText(timeDesc);
        }

        txtGiftHint.setText(campaign.getGift());
        txtIndicator.setVisibility(VISIBLE);

        switch (position) {
            case HomePage:
                txtIndicator.setBackgroundResource(R.drawable.selector_image_campaign_history);
                txtIndicator.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        NavigationHelper.navigateToPath(getContext(), NavigationHelper.PATH_CAMPAIGN_HISTORY, null);
                    }
                });

                // participant num
                txtParticipant.setCompoundDrawables(getDrawable(R.drawable.ic_participant), null, null, null);
                txtParticipant.setText(getResources().getQuantityString(R.plurals.campaign_join_num,
                        campaign.getParticipantNum(), campaign.getParticipantNum()));

                // participate button
                btnParticipate.setVisibility(VISIBLE);
                btnParticipate.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        HashMap<String, String> queries = new HashMap<>();
                        campaign.getNavQuery(queries);
                        NavigationHelper.navigateToPath(btnParticipate.getContext(),
                                NavigationHelper.PATH_CAMPAIGN_DETAILS, queries);
                    }
                });
                viewExtra.setVisibility(GONE);
                break;
            case DetailsPage:
                txtIndicator.setBackgroundResource(R.drawable.selector_image_campaign_rules);
                txtIndicator.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startCampaignRulesPage(campaign);
                    }
                });

                // viewer num
                txtParticipant.setCompoundDrawables(getDrawable(R.drawable.ic_viewer), null, null, null);
                txtParticipant.setText(getResources().getQuantityString(R.plurals.campaign_viewer_num,
                        campaign.getViewerNum(), campaign.getViewerNum()));

                // gift
                txtGiftHint.setBackgroundResource(R.drawable.selector_button_campaign_gift_bg);
                txtGiftHint.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startCampaignRulesPage(campaign);
                    }
                });
                viewExtra.setVisibility(VISIBLE);
                break;
            case HistoryPage:
                // participant num
                txtParticipant.setCompoundDrawables(getDrawable(R.drawable.ic_participant), null, null, null);
                txtParticipant.setText(getResources().getQuantityString(R.plurals.campaign_join_num,
                        campaign.getParticipantNum(), campaign.getParticipantNum()));
                txtIndicator.setVisibility(GONE);
                viewExtra.setVisibility(GONE);
                break;
            default:
                break;
        }
    }

    public void setCampaignDefault(CampaignPosition position) {
        switch (position) {
            case HomePage:
                // participant num
                txtParticipant.setCompoundDrawables(getDrawable(R.drawable.ic_participant), null, null, null);
                break;
            case DetailsPage:
                // viewer num
                txtParticipant.setCompoundDrawables(getDrawable(R.drawable.ic_viewer), null, null, null);
                break;
            case HistoryPage:
                // participant num
                txtParticipant.setCompoundDrawables(getDrawable(R.drawable.ic_participant), null, null, null);
                break;
            default:
                break;
        }
        txtParticipant.setText(DEFAULT_TEXT);
        txtTimeRemain.setText(DEFAULT_TEXT);
        txtIndicator.setVisibility(GONE);
        viewExtra.setVisibility(GONE);
    }

    private void init(AttributeSet attrs, int defStyle) {
        View.inflate(getContext(), R.layout.view_campaign_item, this);
        initWidgets();
    }

    private void initWidgets() {
        imgCover = (ImageView) findViewById(R.id.img_cover);
        txtIndicator = (TextView) findViewById(R.id.txt_history_rules_indicator);
        txtParticipant = (TextView) findViewById(R.id.txt_participant_num);
        txtTimeRemain = (TextView) findViewById(R.id.txt_time_remaining);
        txtGiftHint = (TextView) findViewById(R.id.txt_gift_hint);
        btnParticipate = findViewById(R.id.btn_participate);
        viewExtra = findViewById(R.id.view_extra_info);
    }

    private int getDays(long times) {
        int oneDay = 24 * 60 * 60;
        return (int) (times / oneDay);
    }

    private Drawable getDrawable(int id) {
        Drawable drawable = ContextCompat.getDrawable(getContext(), id);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        return drawable;
    }

    private void startCampaignRulesPage(SnsCampaign campaign) {
        NavigationHelper.navigateToMainProjectRecommandSpPageForResult((Activity) getContext(),
                campaign, isOver);

        ReportHelper.clickCampaignRules(getContext(), campaign.getId());
    }

}
