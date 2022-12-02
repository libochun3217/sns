package com.charlee.sns.model;

import java.security.InvalidParameterException;

import android.util.SparseArray;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.charlee.sns.BuildConfig;
import com.charlee.sns.data.BasicUrlChecker;
import com.charlee.sns.data.Image;

/**
 * 用于保存图片服务器上的图片信息。包括URL和长宽，并返回使用服务器进行缩放、裁剪的URL。
 */
public class SnsImage {
    public static final int SIZE_UNKNOWN = -1;

    // 缩放比例阈值
    private static final float SCALE_DOWN_THRESHOLD = 0.9f;    // 缩小比例大于此阈值的图片使用qualityFullWidth
    private static final float SCALE_UP_THRESHOLD = 1.2f;      // 放大比例大于此阈值的图片使用qualityForScaleUp

    private static class ImageQualityConfig {
        public final int qualityLow;            // 显示小图片时使用较低质量的图片
        public final int qualityHigh;           // 显示大图片时使用较高质量的图片
        public final int qualityFullWidth;      // 获取原宽度图片时使用的质量（Q=100时图片太大）
        public final int qualityForScaleUp;     // 图片会被放大显示时使用的质量（Q=100时图片太大）

        public ImageQualityConfig(int low, int high, int full, int scaleUp) {
            qualityLow = low;
            qualityHigh = high;
            qualityFullWidth = full;
            qualityForScaleUp = scaleUp;
        }
    }

    // 匹配不同DPI的图片质量配置
    private static final ImageQualityConfig QUALITY_CONFIG_HDPI = new ImageQualityConfig(40, 50, 60, 80);
    private static final ImageQualityConfig QUALITY_CONFIG_XHDPI = new ImageQualityConfig(30, 40, 50, 80);
    private static final ImageQualityConfig QUALITY_CONFIG_XXHDPI = new ImageQualityConfig(25, 30, 50, 80);

    private static final float DENSITY_HDPI = 1.5f;
    private static final float DENSITY_XHDPI = 2.0f;

    private static ImageQualityConfig imageQualityConfig = QUALITY_CONFIG_HDPI;

    private static final String BOS_COMMAND_PROGRESSIVE = Constants.BOS_COMMAND_SEPARATOR
            + Constants.BOS_PARAM_DISPLAY_PROGRESSIVE;

    private static final String BOS_COMMAND_FMT_ORIGINAL = BOS_COMMAND_PROGRESSIVE
            + Constants.BOS_PARAM_SEPARATOR
            + Constants.BOS_PARAM_SCALE_QUALITY + "%d";

    private static final String BOS_COMMAND_FMT_SCALE_TO_WIDTH = BOS_COMMAND_PROGRESSIVE
            + Constants.BOS_PARAM_SEPARATOR + Constants.BOS_PARAM_SCALE_QUALITY + "%d"
            + Constants.BOS_PARAM_SEPARATOR + Constants.BOS_PARAM_SCALE_WIDTH_PX + "%d";

    private static final String BOS_COMMAND_FMT_SCALE_CROP_IMAGE = BOS_COMMAND_PROGRESSIVE
            + Constants.BOS_PARAM_SEPARATOR + Constants.BOS_PARAM_SCALE_TYPE_UNIFORM_CENTER_CROP
            + Constants.BOS_PARAM_SEPARATOR + Constants.BOS_PARAM_SCALE_QUALITY + "%d"
            + Constants.BOS_PARAM_SEPARATOR + Constants.BOS_PARAM_SCALE_WIDTH_PX + "%d"
            + Constants.BOS_PARAM_SEPARATOR + Constants.BOS_PARAM_SCALE_HEIGHT_PX + "%d";

    // 经过MTJ终端分析得到分辨率分布，计算得到的分割点
    private static final int[] IMAGE_WIDTH_BREAKPOINTS = {300, 400, 540, 720, 800, 1080, 1200, 1440, 1600, 2048};
    private static final int[] SQUARE_IMAGE_SIZE_BREAKPOINTS = {180, 240, 340, 450, 480, 640, 720, 864, 960, 1280};

    private static class RequestParam {
        public int width;
        public int quality;

        public RequestParam(int width, int quality) {
            this.width = width;
            this.quality = quality;
        }
    }

