package org.deviceconnect.android.libmedia.streaming.util;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class WeakReferenceList<T> implements Iterable<T> {
    private List<WeakReference<T>> mList = new ArrayList<>();

    public List<T> get() {
        List<T> ret = new ArrayList<>(mList.size());
        List<WeakReference<T>> removeList = new LinkedList<>();
        for (WeakReference<T> ref : mList) {
            T item = ref.get();
            if (item == null) {
                removeList.add(ref);
            } else {
                ret.add(item);
            }
        }
        for (WeakReference<T> ref : removeList) {
            mList.remove(ref);
        }
        return ret;
    }

    public void add(T value) {
        mList.add(new WeakReference<>(value));
    }

    public void remove(T value) {
        for (Iterator<WeakReference<T>> iterator = mList.iterator(); iterator.hasNext(); ) {
            WeakReference<T> weakRef = iterator.next();
            if (weakRef.get() == value) {
                iterator.remove();
            }
        }
    }

    public int size() {
        return mList.size();
    }

    public void clear() {
        mList.clear();
    }

    @Override
    public Iterator<T> iterator() {
        return new ListIterator();
    }

    private class ListIterator implements Iterator<T> {
        private Iterator<WeakReference<T>> mIterator;
        private T mNextValue;

        ListIterator() {
            mIterator = mList.iterator();
            mNextValue = get();
        }

        @Override
        public boolean hasNext() {
            return mNextValue != null;
        }

        @Override
        public T next() {
            T value = mNextValue;
            mNextValue = get();
            return value;
        }

        private T get() {
            while (mIterator.hasNext()) {
                WeakReference<T> value = mIterator.next();
                if (value.get() != null) {
                    return value.get();
                }
                mIterator.remove();
            }
            return null;
        }
    }
}
