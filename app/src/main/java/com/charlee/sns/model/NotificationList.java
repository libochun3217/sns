package com.charlee.sns.model;


import androidx.annotation.NonNull;

import com.charlee.sns.data.NotificationItem;
import com.charlee.sns.data.NotificationResult;
import com.charlee.sns.data.PagedList;

import bolts.Continuation;
import bolts.Task;

/**
 */
public class NotificationList extends PageableList<UserNotification, NotificationItem> {
    private String mLastId = IMotuSns.FIRST_PAGE_ID;

    public NotificationList(@NonNull final IMotuSns motuSns, @NonNull final IModelRepository repository) {
        super(motuSns, repository, IPageableList.TOTAL_SIZE_INFINITE, PagingType.IndexBased);
    }

    private Continuation<NotificationResult, PagedList<NotificationItem>> continuation =
            new Continuation<NotificationResult, PagedList<NotificationItem>>() {
                @Override
                public PagedList<NotificationItem> then(Task<NotificationResult> task) throws Exception {
                    PagedList<NotificationItem> list = task.getResult().getItems();
                    if (list != null) {
                        mLastId = list.getLastId();
                    }
                    return list;
                }
            };

    @NonNull
    @Override
    protected Task<PagedList<NotificationItem>> getFirstPage() {
        return motuSns.getNotifications(IMotuSns.FIRST_PAGE_ID, IMotuSns.DEFAULT_PAGE_SIZE)
                        .onSuccess(continuation);
    }

    @NonNull
    @Override
    protected Task<PagedList<NotificationItem>> getNextPage(@NonNull PagedList<NotificationItem> before) {
        if (before == null || !before.hasMore()) {
            return Task.forResult(null);
        }

        return motuSns.getNotifications(mLastId, IMotuSns.DEFAULT_PAGE_SIZE).onSuccess(continuation);
    }

    @Override
    protected UserNotification createModel(@NonNull NotificationItem data) {
        return repository.getUserNotificationByData(data);
    }

    @Override
    protected boolean hasSameId(@NonNull UserNotification model, @NonNull NotificationItem data) {
        return false;
    }

    public String getLastId() {
        return mLastId;
    }
}
