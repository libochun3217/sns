package com.charlee.sns.data;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 运营活动列表返回结果
 * 注意：成员变量命名要和JSON对应。GSON转换规则为FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES
 */
public class CampaignsResult extends ResultBase {
    private final PagedList<Campaign> campaigns;

    public CampaignsResult(int errCode, @Nullable String errMsg, Boolean hasMore,
                           @NonNull PagedList<Campaign> campaigns) {
        super(errCode, errMsg, hasMore);
        this.campaigns = campaigns;
    }

    @NonNull
    public PagedList<Campaign> getCampaignList() {
        return campaigns;
    }

    @Override
    public boolean isValid() {
        return campaigns != null && campaigns.isValid();
    }

    @Override
    public void setServerTimeStamp(long serverTimeStamp) {
        super.setServerTimeStamp(serverTimeStamp);
        if (campaigns != null) {
            campaigns.setUpdateTime(serverTimeStamp);
        }
    }
}
