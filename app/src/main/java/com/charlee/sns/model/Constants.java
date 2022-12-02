package com.charlee.sns.model;

/**
 * 常量定义
 */
public class Constants {
    public static final float UI_MAX_IMG_ASPECT_RATIO = 2.0f; // 图片显示区域的最大高宽比，大于这个比例的图片会被等比例缩小

    public static final int THUMB_SIZE = 200; // 缩略图大小
    public static final int PORTRAIT_SIZE = 500; // 头像大小
    public static final int IMAGE_LIMITED_SIZE = 5 * 1000 * 1000 * 4;   // 图像大小限制，换算关系是1000

    public static final int THUMB_QUALITY = 100; // 缩略图质量，最大100

    public static final int INVISIBLE_IMAGE_LOAD_DELAY = 100; // 不可见图片延迟加载的时间（毫秒）

    // 图片缓存
    public static final int AVATAR_IMAGE_DISK_CACHE_MAX_SIZE = 10 * 1024 * 1024;    // 头像缓存大小

    // 图片加载参数
    public static final int IMAGE_LOAD_TIMEOUT = 60; // 图片加载超时时间（秒）
    public static final int PROGRESSIVE_JPEG_DECODE_STEP = 2; // 渐进式JPEG解码的扫描数间隔
    public static final int PROGRESSIVE_JPEG_QUALITY_THRESHOLD = 2; // 渐进式JPEG显示的质量阈值（扫描数，1~10）

    // 图片服务器参数定义
    // - 前提：图片存储于BOS服务器上。
    public static final String BOS_COMMAND_SEPARATOR = "@";                         // 图片处理命令的分隔符
    public static final String BOS_PARAM_SEPARATOR = ",";                           // 图片处理参数的分隔符
    public static final String BOS_PARAM_SCALE_TYPE_UNIFORM_NO_CLIP = "s_0";        // 缩放方式：无裁剪，等比例缩放。缺省值
    public static final String BOS_PARAM_SCALE_TYPE_FIT = "s_1";                    // 缩放方式：拉伸缩放.
    public static final String BOS_PARAM_SCALE_TYPE_UNIFORM_CENTER_CROP = "s_2";    // 缩放方式：等比例居中裁剪缩放。
    public static final String BOS_PARAM_SCALE_WIDTH_PX = "w_";                     // 指定缩放最大宽度。单位为px。
    public static final String BOS_PARAM_SCALE_HEIGHT_PX = "h_";                    // 指定缩放最大高度。单位为px。
    public static final String BOS_PARAM_SCALE_QUALITY = "q_";                      // 指定目标图片的绝对质量。
    public static final String BOS_PARAM_DISPLAY_BASELINE = "d_baseline";           // 指定图片显示方式：标准式。
    public static final String BOS_PARAM_DISPLAY_PROGRESSIVE = "d_progressive";     // 指定图片显示方式：渐进式。
}
