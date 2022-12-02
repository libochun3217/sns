package com.charlee.sns.exception;

/**
 * 服务器返回HTTP错误5xx时抛出此异常
 */
public class HttpServerErrorException extends Exception {
    private final int errCode;

    public HttpServerErrorException(int code) {
        errCode = code;
    }

    public int getErrCode() {
        return errCode;
    }
}
