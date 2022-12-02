package com.charlee.sns.data;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 用户列表结果
 * 注意：成员变量命名要和JSON对应。GSON转换规则为FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES
 */
public class UsersResult extends ResultBase {
    private PagedList<UserInfo> users;

    public UsersResult(int errCode, @Nullable String errMsg, Boolean hasMore, @NonNull PagedList<UserInfo> userList) {
        super(errCode, errMsg, hasMore);
        users = userList;
    }

    @NonNull
    public PagedList<UserInfo> getUserList() {
        return users;
    }

    @Override
    public boolean isValid() {
        return users != null && users.isValid();
    }

    @Override
    public void setServerTimeStamp(long serverTimeStamp) {
        super.setServerTimeStamp(serverTimeStamp);
        if (users != null) {
            users.setUpdateTime(serverTimeStamp);
        }
    }
}
