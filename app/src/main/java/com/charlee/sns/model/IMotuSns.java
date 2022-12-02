package com.charlee.sns.model;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.charlee.sns.data.CampaignResult;
import com.charlee.sns.data.CampaignsResult;
import com.charlee.sns.data.CardsResult;
import com.charlee.sns.data.CarouselResult;
import com.charlee.sns.data.CommentsResult;
import com.charlee.sns.data.ConfigurationResult;
import com.charlee.sns.data.LoginResult;
import com.charlee.sns.data.MessageResult;
import com.charlee.sns.data.MessagesResult;
import com.charlee.sns.data.MotuSnsService;
import com.charlee.sns.data.NotificationResult;
import com.charlee.sns.data.PublishCommentResult;
import com.charlee.sns.data.ResultBase;
import com.charlee.sns.data.S3ConfigResult;
import com.charlee.sns.data.TagsResult;
import com.charlee.sns.data.UserDetailsResult;
import com.charlee.sns.data.UserInfo;
import com.charlee.sns.data.UsersResult;

import java.util.ArrayList;

import bolts.Task;

/**
 * 魔图社区数据层接口，封装对Web API的访问，所有方法都返回Task<>。
 */
public interface IMotuSns {
    int FIRST_PAGE = 0;
    String FIRST_PAGE_ID = "";
    int DEFAULT_PAGE_SIZE = 40;

    /**
     * 消息类型
     */
    int MESSAGE_TYPE_IMAGE = 0;
    int MESSAGE_TYPE_VIDEO = 1;

    /**
     * 获取本地时间与服务器时间的差
     * @return
     */
    long getTimeOffsetInSeconds();

    /**
     * 获取广场页轮播图展示内容
     * @return
     */
    Task<CarouselResult> getCarousel();

    /**
     * 获取热门用户列表
     * @param startIndex    分页起始索引号
     * @param pageSize      分页大小
     * @return              包含用户列表的Task
     */
    Task<UsersResult> getHotUsers(int startIndex, int pageSize);

    /**
     * 获取热门标签列表
     * @param startIndex    分页起始索引号
     * @param pageSize      分页大小
     * @return              包含标签列表的Task
     */
    Task<TagsResult> getHotTags(int startIndex, int pageSize);

    /**
     * 获取最新消息列表
     * @param startId       分页起始ID
     * @param pageSize      分页大小
     * @return              包含消息列表的Task
     */
    Task<MessagesResult> getLatestMessages(@NonNull String startId, int pageSize);

    /**
     * 获取热门消息列表
     * @param startIndex    分页起始索引号
     * @param pageSize      分页大小
     * @return              包含消息列表的Task
     */
    Task<MessagesResult> getHotMessages(int startIndex, int pageSize);

    /**
     * 获取标签消息列表
     * @param tagId     标签ID
     * @param startIndex   分页起始索引
     * @param pageSize  分页大小
     * @return          消息列表
     */
    Task<MessagesResult> getTagMessages(@NonNull String tagId, int startIndex, int pageSize);

    /**
     * 获取用户详细信息
     * @param id        用户ID
     * @return          用户详细信息
     */
    Task<UserDetailsResult> getUserDetails(@NonNull String id);

    /**
     * 获取用户自己发布的消息列表
     * @param id        用户ID
     * @param startId   分页起始ID
     * @param pageSize  分页大小
     * @return          消息列表
     */
    Task<MessagesResult> getUserMessages(@NonNull String id, @NonNull String startId, int pageSize);

    /**
     * 根据消息ID获取消息对象
     * @param userId    用户ID
     * @param messageId 消息ID
     * @return
     */
    Task<MessageResult> getMessage(@NonNull String userId, @NonNull String messageId);

    /**
     * 发布消息
     * @param id          用户ID
     * @param description 消息内容
     * @param imageUri    图片URI
     * @param width       图片宽度
     * @param height      图片高度
     * @param campaignIds 活动ID数组
     * @return
     */
    Task<MessageResult> postMessage(@NonNull final String id, @NonNull final String description,
                                    @NonNull final String imageUri, @Nullable final String videoUri,
                                    final int width, final int height, @Nullable final ArrayList<String> campaignIds);

    /**
     * 删除消息
     * @param userId 消息发布者ID
     * @param msgId  消息ID
     * @return
     */
    Task<ResultBase> deleteMessage(String userId, String msgId);

    /**
     * 获取消息评论列表
     * @param userId    消息发布者ID
     * @param msgId     消息ID
     * @param startId   分页起始ID
     * @param pageSize  分页大小
     * @return          包含消息评论列表的Task
     */
    Task<CommentsResult> getMessageComments(@NonNull String userId,
                                            @NonNull String msgId, @NonNull String startId, int pageSize);

    /**
     * 发布评论
     * @param userId    消息发布者ID
     * @param msgId     消息ID
     * @param content   消息内容
     * @return
     */
    Task<PublishCommentResult> postComment(@NonNull String userId, @NonNull String msgId,
                                           @NonNull String content, @Nullable final String fatherCommentId);

