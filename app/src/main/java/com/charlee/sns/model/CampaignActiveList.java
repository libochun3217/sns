package com.charlee.sns.model;


import androidx.annotation.NonNull;

import com.charlee.sns.data.Campaign;
import com.charlee.sns.data.CampaignsResult;
import com.charlee.sns.data.PagedList;

import bolts.Continuation;
import bolts.Task;

/**
 */
public class CampaignActiveList extends PageableList<SnsCampaign, Campaign> {
    private final int campaignType;

    public CampaignActiveList(@NonNull final IMotuSns motuSns, @NonNull final IModelRepository repository,
                              int campaignType) {
        super(motuSns, repository, IPageableList.TOTAL_SIZE_INFINITE, PagingType.IdBased);
        this.campaignType = campaignType;
    }

    private Continuation<CampaignsResult, PagedList<Campaign>> continuation =
            new Continuation<CampaignsResult, PagedList<Campaign>>() {
                @Override
                public PagedList<Campaign> then(Task<CampaignsResult> task) throws Exception {
                    return task.getResult().getCampaignList();
                }
            };

    @NonNull
    @Override
    protected Task<PagedList<Campaign>> getFirstPage() {
        return motuSns.getActiveCampaigns(campaignType).onSuccess(continuation);
    }

    @NonNull
    @Override
    protected Task<PagedList<Campaign>> getNextPage(@NonNull PagedList<Campaign> before) {
        // 当前运营的活动，一次请求返回全部活动的列表，不需要翻页请求
        return motuSns.getActiveCampaigns(campaignType).onSuccess(continuation);
    }

    @Override
    protected SnsCampaign createModel(@NonNull Campaign data) {
        return repository.getCampaignByData(data);
    }

    @Override
    protected boolean hasSameId(@NonNull SnsCampaign model, @NonNull Campaign data) {
        return model.getId().equals(data.getId());
    }
}
