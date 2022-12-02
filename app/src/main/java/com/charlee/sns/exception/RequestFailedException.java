package com.charlee.sns.exception;

/**
 * 在Web API请求正常返回但errno不为ERR_SUCCESS或ERR_NEED_LOGIN时抛出此异常
 */
public class RequestFailedException extends Exception {
    private final int errCode;

    private final String errMessage;

    public static final int ERR_ALREADY_FOLLOWED = -11;
    public static final int ERR_NOT_FOUND = -3;   // 资源不存在
    public static final int ERR_PIC_FORBIDDEN = -12;  // 图片被禁止
    public static final int ERR_USER_FORBIDDEN = -9;  // 用户被禁止

    public RequestFailedException(int code, String msg) {
        errCode = code;
        errMessage = msg;
    }

    public int getErrCode() {
        return errCode;
    }

    public String getErrMessage() {
        return errMessage;
    }
}
