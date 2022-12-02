package com.charlee.sns.model;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.charlee.sns.data.Campaign;
import com.charlee.sns.data.CardItem;
import com.charlee.sns.data.CarouselResult;
import com.charlee.sns.data.ConfigurationResult;
import com.charlee.sns.data.LoginResult;
import com.charlee.sns.data.Message;
import com.charlee.sns.data.MessageComment;
import com.charlee.sns.data.MotuSns;
import com.charlee.sns.data.MotuSnsService;
import com.charlee.sns.data.MotuSnsServiceProvider;
import com.charlee.sns.data.NotificationItem;
import com.charlee.sns.data.ResultBase;
import com.charlee.sns.data.Tag;
import com.charlee.sns.data.UserDetailsResult;
import com.charlee.sns.data.UserInfo;
import com.charlee.sns.helper.ObservableArrayList;
import com.charlee.sns.manager.ISnsNetworkParams;
import com.charlee.sns.manager.MonitorManager;
import com.charlee.sns.manager.SnsEnvController;
import com.charlee.sns.manager.SnsUserNotificationManager;
import com.charlee.sns.model.userlist.HotUsersList;
import com.charlee.sns.storage.Storage;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;

import bolts.Continuation;
import bolts.Task;

/**
 * 模型层：社区接口
 */
public class SnsModel implements ISnsModel {
    // region Private Fields

    private final IMotuSns snsApi;

    private SnsUser loginUser = null;
    private FeedsList feedsList;
    private CardList cardList;

    private final HotUsersList hotUsersList;
    private final HotTagsList hotTagsList;
    private final LatestMessageList latestMessageList;
    private final HotMessageList hotMessageList;
    private final NotificationList mUserNotificationList;
    private final CampaignActiveList campaignActiveList;
    private final CampaignHistoryList campaignHistoryList;

    private ObservableArrayList<Publish> publishArray = new ObservableArrayList<>();

    private LoginEvent loginEvent = new LoginEvent();

    private static final int INIT_USER_POOL_SIZE = 200;
    private static final int INIT_MESSAGE_POOL_SIZE = 500;
    private static final int INIT_TAG_POOL_SIZE = 200;
    private static final int INIT_NOTIFICATION_CACHE_SIZE = 200;
    private static final int INIT_CAMPAIGH_CACHE_SIZE = 200;

    // 发布消息的状态
    public enum PublishedState {
        PUBLISHED,
        PUBLISHING,
        FAILED,
        PIC_FORBIDDEN,  // 图片违规被禁止发布
        USER_FORBIDDEN  // 用户被封禁
    }

    // 模型对象池
    private final Repository<SnsUser, UserInfo> userRepository =
            new Repository<SnsUser, UserInfo>(INIT_USER_POOL_SIZE) {
                @Override
                protected String getModelId(@NonNull SnsUser model) {
                    return model.getId();
                }

                @Override
                protected SnsUser create(UserInfo data) {
                    return new SnsUser(snsApi, repository, data);
                }

                @Override
                protected void updateModel(SnsUser model, UserInfo data) {
                    model.update(data);
                }
            };

    private final Repository<UserMessage, Message> messageRepository =
            new Repository<UserMessage, Message>(INIT_MESSAGE_POOL_SIZE) {
                @Override
                protected String getModelId(@NonNull UserMessage model) {
                    return model.getId();
                }

                @Override
                protected UserMessage create(Message data) {
                    return new UserMessage(snsApi, repository, data);
                }

                @Override
                protected void updateModel(UserMessage model, Message data) {
                    model.update(data);
                }
            };

    private final Repository<MessageTag, Tag> tagRepository =
            new Repository<MessageTag, Tag>(INIT_TAG_POOL_SIZE) {
                @Override
                protected String getModelId(@NonNull MessageTag model) {
                    return model.getId();
                }

                @Override
                protected MessageTag create(Tag data) {
                    return new MessageTag(snsApi, repository, data);
                }

                @Override
                protected void updateModel(MessageTag model, Tag data) {
                    model.update(data);
                }
            };

