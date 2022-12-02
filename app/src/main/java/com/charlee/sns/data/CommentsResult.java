package com.charlee.sns.data;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 评论列表返回结果
 */
public class CommentsResult extends ResultBase {
    private final PagedList<MessageComment> comments;

    public CommentsResult(int errCode,
                          @Nullable String errMsg,
                          Boolean hasMore,
                          @NonNull PagedList<MessageComment> comments) {
        super(errCode, errMsg, hasMore);
        this.comments = comments;
    }

    @NonNull
    public PagedList<MessageComment> getComments() {
        return comments;
    }

    @Override
    public boolean isValid() {
        return comments != null && comments.isValid();
    }

    @Override
    public void setServerTimeStamp(long serverTimeStamp) {
        super.setServerTimeStamp(serverTimeStamp);
        if (comments != null) {
            comments.setUpdateTime(serverTimeStamp);
        }
    }
}