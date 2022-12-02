package com.charlee.sns.model;

/**
 */
public class ObjectObservableBase extends WeakObservable<IObserver> {

    /**
     * If {@code hasChanged()} returns {@code true}, calls the {@code update()}
     * method for every observer in the list of observers using null as the
     * argument. Afterwards, calls {@code clearChanged()}.
     * <p>
     * Equivalent to calling {@code notifyObservers(null)}.
     */
    protected void notifyObservers() {
        notifyObservers(null);
    }

    /**
     * If {@code hasChanged()} returns {@code true}, calls the {@code update()}
     * method for every Observer in the list of observers using the specified
     * argument. Afterwards calls {@code clearChanged()}.
     *
     * @param data
     *            the argument passed to {@code update()}.
     */
    @SuppressWarnings("unchecked")
    protected void notifyObservers(final Object data) {
        foreachObserver(new INotificationCallback<IObserver>() {
            @Override
            public void onNotify(IObserver observer) throws Exception {
                observer.update(ObjectObservableBase.this, data);
            }
        });
    }
}
