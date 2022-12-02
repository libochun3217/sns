package com.charlee.sns.model;

import android.net.Uri;

import com.charlee.sns.BuildConfig;
import com.charlee.sns.data.CardItem;
import com.charlee.sns.data.Message;
import com.charlee.sns.data.MessageResult;
import com.charlee.sns.data.MotuSnsService;
import com.charlee.sns.data.PagedList;
import com.charlee.sns.data.ResultBase;
import com.charlee.sns.data.UserDetailsResult;
import com.charlee.sns.data.UserInfo;
import com.charlee.sns.exception.RequestFailedException;
import com.charlee.sns.helper.EventConstant;
import com.charlee.sns.helper.IVideoUploader;
import com.charlee.sns.helper.RecommendUtils;
import com.charlee.sns.helper.ReportHelper;
import com.charlee.sns.manager.SnsEnvController;
import com.charlee.sns.model.userlist.FolloweesUserList;
import com.charlee.sns.model.userlist.FollowersUserList;
import com.charlee.sns.model.userlist.RecommendUserList;
import com.charlee.sns.storage.Storage;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import bolts.Continuation;
import bolts.Task;

/**
 * 用户模型类，为避免与数据层同名所以命名为SnsUser
 */
public class SnsUser extends ModelBase {
    private final IMotuSns motuSns;
    protected final IModelRepository repository;
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

    private final FollowersUserList followersUserList;
    private final FolloweesUserList followeesUserList;
    private RecommendUserList recommendUserList;

    private UserInfo userInfo;
    private IPageableList<UserMessage> messages;

    private String recommendReason;

    // 仅供UserRepository调用以保证每个用户只有唯一实例
    SnsUser(@NonNull IMotuSns motuSns, @NonNull final IModelRepository repository, @NonNull UserInfo userInfo) {
        if (BuildConfig.DEBUG) {
            if (repository.getUserById(userInfo.getId()) != null) {
                throw new InvalidParameterException("DO NOT create a different instance for the same ID!");
            }
        }

        this.motuSns = motuSns;
        this.repository = repository;

        followersUserList = new FollowersUserList(motuSns, repository, this);
        followeesUserList = new FolloweesUserList(motuSns, repository, this);
        recommendUserList = new RecommendUserList(motuSns, repository, this, userInfo.getFollowees());

        update(userInfo);

        // 这个属性本来应该由服务器传递，但是现在服务器无法支持，所以暂时由客户端随机获取
        recommendReason = RecommendUtils.getRecommendReason(SnsEnvController.getInstance().getAppContext());

        itemId = Long.getLong(this.userInfo.getId(),
                this.userInfo.getId().hashCode() * this.userInfo.getNickName().hashCode());
    }

    // 仅供UserRepository调用。
    // 因用户对象会更频繁调用此接口所以采用读写锁。
    boolean update(@NonNull UserInfo userInfo) {
        if (BuildConfig.DEBUG) {
            if (this.userInfo != null && !userInfo.getId().equals(this.userInfo.getId())) {
                throw new InvalidParameterException("Update SnsUser with a different ID!");
            }
        }

        rwLock.writeLock().lock();
        boolean updated = false;
        boolean updateMessage = false;
        boolean updateRecommend = false;
        try {
            if (this.userInfo == null) {
                this.userInfo = userInfo;
                updated = true;
                updateMessage = true;
            } else {
                PagedList<Message> oldMsgs = this.userInfo.getMessages();
                updated = this.userInfo.update(userInfo);
                updateMessage = oldMsgs != userInfo.getMessages();

                PagedList<UserInfo> oldUsers = this.userInfo.getFollowees();
                updateRecommend = oldUsers != userInfo.getFollowees();
            }
        } finally {
            rwLock.writeLock().unlock();
        }

        if (updateMessage) {
            messages = new UserMessageList(motuSns, repository, userInfo, userInfo.getMessages());
        }

        if (updateRecommend) {
            recommendUserList = new RecommendUserList(motuSns, repository, this,
                    getUnFollowedRecommendList(userInfo.getFollowees()));
        }

        return updated;
    }

