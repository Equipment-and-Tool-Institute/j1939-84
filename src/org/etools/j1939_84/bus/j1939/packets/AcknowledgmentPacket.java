/**
 * Copyright (c) 2019. Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.j1939.packets;

import org.etools.j1939_84.bus.Packet;
import org.etools.j1939_84.bus.j1939.J1939DaRepository;

/**
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class AcknowledgmentPacket extends GenericPacket {

    /**
     * The possible responses
     */
    public enum Response {
        ACK(0, "ACK"), BUSY(3, "Busy"), DENIED(2, "Denied"), NACK(1, "NACK"), UNKNOWN(-1, "Unknown");

        private static Response find(int value) {
            for (Response r : Response.values()) {
                if (r.value == value) {
                    return r;
                }
            }
            return Response.UNKNOWN;
        }

        private final String string;

        private final int value;

        private Response(int value, String string) {
            this.value = value;
            this.string = string;
        }

        @Override
        public String toString() {
            return string;
        }
    }

    public static final int PGN = 59392;

    private Response response;

    /**
     * @param packet acknowledgement packet to be returned
     */
    public AcknowledgmentPacket(Packet packet) {
        super(packet, new J1939DaRepository().findPgnDefinition(PGN));
    }

    public int getAddressAcknowledged() {
        return getPacket().get(4);
    }

    public int getGroupFunction() {
        return getPacket().get(1);
    }

    @Override
    public String getName() {
        return "Acknowledgment";
    }

    public int getPgnRequested() {
        return getPacket().get24(5);
    }

    /**
     * Returns the response
     *
     * @return the response
     */
    public Response getResponse() {
        if (response == null) {
            response = Response.find(getPacket().get(0));
        }
        return response;
    }

    @Override
    public String toString() {
        return getStringPrefix() + "Response: " + getResponse() + ", Group Function: " + getGroupFunction()
                + ", Address Acknowledged: " + getAddressAcknowledged() + ", PGN Requested: " + getPgnRequested();
    }

}
