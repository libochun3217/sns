package com.charlee.sns.model.userlist;


import androidx.annotation.NonNull;

import com.charlee.sns.data.PagedList;
import com.charlee.sns.data.UserInfo;
import com.charlee.sns.data.UsersResult;
import com.charlee.sns.model.IModelRepository;
import com.charlee.sns.model.IMotuSns;
import com.charlee.sns.model.IPageableList;
import com.charlee.sns.model.PageableList;
import com.charlee.sns.model.SnsUser;

import bolts.Continuation;
import bolts.Task;

/**
 */
public class FollowersUserList extends PageableList<SnsUser, UserInfo> {
    private final SnsUser snsUser;

    public FollowersUserList(@NonNull final IMotuSns motuSns, @NonNull final IModelRepository repository,
                             @NonNull final SnsUser snsUser) {
        super(motuSns, repository, IPageableList.TOTAL_SIZE_INFINITE, PagingType.IdBased);
        this.snsUser = snsUser;
    }

    private Continuation<UsersResult, PagedList<UserInfo>> continuation =
            new Continuation<UsersResult, PagedList<UserInfo>>() {
                @Override
                public PagedList<UserInfo> then(Task<UsersResult> task) throws Exception {
                    return task.getResult().getUserList();
                }
            };

    @NonNull
    @Override
    protected Task<PagedList<UserInfo>> getFirstPage() {
        return motuSns.getUserFollowers(snsUser.getId(), IMotuSns.FIRST_PAGE_ID, IMotuSns.DEFAULT_PAGE_SIZE)
                .onSuccess(continuation);
    }

    @NonNull
    @Override
    protected Task<PagedList<UserInfo>> getNextPage(@NonNull PagedList<UserInfo> before) {
        if (before == null || !before.hasMore()) {
            return Task.forResult(null);
        }

        String lastId = before.getLastId();
        if (lastId == null || lastId.isEmpty()) {
            return Task.forResult(null);
        }

        return motuSns.getUserFollowers(snsUser.getId(), lastId, IMotuSns.DEFAULT_PAGE_SIZE)
                .onSuccess(continuation);
    }

    @Override
    protected SnsUser createModel(@NonNull UserInfo data) {
        return repository.getUserByData(data);
    }

    @Override
    protected boolean hasSameId(@NonNull SnsUser model, @NonNull UserInfo data) {
        return model.getId().equals(data.getId());
    }
}
