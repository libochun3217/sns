package com.charlee.sns.data;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 标签列表
 * 注意：成员变量命名要和JSON对应。GSON转换规则为FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES
 */
public class TagsResult extends ResultBase {
    private final PagedList<Tag> tags;

    public TagsResult(int errCode, @Nullable String errMsg, Boolean hasMore, @NonNull PagedList<Tag> tags) {
        super(errCode, errMsg, hasMore);
        this.tags = tags;
    }

    @NonNull
    public PagedList<Tag> getTags() {
        return tags;
    }

    @Override
    public boolean isValid() {
        return tags != null && tags.isValid();
    }

    @Override
    public void setServerTimeStamp(long serverTimeStamp) {
        super.setServerTimeStamp(serverTimeStamp);
        if (tags != null) {
            tags.setUpdateTime(serverTimeStamp);
        }
    }
}
