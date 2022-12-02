package com.charlee.sns.data;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import com.charlee.sns.exception.HttpClientErrorException;
import com.charlee.sns.exception.HttpServerErrorException;
import com.charlee.sns.exception.InvalidDataException;
import com.charlee.sns.exception.NeedUpgradeException;
import com.charlee.sns.exception.NotLoggedInException;
import com.charlee.sns.exception.RequestFailedException;
import com.charlee.sns.manager.ISnsNetworkParams;
import com.charlee.sns.model.IMotuSns;
import com.charlee.sns.storage.IStorage;
import com.charlee.sns.storage.Storage;
import com.google.gson.Gson;

import android.net.Uri;
import android.text.format.DateUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import bolts.Task;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Response;

/**
 * 魔图社区数据层接口实现。
 */
public class MotuSns implements IMotuSns {

    // region Private Methods

    private static final Map<String, String> EMPTY_OPTIONS = new HashMap<>();
    private final MotuSnsService service;
    private final IStorage storage;
    private final ISnsNetworkParams networkParams;

    private UserInfo loggedInUser; // 已登录用户
    private boolean isUserInfoUpdated;

    private long timeOffsetInSeconds; // 本地时间与服务器时间的差，

    // endregion

    // region Constructors

    public MotuSns(@NonNull final MotuSnsService service, @NonNull final IStorage storage,
                   @NonNull final ISnsNetworkParams networkParams) {
        this.service = service;
        this.storage = storage;
        this.networkParams = networkParams;
        String loginCookie = storage.getLoginCookie();
        if (loginCookie != null && !loginCookie.isEmpty()) {
            String userInfo = storage.getUserInfo();
            if (userInfo != null && !userInfo.isEmpty()) {
                loggedInUser = new Gson().fromJson(userInfo, UserInfo.class);
                isUserInfoUpdated = false;
            }
        }
    }

    // endregion

    // region Public Overrides

    @Override
    public long getTimeOffsetInSeconds() {
        return timeOffsetInSeconds;
    }

    @Override
    public Task<CarouselResult> getCarousel() {
        return Task.callInBackground(new Callable<CarouselResult>() {
            @Override
            public CarouselResult call() throws Exception {
                Response<CarouselResult> response = service.getCarousel().execute();
                updateTimeOffset(response);
                checkForError(response);
                return response.body();
            }
        });
    }

    @Override
    public Task<UsersResult> getHotUsers(final int startIndex, final int pageSize) {
        return Task.callInBackground(new Callable<UsersResult>() {
            public UsersResult call() throws Exception {
                Map<String, String> options = new HashMap<>();
                options.put(MotuSnsService.QUERY_PAGE_START, String.valueOf(startIndex));
                options.put(MotuSnsService.QUERY_PAGE_SIZE, String.valueOf(pageSize));
                options.put("type", String.valueOf(1));
                Response<UsersResult> response = service.getHotUsers(options).execute();
                updateTimeOffset(response);
                checkForError(response);
                return response.body();
            }
        });
    }

    @Override
    public Task<TagsResult> getHotTags(final int startIndex, final int pageSize) {
        return Task.callInBackground(new Callable<TagsResult>() {
            public TagsResult call() throws Exception {
                Map<String, String> options = getPageQueryMap(startIndex, pageSize);
                Response<TagsResult> response = service.getHotTags(options).execute();
                updateTimeOffset(response);
                checkForError(response);
                return response.body();
            }
        });
    }

    @Override
    public Task<MessagesResult> getLatestMessages(@NonNull final String startId, final int pageSize) {
        return Task.callInBackground(new Callable<MessagesResult>() {
            public MessagesResult call() throws Exception {
                Map<String, String> options = getPageQueryMap(startId, pageSize);
                options.put(MotuSnsService.EMBED_CHILDREN_QUERY, MotuSnsService.EMBED_CHILDREN_KEY_COMMENTS);
                Response<MessagesResult> response = service.getLatestMessages(options).execute();
                updateTimeOffset(response);
                checkForError(response);
                return response.body();
            }
        });
    }

