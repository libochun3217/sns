package com.charlee.sns.model;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.charlee.sns.BuildConfig;
import com.charlee.sns.data.Campaign;
import com.charlee.sns.data.CampaignResult;
import com.charlee.sns.data.Message;
import com.charlee.sns.data.PagedList;

import java.security.InvalidParameterException;
import java.util.Map;

import bolts.Continuation;
import bolts.Task;

/**
 */
public class SnsCampaign extends ModelBase {
    private final IMotuSns motuSns;
    private final IModelRepository repository;

    private Campaign campaign;

    // 一个活动对象的消息列表
    private IPageableList<UserMessage> messages;

    private static class CampaignMessage extends MessageList {
        private final String campaignId;
        private IndexPager indexPager = new IndexPager(IMotuSns.DEFAULT_PAGE_SIZE);

        public CampaignMessage(@NonNull IMotuSns motuSns, @NonNull IModelRepository repository, int totalSize,
                               PagingType pagingType, String campaignId) {
            super(motuSns, repository, totalSize, pagingType);
            this.campaignId = campaignId;
        }

        @NonNull
        @Override
        protected Task<PagedList<Message>> getFirstPage() {
            indexPager.reset();
            return motuSns.getCampaignMessages(campaignId, IMotuSns.FIRST_PAGE, IMotuSns.DEFAULT_PAGE_SIZE)
                    .onSuccess(parseMessageResultContinuation);
        }

        @NonNull
        @Override
        protected Task<PagedList<Message>> getNextPage(@NonNull PagedList<Message> before) {
            if (before == null || before.getData() == null || !before.hasMore()) {
                return Task.forResult(null);
            }

            indexPager.moveToNextPage(before.getData().size());
            return motuSns.getCampaignMessages(campaignId, indexPager.getCurrentStartIndex(),
                    IMotuSns.DEFAULT_PAGE_SIZE)
                    .onSuccess(parseMessageResultContinuation);
        }
    }

    // 仅供UserRepository调用以保证每条消息只有唯一实例
    SnsCampaign(@NonNull final IMotuSns motuSns,
                @NonNull final IModelRepository repository,
                @NonNull final Campaign campaign) {
        if (BuildConfig.DEBUG) {
            if (repository.getCampaignById(campaign.getId()) != null) {
                throw new InvalidParameterException("DO NOT create a different instance for the same ID!");
            }
        }

        this.motuSns = motuSns;
        this.repository = repository;
        update(campaign);
        itemId = Long.getLong(this.getId(),
                this.getId().hashCode() * this.getUrl().hashCode());
    }

    synchronized void update(@NonNull final Campaign other) {
        if (other == null || !other.isValid()) {
            if (BuildConfig.DEBUG) {
                // 如果在此处抛出异常请检查代码逻辑！！！！！！！！！
                throw new InvalidParameterException("Update Campaign with null object!");
            } else {
                return;
            }
        }

        boolean needUpdateMessages = false;
        if (this.campaign == null || other != null) {
            this.campaign = other;
            needUpdateMessages = true;
        }

        if (messages == null || needUpdateMessages) {
            messages = new CampaignMessage(motuSns, repository, IPageableList.TOTAL_SIZE_INFINITE,
                    PageableList.PagingType.IndexBased, campaign.getId());
        }

    }

    @NonNull
    public String getId() {
        return campaign.getId();
    }

    public boolean equalsId(String id) {
        return campaign.getId().equals(id);
    }

    public int getCampaignType() {
        return campaign.getCampaignType();
    }

    public long getStartTime() {
        return campaign.getStartTime();
    }

    public long getEndTime() {
        return campaign.getEndTime();
    }

    @NonNull
    public String getVersionMin() {
        return campaign.getVersionMin();
    }

    @NonNull
    public String getVersionMax() {
        return campaign.getVersionMax();
    }

    public int getToType() {
        return campaign.getToType();
    }

    public Integer getViewerNum() {
        return campaign.getViewerNum();
    }

    public Integer getParticipantNum() {
        return campaign.getParticipantNum();
    }

    public int getMaterialId() {
        Integer id = campaign.getMaterialId();
        return id == null ? 0 : id;
    }

    @NonNull
    public String getTitle() {
        return campaign.getTitle();
    }

    @NonNull
    public String getImg() {
        return campaign.getImg();
    }

    @NonNull
    public String getUrl() {
        return campaign.getUrl();
    }

    @NonNull
    public String getGift() {
        return campaign.getGift();
    }

    @NonNull
    public String getShare() {
        return campaign.getShare();
    }

    @NonNull
    public IPageableList<UserMessage> getMessages() {
        return messages;
    }

    public void setMessages(@NonNull IPageableList<UserMessage> msg) {
        messages = msg;
    }

    // region Uri Navigation

    // 注意：请不要将此常量公开！！！！！！！！！！导航的细节上层不需要知道！！！！！！！！！！
    private static final String QUERY_MESSAGE_ID = "id"; // 消息ID

    /**
     * 获取用户对象的导航查询参数。跳转后的Activity可以从ISnsModel.getUserByNavUri得到这个对象。
     *
     * @param queries 查询参数
     */
    public void getNavQuery(@NonNull Map<String, String> queries) {
        repository.assureCampaign(this);
        queries.put(QUERY_MESSAGE_ID, this.getId());
    }

    static Task<SnsCampaign> getCampaignByNavUri(@NonNull final IModelRepository repository,
                                                 @NonNull final IMotuSns motuSns,
                                                 @NonNull Uri uri,
                                                 boolean forceRefresh) {
        final String id = uri.getQueryParameter(QUERY_MESSAGE_ID);
        if (id == null) {
            return Task.forError(null);
        }

        if (!forceRefresh) {
            SnsCampaign snsCampaign = repository.getCampaignById(id);
            if (snsCampaign != null) {
                return Task.forResult(snsCampaign);
            }
        }

        return motuSns.getCampaign(id).onSuccess(
                new Continuation<CampaignResult, SnsCampaign>() {
                    @Override
                    public SnsCampaign then(Task<CampaignResult> task) throws Exception {
                        Campaign campaign = task.getResult().getCampaign();
                        return repository.getCampaignByData(campaign);
                    }
                });
    }

    static Task<SnsCampaign> getCampaignById(@NonNull final IModelRepository repository,
                                             @NonNull final IMotuSns motuSns,
                                             @NonNull final String id,
                                             boolean forceRefresh) {
        if (id == null) {
            return Task.forError(null);
        }

        if (!forceRefresh) {
            SnsCampaign snsCampaign = repository.getCampaignById(id);
            if (snsCampaign != null) {
                return Task.forResult(snsCampaign);
            }
        }

        return motuSns.getCampaign(id).onSuccess(
                new Continuation<CampaignResult, SnsCampaign>() {
                    @Override
                    public SnsCampaign then(Task<CampaignResult> task) throws Exception {
                        Campaign campaign = task.getResult().getCampaign();
                        return repository.getCampaignByData(campaign);
                    }
                });
    }

    // endregion

}
