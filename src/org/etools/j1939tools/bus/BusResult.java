/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939tools.bus;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.etools.j1939tools.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939tools.j1939.packets.ParsedPacket;

/**
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class BusResult<T extends ParsedPacket> {

    private final Optional<Either<T, AcknowledgmentPacket>> packet;
    private final boolean retryUsed;

    public BusResult(boolean retryUsed) {
        this(retryUsed, Optional.empty());
    }

    // FIXME ugly null parameter
    public BusResult(boolean retryUsed, AcknowledgmentPacket packet) {
        this(retryUsed,
             packet == null
                     ? Optional.empty()
                     : Optional.of(new Either<>(null, packet)));
    }

    public BusResult(boolean retryUsed, Either<T, AcknowledgmentPacket> packet) {
        this(retryUsed, Optional.of(packet));
    }

    /**
     *
     * @param retryUsed
     *                      boolean representing retry has been used
     * @param packet
     *                      the packet on the bus
     */
    public BusResult(boolean retryUsed, Optional<Either<T, AcknowledgmentPacket>> packet) {
        this.retryUsed = retryUsed;
        this.packet = packet;
    }

    // FIXME ugly null parameter
    public BusResult(boolean retryUsed, T packet) {
        this(retryUsed,
             packet == null
                     ? Optional.empty()
                     : Optional.of(new Either<>(packet, null)));
    }

    public static <T extends ParsedPacket> BusResult<T> empty() {
        return new BusResult<>(false);
    }

    public static <T extends ParsedPacket> BusResult<T> of(T packet) {
        return new BusResult<>(false, packet);
    }

    public static <T extends ParsedPacket> BusResult<T> of(AcknowledgmentPacket packet) {
        return new BusResult<>(false, packet);
    }

    @Override
    public int hashCode() {
        return Objects.hash(packet, retryUsed);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof BusResult) {
            BusResult<?> that = (BusResult<?>) o;
            return retryUsed == that.retryUsed && packet.equals(that.packet);
        }
        return false;
    }

    @Override
    public String toString() {
        return requestResult().toString();
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

    public RequestResult<T> requestResult() {
        return new RequestResult<>(isRetryUsed(), getPacket().stream().collect(Collectors.toList()));
    }

    public Stream<T> toPacketStream() {
        return getPacket().stream().flatMap(e -> e.left.stream());
    }
}
