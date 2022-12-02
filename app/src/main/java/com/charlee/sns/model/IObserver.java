package com.charlee.sns.model;

/**
 * 观察者接口
 */
public interface IObserver {
    /**
     * 当{@code IObservable}对象发生变化时， 会调用{@code WeakObservable#notifyObservers}方法，从而调用到每个观察者的这个方法。
     *
     * @param observable
     *            {@link IObservable}对象.
     * @param data
     *            传给{@link ObjectObservableBase#notifyObservers(Object)}的对象. 列表中某个对象改变时会传递此参数。
     */
    void update(IObservable<IObserver> observable, Object data);
}
