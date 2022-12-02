package com.charlee.sns.model;

/**
 * 用于通知模型数据变化的可观察接口
 */
public interface IObservable<ObserverT> {
    void addObserver(ObserverT observer);
    void deleteObserver(ObserverT observer);
    void deleteObservers();
    boolean hasChanged();
}
