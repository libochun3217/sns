package com.charlee.sns.model;

import android.content.Context;

import com.charlee.sns.R;

import java.util.ArrayList;

/**
 * 发布对象,表示一次发布行为，用以维护发布过程中的对象状态
 */
public class Publish {
    private String content;
    private String imageUri;
    private String videoPath;
    private int width;
    private int height;
    private ArrayList<String> campaignIds;
    private SnsModel.PublishedState state;

    public Publish(String content, String imageUri, String videoPath, int width, int height,
                   ArrayList<String> campaignIds, SnsModel.PublishedState state) {
        this.content = content;
        this.imageUri = imageUri;
        this.videoPath = videoPath;
        this.width = width;
        this.height = height;
        this.campaignIds = campaignIds;
        this.state = state;
    }

    public String getContent() {
        return content;
    }

    public String getImageUri() {
        return imageUri;
    }

    public String getVideoPath() {
        return videoPath;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public ArrayList<String> getCampaignIds() {
        return campaignIds;
    }

    public void setState(SnsModel.PublishedState state) {
        this.state = state;
    }

    public SnsModel.PublishedState getState() {
        return state;
    }

    public String getDescription(Context context) {
        if (state == SnsModel.PublishedState.PUBLISHING) {
            return context.getResources().getString(R.string.publishing);
        } else {
            return context.getResources().getString(R.string.publish_failed);
        }
    }
}
