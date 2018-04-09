package com.valkryst.VChat;

import com.valkryst.VChat.message.DummyMessage;
import com.valkryst.VChat.message.Message;
import lombok.Getter;
import lombok.NonNull;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class ChatClient extends Thread {
    /** The host address to connect to. */
    @Getter private final InetAddress hostAddress;
    /** The port to connect to. */
    @Getter private final int hostPort;

    /** Whether or not the client should continue running. */
    @Getter private boolean continueRunning = true;

    /** The queue of messages to be sent. */
    @Getter private final MessageQueue messageQueue;

    /**
     * Constructs a new ChatClient.
     *
     * @param host
     *          The host address to connect to.
     *
     * @param hostPort
     *          The port to connect to.
     *
     * @param messageQueue
     *          The queue of messages to be sent.
     *
     * @throws NullPointerException
     *          If the host or messageQueue are null.
     *
     * @throws IllegalArgumentException
     *          If the host is empty.
     *          If the port isn't within the range of 0-65535.
     *
     * @throws UnknownHostException
     *          If the host is unknown.
     */
    public ChatClient(final @NonNull String host, final int hostPort, final @NonNull MessageQueue messageQueue) throws UnknownHostException {
        if (host.isEmpty()) {
            throw new IllegalArgumentException("You must specify a host.");
        }

        if (hostPort < 0 || hostPort > 65535) {
            throw new IllegalArgumentException("The port must be from 0-65535.");
        }

        this.hostAddress = InetAddress.getByName(host);
        this.hostPort = hostPort;
        this.messageQueue = messageQueue;
    }

    @Override
    public void run() {
        try {
            // Create Socket & Output Streams
            final DatagramSocket datagramSocket = new DatagramSocket();

            // Continually retrieve messages and send them
            while (continueRunning) {
                final Message message = messageQueue.take();

                if (message instanceof DummyMessage) {
                    break;
                }

                final byte[] data = Message.toBytes(message);
                final DatagramPacket packet = new DatagramPacket(data, data.length, hostAddress, hostPort);
                datagramSocket.send(packet);
            }

            datagramSocket.close();
        } catch (final IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void sendMessageLoop(final @NonNull DatagramSocket socket) {

    }

    private void receiveMessageLoop(final @NonNull DatagramSocket socket) {

    }

    /**
     * Sets whether or not the client should continue running.
     *
     * It will cease running as soon as possible, after this is set to false.
     *
     * @param continueRunning
     *          Whether or not the client should continue running.
     *
     * @throws InterruptedException
     *          If interrupted while waiting to put a dummy message into the queue.
     */
    public void setContinueRunning(final boolean continueRunning) throws InterruptedException {
        this.continueRunning = continueRunning;

        if (!continueRunning && messageQueue.size() == 0) {
            /*
             * To ensure the client shuts down faster, we give it a dummy message to process if it has nothing
             * to process.
             *
             * We do this because the client may be in a state where it's waiting for a message to enter the
             * queue before it continues running. Because the check against the continueRunning instance
             * variable is only run after a message is processed, the client will shut down much more slowly
             * (or never in some cases) if it has to wait on a message to be sent before it runs the check.
             */
            messageQueue.put(new DummyMessage());
        }
    }
}
