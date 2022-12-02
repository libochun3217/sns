package com.charlee.sns.data;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 用户登录的返回值
 */
public class LoginResult extends ResultBase {
    private final UserInfo user;

    public LoginResult(int errCode, @Nullable String errMsg, Boolean hasMore, @NonNull UserInfo user) {
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