    private PagedList<UserInfo> getUnFollowedRecommendList(PagedList<UserInfo> userPagedList) {
        if (userPagedList == null) {
            return null;
        }
        List<UserInfo> userList = userPagedList.getData();
        Iterator<UserInfo> itr = userList.iterator();
        while (itr.hasNext()) {
            UserInfo info = itr.next();
            if (info.isFollowed() != null && info.isFollowed() == true) {
                itr.remove();
            }
            if (SnsModel.getInstance().isUserLoggedIn()) {
                if (info.getId().equals(SnsModel.getInstance().getLoginUser().getId())) {
                    itr.remove();
                }
            }
        }
        return new PagedList<>(userPagedList.hasMore(), userPagedList.getLastId(), userList);
    }

    public void updateWithProfileChanged(@NonNull UserInfo userInfo) {
        if (update(userInfo)) {
            notifyDataChanged();
        }
    }

    public void updateWithLogout() {
        userInfo.setIsFollowed(false);
        notifyDataChanged();
    }

    public void notifyDataChanged() {
        setChanged();
        notifyObservers();
    }

    @NonNull
    public String getId() {
        return userInfo.getId();
    }

    public boolean isLoginUser() {
        return this == SnsModel.getInstance().getLoginUser();
    }

    public boolean isSameUser(String userId) {
        if (userId == null) {
            return false;
        }
        return userId.equals(userInfo.getId());
    }

    public boolean isAnonymous() {
        if (userInfo == null) {
            return false;
        }

        if (!isLoginUser()) {
            return false;
        }

        return Storage.getInstance().getLoginType() == MotuSnsService.LOGIN_SRC_ANONYMOUS;
    }

    @NonNull
    public String getNickName() {
        return userInfo.getNickName();
    }

    @NonNull
    public String getPortraitUri() {
        return userInfo.getPortraitUrl();
    }

    public void setPortraitUri(String uri) {
        userInfo.setPortraitUrl(uri);
    }

    public int getFollowerNum() {
        Integer followerNum = userInfo.getFollowerNum();
        return followerNum == null ? 0 : followerNum;
    }

    public int getFolloweeNum() {
        Integer followeeNum = userInfo.getFolloweeNum();
        return followeeNum == null ? 0 : followeeNum;
    }

    public int getMessageNum() {
        Integer messageNum = userInfo.getMessageNum();
        return messageNum == null ? 0 : messageNum;
    }

    public IPageableList<SnsUser> getFollowees() {
        return followeesUserList;
    }

    public IPageableList<SnsUser> getFollowers() {
        return followersUserList;
    }

    public IPageableList<SnsUser> getRecommendUsers() {
        return recommendUserList;
    }

    public String getRecommendReason() {
        return recommendReason;
    }

    @NonNull
    public IPageableList<UserMessage> getMessages() {
        return messages;
    }

    public boolean isFollowed() {
        Boolean isFollowed = userInfo.isFollowed();
        return isFollowed == null ? false : isFollowed;
    }

    public boolean isFollower() {
        return userInfo.isFollower();
    }

    public boolean isOfficial() {
        return userInfo.isOfficial();
    }

    public Task<Boolean> updateDetails() {
        return motuSns.getUserDetails(userInfo.getId()).onSuccess(new Continuation<UserDetailsResult, Boolean>() {
            @Override
            public Boolean then(Task<UserDetailsResult> task) {
                if (!task.getResult().isValid()) {
                    return false;
                }

                UserInfo info = task.getResult().getUserInfo();
                if (update(info)) {
                    setChanged();
                    notifyObservers();
                }

                return true;
            }
        }, Task.UI_THREAD_EXECUTOR);
    }

