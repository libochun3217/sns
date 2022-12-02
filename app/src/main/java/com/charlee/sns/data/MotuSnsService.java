package com.charlee.sns.data;


import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Map;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PartMap;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

/**
 */
public interface MotuSnsService {
    // region Constants

    // 返回值定义（在数据层会针对错误抛出异常。上层处理的错误定义在RequestFailedException内）
    int ERR_SUCCESS = 0;
    int ERR_NEED_LOGIN = -7;
    int ERR_UNSUPPORTED_VERSION = -10; // 客户端使用的API版本已经不被服务器支持，需要升级

    // 分页相关查询
    /**
     * 基于ID的分页，使用查询参数：
     * {@link com.charlee.sns.data.MotuSnsService#QUERY_PAGE_START_ID}
     * {@link com.charlee.sns.data.MotuSnsService#QUERY_PAGE_SIZE}
     * 基于Index的分页，使用查询参数：
     * {@link com.charlee.sns.data.MotuSnsService#QUERY_PAGE_START}
     * {@link com.charlee.sns.data.MotuSnsService#QUERY_PAGE_SIZE}
     */

    /**
     * 分页起始计数，用于列表排序不固定的场景（如热门用户、热门标签、热门消息等）
     */
    String QUERY_PAGE_START = "start";

    /**
     * 分页起始ID，用于列表顺序固定的场景（如Feed流、关注列表、粉丝列表、消息列表、通知等）     *
     */
    String QUERY_PAGE_START_ID = "start_id";

    /**
     *分页时页面大小
     */
    String QUERY_PAGE_SIZE = "size";

    /**
     * 嵌入子资源。如在用户详情结果中嵌入发布的消息及评论可使用"embed=messages,message.comments"
     */
    String EMBED_CHILDREN_QUERY = "embed";
    String EMBED_CHILDREN_KEY_COMMENTS = "comments";

    // endregion

    // region Motu SNS service public interface

    @GET("/motusns/carousels")
    Call<CarouselResult> getCarousel();

    @GET("/motusns/hot/users")
    Call<UsersResult> getHotUsers(@QueryMap Map<String, String> options);

    @GET("/motusns/hot/tags")
    Call<TagsResult> getHotTags(@QueryMap Map<String, String> options);

    @GET("/motusns/hot/messages")
    Call<MessagesResult> getHotMessages(@QueryMap Map<String, String> options);

    @GET("/motusns/new/messages")
    Call<MessagesResult> getLatestMessages(@QueryMap Map<String, String> options);

    @GET("/motusns/tags/{tagId}/messages")
    Call<MessagesResult> getTagMessages(@Path("tagId") String tagId, @QueryMap Map<String, String> options);

    @GET("/motusns/users/{userId}")
    Call<UserDetailsResult> getUserDetails(@Path("userId") String userId, @QueryMap Map<String, String> options);

    @GET("/motusns/users/{userId}/messages")
    Call<MessagesResult> getUserMessages(@Path("userId") String userId, @QueryMap Map<String, String> options);

    @GET("/motusns/users/{userId}/messages/{messageId}")
    Call<MessageResult> getMessage(@Path("userId") String userId, @Path("messageId") String messageId,
                                   @Query("embed") String embedContent);

    @Multipart
    @POST("/motusns/users/{userId}/messages")
    Call<MessageResult> postMessage(@Path("userId") String userId,
                                    @PartMap Map<String, RequestBody> bodyMap);

    @DELETE("/motusns/users/{userId}/messages/{messageId}")
    Call<ResultBase> deleteMessage(@Path("userId") String userId,
                                   @Path("messageId") String messageId);

    @GET("/motusns/users/{userId}/messages/{msgId}/comments")
    Call<CommentsResult> getMessageComments(@Path("userId") String userId,
                                            @Path("msgId") String msgId,
                                            @QueryMap Map<String, String> options);

    @FormUrlEncoded
    @POST("/motusns/users/{userId}/messages/{msgId}/comments")
    Call<PublishCommentResult> postComment(@Path("userId") String userId,
                                           @Path("msgId") String msgId,
                                           @FieldMap Map<String, String> options);

    @DELETE("/motusns/users/{userId}/messages/{msgId}/comments/{commentId}")
    Call<ResultBase> deleteComment(@Path("userId") String userId,
                                   @Path("msgId") String msgId,
                                   @Path("commentId") String commentId);

    @GET("/motusns/users/{userId}/followers")
    Call<UsersResult> getUserFollowers(@Path("userId") String userId, @QueryMap Map<String, String> options);

    @GET("/motusns/users/{userId}/followees")
    Call<UsersResult> getUserFollowees(@Path("userId") String userId, @QueryMap Map<String, String> options);

