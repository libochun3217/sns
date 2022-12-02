package com.charlee.sns.model;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.charlee.sns.BuildConfig;
import com.charlee.sns.data.MessageComment;

import java.security.InvalidParameterException;

/**
 * 评论
 */
public class Comment extends ModelBase {
    private final IMotuSns motuSns;
    private final SnsUser user;
    private MessageComment comment;

    private final SnsUser fatherUser;
    private final Comment fatherComment;

    private SnsModel.PublishedState state;

    // 仅供UserRepository调用以保证每个评论只有唯一实例
    Comment(@NonNull IMotuSns motuSns, @NonNull IModelRepository repository, MessageComment comment) {
        if (BuildConfig.DEBUG) {
            if (repository.getCommentById(comment.getId()) != null) {
                throw new InvalidParameterException("DO NOT create a different instance for the same ID!");
            }
        }

        this.motuSns = motuSns;
        this.comment = comment;
        this.user = comment.getUser() != null ? repository.getUserByData(comment.getUser()) : null;

        this.fatherUser = comment.getFatherComment() != null
                ? repository.getUserByData(comment.getFatherComment().getUser()) : null;
        this.fatherComment = comment.getFatherComment() != null
                ? repository.getCommentByData(comment.getFatherComment()) : null;

        state = SnsModel.PublishedState.PUBLISHED;
        itemId = Long.getLong(this.getId(),
                this.getId().hashCode() * this.comment.getContent().hashCode());
    }

    @NonNull
    public String getId() {
        return comment.getId();
    }

    @NonNull
    public String getContent() {
        return comment.getContent();
    }

    @NonNull
    public MessageComment getData() {
        return comment;
    }

    @Nullable
    public SnsUser getFatherUser() {
        return fatherUser;
    }

    @Nullable
    public Comment getFatherComment() {
        return fatherComment;
    }

    /**
     * 获取评论创建的时间
     * @return      创建时间(时间戳,但单位为秒)
     */
    public long getCreateTime() {
        return comment.getCreateTime();
    }

    @NonNull
    public SnsUser getUser() {
        return user;
    }

    public void setState(SnsModel.PublishedState state) {
        this.state = state;
        setChanged();
        notifyObservers();
    }

    public SnsModel.PublishedState getState() {
        return state;
    }

    synchronized void update(@NonNull final MessageComment other) {
        if (other == null) {
            if (BuildConfig.DEBUG) {
                throw new InvalidParameterException("Update Comment with null object!");
            } else {
                return;
            }
        }

        if (other.getCreateTime() != 0) {
            this.comment = other;
        }

    }

}