    // 图片的最大尺寸。硬件加速（OpenGL ES）允许的最大宽高。超过尺寸的图片会被缩小显示。
    // 在init()中初始化。请勿在其他地方修改。
    private static int maxImageSize;

    // 显示允许的最大宽度，以保证图片的高、宽均不会超出maxImageSize
    private final int maxDisplayWidth;

    private final Image imageData;
    private final float aspectRatio;

    private static final int DEFAULT_WIDTH_NUMBER = 5;
    private SparseArray<String> widthRequests = new SparseArray<>(DEFAULT_WIDTH_NUMBER);
    private SparseArray<String> squareRequests = new SparseArray<>(DEFAULT_WIDTH_NUMBER);

    private String fullWidthRequest;        // 原宽度请求
    private String scaleUpQualityRequest;   // 需要放大显示的图片请求

    private boolean localImage;  // 标识是否为本地图片

    private boolean isAnimatable; // 是否动态图（GIF或WEBP）

    /**
     * 初始化，设置图片的最大尺寸
     *
     * @param maxSize
     */
    public static void init(int maxSize, float pixelDensity) {
        maxImageSize = maxSize;
        if (pixelDensity <= DENSITY_HDPI) {
            imageQualityConfig = QUALITY_CONFIG_HDPI;
        } else if (pixelDensity <= DENSITY_XHDPI) {
            imageQualityConfig = QUALITY_CONFIG_XHDPI;
        } else {
            imageQualityConfig = QUALITY_CONFIG_XXHDPI;
        }
    }

    public SnsImage(@NonNull String imageUrl) {
        this.imageData = new Image(imageUrl, SIZE_UNKNOWN, SIZE_UNKNOWN);
        this.aspectRatio = SIZE_UNKNOWN;
        this.maxDisplayWidth = SIZE_UNKNOWN;
        this.isAnimatable = checkIsAnimatable(imageUrl);

        if (BuildConfig.DEBUG) {
            if (imageUrl == null || imageUrl.isEmpty()) {
                throw new InvalidParameterException("Invalid URL for creating SnsImage!");
            }
        }
    }

    SnsImage(@NonNull Image imageData) {
        this.imageData = imageData;
        this.aspectRatio = ((float) imageData.getHeight()) / imageData.getWidth();
        this.maxDisplayWidth = calculateMaxDisplayWidth();

        if (!BasicUrlChecker.isValidHttpUrl(imageData.getUrl())) {
            localImage = true;
        }

        this.isAnimatable = checkIsAnimatable(imageData.getUrl());

        if (BuildConfig.DEBUG) {
            if (imageData == null || !imageData.isValid()) {
                throw new InvalidParameterException("Invalid data for creating SnsImage!");
            }
        }
    }

    @NonNull
    public String getUrl() {
        return imageData.getUrl();
    }

    /**
     * 取得缩小到所需宽度的URL。
     *
     * @param width 期望宽度
     *
     * @return 图片服务器的URL
     */
    @NonNull
    public String getUrl(int width, boolean isLargeImage) {
        if (localImage) {
            return getUrl();
        }

        if (width > 0) {
            RequestParam param = getRequestParamForWidth(width, isLargeImage);
            return getWidthRequestUrl(param);
        }

        return getUrl() + BOS_COMMAND_PROGRESSIVE;
    }

    /**
     * 取得小于指定宽度的低分辨率图片URL。
     * 注意：必须在getUrl()之前调用才不会因为已经获取过高分辨率URL而返回null。
     *
     * @param width 目标宽度
     *
     * @return 图片服务器的URL，没有已经获取过的低分辨率图片则返回null
     */
    @Nullable
    public String getLowResUrl(int width) {
        if (localImage) {
            return getUrl();
        }

        if (width > 0) {
            // 如果已经获取过高分辨率图片则直接返回null
            RequestParam param = getRequestParamForWidth(width, true);
            if (param.width == maxDisplayWidth && (fullWidthRequest != null || scaleUpQualityRequest != null)) {
                return null;
            }

            if (widthRequests.get(param.width) != null) {
                return null;
            }

            // 如果已经获取过低分辨率图片则返回对应URL
            if (widthRequests.size() > 0 && widthRequests.keyAt(0) <= width) {
                return widthRequests.valueAt(0);
            }
        }

        return null;
    }

