package com.valkryst.VChat;

import com.valkryst.VChat.message.Message;

import java.io.IOException;

public class Driver {
    public static void main(String[] args) throws IOException, InterruptedException {
        /*
        final MessageQueue serverMessageQueue = new MessageQueue();
        final ChatServer server = new ChatServer(21050, serverMessageQueue);
        server.start();
        */

        final MessageQueue clientMessageQueue = new MessageQueue();
        final ChatClient client = new ChatClient("t.tiq.cc", 21050, clientMessageQueue);
        client.start();

        for (int i = 0 ; i < 10 ; i++) {
            client.getMessageQueue().put(new Message(String.valueOf(i)));
        }

        Thread.sleep(3000);
        client.setContinueRunning(false);
    }
}
