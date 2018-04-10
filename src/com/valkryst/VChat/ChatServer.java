package com.valkryst.VChat;

import com.valkryst.VChat.message.DummyMessage;
import com.valkryst.VChat.message.Message;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;

public class ChatServer extends Thread {
    /** The port to listen to. */
    @Getter private int port;

    /** Whether or not the server should continue running. */
    @Getter private boolean continueRunning = true;

    /** The queue of received messages to process. */
    @Getter private final MessageQueue messageQueue = new MessageQueue();

    /**
     * Constructs a new ChatServer.
     *
     * @param port
     *          The port to listen to.
     *
     * @throws IllegalArgumentException
     *          If the port isn't within the range of 0-65535.
     */
    public ChatServer(final int port) {
        if (port < 0 || port > 65535) {
            throw new IllegalArgumentException("The port must be from 0-65535.");
        }

        this.port = port;
    }

    @Override
    public void run() {
        try {
            // Create Socket
            final DatagramSocket datagramSocket = new DatagramSocket(port);
            datagramSocket.setSoTimeout(10000);

            // Receive Data from Client
            final byte[] buffer = new byte[600];
            final DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            while (continueRunning) {
                try {
                    System.out.println("Waiting on Packet");
                    datagramSocket.receive(packet);
                } catch (final SocketTimeoutException ignored) {
                    // This timeout only exists to allow the server to re-check the continueRunning value.
                    continue;
                }

                // Retrieve Message from Datagram Packet
                Message message;

                try {
                    message = Message.fromBytes(packet.getData());
                } catch (final IOException | ClassNotFoundException e) {
                    LogManager.getLogger().error(e.getMessage());
                    continue;
                }

                // Handle Message
                if (message instanceof DummyMessage) {
                    continue;
                } else {
                    for (int attempts = 0 ; attempts < 4 ; attempts++) {
                        try {
                            messageQueue.put(message);
                            System.out.println("Received Message (Attempt #" + attempts + "): " + message.getMessage());
                            break;
                        } catch (final InterruptedException e) {
                            LogManager.getLogger().error(e.getMessage());
                        }
                    }
                }
            }

            datagramSocket.close();
        } catch (final IOException e) {
            LogManager.getLogger().error(e.getMessage());
            e.printStackTrace();
        }
    }



    /**
     * Sets whether or not the client should continue running.
     *
     * It will cease running as soon as possible, after this is set to false.
     *
     * @param continueRunning
     *          Whether or not the client should continue running.
     */
    public void setContinueRunning(final boolean continueRunning) {
        this.continueRunning = continueRunning;
    }
}
