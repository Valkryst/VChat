package com.valkryst.VChat.queue;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Queue<T> {
    /** The FIFO queue of objects. */
    protected final BlockingQueue<T> queue = new LinkedBlockingQueue<>();

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();

        for (final T t : queue) {
            sb.append(t.toString());
            sb.append(System.lineSeparator());
        }

        return sb.toString();
    }

    /**
     * Inserts an element at the tail of the queue.
     *
     * @param t
     *          The element.
     *
     * @throws InterruptedException
     *          If interrupted while waiting to put an element into the
     *          queue.
     */
    public void put(final T t) throws InterruptedException {
        if (t == null) {
            return;
        }

        queue.put(t);
    }

    /**
     * Retrieves the element at the head of the queue, waiting if necessary
     * until an element becomes available.
     *
     * @return
     *          The element.
     *
     * @throws InterruptedException
     *          If interrupted while waiting to take an element from the
     *          queue.
     */
    public T take() throws InterruptedException {
        return queue.take();
    }

    /**
     * Retrieves the number of elements in the queue.
     *
     * @return
     *          The number of elements in the queue.
     */
    public int size() {
        return queue.size();
    }
}
