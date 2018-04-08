package com.valkryst.VChat;

import com.valkryst.VChat.message.Message;

import java.io.IOException;

public class Driver {
    public static void main(String[] args) throws IOException, InterruptedException {
        /*
        final MessageQueue serverMessageQueue = new MessageQueue();
        final ChatServer server = new ChatServer(21050, serverMessageQueue);
        server.start();

        Thread.sleep(5000);
        server.setContinueRunning(false);
        */

        final MessageQueue clientMessageQueue = new MessageQueue();
        final ChatClient client = new ChatClient("t.tiq.cc", 21050, clientMessageQueue);
        client.start();

        for (int i = 0 ; i < 1 ; i++) {
            client.getMessageQueue().put(new Message(String.valueOf(i)));
        }

        Thread.sleep(5000);
        client.setContinueRunning(false);

    }
}
