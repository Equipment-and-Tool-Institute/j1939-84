/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939tools.bus;

import static org.etools.j1939_84.J1939_84.NL;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.etools.j1939tools.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939tools.j1939.packets.ParsedPacket;

/**
 * @author Matt Gumbel (matt@soliddesign.net)
 */
public class RequestResult<T extends ParsedPacket> {

    private final List<AcknowledgmentPacket> acks;
    private final List<T> packets;
    private final boolean retryUsed;

    public RequestResult(boolean retryUsed,
                         List<Either<T, AcknowledgmentPacket>> packets) {
        this(retryUsed,
             packets.stream().flatMap(e -> e.left.stream()).collect(Collectors.toList()),
             packets.stream().flatMap(e -> e.right.stream()).collect(Collectors.toList()));
    }

    /**
     * @param retryUsed
     *                      boolean representation of retry used
     * @param packets
     *                      list of packets to be included in the requestResult
     */
    public RequestResult(boolean retryUsed, List<T> packets, List<AcknowledgmentPacket> acks) {
        this.retryUsed = retryUsed;
        this.packets = Objects.requireNonNull(packets);
        this.acks = Objects.requireNonNull(acks);
    }

    @SafeVarargs
    public RequestResult(boolean retryUsed, T... packets) {
        this.retryUsed = retryUsed;
        this.packets = Arrays.asList(packets);
        acks = Collections.emptyList();
    }

    @SafeVarargs
    public <TP extends AcknowledgmentPacket> RequestResult(boolean retryUsed, TP... packets) {
        this.retryUsed = retryUsed;
        this.packets = Collections.emptyList();
        acks = Arrays.asList(packets);
    }

    public static <S extends ParsedPacket> RequestResult<S> empty() {
        return empty(true);
    }

    public static <S extends ParsedPacket> RequestResult<S> empty(boolean retry) {
        return new RequestResult<>(retry, Collections.emptyList());
    }

    @SafeVarargs
    public static <S extends ParsedPacket> RequestResult<S> of(S... packets) {
        return new RequestResult<>(false, packets);
    }

    public List<AcknowledgmentPacket> getAcks() {
        return acks;
    }

    public List<Either<T, AcknowledgmentPacket>> getEither() {
        return Stream
                     .concat(packets.stream().map(p -> new Either<>(p, (AcknowledgmentPacket) null)),
                             acks.stream().map(a -> new Either<>((T) null, a)))
                     .collect(Collectors.toList());
    }

    /**
     * @return the packets
     */
    public List<T> getPackets() {
        return packets;
    }

    @Override
    public int hashCode() {
        return Objects.hash(isRetryUsed(), getPackets(), getAcks());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof RequestResult)) {
            return false;
        }

        RequestResult<?> that = (RequestResult<?>) obj;
        return isRetryUsed() == that.isRetryUsed()
                && Objects.equals(getPackets(), that.getPackets())
                && Objects.equals(getAcks(), that.getAcks());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("RequestResult");
        sb.append(NL)
          .append("Retry  used : ")
          .append(isRetryUsed())
          .append(NL)
          .append("Response packets :");
        getPackets()
            .forEach(packet -> sb.append(NL)
                                 .append("Source address : ")
                                 .append(packet.getSourceAddress())
                                 .append(" returned ")
                                 .append(packet.toString()));
        if (getPackets().isEmpty()) {
            sb.append(NL).append("No packets returned");
        }
        sb.append(NL)
          .append("Ack packets :");
        getAcks()
            .forEach(ack -> sb.append(NL)
                              .append("Source address : ")
                              .append(ack.getSourceAddress())
                              .append(" returned ")
                              .append(ack.toString()));
        if (getAcks().isEmpty()) {
            sb.append(NL).append("No acks returned");
        }

        return sb.toString();
    }

    /**
     * @return the retryUsed
     */
    public boolean isRetryUsed() {
        return retryUsed;
    }

    public BusResult<T> busResult() {

        List<Either<T, AcknowledgmentPacket>> either = getEither();
        if (either.isEmpty()) {
            return new BusResult<>(retryUsed);
        } else {
            return new BusResult<>(retryUsed, either.get(0));
        }
    }

    public Stream<T> toPacketStream() {
        return getPackets().stream();
    }

}
