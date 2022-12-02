package com.charlee.sns.model;

import java.security.InvalidParameterException;

import com.facebook.animated.gif.BuildConfig;

/**
 */
public abstract class ModelBase extends ObjectObservableBase {
    protected long itemId;
    private boolean isRemoved;
    private boolean isFiltered;

    /**
     * 返回对象的唯一ID，用于在RecyclerView中提供给Adapter的唯一标识
     */
    public final long getItemId() {
        if (BuildConfig.DEBUG && itemId == 0) {
            throw new InvalidParameterException("itemId not initialized!");
        }

        return itemId;
    }

    /**
     * 是否被标记为删除。用于阻止模型对象显示在UI上。两种情况：
     * 1. 通过IObservable的通知机制，在一个界面上被删除之后通知其他包含该对象的界面进行更新。
     * 2. 在服务器的删除状态没有同步的情况下，让已经删除的项目在刷新后仍然不显示。
     *
     * @return true表示已经标记为删除，不应该再出现在UI上。false则为正常状态。
     */
    public final boolean isRemoved() {
        return isRemoved;
    }

    /**
     * 设置对象的已删除状态。同时发送通知。
     */
    public final void setRemoved() {
        setRemoved(true);
    }

    /**
     * 设置对象的已删除状态。同时发送通知。
     */
    public final void setRemoved(boolean isRemoved) {
        this.isRemoved = isRemoved;
        setChanged();
        notifyObservers();
    }

    /**
     * 是否被标记为过滤。不同于removed状态，被标记的模型对象应该从UI上删除，但是不影响该对象被再次创建。
     */
    public final boolean isFiltered() {
        return isFiltered;
    }

    /**
     * 设置对象的过滤状态。同时发送通知。
     */
    public final void setFiltered() {
        setFiltered(true);
    }

    /**
     * 设置对象的过滤状态
     */
    public final void setFiltered(boolean isFiltered) {
        this.isFiltered = isFiltered;
        setChanged();
        notifyObservers();
    }

}