    private final Repository<Comment, MessageComment> commentRepository =
            new Repository<Comment, MessageComment>(INIT_TAG_POOL_SIZE) {
                @Override
                protected String getModelId(@NonNull Comment model) {
                    return model.getId();
                }

                @Override
                protected Comment create(MessageComment data) {
                    return new Comment(snsApi, repository, data);
                }

                @Override
                protected void updateModel(Comment model, MessageComment data) {
                    model.update(data);
                }
            };

    private final Repository<UserNotification, NotificationItem> notificationRepository =
            new Repository<UserNotification, NotificationItem>(INIT_NOTIFICATION_CACHE_SIZE) {
                @Override
                protected String getModelId(@NonNull UserNotification model) {
                    return model.getId();
                }

                @Override
                protected UserNotification create(@NonNull NotificationItem data) {
                    return new UserNotification(snsApi, repository, data);
                }

                @Override
                protected void updateModel(@NonNull UserNotification model, @NonNull NotificationItem data) {
                    // TODO 考虑是否添加更新
                }
            };

    private final Repository<SnsCampaign, Campaign> campaignRepository =
            new Repository<SnsCampaign, Campaign>(INIT_CAMPAIGH_CACHE_SIZE) {
                @Override
                protected String getModelId(@NonNull SnsCampaign model) {
                    return model.getId();
                }

                @Override
                protected SnsCampaign create(@NonNull Campaign data) {
                    return new SnsCampaign(snsApi, repository, data);
                }

                @Override
                protected void updateModel(@NonNull SnsCampaign model, @NonNull Campaign data) {
                    model.update(data);
                }
            };

    private final Repository<Card, CardItem> cardRepository =
            new Repository<Card, CardItem>(INIT_CAMPAIGH_CACHE_SIZE) {
                @Override
                protected String getModelId(@NonNull Card model) {
                    return model.getId();
                }

                @Override
                protected Card create(@NonNull CardItem data) {
                    return new Card(snsApi, repository, data);
                }

                @Override
                protected void updateModel(@NonNull Card model, @NonNull CardItem data) {
                    model.update(data);
                }
            };

    private final IModelRepository repository = new IModelRepository() {
        @NonNull
        @Override
        public SnsUser getUserByData(UserInfo user) {
            return userRepository.getModelByData(user);
        }

        @Nullable
        @Override
        public SnsUser getUserById(String id) {
            return userRepository.getModelById(id);
        }

        @Override
        public void assureUser(SnsUser user) {
            userRepository.assureModel(user);
        }

        @NonNull
        @Override
        public UserMessage getMessageByData(Message msg) {
            return messageRepository.getModelByData(msg);
        }

        @Nullable
        @Override
        public UserMessage getMessageById(String id) {
            return messageRepository.getModelById(id);
        }

        @Override
        public void assureMessage(UserMessage message) {
            messageRepository.assureModel(message);
        }

        @NonNull
        @Override
        public MessageTag getTagByData(Tag tag) {
            return tagRepository.getModelByData(tag);
        }

        @Nullable
        @Override
        public MessageTag getTagById(String id) {
            return tagRepository.getModelById(id);
        }

        @Override
        public void assureTag(MessageTag tag) {
            tagRepository.assureModel(tag);
        }

        @NonNull
        @Override
        public Comment getCommentByData(MessageComment comment) {
            return commentRepository.getModelByData(comment);
        }

        @Nullable
        @Override
        public Comment getCommentById(String id) {
            return commentRepository.getModelById(id);
        }

        @Override
        public void assureCampaign(SnsCampaign campaign) {
            campaignRepository.assureModel(campaign);
        }

        @NonNull
        @Override
        public SnsCampaign getCampaignByData(Campaign campaign) {
            return campaignRepository.getModelByData(campaign);
        }

        @Nullable
        @Override
        public SnsCampaign getCampaignById(String id) {
            return campaignRepository.getModelById(id);
        }

        @NonNull
        @Override
        public Card getCardByData(CardItem cardItem) {
            return cardRepository.getModelByData(cardItem);
        }

        @Nullable
        @Override
        public Card getCardById(String id) {
            return cardRepository.getModelById(id);
        }

        @NonNull
        @Override
        public UserNotification getUserNotificationByData(NotificationItem data) {
            return notificationRepository.getModelByData(data);
        }
    };

