/**
 * Copyright (c) 2019. Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.j1939;

/**
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class BusResult<T> {

    private final T packet;
    private final boolean retryUsed;

    /**
     *
     */
    public BusResult(boolean retryUsed, T packet) {
        this.retryUsed = retryUsed;
        this.packet = packet;
    }

    /**
     * @return the packets
     */
    public T getPacket() {
        return packet;
    }

    /**
     * @return the retryUsed
     */
    public boolean isRetryUsed() {
        return retryUsed;
    }

}
