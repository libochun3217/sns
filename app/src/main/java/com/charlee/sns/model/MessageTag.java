package com.charlee.sns.model;

import java.security.InvalidParameterException;
import java.util.List;
import java.util.Map;


import android.net.Uri;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.charlee.sns.BuildConfig;
import com.charlee.sns.data.Message;
import com.charlee.sns.data.MessagesResult;
import com.charlee.sns.data.PagedList;
import com.charlee.sns.data.Tag;

import bolts.Continuation;
import bolts.Task;

/**
 * 标签模型类
 */
public class MessageTag extends ModelBase {
    private final IMotuSns motuSns;
    private final IModelRepository repository;

    private Tag tag;
    private SnsImage tagImage;

    private boolean canBeShared = false;

    private static class TagMessage extends MessageList {
        private final String tagId;
        private IndexPager indexPager = new IndexPager(IMotuSns.DEFAULT_PAGE_SIZE);

        public TagMessage(@NonNull IMotuSns motuSns, @NonNull IModelRepository repository, int totalSize,
                          PagingType pagingType, String tagId, PagedList<Message> firstPage) {
            super(motuSns, repository, totalSize, pagingType, firstPage);
            this.tagId = tagId;
        }

        @NonNull
        @Override
        protected Task<PagedList<Message>> getFirstPage() {
            indexPager.reset();
            return motuSns.getTagMessages(tagId, IMotuSns.FIRST_PAGE, IMotuSns.DEFAULT_PAGE_SIZE)
                    .onSuccess(parseMessageResultContinuation);
        }

        @NonNull
        @Override
        protected Task<PagedList<Message>> getNextPage(@NonNull PagedList<Message> before) {
            if (before == null || before.getData() == null || !before.hasMore()) {
                return Task.forResult(null);
            }

            indexPager.moveToNextPage(before.getData().size());
            return motuSns.getTagMessages(tagId, indexPager.getCurrentStartIndex(), IMotuSns.DEFAULT_PAGE_SIZE)
                    .onSuccess(parseMessageResultContinuation);
        }
    }

    private IPageableList<UserMessage> messages;

    // 仅供UserRepository调用以保证每个标签只有唯一实例
    MessageTag(@NonNull final IMotuSns motuSns, @NonNull final IModelRepository repository, @NonNull final Tag tag) {
        if (BuildConfig.DEBUG) {
            if (repository.getTagById(tag.getId()) != null) {
                throw new InvalidParameterException("DO NOT create a different instance for the same ID!");
            }
        }

        this.motuSns = motuSns;
        this.repository = repository;
        update(tag);
        itemId = Long.getLong(this.getId(),
                this.getId().hashCode() * this.getName().hashCode());
    }

    synchronized void update(@NonNull final Tag other) {
        if (other == null) {
            if (BuildConfig.DEBUG) {
                throw new InvalidParameterException("Update MessageTag with null object!");
            } else {
                return;
            }
        }

        boolean needUpdateMessages = false;
        if (this.tag == null) {
            this.tag = other;
            needUpdateMessages = true;
        } else {
            PagedList<Message> oldMessages = this.tag.getMessages();
            this.tag.update(other);
            if (oldMessages != this.tag.getMessages()) {
                needUpdateMessages = true;
            }
        }

        if (messages == null || needUpdateMessages) {
            messages = new TagMessage(motuSns, repository, IPageableList.TOTAL_SIZE_INFINITE,
                    PageableList.PagingType.IndexBased, tag.getId(), tag.getMessages());
        }

        String tagImageUrl = tag.getImage();

        if (tagImageUrl == null) {
            // 如果标签自身不带图片则使用第一个消息的图片。使用已经过有效性验证的messages列表。
            if (messages != null && !messages.isEmpty()) {
                tagImageUrl = messages.get(0).getImage().getUrl();
            }
        }

        if (!TextUtils.isEmpty(tagImageUrl)) {
            if (this.tagImage == null || !tagImageUrl.equals(this.tagImage.getUrl())) {
                this.tagImage = new SnsImage(tagImageUrl);
            }
        }

        canBeShared = !TextUtils.isEmpty(tag.getShareUrl()) && tagImage != null;
    }

    public boolean canBeShared() {
        return canBeShared;
    }

    @NonNull
    String getId() {
        return tag.getId();
    }

    @NonNull
    public String getName() {
        return tag.getName();
    }

    @Nullable
    public String getShareUrl() {
        return tag.getShareUrl();
    }

    @Nullable
    public SnsImage getTagImage() {
        return tagImage;
    }

    @NonNull
    public IPageableList<UserMessage> getMessages() {
        return messages;
    }

    // region Uri Navigation

    private static final String NAV_QUERY_TAG_ID = "id";

    /**
     * 获取用户对象的导航查询参数。跳转后的Activity可以从ISnsModel.getUserByNavUri得到这个对象。
     *
     * @param queries 查询参数
     */
    public void getNavQuery(@NonNull Map<String, String> queries) {
        repository.assureTag(this);
        queries.put(NAV_QUERY_TAG_ID, this.getId());
    }

    static Task<MessageTag> getTagByNavUri(@NonNull final IModelRepository repository,
                                           @NonNull final IMotuSns motuSns,
                                           @NonNull Uri uri) {
        final String id = uri.getQueryParameter(NAV_QUERY_TAG_ID);
        if (id == null) {
            return Task.forError(null);
        }

        MessageTag tag = repository.getTagById(id);
        if (tag != null) {
            return Task.forResult(tag);
        }

        return motuSns.getTagMessages(id, IMotuSns.FIRST_PAGE, IMotuSns.DEFAULT_PAGE_SIZE)
                .onSuccess(new Continuation<MessagesResult, MessageTag>() {
                    @Override
                    public MessageTag then(Task<MessagesResult> task) throws Exception {
                        List<Message> messageList = task.getResult().getMessages().getData();
                        if (messageList.size() == 0) {
                            return null;
                        }

                        List<Tag> tagList = messageList.get(0).getContent().getTags();
                        for (Tag tag : tagList) {
                            if (tag.getId().equals(id)) {
                                return repository.getTagByData(tag);
                            }
                        }
                        return null;
                    }
                });

    }

    // endregion
}
