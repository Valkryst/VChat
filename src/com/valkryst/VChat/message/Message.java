package com.valkryst.VChat.message;

import lombok.Getter;
import org.apache.logging.log4j.LogManager;

import java.io.*;
import java.net.DatagramPacket;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Message implements Serializable {
    /** The message. */
    @Getter private final String message;

    /**
     * Constructs a new Message.
     *
     * @param message
     *          The message.
     */
    public Message(final String message) {
        this.message = (message.isEmpty() ? "" : message);
    }

    @Override
    public String toString() {
        return message;
    }

    /**
     * Converts a Message object into a GZIPed array of bytes.
     *
     * @param message
     *          The message.
     *
     * @return
     *          The GZIPed bytes.
     *
     * @throws IOException
     *          If an IO exception occurs.
     */
    public static byte[] toBytes(final Message message) throws IOException {
        if (message == null) {
            return new byte[0];
        }

        // Create IO Streams
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream);
        final ObjectOutputStream objectOutputStream = new ObjectOutputStream(gzipOutputStream);

        // Create Shutdown Hook for IO Streams
        final Thread shutdownCode = new Thread(() -> {
            try {
                objectOutputStream.close();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        });

        Runtime.getRuntime().addShutdownHook(shutdownCode);

        // Write Object to Stream
        objectOutputStream.writeObject(message);
        objectOutputStream.flush();

        // Remove Shutdown Hook and Close Streams
        Runtime.getRuntime().removeShutdownHook(shutdownCode);
        shutdownCode.run();

        // Retrieve Compressed Object Bytes from Byte Output Stream
        return byteArrayOutputStream.toByteArray();
    }

    /**
     * Attempts to read a Message from a set of GZIPed bytes.
     *
     * @param data
     *          The byte data.
     *
     * @return
     *          The message.
     *
     * @throws IOException
     *          If there's an IO error.
     *
     * @throws ClassNotFoundException
     *          If the class of the serialized object, represented by the
     *          input data, cannot be found.
     */
    public static Message fromBytes(final byte[] data) throws IOException, ClassNotFoundException {
        // Create IO Streams
        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
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
        final Message message = (Message) objectInputStream.readObject();

        // Remove Shutdown Hook and Close Streams
        Runtime.getRuntime().removeShutdownHook(shutdownCode);
        shutdownCode.run();

        return message;
    }

    /**
     * Attempts to read a Message from a DatagramPacket.
     *
     * @param packet
     *          The packet.
     *
     * @return
     *          The message, or null if a ClassNotFoundException occurs.
     *
     * @throws IOException
     *          If there's an IO error.
     */
    public static Message fromDatagramPacket(final DatagramPacket packet) throws IOException {
        try {
            return fromBytes(packet.getData());
        } catch (final ClassNotFoundException e) {
            // Because the server isn't able to use the packet of the class, sent by the client, it logs
            // the exception and ignores the packet.
            LogManager.getLogger().error(packet.toString() + "\n\n" + e.getMessage());
            return null;
        }
    }
}
