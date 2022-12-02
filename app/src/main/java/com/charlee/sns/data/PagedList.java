package com.charlee.sns.data;

import androidx.annotation.Nullable;

import java.util.List;


/**
 * 可分页列表类
 * 注意：成员变量命名要和JSON对应。GSON转换规则为FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES
 */
public class PagedList<ItemTypeT extends DataModelBase> {
    private final Boolean hasMore;
    private final String lastId;
    private final List<ItemTypeT> data;

    public PagedList(boolean hasMore, String lastId, List<ItemTypeT> data) {
        this.hasMore = hasMore;
        this.lastId = lastId;
        this.data = data;
    }

    public boolean hasMore() {
        return hasMore != null && hasMore.booleanValue();
    }

    @Nullable
    public String getLastId() {
        return lastId;
    }

    @Nullable
    public List<ItemTypeT> getData() {
        return data;
    }

    /**
     * 数据是否有效。注意这里不检查列表元素的有效性。
     * @return      数据有效则返回true，否则返回false。
     */
    public boolean isValid() {
        return data != null;
    }

    public void setUpdateTime(long serverTimeStamp) {
        if (data != null) {
            for (ItemTypeT item : data) {
                item.setUpdateTime(serverTimeStamp);
            }
        }
    }
}
