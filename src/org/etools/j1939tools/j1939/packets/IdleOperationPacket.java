/*
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939tools.j1939.packets;

import org.etools.j1939tools.bus.Packet;
import org.etools.j1939tools.utils.CollectionUtils;

public class IdleOperationPacket extends GenericPacket {

    public static final int PGN = 65244;

    public static IdleOperationPacket create(int address, long idleHours) {
        long hours = (long) (idleHours / 0.05);
        int[] data = new int[] { 0xFF, 0xFF, 0xFF, 0xFF };
        data = CollectionUtils.join(data, ParsedPacket.to4Ints(hours));
        return new IdleOperationPacket(Packet.create(PGN, address, data));
    }

    public IdleOperationPacket(Packet packet) {
        super(packet);
    }

    public double getEngineIdleHours() {
        return getSpnValue(235).orElse(NOT_AVAILABLE);
    }

}
