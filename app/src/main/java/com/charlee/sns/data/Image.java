package com.charlee.sns.data;

/**
 * 服务器返回的图片对象
 */
public class Image {
    private final String url;
    private final int width;
    private final int height;

    public Image(String url, int width, int height) {
        this.url = url;
        this.width = width;
        this.height = height;
    }

    public String getUrl() {
        return url;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isValid() {
        return url != null && !url.isEmpty() && width != 0 && height != 0;
    }
}