    // endregion

    // region Weak Singleton

    private static SoftReference<SnsModel> instance = null;

    /**
     * 获取魔图模型的单例。
     * 注意该单例为SoftReference，所以请注意生命周期的管理。
     * 只应该在视图层调用。模型层对象如果需要应该从构造函数注入。
     */
    public static synchronized ISnsModel getInstance() {
        SnsModel inst = instance == null ? null : instance.get();
        if (inst == null) {
            ISnsNetworkParams networkParams = SnsEnvController.getInstance().getNetworkParams();
            MotuSnsService service = MotuSnsServiceProvider.createMotuSnsService(Storage.getInstance(), networkParams);
            IMotuSns motuSns = new MotuSns(service, Storage.getInstance(), networkParams);
            instance = new SoftReference<>(inst = new SnsModel(motuSns));
        }

        return inst;
    }

    // endregion

    // region Constructors

    SnsModel(final IMotuSns snsApi) {
        this.snsApi = snsApi;

        cardList = new CardList(snsApi, repository,
                IPageableList.TOTAL_SIZE_INFINITE, PageableList.PagingType.IndexBased);

        hotUsersList = new HotUsersList(snsApi, repository);
        hotTagsList = new HotTagsList(snsApi, repository);
        latestMessageList = new LatestMessageList(snsApi, repository);
        hotMessageList = new HotMessageList(snsApi, repository);
        mUserNotificationList = new NotificationList(snsApi, repository);
        campaignActiveList = new CampaignActiveList(snsApi, repository, MotuSnsService.CAMPAIGN_TYPE_OPERATION);
        campaignHistoryList = new CampaignHistoryList(snsApi, repository);

        if (snsApi.isUserLoggedIn()) {
            loginUser = repository.getUserByData(snsApi.getLoggedInUser());
            feedsList = new FeedsList(snsApi, repository);
            if (!snsApi.isUserInfoUpdated()) {
                snsApi.getUserDetails(loginUser.getId()).continueWith(new Continuation<UserDetailsResult, Object>() {
                    @Override
                    public Object then(Task<UserDetailsResult> task) throws Exception {
                        if (!task.isFaulted()) {
                            loginUser.update(task.getResult().getUserInfo());
                        }
                        return null;
                    }
                });
            }
        }

        Task.delay(1000).continueWith(new Continuation<Void, Object>() {
            @Override
            public Object then(Task<Void> task) throws Exception {
                return hotUsersList.refresh();
            }
        });
    }

    // endregion

    // region Public Overrides

    @Override
    public long getTimeOffsetInSeconds() {
        return snsApi.getTimeOffsetInSeconds();
    }

    @NonNull
    @Override
    public IPageableList<SnsUser> getHotUsers() {
        filterFollowedHotUsers();
        return hotUsersList;
    }

    @NonNull
    @Override
    public IPageableList<MessageTag> getHotTags() {
        return hotTagsList;
    }

    @NonNull
    @Override
    public IPageableList<UserMessage> getLatestMessages() {
        return latestMessageList;
    }

    @NonNull
    @Override
    public IPageableList<UserMessage> getHotMessages() {
        return hotMessageList;
    }

    @NonNull
    @Override
    public IPageableList<UserNotification> getUserNotificationList() {
        return mUserNotificationList;
    }

