package com.charlee.sns.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


/**
 * 通知中心返回的结果
 * 注意：成员变量命名要和JSON对应。GSON转换规则为FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES
 */
public class NotificationItem extends DataModelBase {
    @StringDef({TYPE_COMMENT, TYPE_LIKE, TYPE_FOLLOW, TYPE_AT, TYPE_PERSONAL_SYSTEM})
    @Retention(RetentionPolicy.SOURCE)
    public @interface NotificationType { }

    public static final String TYPE_COMMENT = "comment";
    public static final String TYPE_LIKE = "like";
    public static final String TYPE_FOLLOW = "follow";
    public static final String TYPE_AT = "at";
    public static final String TYPE_PERSONAL_SYSTEM = "personal_system_notification";

    private final String type;
    private final boolean replyComment;
    private final String text;
    private final UserInfo user;
    private final Message message;
    private final MessageComment comment;
    private final String ownerId;
    private final long createTime;

    public NotificationItem(@NonNull @NotificationType String type,
                            boolean replyComment,
                            String text,
                            @NonNull UserInfo user,
                            @Nullable Message message,
                            @Nullable MessageComment comment,
                            @Nullable String ownerId,
                            long createTime) {
        this.type = type;
        this.replyComment = replyComment;
        this.text = text;
        this.user = user;
        this.message = message;
        this.comment = comment;
        this.ownerId = ownerId;
        this.createTime = createTime;
    }

    @Override
    public boolean isValid() {
        if (type != null && type.equals(TYPE_PERSONAL_SYSTEM)) {
            return true;
        }
        return type != null && !type.isEmpty()
                && user != null && user.isValid()
                && ((type.equals(TYPE_COMMENT) && comment != null && comment.isValidForNotification()
                && message != null && message.isValidForNotification())
                || (type.equals(TYPE_LIKE) && message != null && message.isValidForNotification())
                || (type.equals(TYPE_FOLLOW)));
    }

    @Nullable
    @Override
    public String getId() {
        return Integer.toHexString(hashCode()); // 现在服务器没有返回ID，暂时采用哈希值
    }

    @NonNull
    @NotificationType
    public String getType() {
        return type;
    }

    @NonNull
    public UserInfo getUser() {
        return user;
    }

    @Nullable
    public Message getMessage() {
        return message;
    }

    @Nullable
    public MessageComment getComment() {
        return comment;
    }

    public boolean getReplyCommentStatus() {
        return replyComment;
    }

    public String getText() {
        return text;
    }

    @Nullable
    public String getOwnerId() {
        return ownerId;
    }

    @NonNull
    public long getCreateTime() {
        return createTime;
    }
}
