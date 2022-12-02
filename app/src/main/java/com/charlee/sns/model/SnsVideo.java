package com.charlee.sns.model;

import androidx.annotation.NonNull;

import com.charlee.sns.BuildConfig;
import com.charlee.sns.data.Image;
import com.charlee.sns.data.Video;

import java.security.InvalidParameterException;


/**
 * 用于保存服务器上的视频信息。包括视频URL、封面图片URL、长宽。
 * 由于选择了支持自适应媒体流的标准（TLS/MPEG-DASH）作为视频源，所以视频信息只需要对应源文件的URL即可。
 */
public class SnsVideo {
    private final Video videoData;
    private final SnsImage coverImage;

    public SnsVideo(@NonNull Video videoData) {
        if (BuildConfig.DEBUG) {
            if (videoData == null || !videoData.isValid()) {
                throw new InvalidParameterException("Invalid data for creating SnsVideo!");
            }
        }

        this.videoData = videoData;
        this.coverImage = new SnsImage(new Image(videoData.getCoverUrl(), videoData.getWidth(), videoData.getHeight()));
    }

    /**
     * 原始视频文件URL
     * @return 原始视频文件URL
     */
    public String getUrlOriginal() {
        return videoData.getUrlOriginal();
    }

    /**
     * 经过服务器转码的视频文件URL（m3u或mdp）
     * @return 经过服务器转码的视频文件URL
     */
    public String getUrlConverted() {
        return videoData.getUrlConverted();
    }

    public SnsImage getCoverImage() {
        return coverImage;
    }

    public int getWidth() {
        return videoData.getWidth();
    }

    public int getHeight() {
        return videoData.getHeight();
    }
}
