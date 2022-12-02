package com.charlee.sns.model;


import androidx.annotation.NonNull;

import com.charlee.sns.data.Campaign;
import com.charlee.sns.data.CampaignsResult;
import com.charlee.sns.data.MotuSnsService;
import com.charlee.sns.data.PagedList;

import bolts.Continuation;
import bolts.Task;

/**
 */
public class CampaignHistoryList extends PageableList<SnsCampaign, Campaign> {

    public CampaignHistoryList(@NonNull final IMotuSns motuSns, @NonNull final IModelRepository repository) {
        super(motuSns, repository, IPageableList.TOTAL_SIZE_INFINITE, PagingType.IdBased);
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
        return motuSns.getHistoryCampaigns(MotuSnsService.CAMPAIGN_TYPE_OPERATION,
                IMotuSns.FIRST_PAGE_ID, IMotuSns.DEFAULT_PAGE_SIZE).onSuccess(continuation);
    }

    @NonNull
    @Override
    protected Task<PagedList<Campaign>> getNextPage(@NonNull PagedList<Campaign> before) {
        if (before == null || !before.hasMore()) {
            return Task.forResult(null);
        }

        String lastId = before.getLastId();
        if (lastId == null || lastId.isEmpty()) {
            return Task.forResult(null);
        }

        return motuSns.getHistoryCampaigns(MotuSnsService.CAMPAIGN_TYPE_OPERATION,
                lastId, IMotuSns.DEFAULT_PAGE_SIZE).onSuccess(continuation);
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
