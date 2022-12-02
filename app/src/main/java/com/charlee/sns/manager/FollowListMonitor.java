package com.charlee.sns.manager;


import com.charlee.sns.model.Card;
import com.charlee.sns.model.IPageableList;
import com.charlee.sns.storage.Storage;

/**
 * 检查是否有最新的关注消息
 */
public class FollowListMonitor extends PageableListMonitor<Card> {

    public FollowListMonitor(IPageableList<Card> itemList) {
        super(itemList);
        initUpdateIntervalTime();
    }

    @Override
    protected void initUpdateIntervalTime() {
        super.initUpdateIntervalTime();
    }

    @Override
    protected String getUpdateId() {
        return Storage.getInstance().getFollowListUpdateId();
    }

    @Override
    protected void setUpdateId(String id) {
        Storage.getInstance().setFollowListUpdateId(id);
    }

    @Override
    protected boolean getNewStatus() {
        return Storage.getInstance().getNewFollowStatus();
    }

    @Override
    protected void setNewStatus(boolean status) {
        Storage.getInstance().setNewFollowStatus(status);
    }
}
