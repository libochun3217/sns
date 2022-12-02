package com.charlee.sns.data;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 消息评论
 */
public class MessageComment extends DataModelBase {
    private String commentId;
    private final String content;
    private long createTime;
    private UserInfo user;
    private MessageComment fatherComment;

    public MessageComment(@NonNull String commentId,
                          @NonNull String content,
                          long createTime,
                          @NonNull UserInfo user,
                          @Nullable MessageComment fatherComment) {
        this.commentId = commentId;
        this.content = content;
        this.createTime = createTime;
        this.user = user;
        this.fatherComment = fatherComment;
    }

    @Override
    public boolean isValid() {
        return isValidForNotification() && user != null && user.isValid();
    }

    // 通知内的评论可以不包含用户信息（而是在通知消息内）
    public boolean isValidForNotification() {
        return commentId != null && !commentId.isEmpty()
                && content != null && !content.isEmpty();
    }

    @NonNull
    @Override
    public String getId() {
        return commentId;
    }

    @Override
    public void setUpdateTime(Long updateTime) {
        super.setUpdateTime(updateTime);
        if (user != null) {
            user.setUpdateTime(updateTime);
        }
        if (fatherComment != null) {
            fatherComment.setUpdateTime(updateTime);
        }
    }

    public void setId(String id) {
        this.commentId = id;
    }

    @NonNull
    public String getContent() {
        return content;
    }

    @NonNull
    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long time) {
        createTime = time;
    }

    // 只有在通知内的原始数据才会返回null
    @Nullable
    public UserInfo getUser() {
        return user;
    }

    public void setUser(UserInfo user) {
        this.user = user;
    }

    public MessageComment getFatherComment() {
        return fatherComment;
    }

}
