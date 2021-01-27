/**
 * Copyright (c) 2019. Equipment & Tool Institute
 */
package org.etools.j1939_84.model;

import static org.etools.j1939_84.J1939_84.NL;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.etools.j1939_84.bus.Either;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939_84.bus.j1939.packets.ParsedPacket;

/**
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class RequestResult<T extends ParsedPacket> {

    public static <S extends ParsedPacket> RequestResult<S> empty() {
        return empty(true);
    }

    public static <S extends ParsedPacket> RequestResult<S> empty(boolean retry) {
        return new RequestResult<>(retry, Collections.emptyList());
    }

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
     *            boolean representation of retry used
     * @param packets
     *            list of packets to be included in the requestResult
     *
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
        this.acks = Collections.emptyList();
    }

    @SafeVarargs
    public <TP extends AcknowledgmentPacket> RequestResult(boolean retryUsed, TP... packets) {
        this.retryUsed = retryUsed;
        this.packets = Collections.emptyList();
        this.acks = Arrays.asList(packets);
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
        return this.isRetryUsed() == that.isRetryUsed()
                && Objects.equals(this.getPackets(), that.getPackets())
                && Objects.equals(this.getAcks(), that.getAcks());
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

    /**
     * @return the retryUsed
     */
    public boolean isRetryUsed() {
        return retryUsed;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("RequestResult");
        sb.append(NL)
                .append("Retry  used : ")
                .append(isRetryUsed())
                .append(NL)
                .append("Response packets :");
        this.getPackets().forEach(packet -> {
            sb.append(NL)
                    .append("Source address : ")
                    .append(packet.getSourceAddress())
                    .append(" returned ")
                    .append(packet.toString());
        });
        if (this.getPackets().isEmpty()) {
            sb.append(NL).append("No packets returned");
        }
        sb.append(NL)
                .append("Ack packets :");
        this.getAcks().forEach(ack -> {
            sb.append(NL)
                    .append("Source address : ")
                    .append(ack.getSourceAddress())
                    .append(" returned ")
                    .append(ack.toString());
        });
        if (this.getAcks().isEmpty()) {
            sb.append(NL).append("No acks returned");
        }

        return sb.toString();
    }

}
