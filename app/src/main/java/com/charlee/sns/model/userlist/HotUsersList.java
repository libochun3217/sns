package com.charlee.sns.model.userlist;


import androidx.annotation.NonNull;

import com.charlee.sns.data.PagedList;
import com.charlee.sns.data.UserInfo;
import com.charlee.sns.data.UsersResult;
import com.charlee.sns.model.IModelRepository;
import com.charlee.sns.model.IMotuSns;
import com.charlee.sns.model.IPageableList;
import com.charlee.sns.model.IndexPager;
import com.charlee.sns.model.PageableList;
import com.charlee.sns.model.SnsModel;
import com.charlee.sns.model.SnsUser;

import java.util.Iterator;
import java.util.List;

import bolts.Continuation;
import bolts.Task;

/**
 */
public class HotUsersList extends PageableList<SnsUser, UserInfo> {
    private static final int LEAST_LIST_SIZE = 5;

    private IndexPager indexPager = new IndexPager(IMotuSns.DEFAULT_PAGE_SIZE);

    public HotUsersList(@NonNull final IMotuSns motuSns, @NonNull final IModelRepository repository) {
        super(motuSns, repository, IPageableList.TOTAL_SIZE_INFINITE, PagingType.IndexBased);
    }

    private Continuation<UsersResult, PagedList<UserInfo>> continuation =
            new Continuation<UsersResult, PagedList<UserInfo>>() {
                @Override
                public PagedList<UserInfo> then(Task<UsersResult> task) throws Exception {
                    PagedList<UserInfo> resultList = task.getResult().getUserList();
                    List<UserInfo> userList = resultList.getData();
                    Iterator<UserInfo> itr = userList.iterator();
                    while (itr.hasNext()) {
                        UserInfo info = itr.next();
                        if (info.isFollowed() != null && info.isFollowed() == true) {
                            itr.remove();
                        }
                        if (SnsModel.getInstance().isUserLoggedIn()) {
                            if (info.getId().equals(SnsModel.getInstance().getLoginUser().getId())) {
                                itr.remove();
                            }
                        }
                    }
                    return new PagedList<>(resultList.hasMore(), resultList.getLastId(), userList);
                }
            };

    private Continuation<PagedList<UserInfo>, PagedList<UserInfo>> checkListSize =
            new Continuation<PagedList<UserInfo>, PagedList<UserInfo>>() {
                @Override
                public PagedList<UserInfo> then(Task<PagedList<UserInfo>> task) throws Exception {
                    final PagedList<UserInfo> resultList = task.getResult();
                    if (resultList != null && resultList.getData().size() < LEAST_LIST_SIZE) {
                        getNextPage(resultList).continueWith(new Continuation<PagedList<UserInfo>, Object>() {
                            @Override
                            public Object then(Task<PagedList<UserInfo>> task) throws Exception {
                                resultList.getData().addAll(resultList.getData().size(), task.getResult().getData());
                                return null;
                            }
                        }).waitForCompletion();
                        return resultList;
                    }
                    return resultList;
                }
            };

    @NonNull
    @Override
    protected Task<PagedList<UserInfo>> getFirstPage() {
        indexPager.reset();
        return motuSns.getHotUsers(IMotuSns.FIRST_PAGE, IMotuSns.DEFAULT_PAGE_SIZE)
                .continueWith(continuation)
                .continueWith(checkListSize, Task.BACKGROUND_EXECUTOR);
    }

    @NonNull
    @Override
    protected Task<PagedList<UserInfo>> getNextPage(@NonNull PagedList<UserInfo> before) {
        if (before == null || !before.hasMore()) {
            return Task.forResult(null);
        }

        indexPager.moveToNextPage(before.getData().size());
        return motuSns.getHotUsers(indexPager.getCurrentStartIndex(), IMotuSns.DEFAULT_PAGE_SIZE)
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
