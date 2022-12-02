package com.charlee.sns.data;

/**
 * 对URL进行简单检查。
 */
public class BasicUrlChecker {
    public static final String SCHEME_HTTP = "http";
    public static final String SCHEME_HTTPS = "https";

    /**
     * 检查URL是否以“http”或者“https”开头
     * @param url   URL字符串
     * @return      URL以“http”或者“https”开头则返回true，否则返回false。
     */
    public static boolean isValidHttpUrl(String url) {
        String urlLower = url.toLowerCase();
        return urlLower.startsWith(SCHEME_HTTP) || urlLower.startsWith(SCHEME_HTTPS);
    }
}
