/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939tools.j1939.packets;

import org.etools.j1939tools.bus.Packet;
import org.etools.j1939tools.j1939.J1939;

/**
 * @author Matt Gumbel (matt@soliddesign.net)
 */
public class AcknowledgmentPacket extends GenericPacket {

    public static final int PGN = 59392; // 0xE800
    private Response response;

    public AcknowledgmentPacket(Packet packet) {
        super(packet);
    }

    public static AcknowledgmentPacket create(int sourceAddress, Response response) {
        return create(sourceAddress, response, 0, 0xF9, 0);
    }

    public static AcknowledgmentPacket create(int sourceAddress,
                                              Response response,
                                              int groupFunction,
                                              int addressAcknowledged,
                                              long pgnRequested) {
        int[] data = new int[8];
        data[0] = response.value;
        data[1] = groupFunction;
        data[2] = 0xFF;
        data[3] = 0xFF;
        data[4] = addressAcknowledged;

        int[] pgnInts = to4Ints(pgnRequested);
        data[5] = pgnInts[0];
        data[6] = pgnInts[1];
        data[7] = pgnInts[2];

        Packet packet = Packet.create(PGN | J1939.GLOBAL_ADDR, sourceAddress, data);
        return new AcknowledgmentPacket(packet);
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

    @Override
    public String toString() {
        return getStringPrefix() + "Response: " + getResponse() + ", Group Function: " + getGroupFunction()
                + ", Address Acknowledged: " + getAddressAcknowledged() + ", PGN Requested: " + getPgnRequested();
    }

    public int getPgnRequested() {
        return getPacket().get24(5);
    }

    public Response getResponse() {
        if (response == null) {
            response = Response.find(getPacket().get(0));
        }
        return response;
    }

    public enum Response {
        ACK(0, "ACK"), BUSY(3, "Busy"), DENIED(2, "Denied"), NACK(1, "NACK"), UNKNOWN(-1, "Unknown");

        private final String string;
        private final int value;

        Response(int value, String string) {
            this.value = value;
            this.string = string;
        }

        private static Response find(int value) {
            for (Response r : Response.values()) {
                if (r.value == value) {
                    return r;
                }
            }
            return Response.UNKNOWN;
        }

        @Override
        public String toString() {
            return string;
        }
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
