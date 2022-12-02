package com.charlee.sns.data;

/**
 * 服务器返回的视频对象
 */
public class Video {
    private final String cover;
    private final String urlOri;
    private final String urlTrans;
    private final int width;
    private final int height;

    public Video(String urlOriginal, String coverUrl, String urlTrans, int width, int height) {
        this.urlOri = urlOriginal;
        this.urlTrans = urlTrans;
        this.cover = coverUrl;
        this.width = width;
        this.height = height;
    }

    /**
     * 原始视频文件URL
     * @return 原始视频文件URL
     */
    public String getUrlOriginal() {
        return urlOri;
    }

    /**
     * 经过服务器转码的视频文件URL（m3u或mdp）
     * @return 经过服务器转码的视频文件URL
     */
    public String getUrlConverted() {
        return urlTrans;
    }

    public String getCoverUrl() {
        return cover;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isValid() {
        return ((urlOri != null && !urlOri.isEmpty()) || (urlTrans != null && !urlTrans.isEmpty()))
                && cover != null && !cover.isEmpty() && width != 0 && height != 0;
    }
}
