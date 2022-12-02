package com.charlee.sns.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.animated.gif.BuildConfig;

import java.security.InvalidParameterException;
import java.util.ArrayList;


/**
 * 标签
 * 注意：成员变量命名要和JSON对应。GSON转换规则为FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES
 */
public class Tag extends DataModelBase {
    private static final PagedList<Message> EMPTY_MESSAGES = new PagedList<>(false, "", new ArrayList<Message>());
    private final String tagId;
    private final String tagName;
    private String shareUrl;

    private String image;
    private PagedList<Message> messages;

    public Tag(@NonNull String id, @NonNull String name, @NonNull String shareUrl, @Nullable String image,
               @NonNull PagedList<Message> msgs) {
        this.tagId = id;
        this.tagName = name;
        this.shareUrl = shareUrl;
        this.image = image;
        this.messages = msgs;
    }

    @Override
    public boolean isValid() {
        return tagId != null && !tagId.isEmpty()
                && tagName != null && !tagName.isEmpty();
    }

    @NonNull
    @Override
    public String getId() {
        return tagId;
    }

    @Override
    public void setUpdateTime(Long updateTime) {
        super.setUpdateTime(updateTime);
        if (messages != null) {
            messages.setUpdateTime(updateTime);
        }
    }

    @NonNull
    public String getName() {
        return tagName;
    }

    @NonNull
    public String getShareUrl() {
        return shareUrl;
    }

    @Nullable
    public String getImage() {
        return image;
    }

    @NonNull
    public PagedList<Message> getMessages() {
        return messages == null ? EMPTY_MESSAGES : messages;
    }

    /**
     * 更新标签的数据
     * @param other                 用来更新的标签数据
     */
    public boolean update(@NonNull Tag other) {
        // 调用此方法前需要保证other的值有效
        // 标签一旦创建则不可能更改ID
        if (!other.isValid() || !other.tagId.equals(this.tagId)) {
            if (BuildConfig.DEBUG) {
                // 如果在此处抛出异常请检查代码逻辑！！！！！！！！！
                throw new InvalidParameterException("Update Tag with invalid data or a different ID!");
            } else {
                return false;
            }
        }

        // 标签一旦创建则不可能更改名字
        if (other.tagName != null && tagName != null
                && !other.tagName.equals(tagName)) {
            if (BuildConfig.DEBUG) {
                // 如果在此处抛出异常请检查代码逻辑！！！！！！！！！
                throw new InvalidParameterException("Update Tag with a different name!");
            } else {
                return false;
            }
        }

        // 更新前确保已设置updateTime
        if (BuildConfig.DEBUG && other.updateTime == null) {
            // 如果在此处抛出异常请检查代码逻辑！！！！！！！！！
            throw new InvalidParameterException("Update Tag without updateTime!");
        }

        // 检查other是否更新时间更近。
        boolean isOtherMoreRecent = this.updateTime == null || other.updateTime >= this.updateTime;

        boolean updated = false;

        if (other.shareUrl != null) {
            if (this.shareUrl == null) {
                this.shareUrl = other.shareUrl;
                updated = true;
            } else if (isOtherMoreRecent && !this.shareUrl.equals(other.shareUrl)) {
                this.shareUrl = other.shareUrl;
                updated = true;
            }
        }

        if (other.image != null) {
            if (this.image == null) {
                this.image = other.image;
                updated = true;
            } else if (isOtherMoreRecent && !this.image.equals(other.image)) {
                this.image = other.image;
                updated = true;
            }
        }

        if (other.messages != null) {
            if (this.messages == null) {
                this.messages = other.messages;
                updated = true;
            } else if (isOtherMoreRecent && this.messages != other.messages) {
                this.messages = other.messages;
                updated = true;
            }
        }

        if (updated && isOtherMoreRecent) {
            this.updateTime = other.updateTime;
        }

        return updated;
    }
}
