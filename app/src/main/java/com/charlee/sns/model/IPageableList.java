package com.charlee.sns.model;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import bolts.Task;

/**
 * 可分页列表接口
 * @param <E> 列表元素类型
 */
public interface IPageableList<E> extends IObservable<ICollectionObserver> {
    int TOTAL_SIZE_INFINITE = -1; // 列表总大小不确定时使用此值

    /**
     * 获取位于location的元素
     * @param location  元素的位置
     * @return          位于location的元素，若location无效则返回null
     */
    @Nullable
    E get(int location);

    /**
     * 添加一个元素
     * 注意：此方法会触发Observable的通知，要在通知中更新UI时请调用者保证是在UI线程中处理回调
     * @param e 要添加的元素对象
     */
    void add(@NonNull E e);

    /**
     * 删除指定的元素
     * 注意：此方法会触发Observable的通知，要在通知中更新UI时请调用者保证是在UI线程中处理回调
     * @param e 要删除的元素对象
     * @return  true表示对象存在列表中已经被删除，否则返回false
     */
    boolean remove(@Nullable E e);

    /**
     * 过滤掉指定的元素
     * 注意：此方法会触发Observable的通知，要在通知中更新UI时请调用者保证是在UI线程中处理回调
     * @param e 要过滤的元素对象
     * @return
     */
    boolean filter(@Nullable E e);

    /**
     * 删除所有元素
     */
    void clear();

    /**
     * 获取对象在列表中的索引值
     * @param e 元素对象
     * @return  元素对象在列表中的索引值。不存在列表中则返回-1。
     */
    int indexOf(@Nullable E e);

    /**
     * 列表是否为空
     * @return  true则列表为空
     */
    boolean isEmpty();

    /**
     * 返回列表当前元素的数目
     * @return  列表元素的数目
     */
    int size();

    /**
     * 返回列表在服务器端的所有元素的数目，当前请求到的列表元素可能只是部分数据
     * @return 列表所有元素的数目。大小不确定时返回TOTAL_SIZE_INFINITE。
     */
    int getTotalSize();

    /**
     * 获取首页的的lastId，以此判断该列表是否更新
     * @return
     */
    String getLastId();

    /**
     * 刷新列表，原有内容会被清除。
     * 注意：首次加载需要调用此方法得到第一页的内容。
     *      此方法会触发Observable的通知，要在通知中更新UI时请调用者保证是在UI线程中处理回调
     * @return Task. 结果为true表示刷新成功，否则为false
     * @throws Exception
     */
    Task<Boolean> refresh() throws Exception;

    /**
     * 是否还有下一页
     * @return true则还有下一页，否则已经到达尾页。
     */
    boolean hasNextPage();

    /**
     * 加载列表的下一页
     *      此方法会触发Observable的通知，要在通知中更新UI时请调用者保证是在UI线程中处理回调
     * @return Task. 结果为true表示加载成功，否则为false
     * @throws Exception
     */
    Task<Boolean> loadNextPage() throws Exception;
}
