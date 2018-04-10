package com.valkryst.VChat;

import com.valkryst.VChat.message.Message;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class MessageQueue {
    /** The FIFO queue of messages. */
    private final BlockingQueue<Message> messageQueue = new LinkedBlockingQueue<>();

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();

        for (final Message message : messageQueue) {
            String toString = message.toString();
            toString = toString.replace("\n", "\n\t");

            sb.append(toString);
            sb.append(System.lineSeparator());
        }

        return sb.toString();
    }

    /**
     * Inserts a message at the tail of the queue.
     *
     * @param message
     *          The message.
     *
     * @throws InterruptedException
     *          If interrupted while waiting to put a message into the queue.
     */
    public void put(final Message message) throws InterruptedException {
        if (message == null) {
            return;
        }

        if (message.getMessage().isEmpty()) {
            return;
        }

        messageQueue.put(message);
    }

    /**
     * Retrieves the head of the queue, waiting if necessary until an
     * element becomes available.
     *
     * @return
     *          The head message.
     *
     * @throws InterruptedException
     *          If interrupted while waiting to take a message from the queue.
     */
    public Message take() throws InterruptedException {
        return messageQueue.take();
    }

    /**
     * Retrieves the number of elements in the queue.
     *
     * @return
     *          The number of elements in the queue.
     */
    public int size() {
        return messageQueue.size();
    }
}
