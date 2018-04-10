package com.valkryst.VChat;

import com.valkryst.VChat.message.DummyMessage;
import com.valkryst.VChat.message.Message;
import com.valkryst.VChat.queue.MessageQueue;
import lombok.Getter;
import lombok.NonNull;
import org.apache.logging.log4j.LogManager;

import java.io.IOException;
import java.net.*;

public class ChatClient extends Thread {
    /** The host address to connect to. */
    @Getter private final InetAddress hostAddress;
    /** The port to connect to. */
    @Getter private final int hostPort;

    /** Whether or not the client should continue running. */
    @Getter private boolean continueRunning = true;

    /** The queue of messages to be sent. */
    private final MessageQueue sendQueue = new MessageQueue();

    /** The queue of messages received. */
    private final MessageQueue receiveQueue = new MessageQueue();

    /**
     * Constructs a new ChatClient.
     *
     * @param host
     *          The host address to connect to.
     *
     * @param hostPort
     *          The port to connect to.
     *
     * @throws NullPointerException
     *          If the host or sendQueue are null.
     *
     * @throws IllegalArgumentException
     *          If the host is empty.
     *          If the port isn't within the range of 0-65535.
     *
     * @throws UnknownHostException
     *          If the host is unknown.
     */
    public ChatClient(final @NonNull String host, final int hostPort) throws UnknownHostException {
        if (host.isEmpty()) {
            throw new IllegalArgumentException("You must specify a host.");
        }

        if (hostPort < 0 || hostPort > 65535) {
            throw new IllegalArgumentException("The port must be from 0-65535.");
        }

        this.hostAddress = InetAddress.getByName(host);
        this.hostPort = hostPort;
    }

    @Override
    public void run() {
        try {
            final DatagramSocket datagramSocket = new DatagramSocket(hostPort);
            datagramSocket.setSoTimeout(10000);

            final Thread sender = new Thread(() -> {
                // Continually send messages
                Message message = null;

                while (continueRunning) {
                    if (message == null) {
                        try {
                            message = sendQueue.take();
                        } catch (final InterruptedException e) {
                            LogManager.getLogger().error(e.getMessage());
                            continue;
                        }

                        if (message instanceof DummyMessage) {
                            message = null;
                            break;
                        }
                    }

                    try {
                        final byte[] data = Message.toBytes(message);
                        final DatagramPacket packet = new DatagramPacket(data, data.length, hostAddress, hostPort);
                        datagramSocket.send(packet);

                        message = null;
                    } catch (final SocketTimeoutException ignored) {
                        // This timeout only exists to allow the server to re-check the continueRunning value.
                    } catch (final IOException e) {
                        LogManager.getLogger().error(e);
                        message = null;
                    }
                }
            });

            final Thread receiver = new Thread(() -> {
                // Continually receive messages
                final byte[] buffer = new byte[600];
                final DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                while (continueRunning) {
                    try {
                        datagramSocket.receive(packet);
                    } catch (final SocketTimeoutException ignored) {
                        // This timeout only exists to allow the server to re-check the continueRunning value.
                        continue;
                    } catch (final IOException e) {
                        LogManager.getLogger().error(e.getMessage());
                        continue;
                    }

                    // Retrieve Message & Add to Queue
                    try {
                        final Message message = Message.fromBytes(packet.getData());

                        for (int attempts = 0 ; attempts < 4 ; attempts++) {
                            try {
                                receiveQueue.put(message);
                                break;
                            } catch (InterruptedException e) {
                                LogManager.getLogger().error(e.getMessage());
                            }
                        }
                    } catch (final IOException | ClassNotFoundException e) {
                        LogManager.getLogger().error(e.getMessage());
                    }
                }
            });

            sender.start();
            receiver.start();

            sender.join();
            receiver.join();

            datagramSocket.close();
        } catch (final IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Inserts a message at the tail of the send queue.
     *
     * If an InterruptedException occurs when inserting the message, a
     * retry will be attempted. If all retries fail, then the message is not
     * inserted.
     *
     * @param message
     *          The message.
     */
    public void sendMessage(final Message message) {
        if (message != null) {
            for (int attempts = 0 ; attempts < 4 ; attempts++) {
                try {
                    sendQueue.put(message);
                    break;
                } catch (final InterruptedException e) {
                    LogManager.getLogger().error(e.getMessage());
                }
            }
        }
    }

    /**
     * Retrieves the head of the receive queue, waiting if necessary until
     * an element becomes available.
     *
     * @return
     *          The head message.
     *
     * @throws InterruptedException
     *          If interrupted while waiting to take a message from the
     *          queue.
     */
    public Message receiveMessage() throws InterruptedException {
        return receiveQueue.take();
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

        // Receiver thread can spin forever, waiting on a packet.
        // To ensure it gets a chance to re-check it's continueRunning var
        // we send it a dummy message to process.
        sendMessage(new DummyMessage());
    }
}
