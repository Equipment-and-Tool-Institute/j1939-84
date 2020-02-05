/**
 * Copyright (c) 2019. Equipment & Tool Institute
 */
package org.etools.j1939_84.model;

import java.util.List;

/**
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class RequestResult<T> {

    private final List<T> packets;
    private final boolean retryUsed;

    /**
     * @param retryUsed boolean representation of retry used
     * @param packets   list of packets to be included in the requestResult
     *
     */
    public RequestResult(boolean retryUsed, List<T> packets) {
        this.retryUsed = retryUsed;
        this.packets = packets;
    }

    /**
     * @return the packets
     */
    public List<T> getPackets() {
        return packets;
    }

    /**
     * @return the retryUsed
     */
    public boolean isRetryUsed() {
        return retryUsed;
    }

}