    @Nullable
    @Override
    public IPageableList<SnsCampaign> getCampaignActiveList() {
        return campaignActiveList;
    }

    @Nullable
    @Override
    public IPageableList<SnsCampaign> getCampaignHistoryList() {
        return campaignHistoryList;
    }

    @NonNull
    @Override
    public ObservableArrayList<Publish> getPublishArray() {
        return publishArray;
    }

    @Override
    public ModelBase getLoginEvent() {
        return loginEvent;
    }

    /**
     * 用户是否已登录
     *
     * @return true: 用户已登录；false：用户未登录
     */
    @Override
    public boolean isUserLoggedIn() {
        // 不要缓存登录状态。MotuSns中会根据服务器返回的错误值清除登录状态。
        return snsApi.isUserLoggedIn();
    }

    @Override
    public String getAnonymousUserId() {
        if (loginUser != null) {
            return loginUser.getId();
        }
        return "";
    }

    @NonNull
    @Override
    public Task<Void> login(int userSrcType, String oauthToken, String pushToken, String anonymousId, String fcmToken) {

        return snsApi.login(userSrcType, oauthToken, pushToken, anonymousId, fcmToken)
                .onSuccess(new Continuation<LoginResult, Void>() {
                    @Override
                    public Void then(Task<LoginResult> task) throws Exception {
                        SnsModel.this.loginUser = repository.getUserByData(task.getResult().getUserInfo());
                        SnsModel.this.loginUser.notifyDataChanged();
                        fillDataWhenLogin();
                        loginEvent.setChanged();
                        loginEvent.notifyObservers(true);

                        SnsUserNotificationManager.getInstance().removeAllNotifications();
                        SnsUserNotificationManager.getInstance().restart();
                        return null;
                    }
                });
    }

    @Override
    public Task<Boolean> logout() {
        if (snsApi.isUserLoggedIn()) {
            return snsApi.logout().continueWith(new Continuation<ResultBase, Boolean>() {
                @Override
                public Boolean then(Task<ResultBase> task) throws Exception {
                    // 对于注销行为，不需要判断成功与否，直接设置状态即可
                    // 由于注销过程需要清空cookie，所以注销后的操作需要在continueWith执行
                    SnsUser temp = loginUser;
                    loginUser = null;
                    temp.updateWithLogout();

                    clearDataWhenLogout();
                    loginEvent.setChanged();
                    loginEvent.notifyObservers(false);

                    SnsUserNotificationManager.getInstance().stop();
                    Storage.getInstance().setNewNotificationStatus(false);

                    MonitorManager.getInstance().getFollowListMonitor().stop();
                    Storage.getInstance().setNewFollowStatus(false);
                    return null;
                }
            }, Task.UI_THREAD_EXECUTOR);
        } else {
            return Task.forResult(false);
        }
    }

    @NonNull
    @Override
    public Task<Boolean> updateLoginProfile(int userSrcType, String oauthToken, String pushToken,
                                            final String portraitUri, String nickName, String fcmToken) {
        return snsApi.updateLoginProfile(userSrcType, oauthToken, pushToken, portraitUri, nickName, fcmToken)
                .continueWith(new Continuation<UserDetailsResult, Boolean>() {
                    @Override
                    public Boolean then(Task<UserDetailsResult> task) throws Exception {
                        if (task.isFaulted() || !task.getResult().isValid()) {
                            return false;
                        }
                        UserInfo info = task.getResult().getUserInfo();
                        // 使用本地缓存的图片URI
                        info.setPortraitUrl(portraitUri);
                        loginUser.updateWithProfileChanged(info);
                        return true;
                    }
                });
    }

    @Nullable
    @Override
    public SnsUser getLoginUser() {
        return snsApi.isUserLoggedIn() ? loginUser : null;
    }

    /**
     * 获取关注消息列表
     *
     * @return 消息列表
     */
    @Nullable
    @Override
    public IPageableList<UserMessage> getFeedList() {
        return feedsList;
    }

