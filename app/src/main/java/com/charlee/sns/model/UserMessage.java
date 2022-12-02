package com.charlee.sns.model;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.charlee.sns.BuildConfig;
import com.charlee.sns.data.CommentsResult;
import com.charlee.sns.data.Image;
import com.charlee.sns.data.Message;
import com.charlee.sns.data.MessageComment;
import com.charlee.sns.data.MessageResult;
import com.charlee.sns.data.PagedList;
import com.charlee.sns.data.PublishCommentResult;
import com.charlee.sns.data.ResultBase;
import com.charlee.sns.data.Tag;
import com.charlee.sns.data.Video;
import com.charlee.sns.helper.NavigationHelper;
import com.charlee.sns.manager.SnsEnvController;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import bolts.Continuation;
import bolts.Task;

/**
 */
public class UserMessage extends ModelBase {
    private static final int CAMPAIGN_DETAIL = 1;
    private static final int WEBVIEW = 2;
    private static final int NONE_LOGIC = 3;

    private final IMotuSns motuSns;
    private final IModelRepository repository;

    private Message message;
    private SnsUser publisher;
    private SnsImage messageImage;
    private SnsVideo messageVideo;
    private List<MessageTag> tags = new ArrayList<>();

    private IPageableList<Comment> comments;

    private IPageableList<Comment> publishComments;

    private boolean canBeShared = false;

    // 标识是否为静音状态
    private boolean mutedStatus;

    // 由于交互设计的要求，点赞的结果和赞的数目这里保存的是UI的状态，和数据层会不一致
    private boolean isLikedForUI;
    private int likeNumForUI;

    private boolean isRemoving; // 是否正在删除中的状态
    private SnsModel.PublishedState state;

    // 仅供UserRepository调用以保证每条消息只有唯一实例
    UserMessage(@NonNull final IMotuSns motuSns,
                @NonNull final IModelRepository repository,
                @NonNull final Message message) {
        if (BuildConfig.DEBUG) {
            if (repository.getMessageById(message.getId()) != null) {
                throw new InvalidParameterException("DO NOT create a different instance for the same ID!");
            }
        }

        this.motuSns = motuSns;
        this.repository = repository;
        publishComments = new CommentList(motuSns, repository,
                IPageableList.TOTAL_SIZE_INFINITE, PageableList.PagingType.IdBased, null);
        update(message);
        itemId = Long.getLong(this.getId(),
                this.getId().hashCode() * this.getPublisher().getNickName().hashCode());
    }

    synchronized void update(@NonNull final Message message) {
        if (message == null || !message.isValid()) {
            if (BuildConfig.DEBUG) {
                // 如果在此处抛出异常请检查代码逻辑！！！！！！！！！
                throw new InvalidParameterException("Update Message with null object!");
            } else {
                return;
            }
        }

        PagedList<MessageComment> oldComments = this.message != null ? this.message.getComments() : null;

        boolean updateLikes = false;
        if (this.message == null) {
            // 消息创建后不能修改，消息内容（图片、描述、标签）只需要初始化一次
            this.message = message;
            Image image = message.getContent().getImage();
            if (image != null) {
                messageImage = new SnsImage(image);
            }

            Video video = message.getContent().getVideo();
            if (video != null) {
                messageVideo = new SnsVideo(video);
            }

            List<Tag> tagList = message.getContent().getTags();
            if (tagList != null && !tagList.isEmpty()) {
                for (Tag tag : tagList) {
                    if (tag.isValid()) {
                        tags.add(repository.getTagByData(tag));
                    }
                }
            }

            updateLikes = true;
        } else {
            if (this.message.update(message)) {
                updateLikes = true;
            }
        }

        publisher = repository.getUserByData(this.message.getUser()); // 更新用户
        canBeShared = message.getShareUrl() != null && !message.getShareUrl().isEmpty();
        if (updateLikes) {
            isLikedForUI = message.isLiked() != null ? message.isLiked() : false;
            likeNumForUI = message.getLikeNum() != null ? message.getLikeNum() : 0;
        }

        // 更新评论
        if (comments == null || oldComments != this.message.getComments()) {
            int commentsNum = this.message.getCommentNum() != null
                    ? this.message.getCommentNum() : PageableList.TOTAL_SIZE_INFINITE;
            comments = new CommentList(motuSns, repository,
                    commentsNum, PageableList.PagingType.IdBased, this.message.getComments());
        }
    }

    @NonNull
    public String getId() {
        return message.getId();
    }

    @NonNull
    public SnsUser getPublisher() {
        return publisher;
    }

    @Nullable
    public SnsImage getImage() {
        return messageImage != null ? messageImage : messageVideo.getCoverImage();
    }

    @Nullable
    public SnsVideo getVideo() {
        return messageVideo;
    }

