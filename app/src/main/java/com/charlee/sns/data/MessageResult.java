package com.charlee.sns.data;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 一条消息的返回结果
 */
public class MessageResult extends ResultBase {
    private final Message message;

    public MessageResult(int errCode, @Nullable String errMsg, Boolean hasMore, @NonNull Message msg) {
        super(errCode, errMsg, hasMore);
        message = msg;
    }

    @NonNull
    public Message getMessage() {
        return message;
    }

    @Override
    public boolean isValid() {
        return message != null && message.isValid();
    }

    @Override
    public void setServerTimeStamp(long serverTimeStamp) {
        super.setServerTimeStamp(serverTimeStamp);
        if (message != null) {
            message.setUpdateTime(serverTimeStamp);
        }
    }
}
