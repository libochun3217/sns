package com.charlee.sns.model;

import androidx.annotation.NonNull;

import com.charlee.sns.data.Message;
import com.charlee.sns.data.PagedList;
import com.charlee.sns.data.UserInfo;

import bolts.Continuation;
import bolts.Task;

/**
 */
public class UserMessageList extends MessageList {
    private UserInfo user;

    // 服务器在用户消息列表中省略了用户，需要在此设置
    protected Continuation<PagedList<Message>, PagedList<Message>> setUserContinuation =
            new Continuation<PagedList<Message>, PagedList<Message>>() {
                @Override
                public PagedList<Message> then(Task<PagedList<Message>> task) throws Exception {
                    for (Message item : task.getResult().getData()) {
                        item.setUser(user);
                    }

                    return task.getResult();
                }
            };

    public UserMessageList(@NonNull final IMotuSns motuSns,
                           @NonNull final IModelRepository repository,
                           UserInfo user,
                           PagedList<Message> firstPage) {
        super(motuSns, repository, IPageableList.TOTAL_SIZE_INFINITE, PagingType.IdBased, firstPage);
        this.user = user;
    }

    @NonNull
    @Override
    protected Task<PagedList<Message>> getFirstPage() {
        return motuSns.getUserMessages(user.getId(), IMotuSns.FIRST_PAGE_ID, IMotuSns
                .DEFAULT_PAGE_SIZE)
                .onSuccess(parseMessageResultContinuation)
                .onSuccess(setUserContinuation);
    }

    @NonNull
    @Override
    protected Task<PagedList<Message>> getNextPage(@NonNull PagedList<Message> before) {
        String lastId = before.getLastId();
        if (lastId == null || lastId.isEmpty()) {
            return Task.forResult(null);
        }

        return motuSns.getUserMessages(user.getId(), lastId, IMotuSns.DEFAULT_PAGE_SIZE)
                .onSuccess(parseMessageResultContinuation)
                .onSuccess(setUserContinuation);
    }
}