    // 仅供模型层发新消息时调用
    void setImage(String uri, int width, int height) {
        messageImage = new SnsImage(new Image(uri, width, height));
    }

    // TODO: 开发完成后删除
    public void setVideo(SnsVideo video) {
        this.messageVideo = video;
    }

    // 仅供模型层发新消息时调用
    void setVideo(String videoUri, String coverUri, int width, int height) {
        messageVideo = new SnsVideo(new Video(videoUri, coverUri, null, width, height));
    }

    @Nullable
    public String getDescription() {
        return message.getContent().getDescription();
    }

    @Nullable
    public List<MessageTag> getTags() {
        return tags;
    }

    /**
     * 获取消息创建的时间
     *
     * @return 创建时间(时间戳, 但单位为秒)
     */
    public long getCreateTime() {
        return message.getCreateTime();
    }

    public boolean canBeShared() {
        return canBeShared;
    }

    @Nullable
    public String getShareUrl() {
        return message.getShareUrl();
    }

    @NonNull
    public IPageableList<Comment> getComments() {
        return comments;
    }

    public boolean isLiked() {
        return isLikedForUI;
    }

    public int getLikeNum() {
        return likeNumForUI;
    }

    public boolean isRemoving() {
        return isRemoving;
    }

    public void setIsRemoving(boolean isRemoving) {
        this.isRemoving = isRemoving;
    }

    public IPageableList<Comment> getPublishComments() {
        return publishComments;
    }

    public int getToType() {
        return message.getToType();
    }

    @Nullable
    public String getToId() {
        return message.getToId();
    }

    @Nullable
    public String getToTitle() {
        return message.getToTitle();
    }

    @Nullable
    public String getToUrl() {
        return message.getToUrl();
    }

    @NonNull
    public Task<Boolean> like() {
        if (!isLikedForUI) {
            isLikedForUI = true;
            ++likeNumForUI;
            return motuSns.setLikes(message.getUser().getId(), message.getId()).onSuccess(
                    new Continuation<ResultBase, Boolean>() {
                        @Override
                        public Boolean then(Task<ResultBase> task) throws Exception {
                            message.setIsLiked(true);
                            return true;
                        }
                    });
        }

        return Task.forResult(true);
    }

    @NonNull
    public Task<Boolean> removeLike() {
        if (isLikedForUI) {
            isLikedForUI = false;
            --likeNumForUI;
            if (likeNumForUI < 0) {
                likeNumForUI = 0;
            }
            return motuSns.removeLikes(message.getUser().getId(), message.getId()).onSuccess(
                    new Continuation<ResultBase, Boolean>() {
                        @Override
                        public Boolean then(Task<ResultBase> task) throws Exception {
                            message.setIsLiked(false);
                            return true;
                        }
                    });
        }

        return Task.forResult(true);
    }

    @NonNull
    public Task<Boolean> postComment(final String content, final Comment fatherComment) {
        long timeStamp = System.currentTimeMillis();
        final MessageComment commentData = new MessageComment(
                String.valueOf(timeStamp), // 保证每个评论ID不同
                content,
                timeStamp / 1000,
                motuSns.getLoggedInUser(),
                fatherComment != null ? fatherComment.getData() : null);

        final Comment comment = new Comment(motuSns, repository, commentData);
        comment.setState(SnsModel.PublishedState.PUBLISHING);
        publishComments.add(comment);

        String fatherCommentId = null;
        if (fatherComment != null) {
            fatherCommentId = fatherComment.getId();
        }

        return motuSns.postComment(message.getUser().getId(), message.getId(), content, fatherCommentId).continueWith(
                new Continuation<PublishCommentResult, Boolean>() {
                    @Override
                    public Boolean then(Task<PublishCommentResult> task) throws Exception {
                        if (!task.isFaulted()) {
                            publishComments.remove(comment);

                            // 发布成功后
                            // 1，更新commentID
                            // 2，更新createTime，取用服务器的时间，避免本地时间错误引发的界面时间不一致
                            // 3，重新创建一个Comment对象，避免发布列表和评论列表公用一个对象引发的刷新问题
                            commentData.setId(task.getResult().getCommentId());
                            commentData.setCreateTime(task.getResult().getServerTimeStamp());
                            comment.setState(SnsModel.PublishedState.PUBLISHED);
                            Comment commentPublished = new Comment(motuSns, repository, commentData);
                            comments.add(commentPublished);

                            setChanged();
                            notifyObservers();
                            return true;
                        } else {
                            comment.setState(SnsModel.PublishedState.FAILED);
                            return false;
                        }
                    }
                }, Task.UI_THREAD_EXECUTOR);
    }

