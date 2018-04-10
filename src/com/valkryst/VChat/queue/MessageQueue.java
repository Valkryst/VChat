package com.valkryst.VChat.queue;

import com.valkryst.VChat.message.Message;

public class MessageQueue extends Queue<Message> {
    @Override
    public void put(final Message message) throws InterruptedException {
        if (message == null) {
            return;
        }

        if (message.getMessage().isEmpty()) {
            return;
        }

        super.queue.put(message);
    }
}
