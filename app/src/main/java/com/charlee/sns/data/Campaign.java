package com.charlee.sns.data;


import androidx.annotation.NonNull;

import com.charlee.sns.BuildConfig;

import java.security.InvalidParameterException;

/**
 * 运营活动返回结果
 * 注意：成员变量命名要和JSON对应。GSON转换规则为FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES
 */
public class Campaign extends DataModelBase {

    private final String campaignId;
    private final int campaignType;
    private final long startTime;
    private long endTime;
    private final String versionMin;
    private final String versionMax;
    private final int toType;
    private Integer viewerNum;
    private Integer participantNum;
    private Integer materialId;
    private final String title;
    private final String img;
    private final String url;
    private final String gift;
    private final String share;

    public Campaign(@NonNull String campaignId,
                    int campaignType,
                    long startTime,
                    long endTime,
                    @NonNull String versionMin,
                    @NonNull String versionMax,
                    int toType,
                    Integer viewerNum,
                    Integer participantNum,
                    Integer materialId,
                    @NonNull String title,
                    @NonNull String img,
                    @NonNull String url,
                    @NonNull String gift,
                    @NonNull String share) {
        this.campaignId = campaignId;
        this.campaignType = campaignType;
        this.startTime = startTime;
        this.endTime = endTime;
        this.versionMin = versionMin;
        this.versionMax = versionMax;
        this.toType = toType;
        this.viewerNum = viewerNum;
        this.participantNum = participantNum;
        this.materialId = materialId;
        this.title = title;
        this.img = img;
        this.url = url;
        this.gift = gift;
        this.share = share;
    }

    @Override
    public boolean isValid() {
        return startTime < endTime
                && versionMin != null
                && versionMax != null
                && title != null
                && img != null
                && url != null
                && gift != null
                && share != null;
    }

    @NonNull
    @Override
    public String getId() {
        return campaignId;
    }

    @Override
    public void setUpdateTime(Long updateTime) {
        super.setUpdateTime(updateTime);
    }

    public int getCampaignType() {
        return campaignType;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    @NonNull
    public String getVersionMin() {
        return versionMin;
    }

    @NonNull
    public String getVersionMax() {
        return versionMax;
    }

    public int getToType() {
        return toType;
    }

    public Integer getViewerNum() {
        return viewerNum;
    }

    public Integer getParticipantNum() {
        return participantNum;
    }

    public Integer getMaterialId() {
        return materialId;
    }

    @NonNull
    public String getTitle() {
        return title;
    }

    @NonNull
    public String getImg() {
        return img;
    }

    @NonNull
    public String getUrl() {
        return url;
    }

    @NonNull
    public String getGift() {
        return gift;
    }

    @NonNull
    public String getShare() {
        return share;
    }

    /**
     * 更新活动的数据
     *
     * @param other 用来更新的数据
     */
    public boolean update(@NonNull Campaign other) {
        // 调用此方法前需要保证other的值有效
        // 活动不可能更改ID
        if (!other.isValid() || !other.campaignId.equals(this.campaignId)) {
            if (BuildConfig.DEBUG) {
                // 如果在此处抛出异常请检查代码逻辑！！！！！！！！！
                throw new InvalidParameterException("Update campaign with invalid data or a different ID!");
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

        if (other.viewerNum != null) {
            if (this.viewerNum == null) {
                this.viewerNum = other.viewerNum;
                updated = true;
            } else if (isOtherMoreRecent && !this.viewerNum.equals(other.viewerNum)) {
                this.viewerNum = other.viewerNum;
                updated = true;
            }
        }

        if (other.participantNum != null) {
            if (this.participantNum == null) {
                this.participantNum = other.participantNum;
                updated = true;
            } else if (isOtherMoreRecent && !this.participantNum.equals(other.participantNum)) {
                this.participantNum = other.participantNum;
                updated = true;
            }
        }

        if (other.endTime != 0) {
            if (this.endTime == 0) {
                this.endTime = other.endTime;
            } else if (isOtherMoreRecent && this.endTime != other.endTime) {
                this.endTime = other.endTime;
                updated = true;
            }
        }

        if (updated && isOtherMoreRecent) {
            this.updateTime = other.updateTime;
        }

        return updated;
    }
}
