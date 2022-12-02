package com.charlee.sns.model;

import java.util.List;

/**
 * 集合的观察者接口
 */
public interface ICollectionObserver {
    // 当前交互需要的场景
    enum Action {
        Clear,          // 列表清空
        AppendRange,    // 添加列表到结尾
        AddItemToFront, // item添加到列表头
        RemoveItem,     // item删除
        UpdateItem,     // item更新
    }

    /**
     * 当{@code IObservable}对象发生变化时， 会调用{@code WeakObservable#notifyObservers}方法，从而调用到每个观察者的这个方法。
     *
     * @param observable
     *            {@link IObservable}对象.
     * @param action
     *            {@link Action}发生变化的操作。
     * @param item
     *            发生改变的对象. 列表中某个对象改变（RemoveItem, UpdateItem）时会传递此参数。
     * @param range
     *            发生改变的列表范围. 列表中某个范围的对象改变（AppendRange）时会传递此参数。
     */
    void update(IObservable<ICollectionObserver> observable, Action action, Object item, List<Object> range);
}
