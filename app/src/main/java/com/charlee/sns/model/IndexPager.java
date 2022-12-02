package com.charlee.sns.model;

/**
 * 基于索引的分页参数计算。
 */
public class IndexPager {
    private final int pageSize;
    private int currentStartIndex = 0;

    public IndexPager(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getCurrentStartIndex() {
        return currentStartIndex;
    }

    public void reset() {
        currentStartIndex = 0;
    }

    public void moveToNextPage() {
        currentStartIndex += pageSize;
    }

    public void moveToNextPage(int realPageSize) {
        currentStartIndex += realPageSize;
    }
}
