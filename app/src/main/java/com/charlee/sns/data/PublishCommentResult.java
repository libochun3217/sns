package com.charlee.sns.data;


import androidx.annotation.Nullable;

/**
 */
public class PublishCommentResult extends ResultBase {
    private final String commentId;

    public PublishCommentResult(int errCode, @Nullable String errMsg, Boolean hasMore, String commentId) {
        super(errCode, errMsg, hasMore);
        this.commentId = commentId;
    }

    public String getCommentId() {
        return commentId;
    }

    @Override
    public boolean isValid() {
        return commentId != null && !commentId.isEmpty();
    }
}
