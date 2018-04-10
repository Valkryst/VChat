package com.valkryst.VChat.queue;

import java.net.DatagramPacket;

public class PacketQueue extends Queue<DatagramPacket> {
    @Override
    public void put(final DatagramPacket packet) throws InterruptedException {
        if (packet == null) {
            return;
        }

        if (packet.getData().length == 0) {
            return;
        }

        super.queue.put(packet);
    }
}
