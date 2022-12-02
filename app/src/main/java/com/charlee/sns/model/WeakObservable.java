package com.charlee.sns.model;


import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;


/**
 * 将Observable中对Observer的引用改为弱引用，防止内存泄漏
 *
 * @see IObservable
 * @see IObserver
 */
public abstract class WeakObservable<ObserverT> implements IObservable<ObserverT> {

    List<WeakReference<ObserverT>> observers = new ArrayList<>();

    boolean changed = false;

    /**
     * Constructs a new {@code Observable} object.
     */
    public WeakObservable() {
    }

    /**
     * Adds the specified observer to the list of observers. If it is already
     * registered, it is not added a second time.
     *
     * @param observer
     *            the Observer to add.
     */
    public void addObserver(@NonNull ObserverT observer) {
        if (observer == null) {
            throw new NullPointerException("observer == null");
        }

        synchronized (this) {
            boolean found = false;
            for (WeakReference<ObserverT> item : observers) {
                if (item.get() == observer) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                observers.add(new WeakReference<>(observer));
            }
        }
    }

    /**
     * Clears the changed flag for this {@code Observable}. After calling
     * {@code clearChanged()}, {@code hasChanged()} will return {@code false}.
     */
    protected void clearChanged() {
        changed = false;
    }

    /**
     * Returns the number of observers registered to this {@code Observable}.
     *
     * @return the number of observers.
     */
    public int countObservers() {
        return observers.size();
    }

    /**
     * Removes the specified observer from the list of observers. Passing null
     * won't do anything.
     *
     * @param observer
     *            the observer to remove.
     */
    public synchronized void deleteObserver(@NonNull ObserverT observer) {
        synchronized (this) {
            WeakReference<ObserverT> found = null;
            for (WeakReference<ObserverT> item : observers) {
                if (item.get() == observer) {
                    found = item;
                }
            }

            if (found != null) {
                observers.remove(found);
            }
        }
    }

    /**
     * Removes all observers from the list of observers.
     */
    public synchronized void deleteObservers() {
        observers.clear();
    }

    /**
     * Returns the changed flag for this {@code Observable}.
     *
     * @return {@code true} when the changed flag for this {@code Observable} is
     *         set, {@code false} otherwise.
     */
    public boolean hasChanged() {
        return changed;
    }

    /**
     * Sets the changed flag for this {@code Observable}. After calling
     * {@code setChanged()}, {@code hasChanged()} will return {@code true}.
     */
    protected void setChanged() {
        changed = true;
    }

    protected interface INotificationCallback<ObserverT> {
        void onNotify(ObserverT observer) throws Exception;
    }

    protected void foreachObserver(INotificationCallback<ObserverT> action) {
        List<ObserverT> tempObserverList = new ArrayList<>();
        synchronized (this) {
            if (hasChanged()) {
                clearChanged();
                if (!observers.isEmpty()) {
                    for (WeakReference<ObserverT> item : observers) {
                        if (item.get() != null) {
                            tempObserverList.add(item.get());
                        }
                    }
                }
            }
        }

        if (!tempObserverList.isEmpty()) {
            for (ObserverT observer : tempObserverList) {
                try {
                    action.onNotify(observer);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
