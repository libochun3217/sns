package com.charlee.sns.data;


import com.facebook.animated.gif.BuildConfig;

/**
 */
public class Constants {

    // 服务器URL
    public static final String GP_SERVICE_ENDPOINT = "https://motu-hk.baidu.com"; // GP版生产环境：https://motu-hk.baidu.com


    // DOMAIN
    public static final String GP_SERVICE_ENDPOINT_DOMAIN = "motu-hk.baidu.com";

    public static final long IMAGE_UPLOAD_TIMEOUT = 120; // 图片上传超时时间（秒）

    /**
     */
    public static final String USER_AGENT = "android";

    public static final String HEADER_MTSNS_APP_VERSION = "MTSNS-App-Version";
    public static final String MTSNS_APP_VERSION_PREFIX = "PhotoWonder/";

    public static final String HEADER_MTSNS_ACCEPT_SERVER = "MTSNS-Accept-Server";
    public static final String MTSNS_ACCEPT_SERVER = "1.0";

    public static final String HEADER_MTSNS_CHANNEL = "MTSNS-Channel";
    public static final String HEADER_MTSNS_SDK_VERSION = "MTSNS-SDK-Version";

    public static final String HEADER_MTSNS_IMEI = "MTSNS-IMEI";
    public static final String HEADER_MTSNS_MAC = "MTSNS-MAC";
    public static final String HEADER_MTSNS_MTJ_CUID = "MTSNS-MTJ-CUID";

    public static final String HEADER_MTSNS_RES_LANG = "language";

    public static final String HEAD_MTSNS_FCM_TOKEN = "fcm_token";
}