    /**
     * 取得缩小到所需大小正方形图片的URL。
     *
     * @param size 期望大小
     *
     * @return 图片服务器的URL，size小于0则返回原始URL
     */
    @NonNull
    public String getSquareUrl(int size) {
        if (localImage) {
            return getUrl();
        }

        if (size > 0) {
            RequestParam param = getSquareImageRequestSize(size);
            return getSquareRequestUrl(param);
        }

        return getUrl() + BOS_COMMAND_PROGRESSIVE;
    }

    public final SparseArray<String> getWidthRequests() {
        return widthRequests;
    }

    @Nullable
    public String getWidthUrlForShare() {
        if (localImage) {
            return getUrl();
        }

        if (widthRequests.size() == 0) {
            return fullWidthRequest != null ? fullWidthRequest : scaleUpQualityRequest;  // 可能为null
        }

        // 现在IMAGE_WIDTH_BREAKPOINTS的最小值仍大于Constants.THUMB_SIZE，所以直接返回最小宽度的图片URL
        return widthRequests.valueAt(0);
    }

    public final SparseArray<String> getSquareRequests() {
        return squareRequests;
    }

    @Nullable
    public String getSquareUrlForShare() {
        if (squareRequests.size() == 0) {
            return null;
        }

        // 直接返回最小边长的图片URL
        return squareRequests.valueAt(0);
    }

    public void removeFailedUrl(@NonNull String imageUrl) {
        if (imageUrl.isEmpty()) {
            return;
        }

        int i = widthRequests.indexOfValue(imageUrl);
        if (i > 0) {
            widthRequests.removeAt(i);
            return;
        }

        if (imageUrl.equals(fullWidthRequest)) {
            fullWidthRequest = null;
            return;
        }

        if (imageUrl.equals(scaleUpQualityRequest)) {
            scaleUpQualityRequest = null;
        }
    }

    public int getWidth() {
        return imageData.getWidth();
    }

    public int getHeight() {
        return imageData.getHeight();
    }

    public float getAspectRatio() {
        return aspectRatio;
    }

    public boolean isAnimatable() {
        return isAnimatable;
    }

    private int calculateMaxDisplayWidth() {
        int width = imageData.getWidth();
        int height = imageData.getHeight();
        int maxSize = Math.max(width, height);
        if (maxSize < maxImageSize) {
            return width;
        }

        if (aspectRatio > 1.0) {
            return (int) ((float) width * maxImageSize / maxSize);
        }

        return maxImageSize;
    }

    private synchronized String getWidthRequestUrl(RequestParam param) {
        // 全宽度
        if (param.width == getWidth()) {
            if (param.quality == imageQualityConfig.qualityFullWidth) {
                if (fullWidthRequest == null) {
                    fullWidthRequest = getUrl()
                            + String.format(BOS_COMMAND_FMT_ORIGINAL, imageQualityConfig.qualityFullWidth);
                }

                return fullWidthRequest;
            } else if (param.quality == imageQualityConfig.qualityForScaleUp) {
                if (scaleUpQualityRequest == null) {
                    scaleUpQualityRequest = getUrl()
                            + String.format(BOS_COMMAND_FMT_ORIGINAL, imageQualityConfig.qualityForScaleUp);
                }

                return scaleUpQualityRequest;
            }
        }

        // 查找已有URL
        String url = widthRequests.get(param.width);
        if (url != null) {
            return url;
        }

        // 查找更大图片URL
        int sizeWidthRequests = widthRequests.size();
        if (sizeWidthRequests > 0 && widthRequests.keyAt(sizeWidthRequests - 1) >= param.width) {
            for (int i = 0; i < sizeWidthRequests; ++i) {
                if (widthRequests.keyAt(i) >= param.width) {
                    return widthRequests.valueAt(i);
                }
            }
        }

        // 查找全宽度URL
        if (fullWidthRequest != null) {
            return fullWidthRequest;
        }

        // 查找用来放大显示的URL
        if (scaleUpQualityRequest != null) {
            return scaleUpQualityRequest;
        }

        // 创建并保存新URL
        url = getUrl() + String.format(BOS_COMMAND_FMT_SCALE_TO_WIDTH, param.quality, param.width);
        widthRequests.put(param.width, url);
        return url;
    }

