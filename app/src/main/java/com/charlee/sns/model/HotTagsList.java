package com.charlee.sns.model;

import androidx.annotation.NonNull;

import com.charlee.sns.data.PagedList;
import com.charlee.sns.data.Tag;
import com.charlee.sns.data.TagsResult;

import bolts.Continuation;
import bolts.Task;

/**
 */
public class HotTagsList extends PageableList<MessageTag, Tag> {
    private IndexPager indexPager = new IndexPager(IMotuSns.DEFAULT_PAGE_SIZE);

    public HotTagsList(@NonNull final IMotuSns motuSns, @NonNull final IModelRepository repository) {
        super(motuSns, repository, IPageableList.TOTAL_SIZE_INFINITE, PageableList.PagingType.IndexBased);
    }

    private Continuation<TagsResult, PagedList<Tag>> continuation =
            new Continuation<TagsResult, PagedList<Tag>>() {
                @Override
                public PagedList<Tag> then(Task<TagsResult> task) throws Exception {
                    return task.getResult().getTags();
                }
            };

    @NonNull
    @Override
    protected Task<PagedList<Tag>> getFirstPage() {
        indexPager.reset();
        return motuSns.getHotTags(IMotuSns.FIRST_PAGE, IMotuSns.DEFAULT_PAGE_SIZE)
                .onSuccess(continuation);
    }

    @NonNull
    @Override
    protected Task<PagedList<Tag>> getNextPage(@NonNull PagedList<Tag> before) {
        if (before == null || !before.hasMore()) {
            return Task.forResult(null);
        }

        indexPager.moveToNextPage(before.getData().size());
        return motuSns.getHotTags(indexPager.getCurrentStartIndex(), IMotuSns.DEFAULT_PAGE_SIZE)
                .onSuccess(continuation);
    }

    @Override
    protected MessageTag createModel(@NonNull Tag data) {
        return repository.getTagByData(data);
    }

    @Override
    protected boolean hasSameId(@NonNull MessageTag model, @NonNull Tag data) {
        return model.getId().equals(data.getId());
    }
}