    @Override
    public Task<MessagesResult> getTagMessages(@NonNull final String tagId,
                                               @NonNull final int startIndex, final int pageSize) {
        return Task.callInBackground(new Callable<MessagesResult>() {
            public MessagesResult call() throws Exception {
                Map<String, String> options = getPageQueryMap(startIndex, pageSize);
                options.put(MotuSnsService.EMBED_CHILDREN_QUERY, MotuSnsService.EMBED_CHILDREN_KEY_COMMENTS);
                Response<MessagesResult> response = service.getTagMessages(tagId, options).execute();
                updateTimeOffset(response);
                checkForError(response);
                return response.body();
            }
        });
    }

    @Override
    public Task<MessagesResult> getHotMessages(final int startIndex, final int pageSize) {
        return Task.callInBackground(new Callable<MessagesResult>() {
            public MessagesResult call() throws Exception {
                Map<String, String> options = getPageQueryMap(startIndex, pageSize);
                options.put(MotuSnsService.EMBED_CHILDREN_QUERY, MotuSnsService.EMBED_CHILDREN_KEY_COMMENTS);
                Response<MessagesResult> response = service.getHotMessages(options).execute();
                updateTimeOffset(response);
                checkForError(response);
                return response.body();
            }
        });
    }

    @Override
    public Task<UserDetailsResult> getUserDetails(@NonNull final String id) {
        return Task.callInBackground(new Callable<UserDetailsResult>() {
            public UserDetailsResult call() throws Exception {
                final Map<String, String> options = new HashMap<>();
                options.put("embed", "followees");
                Response<UserDetailsResult> response = service.getUserDetails(id, options).execute();
                updateTimeOffset(response);
                checkForError(response);

                response.body().getUserInfo().setForceUpdate();

                return response.body();
            }
        });
    }

    @Override
    public Task<MessagesResult> getUserMessages(@NonNull final String id,
                                                @NonNull final String startId, final int pageSize) {
        return Task.callInBackground(new Callable<MessagesResult>() {
            public MessagesResult call() throws Exception {
                Map<String, String> options = getPageQueryMap(startId, pageSize);
                options.put(MotuSnsService.EMBED_CHILDREN_QUERY, MotuSnsService.EMBED_CHILDREN_KEY_COMMENTS);
                Response<MessagesResult> response = service.getUserMessages(id, options).execute();
                updateTimeOffset(response);
                checkForError(response);
                return response.body();
            }
        });
    }

    @Override
    public Task<MessageResult> getMessage(@NonNull final String userId, @NonNull final String messageId) {
        return Task.callInBackground(new Callable<MessageResult>() {
            @Override
            public MessageResult call() throws Exception {
                Response<MessageResult> response = service.getMessage(userId, messageId, "comments").execute();
                updateTimeOffset(response);
                checkForError(response);
                return response.body();
            }
        });
    }

