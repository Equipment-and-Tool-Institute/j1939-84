/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939tools.j1939.packets;

import org.etools.j1939tools.bus.Packet;

/**
 * The DM11 DTC Reset/Clear Active DTCs packet won't be received by the vehicle.
 * The response is a NACK or ACK instead.
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class DM11ClearActiveDTCsPacket extends AcknowledgmentPacket {

    public static final int PGN = 65235;
    private final DM11Response response;

    public DM11ClearActiveDTCsPacket(Packet packet) {
        super(packet);
        response = parseResponse();
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
    /**
     * Returns the response
     *
     * @return the response
     */
    public DM11Response getDM11Response() {
        return response;
    }

    @Override
    public String getName() {
        return "DM11";
    }

    @Override
    public String toString() {
        return getStringPrefix() + "Response is " + getDM11Response();
    }

    private DM11Response parseResponse() {
        int responseByte = getPacket().get(0);
        return DM11Response.find(responseByte);
    }

    /**
     * The possible responses to the DM11 request
     */
    public enum DM11Response {
        ACK(0, "Acknowledged"), BUSY(3, "Busy"), DENIED(2, "Denied"), NACK(1, "NACK"), UNKNOWN(-1, "Unknown");

        private final String string;
        private final int value;

        DM11Response(int value, String string) {
            this.value = value;
            this.string = string;
        }

        private static DM11Response find(int value) {
            for (DM11Response r : DM11Response.values()) {
                if (r.value == value) {
                    return r;
                }
            }
            return DM11Response.UNKNOWN;
        }

        @Override
        public String toString() {
            return string;
        }
    }

}