    public Task<Boolean> follow() {
        if (!isFollowed()) {
            return motuSns.followUser(userInfo.getId()).continueWith(new Continuation<ResultBase, Boolean>() {
                @Override
                public Boolean then(Task<ResultBase> task) throws Exception {
                    if (!task.isFaulted()) {
                        userInfo.setIsFollowed(true);
                        userInfo.modifyFollowerNum(true);
                        SnsModel.getInstance().getLoginUser().modifyLoginUserFolloweeNum(true);
                        setChanged();
                        notifyObservers();

                        return true;
                    } else {
                        Exception exception = task.getError();

                        // 处理服务器在消息中没有返回关注状态的情况
                        if (exception instanceof RequestFailedException) {
                            RequestFailedException requestErr = (RequestFailedException) exception;
                            if (requestErr.getErrCode() == RequestFailedException.ERR_ALREADY_FOLLOWED) {
                                userInfo.setIsFollowed(true);
                                setChanged();
                                notifyObservers();
                                return true;
                            }
                        }

                        if (exception != null) {
                            throw exception;
                        }
                        return false;
                    }

                }
            }, Task.UI_THREAD_EXECUTOR);
        } else {
            return Task.forResult(true);
        }
    }

    public Task<Boolean> unfollow() {
        if (isFollowed()) {
            return motuSns.unfollowUser(userInfo.getId()).continueWith(new Continuation<ResultBase, Boolean>() {
                @Override
                public Boolean then(Task<ResultBase> task) throws Exception {
                    if (!task.isFaulted()) {
                        userInfo.setIsFollowed(false);
                        userInfo.modifyFollowerNum(false);
                        SnsModel.getInstance().getLoginUser().modifyLoginUserFolloweeNum(false);
                        setChanged();
                        notifyObservers();
                        return true;
                    } else {
                        Exception exception = task.getError();
                        if (exception != null) {
                            throw exception;
                        }
                        return false;
                    }
                }
            }, Task.UI_THREAD_EXECUTOR);
        } else {
            return Task.forResult(true);
        }
    }

    private void modifyLoginUserFolloweeNum(boolean increase) {
        userInfo.modifyFolloweeNum(increase);
        setChanged();
        notifyObservers();
    }

    /**
     * 删除消息。只是调用服务器接口进行删除操作并返回结果。
     *
     * @return
     */
    @NonNull
    public Task<Boolean> deleteMessage(@NonNull final UserMessage message) {
        if (!isLoginUser()) {
            return Task.forResult(false);
        }

        message.setIsRemoving(true);
        return motuSns.deleteMessage(message.getPublisher().getId(), message.getId()).continueWith(
                new Continuation<ResultBase, Boolean>() {
                    @Override
                    public Boolean then(Task<ResultBase> task) throws Exception {
                        message.setIsRemoving(false);
                        if (!task.isFaulted()) {
                            message.setRemoved();
                            userInfo.modifyMessageNum(false); // 修改消息数
                            return true;
                        } else {
                            throw task.getError();
                        }
                    }
                });
    }

    private class PostMessageContinuation implements Continuation<MessageResult, Boolean> {
        private Publish publish;

        public PostMessageContinuation(Publish publish) {
            this.publish = publish;
        }

