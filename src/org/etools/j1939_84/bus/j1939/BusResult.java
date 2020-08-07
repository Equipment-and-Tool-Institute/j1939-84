/**
 * Copyright (c) 2019. Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.j1939;

import java.util.Optional;

import org.etools.j1939_84.bus.Either;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket;

/**
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class BusResult<T> {

    private final Optional<Either<T, AcknowledgmentPacket>> packet;
    private final boolean retryUsed;

    public BusResult(boolean retryUsed, AcknowledgmentPacket packet) {
        this(retryUsed, packet == null
                ? Optional.empty()
                : Optional.of(new Either<>(null, packet)));
    }

    public BusResult(boolean retryUsed, Either<T, AcknowledgmentPacket> packet) {
        this(retryUsed, Optional.of(packet));
    }

    /**
     *
     * @param retryUsed
     *            boolean representing retry has been used
     * @param packet
     *            the packet on the bus
     */
    public BusResult(boolean retryUsed, Optional<Either<T, AcknowledgmentPacket>> packet) {
        this.retryUsed = retryUsed;
        this.packet = packet;
    }

    public BusResult(boolean retryUsed, T packet) {
        this(retryUsed, packet == null
                ? Optional.empty()
                : Optional.of(new Either<>(packet, null)));
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof BusResult) {
            BusResult<?> that = (BusResult<?>) o;
            return retryUsed == that.retryUsed && packet.equals(that.packet);
        }
        return false;
    }

    /**
     * @return the packets
     */
    public Optional<Either<T, AcknowledgmentPacket>> getPacket() {
        return packet;
    }

    /**
     * @return the retryUsed
     */
    public boolean isRetryUsed() {
        return retryUsed;
    }
}
