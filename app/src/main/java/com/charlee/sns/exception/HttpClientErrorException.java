package com.charlee.sns.exception;

/**
 * 服务器返回HTTP错误4xx时抛出此异常
 */
public class HttpClientErrorException extends Exception {
    private final int errCode;

    public HttpClientErrorException(int code) {
        errCode = code;
    }

    public int getErrCode() {
        return errCode;
    }
}