    private RequestParam getRequestParamForWidth(int width, boolean isLargeImage) {
        if (BuildConfig.DEBUG && imageData.getWidth() < 0) {
            throw new InvalidParameterException("Invalid image should be excluded in data layer.");
        }

        int imageWidth = imageData.getWidth();
        if (width >= maxDisplayWidth) {
            int quality = isLargeImage ? imageQualityConfig.qualityFullWidth : imageQualityConfig.qualityHigh;
            float scale = (float) width / imageWidth;
            if (scale > SCALE_UP_THRESHOLD) {
                quality = imageQualityConfig.qualityForScaleUp;
            }

            return new RequestParam(maxDisplayWidth, quality);
        }

        int requestWidth = width;
        for (int breakpoint : IMAGE_WIDTH_BREAKPOINTS) {
            if (breakpoint >= requestWidth) {
                requestWidth = breakpoint;
                break;
            }
        }

        if (requestWidth >= maxDisplayWidth) {
            return new RequestParam(maxDisplayWidth,
                    isLargeImage ? imageQualityConfig.qualityHigh : imageQualityConfig.qualityHigh);
        }

        int quality = imageQualityConfig.qualityLow;
        if (isLargeImage) {
            int widthThreshold = (int) (imageWidth * SCALE_DOWN_THRESHOLD);
            if (requestWidth > widthThreshold) {
                quality = imageQualityConfig.qualityFullWidth;
            } else {
                quality = imageQualityConfig.qualityHigh;
            }
        }

        return new RequestParam(requestWidth, quality);
    }

    private synchronized String getSquareRequestUrl(RequestParam param) {
        String url = squareRequests.get(param.width);
        if (url != null) {
            return url;
        }

        int sizeSquareRequests = squareRequests.size();
        if (sizeSquareRequests > 0 && squareRequests.keyAt(sizeSquareRequests - 1) >= param.width) {
            for (int i = 0; i < sizeSquareRequests; ++i) {
                if (squareRequests.keyAt(i) >= param.width) {
                    return squareRequests.valueAt(i);
                }
            }
        }

        int sizeWidthRequests = widthRequests.size();
        if (sizeWidthRequests > 0 && widthRequests.keyAt(sizeWidthRequests - 1) >= param.width) {
            for (int i = 0; i < sizeWidthRequests; ++i) {
                if (widthRequests.keyAt(i) >= param.width) {
                    return widthRequests.valueAt(i);
                }
            }
        }

        // 查找全宽度URL
        if (fullWidthRequest != null) {
            return fullWidthRequest;
        }

        // 查找用来放大显示的URL
        if (scaleUpQualityRequest != null) {
            return scaleUpQualityRequest;
        }

        // 创建并保存新URL
        url = getUrl();
        if (param.width == getWidth() && param.width == getHeight()) {
            url += String.format(BOS_COMMAND_FMT_ORIGINAL, imageQualityConfig.qualityFullWidth);
        } else {
            url += String.format(BOS_COMMAND_FMT_SCALE_CROP_IMAGE, param.quality, param.width, param.width);
        }

        squareRequests.put(param.width, url);
        return url;
    }

    private RequestParam getSquareImageRequestSize(int size) {
        int imageWidth = imageData.getWidth();
        int imageHeight = imageData.getHeight();
        int minSize = Math.min(imageWidth, imageHeight);
        if (minSize < 0) {
            minSize = size;
        }

        if (size >= imageWidth || size >= imageHeight) {
            return new RequestParam(minSize, imageQualityConfig.qualityHigh);
        }

        int requestSize = size;
        for (int breakpoint : SQUARE_IMAGE_SIZE_BREAKPOINTS) {
            if (breakpoint >= requestSize) {
                requestSize = breakpoint;
                break;
            }
        }

        if (requestSize > minSize) {
            requestSize = minSize;
        }

        RequestParam param = new RequestParam(requestSize, imageQualityConfig.qualityLow);

        return param;
    }

    private boolean checkIsAnimatable(String url) {
        // TODO: 从服务器获取是否为动图的属性
        String lower = url.toLowerCase();
        return lower.endsWith(".gif") | lower.endsWith(".webp");
    }
}
