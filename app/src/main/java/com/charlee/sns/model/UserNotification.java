package com.charlee.sns.model;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.charlee.sns.BuildConfig;
import com.charlee.sns.data.Message;
import com.charlee.sns.data.MessageComment;
import com.charlee.sns.data.NotificationItem;
import com.charlee.sns.data.UserInfo;

import java.security.InvalidParameterException;

/**
 * 用户通知
 */
public class UserNotification extends ModelBase {
    private final NotificationItem notification;
    private final NotificationType type;
    private final SnsUser user;
    private UserMessage message;
    private Comment comment;

    UserNotification(@NonNull IMotuSns motuSns,
                     @NonNull IModelRepository repository,
                     @NonNull NotificationItem notification) {
        this.notification = notification;
        this.type = NotificationType.parse(notification.getType());

        if (BuildConfig.DEBUG && notification.getCreateTime() == 0) {
            throw new InvalidParameterException("No createTime for the notification!");
        }

        if (type == NotificationType.System) {
            this.user = null;
            message = null;
            comment = null;
            itemId = notification.getText().hashCode();
            return;
        }

        // 服务器逻辑：通知创建的时间是其中的对象更新的时间
        long updateTime = notification.getCreateTime();

        UserInfo user = notification.getUser();
        user.setUpdateTime(updateTime);
        this.user = repository.getUserByData(user);

        Message msg = notification.getMessage();

        MessageComment cmt = notification.getComment();
        if (cmt != null && cmt.getUser() == null) {
            // 除非服务器设置了评论的发布用户，否则默认评论来自通知中的user
            // 如果自己在消息中@自己需要直接设置评论的user。
            cmt.setUser(user);
        }

        switch (type) {
            case Comment:
                if (BuildConfig.DEBUG && cmt == null) {
                    throw new InvalidParameterException();
                }
                // 此处不要加break!!!!
            case Like:
                // 评论及赞通知中的消息为登录用户发布的消息
                if (msg != null) {
                    msg.setUpdateTime(updateTime);
                    msg.setUser(motuSns.getLoggedInUser());
                } else if (BuildConfig.DEBUG) {
                    throw new InvalidParameterException();
                }
                break;
            case At:
                if (BuildConfig.DEBUG && msg == null) {
                    throw new InvalidParameterException();
                }

                if (msg.getUser() == null) {
                    // 别的用户在消息中@你。如果自己@自己需要直接设置消息的user。
                    msg.setUser(user);
                }
                break;
            default:
                break;
        }

        if (cmt != null) {
            cmt.setUpdateTime(updateTime);
            this.comment = repository.getCommentByData(cmt);
        }

        if (msg != null) {
            msg.setUpdateTime(updateTime);
            this.message = repository.getMessageByData(msg);
        }

        itemId = Long.getLong(this.getId(),
                this.getId().hashCode() * this.user.getNickName().hashCode());
    }

    public enum NotificationType {
        None,
        Comment,
        Like,
        Follow,
        At,
        System;

        public static NotificationType parse(@NonNull String type) {
            if (type.equals(NotificationItem.TYPE_COMMENT)) {
                return Comment;
            } else if (type.equals(NotificationItem.TYPE_LIKE)) {
                return Like;
            } else if (type.equals(NotificationItem.TYPE_FOLLOW)) {
                return Follow;
            } else if (type.equals(NotificationItem.TYPE_AT)) {
                return At;
            } else if (type.equals(NotificationItem.TYPE_PERSONAL_SYSTEM)) {
                return System;
            }

            return None;
        }
    }

    @NonNull
    String getId() {
        return notification.getId();
    }

    @NonNull
    public NotificationType getType() {
        return type;
    }

    @NonNull
    public SnsUser getUser() {
        return user;
    }

    @Nullable
    public UserMessage getMessage() {
        return message;
    }

    @Nullable
    public Comment getComment() {
        return comment;
    }

    public boolean getReplyCommentStatus() {
        return notification.getReplyCommentStatus();
    }

    public String getText() {
        return notification.getText();
    }

    /**
     * 获取通知创建的时间
     *
     * @return 创建时间(时间戳, 但单位为秒)
     */
    public long getCreateTime() {
        return notification.getCreateTime();
    }
}
