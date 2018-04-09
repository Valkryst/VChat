package com.valkryst.VChat.message;

import lombok.Getter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
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
}
