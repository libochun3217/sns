package com.charlee.sns.model;


import androidx.annotation.NonNull;

import com.charlee.sns.data.Message;
import com.charlee.sns.data.PagedList;

import bolts.Continuation;
import bolts.Task;

/**
 */
public class FeedsList extends MessageList {
    // 服务器返回的User只有基本信息，需要手动设置已经关注标志
    protected Continuation<PagedList<Message>, PagedList<Message>> setUserInfoContinuation =
            new Continuation<PagedList<Message>, PagedList<Message>>() {
                @Override
                public PagedList<Message> then(Task<PagedList<Message>> task) throws Exception {
                    for (Message item : task.getResult().getData()) {
                        item.getUser().setIsFollowed(true);
                    }

                    return task.getResult();
                }
            };

    public FeedsList(@NonNull final IMotuSns motuSns, @NonNull final IModelRepository repository) {
        super(motuSns, repository, IPageableList.TOTAL_SIZE_INFINITE, PagingType.IdBased);
    }

    @NonNull
    @Override
    protected Task<PagedList<Message>> getFirstPage() {
        return motuSns.getFeeds(IMotuSns.FIRST_PAGE_ID, IMotuSns
                .DEFAULT_PAGE_SIZE)
                .onSuccess(parseMessageResultContinuation).onSuccess(setUserInfoContinuation);
    }

    @NonNull
    @Override
    protected Task<PagedList<Message>> getNextPage(@NonNull PagedList<Message> before) {
        String lastId = before.getLastId();
        if (lastId == null || lastId.isEmpty()) {
            return Task.forResult(null);
        }

        return motuSns.getFeeds(lastId, IMotuSns.DEFAULT_PAGE_SIZE)
                .onSuccess(parseMessageResultContinuation).onSuccess(setUserInfoContinuation);
    }
}
