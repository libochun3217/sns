package com.charlee.sns.data;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.animated.gif.BuildConfig;

import java.security.InvalidParameterException;
import java.util.List;


/**
 * 用户发布的消息
 * 注意：成员变量命名要和JSON对应。GSON转换规则为FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES
 */
public class Message extends DataModelBase {
    public static class MessageContent {
        private final Image image;
        private final Video video;
        private final String description;
        private final List<Tag> tags;

        public MessageContent(@NonNull Image image, @NonNull Video video, @Nullable String description,
                              @Nullable List<Tag> tags) {
            this.image = image;
            this.video = video;
            this.description = description;
            this.tags = tags;
        }

        @Nullable
        public Image getImage() {
            return image;
        }

        @Nullable
        public Video getVideo() {
            return video;
        }

        @Nullable
        public String getDescription() {
            return description;
        }

        @Nullable
        public List<Tag> getTags() {
            return tags;
        }

        public boolean isValid() {
            return (image != null && image.isValid()) || (video != null && video.isValid());
        }
    }

    private final String messageId;
    private final MessageContent content;
    private final long createTime;
    private UserInfo user; // 服务器返回的某个用户的消息列表时不包含用户信息，需要在客户端设置。所以没用final
    private String shareUrl;
    private Boolean hasLiked;
    private Integer likeNum;
    private Integer commentNum;
    private PagedList<MessageComment> comments;

    private int toType;
    private String toId;
    private String toTitle;
    private String toUrl;

    public Message(@NonNull String messageId,
                   @NonNull UserInfo user,
                   @NonNull MessageContent content,
                   @NonNull long createTime,
                   @NonNull String shareUrl,
                   boolean hasLiked,
                   int likeNum,
                   int commentNum,
                   @Nullable PagedList<MessageComment> comments) {
        this.messageId = messageId;
        this.user = user;
        this.content = content;
        this.createTime = createTime;
        this.shareUrl = shareUrl;
        this.hasLiked = hasLiked;
        this.likeNum = likeNum;
        this.commentNum = commentNum;
        this.comments = comments;
    }

    @Override
    public boolean isValid() {
        return isValidForNotification() && user != null && user.isValid();
    }

    // 通知内的Message可以不包含用户信息（而是在通知消息内）
    public boolean isValidForNotification() {
        return messageId != null && !messageId.isEmpty()
                && content != null && content.isValid();
    }

    @NonNull
    @Override
    public String getId() {
        return messageId;
    }

    @Override
    public void setUpdateTime(Long updateTime) {
        super.setUpdateTime(updateTime);
        if (user != null) {
            user.setUpdateTime(updateTime);
        }

        if (content != null && content.tags != null) {
            for (Tag tag : content.tags) {
                tag.setUpdateTime(updateTime);
            }
        }

        if (comments != null) {
            comments.setUpdateTime(updateTime);
        }
    }

    @NonNull
    public MessageContent getContent() {
        return content;
    }

    // 只有在通知内的原始数据才会返回null
    @Nullable
    public UserInfo getUser() {
        return user;
    }

    public void setUser(@NonNull UserInfo user) {
        this.user = user;
    }

    @NonNull
    public long getCreateTime() {
        return createTime;
    }

    @Nullable
    public String getShareUrl() {
        return shareUrl;
    }

    @Nullable
    public Boolean isLiked() {
        return hasLiked;
    }

    public void setIsLiked(boolean liked) {
        hasLiked = liked;
        if (liked) {
            ++likeNum;
        } else {
            --likeNum;
        }
    }

    @Nullable
    public Integer getLikeNum() {
        return likeNum;
    }

    @Nullable
    public Integer getCommentNum() {
        return commentNum;
    }

    @Nullable
    public PagedList<MessageComment> getComments() {
        return comments;
    }

    public int getToType() {
        return toType;
    }

    @Nullable
    public String getToId() {
        return toId;
    }

    @Nullable
    public String getToTitle() {
        return toTitle;
    }

    @Nullable
    public String getToUrl() {
        return toUrl;
    }

    /**
     * 更新消息的数据
     * @param other                 用来更新的数据
     */
    public boolean update(@NonNull Message other) {
        // 调用此方法前需要保证other的值有效
        // 消息一旦发布则不可能更改ID
        if (!other.isValid() || !other.messageId.equals(this.messageId)) {
            if (BuildConfig.DEBUG) {
                // 如果在此处抛出异常请检查代码逻辑！！！！！！！！！
                throw new InvalidParameterException("Update Message with invalid data or a different ID!");
            } else {
                return false;
            }
        }

        // 消息一旦发布则不可能更改发布者
        if (other.user != null && user != null
                && !other.user.getId().equals(user.getId())) {
            if (BuildConfig.DEBUG) {
                // 如果在此处抛出异常请检查代码逻辑！！！！！！！！！
                throw new InvalidParameterException("Update Message with a different user!");
            } else {
                return false;
            }
        }

        // 更新前确保已设置updateTime
        if (BuildConfig.DEBUG && other.updateTime == null) {
            // 如果在此处抛出异常请检查代码逻辑！！！！！！！！！
            throw new InvalidParameterException("Update Message without updateTime!");
        }

        // 检查other是否更新时间更近。
        boolean isOtherMoreRecent = this.updateTime == null || other.updateTime >= this.updateTime;

        boolean updated = false;

        if (other.user != null) {
            if (this.user == null) {
                this.user = other.user;  // 直接替换，真正的update在Repository中完成
                updated = true;
            } else if (isOtherMoreRecent && !this.user.equals(other.user)) {
                this.user = other.user;  // 直接替换，真正的update在Repository中完成
                updated = true;
            }
        }

        if (other.shareUrl != null) {
            if (this.shareUrl == null) {
                this.shareUrl = other.shareUrl;
                updated = true;
            } else if (isOtherMoreRecent && !this.shareUrl.equals(other.shareUrl)) {
                this.shareUrl = other.shareUrl;
                updated = true;
            }
        }

        if (other.hasLiked != null) {
            if (this.hasLiked == null) {
                this.hasLiked = other.hasLiked;
                updated = true;
            } else if (isOtherMoreRecent && !this.hasLiked.equals(other.hasLiked)) {
                this.hasLiked = other.hasLiked;
                updated = true;
            }
        }

        if (other.likeNum != null) {
            if (this.likeNum == null) {
                this.likeNum = other.likeNum;
                updated = true;
            } else if (isOtherMoreRecent && !this.likeNum.equals(other.likeNum)) {
                this.likeNum = other.likeNum;
                updated = true;
            }
        }

        if (other.commentNum != null) {
            if (this.commentNum == null) {
                this.commentNum = other.commentNum;
                updated = true;
            } else if (isOtherMoreRecent && !this.commentNum.equals(other.commentNum)) {
                this.commentNum = other.commentNum;
                updated = true;
            }
        }

        if (other.comments != null) {
            if (this.comments == null) {
                this.comments = other.comments;
                updated = true;
            } else if (isOtherMoreRecent && !this.comments.equals(other.comments)) {
                this.comments = other.comments;
                updated = true;
            }
        }

        if (updated && isOtherMoreRecent) {
            this.updateTime = other.updateTime;
        }

        return updated;
    }
}
