package com.charlee.sns.helper;

import java.util.ArrayList;

/**
 */
public class ObservableArrayList<E> extends ArrayList<E> {

    private DataChangeObserver observer;

    public void registerObserver(DataChangeObserver observer) {
        this.observer = observer;
    }

    @Override
    public void add(int index, E object) {
        super.add(index, object);
        if (observer != null) {
            observer.onChanged();
        }
    }

    @Override
    public boolean add(E object) {
        boolean ret = super.add(object);
        if (observer != null) {
            observer.onChanged();
        }
        return ret;
    }

    @Override
    public boolean remove(Object object) {
        boolean ret = super.remove(object);
        if (observer != null) {
            observer.onChanged();
        }
        return ret;
    }

    public interface DataChangeObserver {
        void onChanged();
    }

    public void updateValue() {
        if (observer != null) {
            observer.onChanged();
        }
    }

}