    @Override
    public Task<MessageResult> postMessage(@NonNull final String id, @NonNull final String description,
                                           @NonNull final String imageUri, @Nullable final String videoUri,
                                           final int width, final int height,
                                           @Nullable final ArrayList<String> campaignIds) {
        if (loggedInUser == null) {
            return Task.forError(new NotLoggedInException());
        }

        return Task.callInBackground(new Callable<MessageResult>() {
            @Override
            public MessageResult call() throws Exception {
                Map<String, RequestBody> bodyMap = new HashMap<>();
                Uri fileUri = Uri.parse(imageUri);
                String imagePath = fileUri.getPath();
                String imageFileName = fileUri.getLastPathSegment();
                File imageFile = new File(imagePath);
                RequestBody fileBody = RequestBody.create(MediaType.parse("image/*"), imageFile);
                bodyMap.put("image\"; filename=\"" + imageFileName + "", fileBody);
                RequestBody descBody = RequestBody.create(MediaType.parse("text/plain"), description);
                bodyMap.put("description", descBody);
                RequestBody widthBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(width));
                bodyMap.put("width", widthBody);
                RequestBody heightBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(height));
                bodyMap.put("height", heightBody);

                // 发布视频消息时的可选参数
                if (videoUri != null) {
                    RequestBody messageTypeBody = RequestBody.create(MediaType.parse("text/plain"),
                            String.valueOf(MESSAGE_TYPE_VIDEO));
                    bodyMap.put("message_type", messageTypeBody);
                    RequestBody videoUriBody = RequestBody.create(MediaType.parse("text/plain"), videoUri);
                    bodyMap.put("video_path", videoUriBody);
                }

                if (campaignIds != null && campaignIds.size() > 0) {
                    String campaigns = "";
                    for (int index = 0; index < campaignIds.size(); index++) {
                        campaigns += campaignIds.get(index);
                        if (index < campaignIds.size() - 1) {
                            campaigns += ',';
                        }
                    }
                    RequestBody campaignIdBody = RequestBody.create(MediaType.parse("text/plain"), campaigns);
                    bodyMap.put("campaigns", campaignIdBody);
                }

                Response<MessageResult> response = service.postMessage(id, bodyMap).execute();
                updateTimeOffset(response);
                checkForError(response);
                return response.body();
            }
        });
    }

    @Override
    public Task<ResultBase> deleteMessage(@NonNull final String messageOwnerId, @NonNull final String msgId) {
        if (loggedInUser == null) {
            return Task.forError(new NotLoggedInException());
        }

        if (!loggedInUser.getId().equals(messageOwnerId)) {
            return Task.forError(new IllegalArgumentException("Cannot delete message of others."));
        }

        return Task.callInBackground(new Callable<ResultBase>() {
            public ResultBase call() throws Exception {
                Response<ResultBase> response = service.deleteMessage(messageOwnerId, msgId).execute();
                checkForError(response);
                return response.body();
            }
        });
    }

    @Override
    public Task<CommentsResult> getMessageComments(
            @NonNull final String userId,
            @NonNull final String msgId,
            @NonNull final String startId,
            final int pageSize) {
        return Task.callInBackground(new Callable<CommentsResult>() {
            public CommentsResult call() throws Exception {
                Map<String, String> options = getPageQueryMap(startId, pageSize);
                Response<CommentsResult> response = service.getMessageComments(userId, msgId, options).execute();
                updateTimeOffset(response);
                checkForError(response);
                return response.body();
            }
        });
    }

    @Override
    public Task<PublishCommentResult> postComment(
            @NonNull final String userId, @NonNull final String msgId, @NonNull final String content,
            @Nullable final String fatherCommentId) {
        if (loggedInUser == null) {
            return Task.forError(new NotLoggedInException());
        }

        final Map<String, String> options = new HashMap<>();
        options.put("content", content);
        if (fatherCommentId != null) {
            options.put("father_comment", fatherCommentId);
        }

        return Task.callInBackground(new Callable<PublishCommentResult>() {
            @Override
            public PublishCommentResult call() throws Exception {
                Response<PublishCommentResult> response = service.postComment(userId, msgId, options).execute();
                updateTimeOffset(response);
                checkForError(response);
                return response.body();
            }
        });
    }

    @Override
    public Task<ResultBase> deleteComment(
            @NonNull final String userId, @NonNull final String msgId, @NonNull final String commentId) {
        if (loggedInUser == null) {
            return Task.forError(new NotLoggedInException());
        }

        return Task.callInBackground(new Callable<ResultBase>() {
            @Override
            public ResultBase call() throws Exception {
                Response<ResultBase> response = service.deleteComment(userId, msgId, commentId).execute();
                checkForError(response);
                return response.body();
            }
        });
    }

    @Override
    public Task<UsersResult> getUserFollowers(@NonNull final String id,
                                              @NonNull final String startId, final int pageSize) {
        return Task.callInBackground(new Callable<UsersResult>() {
            public UsersResult call() throws Exception {
                Map<String, String> options = getPageQueryMap(startId, pageSize);
                Response<UsersResult> response = service.getUserFollowers(id, options).execute();
                updateTimeOffset(response);
                checkForError(response);
                return response.body();
            }
        });
    }

    @Override
    public Task<UsersResult> getUserFollowees(@NonNull final String id,
                                              @NonNull final String startId, final int pageSize) {
        return Task.callInBackground(new Callable<UsersResult>() {
            public UsersResult call() throws Exception {
                Map<String, String> options = getPageQueryMap(startId, pageSize);
                Response<UsersResult> response = service.getUserFollowees(id, options).execute();
                updateTimeOffset(response);
                checkForError(response);
                return response.body();
            }
        });
    }

    @Override
    public Task<CampaignsResult> getActiveCampaigns(final int campaignType) {
        return Task.callInBackground(new Callable<CampaignsResult>() {
            public CampaignsResult call() throws Exception {
                Response<CampaignsResult> response = service.getActiveCampaigns(campaignType).execute();
                updateTimeOffset(response);
                checkForError(response);
                return response.body();
            }
        });
    }

    @Override
    public Task<CampaignsResult> getHistoryCampaigns(final int campaignType,
                                                     @NonNull final String startId, final int pageSize) {
        return Task.callInBackground(new Callable<CampaignsResult>() {
            public CampaignsResult call() throws Exception {
                Map<String, String> options = getPageQueryMap(startId, pageSize);
                options.put("campaign_type", String.valueOf(campaignType));
                Response<CampaignsResult> response = service.getHistoryCampaigns(options).execute();
                updateTimeOffset(response);
                checkForError(response);
                return response.body();
            }
        });
    }

    @Override
    public Task<CampaignResult> getCampaign(@NonNull final String campaignId) {
        return Task.callInBackground(new Callable<CampaignResult>() {
            public CampaignResult call() throws Exception {
                Response<CampaignResult> response = service.getCampaign(campaignId).execute();
                updateTimeOffset(response);
                checkForError(response);
                return response.body();
            }
        });
    }

    @Override
    public Task<MessagesResult> getCampaignMessages(@NonNull final String campaignId,
                                                    @NonNull final int startIndex, final int pageSize) {
        return Task.callInBackground(new Callable<MessagesResult>() {
            public MessagesResult call() throws Exception {
                Map<String, String> options = getPageQueryMap(startIndex, pageSize);
                options.put(MotuSnsService.EMBED_CHILDREN_QUERY, MotuSnsService.EMBED_CHILDREN_KEY_COMMENTS);
                Response<MessagesResult> response = service.getCampaignMessages(campaignId, options).execute();
                updateTimeOffset(response);
                checkForError(response);
                return response.body();
            }
        });
    }

    @Override
    public boolean isUserLoggedIn() {
        return loggedInUser != null;
    }

    /**
     * 用户信息是否已更新
     *
     * @return 已更新则返回true，否则返回false
     */
    @Override
    public boolean isUserInfoUpdated() {
        return isUserInfoUpdated;
    }

    @Nullable
    @Override
    public UserInfo getLoggedInUser() {
        return loggedInUser;
    }

    @Override
    public Task<LoginResult> login(@MotuSnsService.LoginSourceType final int userSrcType,
                                   @NonNull final String oauthToken,
                                   @NonNull final String pushToken,
                                   @NonNull final String anonymousId,
                                   final String fcmToken) {
        return Task.callInBackground(new Callable<LoginResult>() {
            public LoginResult call() throws Exception {
                Map<String, String> fieldMap = new HashMap<>();
                fieldMap.put("user_src_type", String.valueOf(userSrcType));
                fieldMap.put("user_token", oauthToken);
                fieldMap.put("push_token", pushToken);
                if (fcmToken != null) {
                    fieldMap.put("fcm_token", fcmToken);
                }

                if (userSrcType == MotuSnsService.LOGIN_SRC_FACEBOOK) {
                    String androidId = networkParams.getAndroidId();
                    if (androidId != null) {
                        fieldMap.put("aid", androidId);
                    }
                    String googleId = networkParams.getGoogleId();
                    if (googleId != null) {
                        fieldMap.put("goid", googleId);
                    }
                }

                Response<LoginResult> response = service.login(fieldMap, anonymousId).execute();

                updateTimeOffset(response);
                checkForError(response);
                if (!response.body().isValid()) {
                    throw new InvalidDataException();
                }

                Headers header = response.headers();
                List<String> cookieList = header.values("Set-Cookie");
                for (String cookie : cookieList) {
                    if (cookie.startsWith("MTSNS_SID")) {
                        // save cookie
                        storage.setLoginCookie(cookie);
                        // save user info
                        loggedInUser = response.body().getUserInfo();
                        storage.setUserInfo(new Gson().toJson(loggedInUser));
                        isUserInfoUpdated = true;
                        break;
                    }
                }

                return response.body();
            }
        });
    }

    @Override
    public Task<ResultBase> logout() {
        if (loggedInUser == null) {
            return Task.forResult(new ResultBase(MotuSnsService.ERR_SUCCESS, "", false));
        }

        final String userId = loggedInUser.getId();
        removeLoginData(); // 不论服务器返回值如何都会清除本地Login数据。
        return Task.callInBackground(new Callable<ResultBase>() {
            @Override
            public ResultBase call() throws Exception {
                Response<ResultBase> response = service.logout(userId).execute();
                checkForError(response);
                return response.body();
            }
        });
    }

    @Override
    public Task<UserDetailsResult> updateLoginProfile(@MotuSnsService.LoginSourceType final int userSrcType,
                                                      @Nullable final String oauthToken,
                                                      @Nullable final String pushToken,
                                                      @Nullable final String portrait, @Nullable final String nickName,
                                                      final String fcmToken) {
        if (loggedInUser == null) {
            return Task.forError(new NotLoggedInException());
        }

        return Task.callInBackground(new Callable<UserDetailsResult>() {
            @Override
            public UserDetailsResult call() throws Exception {
                Map<String, RequestBody> bodyMap = new HashMap<>();
                if (pushToken != null) {
                    RequestBody pushTokenBody = RequestBody.create(MediaType.parse("text/plain"), pushToken);
                    bodyMap.put("push_token", pushTokenBody);
                }
                if (oauthToken != null) {
                    RequestBody typeBody = RequestBody.create(MediaType.parse("text/plain"),
                            String.valueOf(userSrcType));
                    bodyMap.put("user_src_type", typeBody);
                    RequestBody userTokenBody = RequestBody.create(MediaType.parse("text/plain"), oauthToken);
                    bodyMap.put("user_token", userTokenBody);
                }
                if (portrait != null) {
                    Uri fileUri = Uri.parse(portrait);
                    String imagePath = fileUri.getPath();
                    String imageFileName = fileUri.getLastPathSegment();
                    File imageFile = new File(imagePath);
                    RequestBody fileBody = RequestBody.create(MediaType.parse("image/*"), imageFile);
                    bodyMap.put("portrait\"; filename=\"" + imageFileName + "", fileBody);
                }
                if (nickName != null) {
                    RequestBody nickNameBody = RequestBody.create(MediaType.parse("text/plain"), nickName);
                    bodyMap.put("nick_name", nickNameBody);
                }
                if (fcmToken != null) {
                    RequestBody fcmTokenBody = RequestBody.create(MediaType.parse("text/plain"), fcmToken);
                    bodyMap.put("fcm_token", fcmTokenBody);
                }

                Response<UserDetailsResult> response =
                        service.updateLoginProfile(loggedInUser.getId(), bodyMap).execute();
                updateTimeOffset(response);
                checkForError(response);

                if (portrait != null || nickName != null) {
                    UserInfo userinfo = response.body().getUserInfo();
                    userinfo.setForceUpdate();
                    Storage.getInstance().setUserInfo(new Gson().toJson(userinfo));
                }

                return response.body();
            }
        });

    }

    /**
     * 获取关注用户发布的消息列表（Feed流）
     *
     * @return Feed流结果
     */
    @Override
    public Task<MessagesResult> getFeeds(@NonNull final String startId, final int pageSize) {
        if (loggedInUser == null) {
            return Task.forError(new NotLoggedInException());
        }

        return Task.callInBackground(new Callable<MessagesResult>() {
            public MessagesResult call() throws Exception {
                Map<String, String> options = getPageQueryMap(startId, pageSize);
                options.put(MotuSnsService.EMBED_CHILDREN_QUERY, MotuSnsService.EMBED_CHILDREN_KEY_COMMENTS);
                Response<MessagesResult> response = service.getFeeds(loggedInUser.getId(), options).execute();
                updateTimeOffset(response);
                checkForError(response);
                return response.body();
            }
        });
    }

    @Override
    public Task<CardsResult> getCards(@NonNull final String startId, final int pageSize) {
        return Task.callInBackground(new Callable<CardsResult>() {
            @Override
            public CardsResult call() throws Exception {
                Map<String, String> options = getPageQueryMap(startId, pageSize);
                options.put(MotuSnsService.EMBED_CHILDREN_QUERY, MotuSnsService.EMBED_CHILDREN_KEY_COMMENTS);
                Response<CardsResult> response = service.getCards(options).execute();
                updateTimeOffset(response);
                checkForError(response);
                return response.body();
            }
        });
    }

    @Override
    public Task<ResultBase> followUser(@NonNull final String userId) {
        if (loggedInUser == null) {
            return Task.forError(new NotLoggedInException());
        }

        return Task.callInBackground(new Callable<ResultBase>() {
            public ResultBase call() throws Exception {
                Response<ResultBase> response = service.followUser(userId).execute();
                checkForError(response);
                return response.body();
            }
        });
    }

    @Override
    public Task<ResultBase> unfollowUser(@NonNull final String followeeid) {
        if (loggedInUser == null) {
            return Task.forError(new NotLoggedInException());
        }

        return Task.callInBackground(new Callable<ResultBase>() {
            @Override
            public ResultBase call() throws Exception {
                Response<ResultBase> response = service.unfollowUser(followeeid, loggedInUser.getId()).execute();
                checkForError(response);
                return response.body();
            }
        });
    }

    @Override
    public Task<ResultBase> removeLikes(@NonNull final String messageOwnerId, @NonNull final String msgId) {
        if (loggedInUser == null) {
            return Task.forError(new NotLoggedInException());
        }

        return Task.callInBackground(new Callable<ResultBase>() {
            public ResultBase call() throws Exception {
                Response<ResultBase> response = service.removeLikes(
                        messageOwnerId, msgId, loggedInUser.getId()).execute();
                checkForError(response);
                return response.body();
            }
        });
    }

    @Override
    public Task<ResultBase> setLikes(@NonNull final String messageOwnerId, @NonNull final String msgId) {
        if (loggedInUser == null) {
            return Task.forError(new NotLoggedInException());
        }

        return Task.callInBackground(new Callable<ResultBase>() {
            public ResultBase call() throws Exception {
                Response<ResultBase> response = service.setLikes(messageOwnerId, msgId).execute();
                checkForError(response);
                return response.body();
            }
        });
    }

    @Override
    public Task<NotificationResult> getNotifications(@NonNull final String startId, final int pageSize) {
        if (loggedInUser == null) {
            return Task.forError(new NotLoggedInException());
        }

        return Task.callInBackground(new Callable<NotificationResult>() {
            public NotificationResult call() throws Exception {
                Map<String, String> options = getPageQueryMap(startId, pageSize);
                Response<NotificationResult> response =
                        service.getNotifications(loggedInUser.getId(), options).execute();
                updateTimeOffset(response);
                checkForError(response);
                return response.body();
            }
        });
    }

    @Override
    public Task<ResultBase> reportMessage(@NonNull final String messageOwnerId, @NonNull final String msgId) {
        if (loggedInUser == null) {
            return Task.forError(new NotLoggedInException());
        }

        return Task.callInBackground(new Callable<ResultBase>() {
            public ResultBase call() throws Exception {
                Response<ResultBase> response = service.reportMessage(messageOwnerId, msgId).execute();
                checkForError(response);
                return response.body();
            }
        });
    }

    @Override
    public Task<ConfigurationResult> getConfiguration() {
        return Task.callInBackground(new Callable<ConfigurationResult>() {
            @Override
            public ConfigurationResult call() throws Exception {
                Response<ConfigurationResult> response = service.getConfiguration().execute();
                updateTimeOffset(response);
                checkForError(response);
                return response.body();
            }
        });
    }

    @Override
    public Task<S3ConfigResult> getS3Config() {
        return Task.callInBackground(new Callable<S3ConfigResult>() {
            @Override
            public S3ConfigResult call() throws Exception {
                Response<S3ConfigResult> response = service.getS3Config().execute();
                updateTimeOffset(response);
                checkForError(response);
                return response.body();
            }
        });
    }

    // endregion

    // region Private Methods

    private Map<String, String> getPageQueryMap(int startIndex, int pageSize) {
        Map<String, String> options = new HashMap<>();
        if (startIndex != IMotuSns.FIRST_PAGE || pageSize != IMotuSns.DEFAULT_PAGE_SIZE) {
            options = new HashMap<>(2);
            options.put(MotuSnsService.QUERY_PAGE_START, String.valueOf(startIndex));
            options.put(MotuSnsService.QUERY_PAGE_SIZE, String.valueOf(pageSize));
        }

        return options;
    }

    private Map<String, String> getPageQueryMap(String startId, int pageSize) {
        Map<String, String> options = new HashMap<>();
        if (!IMotuSns.FIRST_PAGE_ID.equals(startId) || pageSize != IMotuSns.DEFAULT_PAGE_SIZE) {
            options.put(MotuSnsService.QUERY_PAGE_START_ID, startId);
            options.put(MotuSnsService.QUERY_PAGE_SIZE, String.valueOf(pageSize));
        }

        return options;
    }

    private <ResultTypeT extends ResultBase> void checkForError(Response<ResultTypeT> response)
            throws Exception {
        if (response.isSuccessful()) { // HTTP层无错误
            if (!response.body().isSuccess()) { // API返回值有错误
                // API返回错误值
                switch (response.body().getErrCode()) {
                    case MotuSnsService.ERR_NEED_LOGIN:
                        // 取消强制登录策略，直接向view层抛异常，由相关的view处理异常情况的展示和后续交互
                        throw new NotLoggedInException();
                    case MotuSnsService.ERR_UNSUPPORTED_VERSION:
                        throw new NeedUpgradeException();
                    default:
                        throw new RequestFailedException(response.body().getErrCode(), response.body().getErrMsg());
                }
            }

        } else {
            // HTTP错误码
            int errorCode = response.code();
            if (errorCode >= 400 && errorCode < 500) {
                throw new HttpClientErrorException(errorCode);
            } else if (errorCode >= 500 && errorCode < 600) {
                throw new HttpServerErrorException(errorCode);
            }
        }
    }

    private <ResultTypeT extends ResultBase> void updateTimeOffset(Response<ResultTypeT> response) {
        String dateString = response.headers().get("Date");
        if (dateString != null) {
            try {
                Date serverDate = Calendar.getInstance().getTime();
                long timeInSeconds = serverDate.getTime() / 1000;
                response.body().setServerTimeStamp(timeInSeconds);
                timeOffsetInSeconds = timeInSeconds - System.currentTimeMillis() / 1000; // 换算为秒
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void removeLoginData() {
        storage.removeLoginCookie();
        storage.removeUserInfo();
        loggedInUser = null;
    }

    // endregion
}
