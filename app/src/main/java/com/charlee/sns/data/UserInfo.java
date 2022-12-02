package com.charlee.sns.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.animated.gif.BuildConfig;

import java.security.InvalidParameterException;
import java.util.ArrayList;


/**
 * 注意：成员变量命名要和JSON对应。GSON转换规则为FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES
 */
public class UserInfo extends DataModelBase {
    private static final PagedList<Message> EMPTY_MESSAGES = new PagedList<>(false, "", new ArrayList<Message>());
    private static final PagedList<UserInfo> EMPTY_FOLLOWEES = new PagedList<>(false, "", new ArrayList<UserInfo>());

    private final String userId;
    private String nickName;
    private String portraitUrl;
    private Integer followerNum;
    private Integer followeeNum;
    private Integer messageNum;
    private PagedList<Message> messages;
    private PagedList<UserInfo> followees;
    private Boolean hasFollowed;
    private boolean isFollower;
    private boolean isOfficial;
    private Long followTime;

    private boolean forceUpdate;    // 表示是否需要强制刷新用户名字和头像，只有修改接口和详情接口才携带这个标记

    public UserInfo(@NonNull String id,
                    @NonNull String nickName,
                    @NonNull String portraitUrl,
                    int followerNum,
                    int followeeNum,
                    int messageNum,
                    @Nullable PagedList<Message> messages,
                    @Nullable PagedList<UserInfo> followees,
                    boolean isFollowed,
                    long followTime) {
        userId = id;
        this.nickName = nickName;
        this.portraitUrl = portraitUrl;
        this.followerNum = followerNum;
        this.followeeNum = followeeNum;
        this.messageNum = messageNum;
        this.messages = messages;
        this.followees = followees;
        this.followTime = followTime;
        hasFollowed = isFollowed;
        isOfficial = false;
    }

    @Override
    public boolean isValid() {
        return userId != null && !userId.isEmpty()
                && nickName != null && !nickName.isEmpty();
    }

    @NonNull
    @Override
    public String getId() {
        return userId;
    }

    /**
     * 设置对象的更新时间
     *
     * @param updateTime 对象更新的时间（服务器时间，从1970年1月1日开始的秒数）
     */
    @Override
    public void setUpdateTime(Long updateTime) {
        super.setUpdateTime(updateTime);
        if (messages != null) {
            messages.setUpdateTime(updateTime);
        }
        if (followees != null) {
            for (UserInfo user : followees.getData()) {
                user.setUpdateTime(updateTime);
            }
        }
    }

    @NonNull
    public String getNickName() {
        return nickName;
    }

    @NonNull
    public String getPortraitUrl() {
        return portraitUrl;
    }

    public void setPortraitUrl(String url) {
        portraitUrl = url;
    }

    @Nullable
    public Integer getFollowerNum() {
        return followerNum;
    }

    /**
     * 修改粉丝数。只有不为空（已经获取了用户详情）才会修改。
     * @param increase  true则加1，false则减少1
     */
    public void modifyFollowerNum(boolean increase) {
        if (followerNum != null) {
            followerNum += increase ? 1 : -1;
        }
    }

    @Nullable
    public Integer getFolloweeNum() {
        return followeeNum;
    }

    /**
     * 修改关注数。只有不为空（已经获取了用户详情）才会修改。
     * @param increase  true则加1，false则减少1
     */
    public void modifyFolloweeNum(boolean increase) {
        if (followeeNum != null) {
            followeeNum += increase ? 1 : -1;
        }
    }

    @Nullable
    public Integer getMessageNum() {
        return messageNum;
    }

    /**
     * 修改消息数。只有不为空（已经获取了用户详情）才会修改。
     * @param increase  true则加1，false则减少1
     */
    public void modifyMessageNum(boolean increase) {
        if (messageNum != null) {
            messageNum += increase ? 1 : -1;
        }
    }

    @NonNull
    public PagedList<Message> getMessages() {
        return messages == null ? EMPTY_MESSAGES : messages;
    }

    @NonNull
    public PagedList<UserInfo> getFollowees() {
        return followees == null ? EMPTY_FOLLOWEES : followees;
    }

    @Nullable
    public Boolean isFollowed() {
        return hasFollowed;
    }

    public void setIsFollowed(boolean isFollowed) {
        hasFollowed = isFollowed;
    }