    @Nullable
    @Override
    public IPageableList<Card> getCardList() {
        return cardList;
    }

    @Override
    public Task<Carousel> getCarousel() {
        return snsApi.getCarousel().onSuccess(new Continuation<CarouselResult, Carousel>() {
            @Override
            public Carousel then(Task<CarouselResult> task) throws Exception {
                return new Carousel(task.getResult());
            }
        });
    }

    /**
     * 通过导航Uri获取用户对象
     *
     * @param uri 导航Uri
     *
     * @return 用户对象。不存在则返回null
     */
    @Nullable
    @Override
    public Task<SnsUser> getUserByNavUri(final Uri uri) {
        return SnsUser.getUserByNavUri(repository, snsApi, uri);
    }

    /**
     * 通过导航Uri获取消息对象
     *
     * @param uri 导航Uri
     *
     * @return 标签对象。不存在则返回null
     */
    @NonNull
    @Override
    public Task<UserMessage> getMessageByNavUri(Uri uri, boolean forceRefresh) {
        return UserMessage.getMessageByNavUri(repository, snsApi, uri, forceRefresh);
    }

    /**
     * 通过导航Uri获取标签对象
     *
     * @param uri 导航Uri
     *
     * @return 标签对象。不存在则返回null
     */
    @NonNull
    @Override
    public Task<MessageTag> getTagByNavUri(final Uri uri) {
        return MessageTag.getTagByNavUri(repository, snsApi, uri);
    }

    /**
     * 通过导航Uri获取活动对象
     *
     * @param uri 导航Uri
     *
     * @return 活动对象。不存在则返回null
     */
    @NonNull
    @Override
    public Task<SnsCampaign> getCampaignByNavUri(final Uri uri, boolean forceRefresh) {
        return SnsCampaign.getCampaignByNavUri(repository, snsApi, uri, forceRefresh);
    }

    @NonNull
    @Override
    public Task<SnsCampaign> getCampaignById(String id) {
        return SnsCampaign.getCampaignById(repository, snsApi, id, true);
    }

    @NonNull
    @Override
    public Task<Void> refreshSnsConfiguration() {
        return snsApi.getConfiguration().onSuccess(new Continuation<ConfigurationResult, Void>() {
            @Override
            public Void then(Task<ConfigurationResult> task) throws Exception {
                if (task != null && task.getResult() != null) {
                    ConfigurationResult cr = task.getResult();
                    if (cr.isValid()) {
                        int notificationRefreshPeriodcr = cr.getConfiguration().getNotification().getRefreshPeriod();
                        Storage.getInstance().setConfigNotificationRefreshPeriod(notificationRefreshPeriodcr);
                    }
                }
                return null;
            }
        });
    }
    // endregion

    // region private functions

    /**
     * 注销的时候清空社区系统数据
     */
    private void clearDataWhenLogout() {
        try {
            cardList.refresh();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 清空通知栏数据，通过Observable机制刷新通知页面
        if (mUserNotificationList != null) {
            mUserNotificationList.clear();
        }
    }

    /**
     * 登录成功的时候填充社区数据
     */
    private void fillDataWhenLogin() {
        try {
            // 第一次登录的时候，这个对象为NULL
            if (feedsList == null) {
                feedsList = new FeedsList(snsApi, repository);
            }

            hotUsersList.refresh();
            mUserNotificationList.refresh();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 再次过滤已经关注的用户
    private void filterFollowedHotUsers() {
        if (hotUsersList == null) {
            return;
        }
        List<SnsUser> removedList = new ArrayList<>();
        for (int index = 0; index < hotUsersList.size(); index++) {
            SnsUser user = hotUsersList.get(index);
            if (user.isFollowed()) {
                removedList.add(user);
            }
        }

        if (removedList.size() > 0) {
            for (SnsUser user : removedList) {
                hotUsersList.filter(user);
            }
        }
    }

    // endregion

}
