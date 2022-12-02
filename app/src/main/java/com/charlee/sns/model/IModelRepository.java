package com.charlee.sns.model;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.charlee.sns.data.Campaign;
import com.charlee.sns.data.CardItem;
import com.charlee.sns.data.Message;
import com.charlee.sns.data.MessageComment;
import com.charlee.sns.data.NotificationItem;
import com.charlee.sns.data.Tag;
import com.charlee.sns.data.UserInfo;

/**
 * 模型对象池接口
 */
public interface IModelRepository {
    @NonNull
    SnsUser getUserByData(UserInfo user);

    @Nullable
    SnsUser getUserById(String id);

    /**
     * 确保用户对象在缓存中。
     * @param user          用户对象
     */
    void assureUser(SnsUser user);

    @NonNull
    UserMessage getMessageByData(Message msg);

    @Nullable
    UserMessage getMessageById(String id);

    /**
     * 确保消息对象在缓存中。
     * @param message       消息对象
     */
    void assureMessage(UserMessage message);

    @NonNull
    MessageTag getTagByData(Tag tag);

    @Nullable
    MessageTag getTagById(String id);

    /**
     * 确保标签对象在缓存中。
     * @param tag           标签对象
     */
    void assureTag(MessageTag tag);

    @NonNull
    Comment getCommentByData(MessageComment comment);

    @Nullable
    Comment getCommentById(String id);

    /**
     * 确保运营活动对象在缓存中
     * @param campaign      活动对象
     * @return
     */
    void assureCampaign(SnsCampaign campaign);

    @NonNull
    SnsCampaign getCampaignByData(Campaign campaign);

    @Nullable
    SnsCampaign getCampaignById(String id);

    @NonNull
    Card getCardByData(CardItem cardItem);

    @Nullable
    Card getCardById(String id);

    @NonNull
    UserNotification getUserNotificationByData(NotificationItem item);
}
