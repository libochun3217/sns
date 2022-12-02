package com.charlee.sns.data;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 数据层模型对象需要实现的公共接口
 */
public abstract class DataModelBase {

    protected Long updateTime; // 对象更新的时间（服务器时间）

    /**
     * 对象的值是否有效。数据层模型是GSON通过反射创建的，并不一定会满足类定义中@NonNull的约束。
     * @return      合法则返回true，否则返回false
     */
    public abstract boolean isValid();

    /**
     * 返回ID。
     * @return      字符串ID。不合法的对象有可能返回null。
     */
    @Nullable
    public abstract String getId();

    /**
     * 获取对象的更新时间
     * @return      对象更新的时间（服务器时间，从1970年1月1日开始的秒数）
     */
    public Long getUpdateTime() {
        return updateTime;
    }

    /**
     * 设置对象的更新时间
     * @param updateTime  对象更新的时间（服务器时间，从1970年1月1日开始的秒数）
     */
    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    /**
     * 是否和另一个对象拥有相同的不为空的ID
     * @param other 另一个数据层模型对象
     * @return      ID都不为空且值相同则返回true，否则返回false
     */
    public boolean hasSameNonEmptyId(@NonNull DataModelBase other) {
        if (this.getId() != null && other.getId() != null) {
            return this.getId().equals(other.getId());
        }

        return false;
    }
}
