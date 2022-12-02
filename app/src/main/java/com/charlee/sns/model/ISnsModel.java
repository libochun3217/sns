package com.charlee.sns.model;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.charlee.sns.helper.ObservableArrayList;

import bolts.Task;

/**
 * 社区模型层入口定义
 * 注意：
 * 1. 这是所有社区功能的入口，不应该有任何社区功能不是从这儿开始。
 * 2. 模型层是OOP的方式，任何操作都是对一个对象的操作，任何模型层对象的公开接口都不应该暴露ID之类的内部信息。
 */
public interface ISnsModel {
    /**
     * 获取本地时间与服务器时间的差
     *
     * @return
     */
    long getTimeOffsetInSeconds();

    /**
     * 获取热门用户列表
     *
     * @return 用户列表
     */
    @NonNull
    IPageableList<SnsUser> getHotUsers();

    /**
     * 获取热门标签列表
     *
     * @return 标签列表
     */
    @NonNull
    IPageableList<MessageTag> getHotTags();

    /**
     * 获取最新消息列表
     *
     * @return 消息列表
     */
    @NonNull
    IPageableList<UserMessage> getLatestMessages();

    /**
     * 获取热门消息列表
     *
     * @return 消息列表
     */
    @NonNull
    IPageableList<UserMessage> getHotMessages();

    /**
     * 用户是否已登录
     *
     * @return true: 用户已登录；false：用户未登录
     */
    boolean isUserLoggedIn();

    /**
     * 获取匿名用户的ID
     *
     * @return
     */
    String getAnonymousUserId();

    /**
     * 用户登录
     *
     * @param userSrcType 第三方认证类型
     * @param oauthToken  第三方认证token
     * @param pushToken   用于推送的token
     *
     * @return 认证Task
     */
    @NonNull
    Task<Void> login(int userSrcType, String oauthToken, String pushToken, String anonymousId, String fcmToken);

    /**
     * 用户注销
     *
     * @return 是否成功的Task
     */
    Task<Boolean> logout();

    /**
     * 更新登录相关的配置
     *
     * @param userSrcType 第三方认证类型
     * @param oauthToken  第三方认证token
     * @param pushToken   用于推送的token
     * @param portraitUri 用户头像
     * @param nickName    用户名字
     * @param fcmToken    fcmToken
     *
     * @return 认证Task
     */
    @NonNull
    Task<Boolean> updateLoginProfile(int userSrcType, String oauthToken, String pushToken,
                                     String portraitUri, String nickName, String fcmToken);

    /**
     * 获取当前用户的关注消息列表
     *
     * @return 消息列表，未登录则返回null
     */
    @Nullable
    IPageableList<UserMessage> getFeedList();

    /**
     * 获取卡片列表
     *
     * @return 卡片列表
     */
    @Nullable
    IPageableList<Card> getCardList();

    @Nullable
    SnsUser getLoginUser();

    /**
     * 获取轮播图的展示内容
     */
    Task<Carousel> getCarousel();

    /**
     * 通过导航Uri获取用户对象
     *
     * @param uri 导航Uri
     *
     * @return 用户对象。不存在则返回null
     */
    @NonNull
    Task<SnsUser> getUserByNavUri(Uri uri);

    /**
     * 通过导航Uri获取消息对象
     *
     * @param uri          导航Uri
     * @param forceRefresh 是否强制从服务器刷新
     *
     * @return 消息对象。不存在则返回null
     */
    @NonNull
    Task<UserMessage> getMessageByNavUri(Uri uri, boolean forceRefresh);

    /**
     * 通过导航Uri获取标签对象
     *
     * @param uri 导航Uri
     *
     * @return 标签对象。不存在则返回null
     */
    @NonNull
    Task<MessageTag> getTagByNavUri(Uri uri);

    /**
     * 通过导航Uri获取活动对象
     *
     * @param uri 导航Uri
     *
     * @return 活动对象。不存在则返回null
     */
    @NonNull
    Task<SnsCampaign> getCampaignByNavUri(Uri uri, boolean forceRefresh);

    /**
     * 通过id获取活动对象
     *
     * @param id 导航Uri
     *
     * @return 活动对象。不存在则返回null
     */
    @NonNull
    Task<SnsCampaign> getCampaignById(String id);

    /**
     * 获取用户消息通知列表
     *
     * @return
     */
    @Nullable
    IPageableList<UserNotification> getUserNotificationList();

    /**
     * 获取当前活动列表
     *
     * @return
     */
    @Nullable
    IPageableList<SnsCampaign> getCampaignActiveList();

    /**
     * 获取历史活动列表
     *
     * @return
     */
    @Nullable
    IPageableList<SnsCampaign> getCampaignHistoryList();

    /**
     * 获取当前用户正在发送消息的列表
     *
     * @return
     */
    ObservableArrayList<Publish> getPublishArray();

    ModelBase getLoginEvent();

    /**
     * 获取客户端配置
     *
     * @return
     */
    @NonNull
    Task<Void> refreshSnsConfiguration();
}
