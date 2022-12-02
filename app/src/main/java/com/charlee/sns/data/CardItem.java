package com.charlee.sns.data;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.charlee.sns.model.Card;
import com.charlee.sns.storage.Storage;
import com.facebook.animated.gif.BuildConfig;

import java.security.InvalidParameterException;

/**
 * 一个卡片页的数据对象
 * 注意：成员变量命名要和JSON对应。GSON转换规则为FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES
 */
public class CardItem extends DataModelBase {
    public static final long ONE_DAY = 24 * 60 * 60 * 1000;
    public static final long DEBUG_ONE_DAY = 3 * 60 * 1000;

    private final int type;
    private Message message;

    public CardItem(int type) {
        this.type = type;
        if (type == Card.HEADER_CARD || type == Card.FOOTER_CARD) {
            setUpdateTime(System.currentTimeMillis() / 1000);
        }
    }

    public CardItem(int type, @Nullable Message message) {
        this.type = type;
        this.message = message;
    }

    @Override
    public boolean isValid() {
        return isTypeSupported() && !isTimeExpired();
    }

    // 通过类型检测客户端是否支持该卡片
    private boolean isTypeSupported() {
        return isMessageType()
                || isHeaderFooterType()
                || type == Card.RECOMMEND_LIST
                || type == Card.RECOMMEND_GRID;
    }

    private boolean isMessageType() {
        return type == Card.NORMAL_MESSAGE || type == Card.OFFICIAL_MESSAGE || type == Card.VIDEO_MESSAGE;
    }

    private boolean isHeaderFooterType() {
        return type == Card.HEADER_CARD || type == Card.FOOTER_CARD;
    }

    private boolean isTimeExpired() {
        if (type == Card.RECOMMEND_LIST) {
            long period = System.currentTimeMillis() - Storage.getInstance().getRecommendShowTime();
            if (period < (BuildConfig.DEBUG ? DEBUG_ONE_DAY : ONE_DAY)) {
                return true;
            }
        }
        return false;
    }

    @NonNull
    @Override
    public String getId() {
        if (isMessageType() && message != null) {
            return message.getId();
        } else {
            return String.valueOf(type * 10);
        }
    }

    @Override
    public void setUpdateTime(Long updateTime) {
        super.setUpdateTime(updateTime);

        if (message != null) {
            message.setUpdateTime(updateTime);
        }
    }

    public int getType() {
        return type;
    }

    @NonNull
    public Message getMessage() {
        return message;
    }

    public void setOfficialIndicator() {
        if (type == Card.OFFICIAL_MESSAGE) {
            if (message.getUser() != null) {
                message.getUser().setIsOfficial(true);
            }
        }
    }

    /**
     * 更新消息的数据
     *
     * @param other 用来更新的数据
     */
    public boolean update(@NonNull CardItem other) {
        // 调用此方法前需要保证other的值有效
        // 消息一旦发布则不可能更改ID
        if (!other.isValid() || !other.getId().equals(this.getId())) {
            if (BuildConfig.DEBUG) {
                // 如果在此处抛出异常请检查代码逻辑！！！！！！！！！
                throw new InvalidParameterException("Update Card with invalid data or a different ID!");
            } else {
                return false;
            }
        }

        // 更新前确保已设置updateTime
        if (BuildConfig.DEBUG && other.updateTime == null) {
            // 如果在此处抛出异常请检查代码逻辑！！！！！！！！！
            throw new InvalidParameterException("Update Message without updateTime!");
        }

        // 检查other是否更新时间更近。
        boolean isOtherMoreRecent = this.updateTime == null || other.updateTime >= this.updateTime;

        boolean updated = false;

        if (other.getMessage() != null) {
            if (this.message == null) {
                this.message = other.getMessage();
                updated = true;
            } else if (isOtherMoreRecent && !this.message.equals(other.getMessage())) {
                this.message = other.getMessage();
                updated = true;
            }
        }

        if (updated && isOtherMoreRecent) {
            this.updateTime = other.updateTime;
        }

        return updated;
    }
}
