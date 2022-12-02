package com.charlee.sns.helper;

import android.content.Context;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 */
public class ReportHelper {

    public enum MessageScene {
        community,
        challenge,
        personal,
        tag,
        feed
    }

    // 到达社区
    public static void showCommunity(Context context) {
        new ShowCommunity("communityClick").report(context);
    }

    // 点击活动
    public static void clickCampaign(Context context, String campaignId) {
        new ClickPageItemWithValue("bannerClick", "community", campaignId).report(context);
    }

    // 查看活动规则页
    public static void clickCampaignRules(Context context, String campaignId) {
        new ClickPageItemWithValue("takeChallenge", "challenge", campaignId).report(context);
    }

    // 挑战赛规则页点击参加
    public static void clickCampaignRulesButton(Context context, String campaignId) {
        new ClickPageItemWithValue("joinClick", "challengeDetail", campaignId).report(context);
    }

    // 活动页点击参加
    public static void clickCampaignDetailsButton(Context context, String campaignId) {
        new ClickPageItemWithValue("joinClick", "challenge", campaignId).report(context);
    }

    // 点击图片，分场景
    public static void clickMessageImage(Context context, String messageId, MessageScene scene) {
        String page = null;
        switch (scene) {
            case community:
                page = "community";
                break;
            case challenge:
                page = "challenge";
                break;
            case personal:
                page = "personal";
                break;
            case tag:
                page = "tag";
                break;
            default:
                break;
        }
        new ClickPageItemWithSort("pictureClick", page, messageId).report(context);
    }

    // 展示消息
    public static void showMessage(Context context, String page, String content) {
        new ClickPageItemWithSort("pictureView", page, content).report(context);
    }

    // 点赞
    public static void clickLike(Context context, String messageId) {
        new ClickPageItemWithSort("like", "pictureDetail", messageId).report(context);
    }

    // 打开评论页面
    public static void showCommentPage(Context context, String messageId) {
        new ClickPageItemWithSort("readComment", "pictureDetail", messageId).report(context);
    }

    // 发送评论
    public static void publishComment(Context context, String messageId) {
        new ClickPageItemWithSort("comment", "commentDetail", messageId).report(context);
    }

    // 分享消息图片
    public static void shareMessage(Context context, String messageId) {
        new ClickPageItemWithSort("share", "pictureDetail", messageId).report(context);
    }

    // 拷贝消息连接
    public static void copyMessageUrl(Context context, String messageId) {
        new ClickPageItemWithSort("copyUrl", "pictureDetail", messageId).report(context);
    }

    // 发布消息
    public static void publishMessage(Context context, boolean isSuccess, String messageId) {
        if (isSuccess) {
            new ClickPageItemWithSort("submit", "release", messageId).report(context);
        } else {
            new ClickPageItemWithSort("submitFailure", "release", messageId).report(context);
        }
    }

    // 关注
    public static void followUser(Context context, String userId) {
        new ClickPageItemWithValue("follow", "", userId).report(context);
    }

    static class ReportModel {
        void report(Context context) {
            Gson gson = new GsonBuilder()
                    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                    .create();
        }
    }

    static class ShowCommunity extends ReportModel {
        String modId;

        ShowCommunity(String modId) {
            this.modId = modId;
        }
    }

    static class ClickPageItemWithValue extends ReportModel {
        String modId;
        String page;
        String value;

        ClickPageItemWithValue(String modId, String page, String value) {
            this.modId = modId;
            this.page = page;
            this.value = value;
        }
    }

    static class ClickPageItemWithSort extends ReportModel {
        String modId;
        String page;
        String sort;

        ClickPageItemWithSort(String modId, String page, String sort) {
            this.modId = modId;
            this.page = page;
            this.sort = sort;
        }
    }


}
