package com.charlee.sns.model;


import androidx.annotation.NonNull;

import com.charlee.sns.data.Message;
import com.charlee.sns.data.MessagesResult;
import com.charlee.sns.data.PagedList;

import bolts.Continuation;
import bolts.Task;

/**
 */
public abstract class MessageList extends PageableList<UserMessage, Message> {

    public MessageList(@NonNull final IMotuSns motuSns,
                       @NonNull final IModelRepository repository,
                       int totalSize,
                       PageableList.PagingType pagingType) {
        super(motuSns, repository, totalSize, pagingType);
    }

    public MessageList(@NonNull final IMotuSns motuSns,
                       @NonNull final IModelRepository repository,
                       int totalSize,
                       PageableList.PagingType pagingType,
                       PagedList<Message> firstPage) {
        super(motuSns, repository, totalSize, pagingType, firstPage);
    }

    protected Continuation<MessagesResult, PagedList<Message>> parseMessageResultContinuation =
            new Continuation<MessagesResult, PagedList<Message>>() {
                @Override
                public PagedList<Message> then(Task<MessagesResult> task) throws Exception {
                    return task.getResult().getMessages();
                }
            };

    @Override
    protected UserMessage createModel(@NonNull Message data) {
        return repository.getMessageByData(data);
    }

    @Override
    protected boolean hasSameId(@NonNull UserMessage model, @NonNull Message data) {
        return model.getId().equals(data.getId());
    }
}
