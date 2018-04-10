package com.valkryst.VChat;

import lombok.NonNull;
import org.apache.logging.log4j.LogManager;

import java.net.*;

public class PacketReadWriter extends Thread {
    /** The host address of the server to communicate with. */
    private final InetAddress serverHost;
    /** The port of the server to communicate with. */
    private final int serverPort;
    /** The port to listen for packets, from the server, on. */
    private final int clientPort;

    /** The socket to read/write packets with. */
    private final DatagramSocket socket;

    /** The packet writer. */
    private final PacketWriter writer;
    /** The packet reader. */
    private final PacketReader reader;

    /**
     * Constructs a new PacketReadWriter.
     *
     * @param serverHost
     *          The host address of the server to communicate with.
     *
     * @param serverPort
     *          The port of the server to communicate with.
     *
     * @param clientPort
     *          The port to listen for packets, from the server, on.
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
     *
     * @throws SocketException
     *          If there is an error getting/setting the SoTimeout of the
     *          DatagramSocket.
     */
    public PacketReadWriter(final @NonNull String serverHost, final int serverPort, final int clientPort) throws UnknownHostException, SocketException {
        if (serverHost.isEmpty()) {
            throw new IllegalArgumentException("You must specify a server host.");
        }

        if (serverPort < 0 || serverPort > 65535) {
            throw new IllegalArgumentException("The server port must be an unused port from 0-65535.");
        }

        if (clientPort < 0 || clientPort > 65535) {
            throw new IllegalArgumentException("The client port must be an unused port from 0-65535.");
        }

        this.serverHost = InetAddress.getByName(serverHost);
        this.serverPort = serverPort;
        this.clientPort = clientPort;

        socket = new DatagramSocket(clientPort);
        socket.setSoTimeout(10_000);

        reader = new PacketReader(socket);
        writer = new PacketWriter(socket);
    }

    @Override
    public void run() {
        reader.start();
        writer.start();

        try {
            reader.join();
            writer.join();
        } catch (final InterruptedException e) {
            LogManager.getLogger().error(e);
        }

        socket.close();
    }

    /**
     * Adds a packet to the tail of the queue of packets to be sent.
     * Waiting, if necessary, for room to be made, in the queue, for the
     * new packet.
     *
     * Sets the destination address/port if it hasn't already been set.
     *
     * @param packet
     *          The packet.
     *
     * @throws InterruptedException
     *          If interrupted while waiting to put a packet in the queue.
     */
    public void queuePacket(final DatagramPacket packet) throws InterruptedException {
        if (packet.getAddress() == null) {
            packet.setAddress(serverHost);
        }

        if (packet.getPort() == -1) {
            packet.setPort(serverPort);
        }

        writer.queuePacket(packet);
    }

    /**
     * Retrieves the head packet from the queue of packets received.
     * Waiting, if necessary, for a packet to be added to the queue.
     *
     * @throws InterruptedException
     *          If interrupted while waiting to take a packet from the queue.
     */
    public DatagramPacket dequeuePacket() throws InterruptedException {
        return reader.dequeuePacket();
    }

    /** Shuts down the reader and writer. */
    public void shutdown() {
        reader.setRunning(false);
        writer.setRunning(false);
    }
}
