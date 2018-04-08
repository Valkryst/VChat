package com.valkryst.VChat;

import com.valkryst.VChat.message.DummyMessage;
import com.valkryst.VChat.message.Message;
import lombok.Getter;
import lombok.NonNull;
import org.apache.logging.log4j.LogManager;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.util.zip.Deflater;
import java.util.zip.DeflaterInputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;

public class ChatServer extends Thread {
    /** The port to listen to. */
    @Getter private int port;

    /** Whether or not the server should continue running. */
    @Getter private boolean continueRunning = true;

    /** The queue of received messages to process. */
    @Getter private final MessageQueue messageQueue;

    /**
     * Constructs a new ChatServer.
     *
     * @param port
     *          The port to listen to.
     *
     * @param messageQueue
     *          The queue of received messages to process.
     *
     * @throws NullPointerException
     *          If the messageQueue is null.
     *
     * @throws IllegalArgumentException
     *          If the port isn't within the range of 0-65535.
     */
    public ChatServer(final int port, final @NonNull MessageQueue messageQueue) {
        if (port < 0 || port > 65535) {
            throw new IllegalArgumentException("The port must be from 0-65535.");
        }

        this.port = port;
        this.messageQueue = messageQueue;
    }

    @Override
    public void run() {
        try {
            // Create Socket
            final DatagramSocket datagramSocket = new DatagramSocket(port);
            datagramSocket.setSoTimeout(10000);

            // Receive Data from Client
            final byte[] buffer = new byte[1024];
            final DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            while (continueRunning) {
                try {
                    System.out.println("Waiting on Packet");
                    datagramSocket.receive(packet);
                } catch (final SocketTimeoutException ignored) {
                    // This timeout only exists to allow the server to re-check the continueRunning value.
                    continue;
                }

                // Create IO Streams
                final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(packet.getData());
                final GZIPInputStream gzipInputStream = new GZIPInputStream(byteArrayInputStream);
                final ObjectInputStream objectInputStream = new ObjectInputStream(gzipInputStream);

                // Create Shutdown Hook for IO Streams
                final Thread shutdownCode = new Thread(() -> {
                    try {
                        objectInputStream.close();
                    } catch (final IOException e) {
                        e.printStackTrace();
                    }
                });

                Runtime.getRuntime().addShutdownHook(shutdownCode);

                // Convert Packet Bytes into Message
                try {
                    final Object object = objectInputStream.readObject();

                    if (object instanceof DummyMessage) {
                        continue;
                    }

                    if (object instanceof Message) {
                        messageQueue.put((Message) object);
                        System.out.println("Received Message: " + ((Message) object).getMessage());
                    }
                } catch (final ClassNotFoundException e) {
                    // Because the server isn't able to use the packet of the class, sent by the client, it logs
                    // the exception and ignores the packet.
                    LogManager.getLogger().error(packet.toString() + "\n\n" + e.getMessage());
                } catch (final InterruptedException ignored) {}

                // Remove Shutdown Hook and Close Streams
                Runtime.getRuntime().removeShutdownHook(shutdownCode);
                shutdownCode.run();
            }

            datagramSocket.close();
        } catch (final IOException e) {
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
     *
     * @throws InterruptedException
     *          If interrupted while waiting to put a dummy message into the queue.
     */
    public void setContinueRunning(final boolean continueRunning) throws InterruptedException {
        this.continueRunning = continueRunning;
    }
}
