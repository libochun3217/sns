package com.charlee.sns.model;

/**
 * 登录事件对象
 */
public class LoginEvent extends ModelBase {

    // 仅供CardRepository调用以保证每个卡片只有唯一实例
    LoginEvent() {
        itemId = System.currentTimeMillis();
    }

}