        @Override
        public Boolean then(Task<MessageResult> task) throws Exception {
            if (!task.isFaulted()) {
                Message message = task.getResult().getMessage();
                message.setUser(userInfo);
                UserMessage userMessage = repository.getMessageByData(message);
                userMessage.setImage(publish.getImageUri(), publish.getWidth(), publish.getHeight());
                String videoPath = publish.getVideoPath();
                if (!TextUtils.isEmpty(videoPath)) {
                    userMessage.setVideo(videoPath, publish.getImageUri(), publish.getWidth(), publish.getHeight());
                }

                messages.add(userMessage);  // 加入用户消息
                userInfo.modifyMessageNum(true); // 修改消息数
                ISnsModel model = SnsModel.getInstance();
                model.getFeedList().add(userMessage);   // 加入Feed流
                CardItem cardItem = new CardItem(Card.NORMAL_MESSAGE, message);
                model.getCardList().add(repository.getCardByData(cardItem));    // 加入Card流

                model.getLatestMessages().add(userMessage); // 加入最新消息

                SnsModel.getInstance().getPublishArray().remove(publish);

                setChanged();
                notifyObservers();


                ReportHelper.publishMessage(SnsEnvController.getInstance().getAppContext(),
                        true, message.getId());
                return true;
            } else {
                publish.setState(SnsModel.PublishedState.FAILED);
                Exception exception = task.getError();
                if (exception instanceof RequestFailedException) {
                    RequestFailedException requestFailedException = (RequestFailedException) exception;
                    int err = requestFailedException.getErrCode();
                    if (err == RequestFailedException.ERR_PIC_FORBIDDEN) {
                        publish.setState(SnsModel.PublishedState.PIC_FORBIDDEN);
                    } else if (err == RequestFailedException.ERR_USER_FORBIDDEN) {
                        publish.setState(SnsModel.PublishedState.USER_FORBIDDEN);
                    }
                }

                SnsModel.getInstance().getPublishArray().updateValue();


                ReportHelper.publishMessage(SnsEnvController.getInstance().getAppContext(), true, "");
            }

            return false;
        }
    }

    public Task<Boolean> postMessage(@NonNull final String description,
                                     @NonNull final String imageUri,
                                     @Nullable final String videoPath,
                                     final int width, final int height,
                                     final ArrayList<String> campaignIds) {
        if (!isLoginUser()) {
            return Task.forResult(false);
        }

        // 生成一个发布对象，用于维护发布过程中的对应消息状态
        final Publish publish = new Publish(description, imageUri, videoPath, width, height,
                campaignIds, SnsModel.PublishedState.PUBLISHING);
        SnsModel.getInstance().getPublishArray().add(publish);

        if (videoPath != null) {
            final IVideoUploader videoUploader = null ;
            return videoUploader.prepare().onSuccessTask(new Continuation<Void, Task<String>>() {
                @Override
                public Task<String> then(Task<Void> task) throws Exception {
                    return videoUploader.upload(videoPath);
                }
            }).onSuccessTask(new Continuation<String, Task<MessageResult>>() {
                @Override
                public Task<MessageResult> then(Task<String> task) throws Exception {
                    String videoUri = task.getResult();
                    return motuSns.postMessage(userInfo.getId(), description, imageUri, videoUri,
                            width, height, campaignIds);
                }
            }).continueWith(new PostMessageContinuation(publish), Task.UI_THREAD_EXECUTOR);
        }

        return motuSns.postMessage(userInfo.getId(), description, imageUri, videoPath, width, height, campaignIds)
                .continueWith(new PostMessageContinuation(publish), Task.UI_THREAD_EXECUTOR);

    }

    // region Uri Navigation

    // 注意：请不要将此常量公开！！！！！！！！！！导航的细节上层不需要知道！！！！！！！！！！
    private static final String QUERY_USER_ID = "id";

    /**
     * 获取用户对象的导航查询参数。跳转后的Activity可以从ISnsModel.getUserByNavUri得到这个对象。
     *
     * @param queries 查询参数
     */
    public void getNavQuery(@NonNull Map<String, String> queries) {
        repository.assureUser(this);
        queries.put(QUERY_USER_ID, this.getId());
    }

    static Task<SnsUser> getUserByNavUri(@NonNull final IModelRepository repository,
                                         @NonNull final IMotuSns motuSns, @NonNull Uri uri) {
        final String id = uri.getQueryParameter(QUERY_USER_ID);
        if (id == null) {
            return Task.forError(null);
        }

        SnsUser user = repository.getUserById(id);
        if (user != null) {
            return Task.forResult(user);
        }

        return motuSns.getUserDetails(id).onSuccess(new Continuation<UserDetailsResult, SnsUser>() {
            @Override
            public SnsUser then(Task<UserDetailsResult> task) throws Exception {
                UserInfo userInfo = task.getResult().getUserInfo();
                return repository.getUserByData(userInfo);
            }
        });

    }

    // endregion
}