    @NonNull
    public Task<Boolean> deleteComment(final Comment comment) {
        return motuSns.deleteComment(message.getUser().getId(), message.getId(), comment.getId()).onSuccess(
                new Continuation<ResultBase, Boolean>() {
                    @Override
                    public Boolean then(Task<ResultBase> task) throws Exception {
                        comment.setRemoved();
                        setChanged();
                        notifyObservers();
                        return true;
                    }
                }
        );
    }

    public Task<Boolean> report() {
        return motuSns.reportMessage(message.getUser().getId(), message.getId()).onSuccess(
                new Continuation<ResultBase, Boolean>() {
                    @Override
                    public Boolean then(Task<ResultBase> task) throws Exception {
                        return true;
                    }
                }
        );
    }

    public void setState(SnsModel.PublishedState state) {
        this.state = state;
    }

    public SnsModel.PublishedState getState() {
        return state;
    }

    public boolean isMuted() {
        return mutedStatus;
    }

    public void setMuted(boolean status) {
        mutedStatus = status;
    }

    public void onClick(Context context) {
        switch (getToType()) {
            case CAMPAIGN_DETAIL:
                String id = getToId();
                if (id != null) {
                    NavigationHelper.navigateToCampaignDetailsPage(context, id);
                }
                break;
            case WEBVIEW:
                String url = getToUrl();
                if (url != null) {
                    SnsEnvController.getInstance().getAdBridge().openByWebView(context, getToTitle(), null, url);
                }
                break;
            case NONE_LOGIC:
                break;
            default:
                break;
        }
    }

    // region Uri Navigation

    // 注意：请不要将此常量公开！！！！！！！！！！导航的细节上层不需要知道！！！！！！！！！！
    private static final String QUERY_MESSAGE_ID = "id"; // 消息ID

    /**
     * 获取用户对象的导航查询参数。跳转后的Activity可以从ISnsModel.getUserByNavUri得到这个对象。
     *
     * @param queries 查询参数
     */
    public void getNavQuery(@NonNull Map<String, String> queries) {
        repository.assureMessage(this);
        queries.put(QUERY_MESSAGE_ID, this.getId());
    }

    static Task<UserMessage> getMessageByNavUri(@NonNull final IModelRepository repository,
                                                @NonNull final IMotuSns motuSns,
                                                @NonNull Uri uri,
                                                boolean forceRefresh) {
        final String id = uri.getQueryParameter(QUERY_MESSAGE_ID);
        if (id == null) {
            return Task.forError(null);
        }

        if (!forceRefresh) {
            UserMessage userMessage = repository.getMessageById(id);
            if (userMessage != null) {
                return Task.forResult(userMessage);
            }
        }

        // 接口UserID后端不需要验证，直接传0
        String userId = "0";
        return motuSns.getMessage(userId, id).onSuccess(
                new Continuation<MessageResult, UserMessage>() {
                    @Override
                    public UserMessage then(Task<MessageResult> task) throws Exception {
                        Message message = task.getResult().getMessage();
                        return repository.getMessageByData(message);
                    }
                });
    }

    // endregion

    class CommentList extends PageableList<Comment, MessageComment> {
        public CommentList(@NonNull final IMotuSns motuSns,
                           @NonNull final IModelRepository repository,
                           int totalSize,
                           PageableList.PagingType pagingType,
                           PagedList<MessageComment> firstPage) {
            super(motuSns, repository, totalSize, pagingType, firstPage);
        }

        private Continuation<CommentsResult, PagedList<MessageComment>> continuation =
                new Continuation<CommentsResult, PagedList<MessageComment>>() {
                    @Override
                    public PagedList<MessageComment> then(Task<CommentsResult> task) throws Exception {
                        return task.getResult().getComments();
                    }
                };

        @NonNull
        @Override
        protected Task<PagedList<MessageComment>> getFirstPage() {
            return UserMessage.this.motuSns.getMessageComments(
                    UserMessage.this.message.getUser().getId(),
                    UserMessage.this.message.getId(), IMotuSns.FIRST_PAGE_ID, IMotuSns.DEFAULT_PAGE_SIZE)
                    .onSuccess(continuation);
        }

        @NonNull
        @Override
        protected Task<PagedList<MessageComment>> getNextPage(@NonNull PagedList<MessageComment> before) {
            String lastId = before.getLastId();
            if (lastId == null || lastId.isEmpty()) {
                return Task.forResult(null);
            }

            return UserMessage.this.motuSns.getMessageComments(
                    UserMessage.this.message.getUser().getId(),
                    UserMessage.this.message.getId(), lastId, IMotuSns.DEFAULT_PAGE_SIZE)
                    .onSuccess(continuation);
        }

        @Override
        protected Comment createModel(@NonNull MessageComment data) {
            return repository.getCommentByData(data);
        }

        @Override
        protected boolean hasSameId(@NonNull Comment model, @NonNull MessageComment data) {
            return model.getId().equals(data.getId());
        }
    }

}