    /**
     * 删除评论
     * @param userId    消息发布者ID
     * @param msgId     消息ID
     * @param commentId 评论ID
     * @return
     */
    Task<ResultBase> deleteComment(@NonNull String userId, @NonNull String msgId, @NonNull String commentId);

    /**
     * 获取用户粉丝列表
     * @param id        用户ID
     * @param startId   分页起始ID
     * @param pageSize  分页大小
     * @return          包含粉丝用户列表的Task
     */
    Task<UsersResult> getUserFollowers(@NonNull String id, @NonNull String startId, int pageSize);

    /**
     * 获取用户关注列表
     * @param id        用户ID
     * @param startId   分页起始ID
     * @param pageSize  分页大小
     * @return          关注的用户列表
     */
    Task<UsersResult> getUserFollowees(@NonNull String id, @NonNull String startId, int pageSize);

    /**
     * 获取运营活动列表
     * @param campaignType  活动类型，1表示运营活动，2表示功能活动
     * @return              返回活动列表
     */
    Task<CampaignsResult> getActiveCampaigns(int campaignType);

    /**
     * 获取历史活动列表
     * @param campaignType  活动类型，1表示运营活动，2表示功能活动
     * @param startId  分页起始ID
     * @param pageSize 分页大小
     * @return 返回活动列表
     */
    Task<CampaignsResult> getHistoryCampaigns(int campaignType, @NonNull String startId, int pageSize);

    /**
     * 根据活动ID获取活动对象
     * @param campaignId 活动ID
     * @return
     */
    Task<CampaignResult> getCampaign(@NonNull String campaignId);

    /**
     * 获取活动消息列表
     * @param campaignId    活动ID
     * @param startIndex    分页起始索引
     * @param pageSize      分页大小
     * @return              消息列表
     */
    Task<MessagesResult> getCampaignMessages(@NonNull String campaignId, int startIndex, int pageSize);

    /**
     * 用户是否已经登录
     * @return          已登录则返回true，否则返回false
     */
    boolean isUserLoggedIn();

    /**
     * 用户信息是否已更新
     * @return          已更新则返回true，否则返回false
     */
    boolean isUserInfoUpdated();

    /**
     * 获取已登录用户
     * @return          已登录的用户信息，如未登录则返回null
     */
    @Nullable
    UserInfo getLoggedInUser();

    /**
     * 用户登录
     * @param userSrcType   用户来源（第三方认证）
     * @param oauthToken    第三方认证/授权token
     * @return              登录结果
     */
    Task<LoginResult> login(@MotuSnsService.LoginSourceType int userSrcType,
                            @NonNull String oauthToken,
                            @NonNull String pushToken,
                            @NonNull String anonymousId,
                            String fcmToken);

    /**
     * 用户注销
     * @return      注销结果
     */
    Task<ResultBase> logout();

    /**
     * 更新用户配置
     */
    Task<UserDetailsResult> updateLoginProfile(@MotuSnsService.LoginSourceType int userSrcType,
                                               @Nullable String oauthToken, @Nullable String pushToken,
                                               @Nullable final String portrait, @Nullable final String nickName,
                                               final String fcmToken);

    /**
     * 获取关注用户发布的消息列表（Feed流）
     * @param startId       分页起始ID
     * @param pageSize      分页大小
     * @return              Feed流结果
     */
    Task<MessagesResult> getFeeds(@NonNull String startId, int pageSize);


    /**
     * 获取卡片流
     * @param startId       分页起始ID
     * @param pageSize      分页大小
     * @return
     */
    Task<CardsResult> getCards(@NonNull String startId, int pageSize);

    /**
     * 添加关注对象
     * @param userId        被关注用户ID
     * @return              关注结果
     */
    Task<ResultBase> followUser(@NonNull String userId);

    /**
     * 取消关注对象
     * @param followeeid    被关注用户ID
     * @return              取消关注的结果
     */
    Task<ResultBase> unfollowUser(@NonNull String followeeid);

    /**
     * 赞消息
     * @param messageOwnerId    消息发布者ID
     * @param msgId             消息ID
     * @return                  赞结果
     */
    Task<ResultBase> setLikes(@NonNull String messageOwnerId, @NonNull String msgId);

    /**
     * 取消点赞
     * @param messageOwnerId    消息发布者ID
     * @param msgId             消息ID
     * @return                  结果
     */
    Task<ResultBase> removeLikes(@NonNull String messageOwnerId, @NonNull String msgId);

    /**
     * 获取用户通知列表
     * @param startId   分页起始ID
     * @param pageSize  分页大小
     * @return          通知列表
     */
    Task<NotificationResult> getNotifications(@NonNull String startId, int pageSize);

    /**
     * 举报消息
     * @param messageOwnerId    消息发布者ID
     * @param msgId             消息ID
     * @return                  举报结果
     */
    Task<ResultBase> reportMessage(@NonNull String messageOwnerId, @NonNull String msgId);

    /**
     * 全局配置
     * @return      配置结果
     */
    Task<ConfigurationResult> getConfiguration();

    /**
     * 获取访问Amazon S3需要的信息
     * @return 访问Amazon S3需要的信息
     */
    Task<S3ConfigResult> getS3Config();
}
