package com.valkryst.VChat;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class PacketWriter extends Thread {
    /** The socket to read from. */
    private final DatagramSocket socket;

    /** Whether to continue running. */
    @Getter @Setter private boolean running = true;

    /** The FIFO queue of packets to send. */
    private final BlockingQueue<DatagramPacket> queue = new LinkedBlockingQueue<>(10_000);

    /**
     * Constructs a new PacketWriter.
     *
     * The socket will have it's SoTimeout value set to 10_000 if it has
     * not been set.
     *
     * @param socket
     *          The socket to read from.
     *
     * @throws SocketException
     *          If there is an error getting/setting the SoTimeout.
     */
    public PacketWriter(final @NonNull DatagramSocket socket) throws SocketException {
        this.socket = socket;

        if (socket.getSoTimeout() == 0) {
            socket.setSoTimeout(10_000);
        }
    }

    @Override
    public void run() {
        while (running) {
            try {
                final DatagramPacket packet = queue.take();
                socket.send(packet);
                queue.remove(packet);
            } catch (final SocketTimeoutException ignored) {
                // Happens, so the `running` var can be re-checked.
            } catch (final IOException | InterruptedException | NullPointerException e) {
                // The NPE can occur when a packet's address/port hasn't been set.
                LogManager.getLogger().error(e.getMessage());
            }
        }
    }

    /**
     * Adds a packet to the tail of the queue. Waiting, if necessary, for
     * room to be made, in the queue, for the new packet.
     *
     * Assumes that the packet's destination address/port has already
     * been set.
     *
     * @param packet
     *          The packet.
     *
     * @throws InterruptedException
     *          If interrupted while waiting to put a packet in the queue.
     *
     * @throws IllegalArgumentException
     *          If the packet's destination address/port have not been set.
     */
    public void queuePacket(final DatagramPacket packet) throws InterruptedException {
        if (packet == null) {
            return;
        }

        if (packet.getAddress() == null) {
            throw new IllegalArgumentException("You must set the packet's destination address.");
        }

        if (packet.getPort() == -1) {
            throw new IllegalArgumentException("You must set the packet's destination port.");
        }

        queue.put(packet);
    }
}
