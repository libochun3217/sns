package com.charlee.sns.data;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 消息列表返回结果
 * 注意：成员变量命名要和JSON对应。GSON转换规则为FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES
 */
public class MessagesResult extends ResultBase {
    private final PagedList<Message> messages;

    public MessagesResult(int errCode, @Nullable String errMsg, Boolean hasMore, @NonNull PagedList<Message> msgs) {
        super(errCode, errMsg, hasMore);
        messages = msgs;
    }

    @NonNull
    public PagedList<Message> getMessages() {
        return messages;
    }

    @Override
    public boolean isValid() {
        return messages != null && messages.isValid();
    }

    @Override
    public void setServerTimeStamp(long serverTimeStamp) {
        super.setServerTimeStamp(serverTimeStamp);
        if (messages != null) {
            messages.setUpdateTime(serverTimeStamp);
        }
    }
}
