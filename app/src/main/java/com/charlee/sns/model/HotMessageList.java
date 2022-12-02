package com.charlee.sns.model;

import androidx.annotation.NonNull;

import com.charlee.sns.data.Message;
import com.charlee.sns.data.PagedList;

import bolts.Task;

/**
 */
public class HotMessageList extends MessageList {
    private IndexPager indexPager = new IndexPager(IMotuSns.DEFAULT_PAGE_SIZE);

    public HotMessageList(@NonNull final IMotuSns motuSns, @NonNull final IModelRepository repository) {
        super(motuSns, repository, IPageableList.TOTAL_SIZE_INFINITE, PageableList.PagingType.IndexBased);
    }

    @NonNull
    @Override
    protected Task<PagedList<Message>> getFirstPage() {
        indexPager.reset();
        return motuSns.getHotMessages(IMotuSns.FIRST_PAGE, IMotuSns.DEFAULT_PAGE_SIZE)
                .onSuccess(parseMessageResultContinuation);
    }

    @NonNull
    @Override
    protected Task<PagedList<Message>> getNextPage(@NonNull PagedList<Message> before) {
        if (before == null || !before.hasMore()) {
            return Task.forResult(null);
        }

        indexPager.moveToNextPage(before.getData().size());
        return motuSns.getHotMessages(indexPager.getCurrentStartIndex(), IMotuSns.DEFAULT_PAGE_SIZE)
                .onSuccess(parseMessageResultContinuation);
    }
}
