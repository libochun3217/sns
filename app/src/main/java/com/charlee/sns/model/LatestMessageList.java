package com.charlee.sns.model;

import androidx.annotation.NonNull;

import com.charlee.sns.data.Message;
import com.charlee.sns.data.PagedList;

import bolts.Task;

/**
 */
public class LatestMessageList extends MessageList {
    public LatestMessageList(@NonNull final IMotuSns motuSns,
                           @NonNull final IModelRepository repository) {
        super(motuSns, repository, IPageableList.TOTAL_SIZE_INFINITE, PagingType.IdBased);
    }

    @NonNull
    @Override
    protected Task<PagedList<Message>> getFirstPage() {
        return motuSns.getLatestMessages(IMotuSns.FIRST_PAGE_ID, IMotuSns.DEFAULT_PAGE_SIZE)
                .onSuccess(parseMessageResultContinuation);
    }

    @NonNull
    @Override
    protected Task<PagedList<Message>> getNextPage(@NonNull PagedList<Message> before) {
        String lastId = before.getLastId();
        if (lastId == null || lastId.isEmpty()) {
            return Task.forResult(null);
        }

        return motuSns.getLatestMessages(lastId, IMotuSns.DEFAULT_PAGE_SIZE)
                .onSuccess(parseMessageResultContinuation);
    }
}
