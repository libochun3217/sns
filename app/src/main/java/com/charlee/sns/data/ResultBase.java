package com.charlee.sns.data;


import androidx.annotation.Nullable;

/**
 * 服务器返回结果的基类。这里允许errno、errmsg和hasMore为空。
 * 注意：成员变量命名要和JSON对应。GSON转换规则为FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES
 */
public class ResultBase {
    private final Integer errno;
    private final String errmsg;
    private final Boolean hasMore;
    private long serverTimeStamp;

    public ResultBase(int errCode, @Nullable String errMsg, Boolean hasMore) {
        errno = errCode;
        errmsg = errMsg;
        this.hasMore = hasMore;
    }

    /**
     * 请求是否成功
     * @return  请求成功则返回true，否则返回false。
     */
    public boolean isSuccess() {
        return errno == null || errno == MotuSnsService.ERR_SUCCESS;
    }

    /**
     * 获取错误码
     * @return  错误码。为null表示没有错误。
     */
    @Nullable
    public Integer getErrCode() {
        return errno;
    }

    /**
     * 获取错误消息
     * @return  错误消息
     */
    @Nullable
    public String getErrMsg() {
        return errmsg;
    }

    /**
     * 是否还有更多结果。用于返回列表的接口。
     * @return  还有更多结果则返回true，否则为false或者null。
     */
    @Nullable
    public Boolean hasMore() {
        return hasMore;
    }

    /**
     * 结果是否有效。子类可根据需要override。
     * @return  结果有效则返回true，否则返回false。
     */
    public boolean isValid() {
        return true;
    }

    /**
     * 返回从HTTP头读取的服务器时间
     * @return  服务器时间（从1970年1月1日开始的秒数）
     */
    public long getServerTimeStamp() {
        return serverTimeStamp;
    }

    /**
     * 设置从HTTP头读取的服务器时间
     * @param serverTimeStamp   从1970年1月1日开始的秒数
     */
    public void setServerTimeStamp(long serverTimeStamp) {
        this.serverTimeStamp = serverTimeStamp;
    }
}
