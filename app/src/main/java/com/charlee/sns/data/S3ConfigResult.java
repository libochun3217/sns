package com.charlee.sns.data;


import androidx.annotation.Nullable;

/**
 */
public class S3ConfigResult extends ResultBase {
    private final S3Config message;

    public S3ConfigResult(int errCode, @Nullable String errMsg, Boolean hasMore, S3Config config) {
        super(errCode, errMsg, hasMore);
        this.message = config;
    }

    public S3Config getConfig() {
        return message;
    }
}