    public boolean isFollower() {
        return isFollower;
    }

    public boolean isOfficial() {
        return isOfficial;
    }

    public void setIsOfficial(boolean isOfficial) {
        this.isOfficial = isOfficial;
    }

    // 关注或被关注的时间
    @Nullable
    public Long getFollowTime() {
        return followTime;
    }

    public void setForceUpdate() {
        forceUpdate = true;
    }

    /**
     * 更新用户的数据
     * @param other                 用来更新的数据
     */
    public boolean update(@NonNull UserInfo other) {
        if (!other.isValid() || !other.userId.equals(this.userId)) {
            if (BuildConfig.DEBUG) {
                // 如果在此处抛出异常请检查代码逻辑！！！！！！！！！
                throw new InvalidParameterException("Update User with invalid data or a different ID!");
            } else {
                return false;
            }
        }

        // 更新前确保已设置updateTime
        if (BuildConfig.DEBUG && other.updateTime == null) {
            // 如果在此处抛出异常请检查代码逻辑！！！！！！！！！
            throw new InvalidParameterException("Update User without updateTime!");
        }

        // 检查other是否更新时间更近。
        boolean isOtherMoreRecent = this.updateTime == null || other.updateTime >= this.updateTime;

        boolean updated = false;

        if (other.forceUpdate || other.forceUpdate == this.forceUpdate) {
            if (other.nickName != null) {
                if (this.nickName == null) {
                    this.nickName = other.nickName;
                    updated = true;
                } else if (isOtherMoreRecent && !this.nickName.equals(other.nickName)) {
                    this.nickName = other.nickName;
                    updated = true;
                }
            }

            if (other.portraitUrl != null) {
                if (this.portraitUrl == null) {
                    this.portraitUrl = other.portraitUrl;
                    updated = true;
                } else if (isOtherMoreRecent && !this.portraitUrl.equals(other.portraitUrl)) {
                    if (BasicUrlChecker.isValidHttpUrl(this.portraitUrl)
                            || !BasicUrlChecker.isValidHttpUrl(other.portraitUrl)) {
                        this.portraitUrl = other.portraitUrl;
                        updated = true;
                    }
                }
            }
            this.forceUpdate = other.forceUpdate;
        }

        if (other.followerNum != null) {
            if (this.followerNum == null) {
                this.followerNum = other.followerNum;
                updated = true;
            } else if (isOtherMoreRecent && !this.followerNum.equals(other.followerNum)) {
                this.followerNum = other.followerNum;
                updated = true;
            }
        }

        if (other.followeeNum != null) {
            if (this.followeeNum == null) {
                this.followeeNum = other.followeeNum;
                updated = true;
            } else if (isOtherMoreRecent && !this.followeeNum.equals(other.followeeNum)) {
                this.followeeNum = other.followeeNum;
                updated = true;
            }
        }

        if (other.messageNum != null) {
            if (this.messageNum == null) {
                this.messageNum = other.messageNum;
                updated = true;
            } else if (isOtherMoreRecent && !this.messageNum.equals(other.messageNum)) {
                this.messageNum = other.messageNum;
                updated = true;
            }
        }

        if (other.hasFollowed != null) {
            if (this.hasFollowed == null) {
                this.hasFollowed = other.hasFollowed;
                updated = true;
            } else if (isOtherMoreRecent && !this.hasFollowed.equals(other.hasFollowed)) {
                this.hasFollowed = other.hasFollowed;
                updated = true;
            }
        }

        if (other.isFollower) {
            this.isFollower = other.isFollower;
            updated = true;
        }

        if (other.followTime != null) {
            if (this.followTime == null) {
                this.followTime = other.followTime;
                updated = true;
            } else if (isOtherMoreRecent && !this.followTime.equals(other.followTime)) {
                this.followTime = other.followTime;
                updated = true;
            }
        }

        if (other.messages != null) {
            if (this.messages == null) {
                this.messages = other.messages;
                updated = true;
            } else if (isOtherMoreRecent && !this.messages.equals(other.messages)) {
                this.messages = other.messages;
                updated = true;
            }
        }

        if (other.isOfficial()) {
            this.isOfficial = true;
        }

        if (updated && isOtherMoreRecent) {
            this.updateTime = other.updateTime;
        }

        return updated;
    }
}
