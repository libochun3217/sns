package com.charlee.sns.model;

import java.util.List;

/**
 */
public class CollectionObservableBase extends WeakObservable<ICollectionObserver> {
    @SuppressWarnings("unchecked")
    protected void notifyObservers(final ICollectionObserver.Action action,
                                   final Object item,
                                   final List<Object> range) {
        foreachObserver(new INotificationCallback<ICollectionObserver>() {
            @Override
            public void onNotify(ICollectionObserver observer) throws Exception {
                observer.update(CollectionObservableBase.this, action, item, range);
            }
        });
    }
}
