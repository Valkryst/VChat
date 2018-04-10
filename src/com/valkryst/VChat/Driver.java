package com.valkryst.VChat;

import com.valkryst.VChat.message.Message;

import java.io.IOException;
import java.net.DatagramPacket;

public class Driver {
    public static void main(String[] args) throws IOException, InterruptedException {
        final PacketReadWriter packetReadWriter = new PacketReadWriter("t.tiq.cc", 21050, 21050);
        packetReadWriter.start();

        int counter = 0;

        // Comment out when building for server.
        Message m = new Message(String.valueOf(counter));
        DatagramPacket p = Message.toPacket(m);
        System.out.println("Sending First Message: " + m.getMessage());
        packetReadWriter.queuePacket(p);
        // End block.

        while (true) {
            if (counter >= 100) {
                break;
            }

            final DatagramPacket receivedPacket = packetReadWriter.dequeuePacket();
            Message message = Message.fromPacket(receivedPacket);
            System.out.println("Received Message: " + message.getMessage());

            counter = Integer.valueOf(message.getMessage());
            counter++;

            message = new Message(String.valueOf(counter));
            final DatagramPacket newPacket = Message.toPacket(message);
            newPacket.setAddress(receivedPacket.getAddress());
            newPacket.setPort(receivedPacket.getPort());
            System.out.println("Sending Message: " + message.getMessage());
            packetReadWriter.queuePacket(newPacket);

            Thread.sleep(500);
        }

        System.out.println("Shutting Down");
        packetReadWriter.shutdown();
        System.out.println("Shut Down.");
    }
}