    @GET("/motusns/now/campaigns")
    Call<CampaignsResult> getActiveCampaigns(@Query("campaign_type") int campaignType);

    @GET("/motusns/history/campaigns")
    Call<CampaignsResult> getHistoryCampaigns(@QueryMap Map<String, String> options);

    @GET("/motusns/campaigns/{campaignId}")
    Call<CampaignResult> getCampaign(@Path("campaignId") String campaignId);

    @GET("/motusns/campaigns/{campaignId}/messages")
    Call<MessagesResult> getCampaignMessages(@Path("campaignId") String campaignId,
                                             @QueryMap Map<String, String> options);

    // 服务端暂不支持
    // @GET("/motusns/users/{msgOwnerId}/messages/{msgId}/likes/{userId}")
    // Call<LikesResult> getLikes(@Path("msgOwnerId") String msgOwnerId, @Path("msgId") String msgId,
    //                          @Path("userId") String userId);

    // endregion

    // region Constants and Annotation for login

    /**
     * login允许的userSrcType取值
     */
    @IntDef({LOGIN_SRC_FACEBOOK, LOGIN_SRC_QQ, LOGIN_SRC_DEBUG, LOGIN_SRC_WEIXIN, LOGIN_SRC_TWITTER,
            LOGIN_SRC_KAKAO, LOGIN_SRC_ANONYMOUS})
    @Retention(RetentionPolicy.SOURCE) // annotation不保存到.class文件
    @interface LoginSourceType { }

    int LOGIN_SRC_INVALID = -1;     // 无效登录方式
    int LOGIN_SRC_FACEBOOK = 0;     // Facebook登录
    int LOGIN_SRC_QQ = 1;           // QQ登录
    int LOGIN_SRC_DEBUG = 2;        // 调试模式登录
    int LOGIN_SRC_WEIXIN = 3;       // 微信登录
    int LOGIN_SRC_TWITTER = 4;      // TWITTER登录
    int LOGIN_SRC_KAKAO = 5;        // KAKAO登录
    int LOGIN_SRC_ANONYMOUS = 6;    // 匿名登录

    int CAMPAIGN_TYPE_OPERATION = 1;       // 运营活动
    int CAMPAIGN_TYPE_FUNCTION = 2; // 功能活动

    // endregion

    // region Motu SNS service interfaces which needs authentication

    /**
     * 登录
     *
     * @return 登录结果
     */
    @FormUrlEncoded
    @POST("/motusns/users")
    Call<LoginResult> login(@FieldMap Map<String, String> fieldMap, @Query("bind_anonymous") String bindAnonymous);

    @DELETE("/motusns/users/{userId}/session")
    Call<ResultBase> logout(@Path("userId") String userId);

    /**
     * 更新用户配置
     * @param userId        当前登录的用户ID
     * @param options       需要更新的列表
     * @return
     */
    @Multipart
    @POST("/motusns/users/{userId}")
    Call<UserDetailsResult> updateLoginProfile(@Path("userId") String userId,
                                        @PartMap Map<String, RequestBody> options);

    @GET("/motusns/users/{userId}/feeds")
    Call<MessagesResult> getFeeds(@Path("userId") String userId, @QueryMap Map<String, String> options);

    @GET("/motusns/users/{userId}/notifications")
    Call<NotificationResult> getNotifications(@Path("userId") String userId, @QueryMap Map<String, String> options);

    @POST("/motusns/users/{userId}/followers")
    Call<ResultBase> followUser(@Path("userId") String userId);

    @DELETE("/motusns/users/{followeeid}/followers/{followerid}")
    Call<ResultBase> unfollowUser(@Path("followeeid") String followeeid, @Path("followerid") String followerid);

    @POST("/motusns/users/{msgOwnerId}/messages/{msgId}/likes")
    Call<ResultBase> setLikes(@Path("msgOwnerId") String msgOwnerId, @Path("msgId") String msgId);

    @DELETE("/motusns/users/{msgOwnerId}/messages/{msgId}/likes/{userId}")
    Call<ResultBase> removeLikes(@Path("msgOwnerId") String msgOwnerId, @Path("msgId") String msgId,
                                  @Path("userId") String userId);

    @POST("/motusns/users/{msgOwnerId}/messages/{msgId}/reports")
    Call<ResultBase> reportMessage(@Path("msgOwnerId") String msgOwnerId, @Path("msgId") String msgId);

    @GET("/motusns/users/cards")
    Call<CardsResult> getCards( @QueryMap Map<String, String> options);

    // endregion

    // region Motu SNS service for configuration

    @GET("/motusns/configuration")
    Call<ConfigurationResult> getConfiguration();

    /**
     * 获取访问Amazon S3需要的信息
     * @return
     */
    @GET("/motusns/api/s3info")
    Call<S3ConfigResult> getS3Config();

    // endregion
}
