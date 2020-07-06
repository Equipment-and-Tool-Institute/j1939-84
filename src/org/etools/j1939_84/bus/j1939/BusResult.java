/**
 * Copyright (c) 2019. Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.j1939;

import org.etools.j1939_84.bus.Either;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket;

/**
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class BusResult<T> {

    private final Either<T, AcknowledgmentPacket> packet;
    private final boolean retryUsed;

    /**
     *
     * @param retryUsed
     *            boolean representing retry has been used
     * @param packet
     *            the packet on the bus
     */
    public BusResult(boolean retryUsed, Either<T, AcknowledgmentPacket> packet) {
        this.retryUsed = retryUsed;
        this.packet = packet;
    }

    public BusResult(boolean retryUsed, T packet) {
        this.retryUsed = retryUsed;
        this.packet = new Either<>(packet, null);
    }

    /**
     * @return the packets
     */
    public Either<T, AcknowledgmentPacket> getPacket() {
        return packet;
    }

    /**
     * @return the retryUsed
     */
    public boolean isRetryUsed() {
        return retryUsed;
    }

}
