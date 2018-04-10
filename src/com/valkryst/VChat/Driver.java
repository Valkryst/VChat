package com.valkryst.VChat;

import com.valkryst.VChat.message.Message;

import java.io.IOException;
import java.net.DatagramPacket;

public class Driver {
    public static void main(String[] args) throws IOException, InterruptedException {
        final PacketReadWriter packetReadWriter = new PacketReadWriter("t.tiq.cc", 21050, 21050);
        packetReadWriter.start();

        /*
        final ChatServer server = new ChatServer(21050);
        server.start();

        while (true) {
            final DatagramPacket packet = server.receivePacket();
            final Message message = Message.fromDatagramPacket(packet);
            System.out.println("Received Message: " + message.getMessage());
        }
        */

        for (int i = 0 ; i < 50 ; i++) {
            final Message message = new Message(String.valueOf(i));
            final DatagramPacket packet = Message.toPacket(message);
            packetReadWriter.queuePacket(packet);
        }

        Thread.sleep(10000);
        packetReadWriter.shutdown();
    }
}
