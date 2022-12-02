package com.charlee.sns.data;


import androidx.annotation.NonNull;

/**
 * 用户详细信息返回结果
 * 注意：成员变量命名要和JSON对应。GSON转换规则为FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES
 */
public class UserDetailsResult extends ResultBase {
    private final UserInfo user;

    public UserDetailsResult(int errCode, String errMsg, Boolean hasMore, @NonNull UserInfo user) {
        super(errCode, errMsg, hasMore);
        this.user = user;
    }

    @NonNull
    public UserInfo getUserInfo() {
        return user;
    }

    @Override
    public boolean isValid() {
        return user != null && user.isValid();
    }

    @Override
    public void setServerTimeStamp(long serverTimeStamp) {
        super.setServerTimeStamp(serverTimeStamp);
        if (user != null) {
            user.setUpdateTime(serverTimeStamp);
        }
    }
}
