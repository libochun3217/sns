package com.charlee.sns.data;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 返回一条活动的详情
 * 注意：成员变量命名要和JSON对应。GSON转换规则为FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES
 */
public class CampaignResult extends ResultBase {
    private final Campaign campaign;

    public CampaignResult(int errCode, @Nullable String errMsg, Boolean hasMore,
                          @NonNull Campaign campaign) {
        super(errCode, errMsg, hasMore);
        this.campaign = campaign;
    }

    @NonNull
    public Campaign getCampaign() {
        return campaign;
    }

    @Override
    public boolean isValid() {
        return campaign != null && campaign.isValid();
    }

    @Override
    public void setServerTimeStamp(long serverTimeStamp) {
        super.setServerTimeStamp(serverTimeStamp);
        if (campaign != null) {
            campaign.setUpdateTime(serverTimeStamp);
        }
    }
}
