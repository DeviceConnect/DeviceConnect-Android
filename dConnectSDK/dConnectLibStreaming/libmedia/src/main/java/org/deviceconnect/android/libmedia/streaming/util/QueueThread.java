package org.deviceconnect.android.libmedia.streaming.util;

import java.util.LinkedList;
import java.util.Queue;

public abstract class QueueThread<T> extends Thread {
    private final Queue<T> mQueue = new LinkedList<>();

    public synchronized int getCount() {
        return mQueue.size();
    }

    public synchronized void add(T data) {
        mQueue.offer(data);
        if (mQueue.size() <= 1) {
            notifyAll();
        }
    }

    public synchronized T get() throws InterruptedException {
        while (mQueue.peek() == null) {
            wait();
        }
        return mQueue.remove();
    }
}
