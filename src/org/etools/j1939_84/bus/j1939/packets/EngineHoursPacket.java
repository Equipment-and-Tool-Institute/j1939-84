/*
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.j1939.packets;

import org.etools.j1939_84.bus.Packet;

public class EngineHoursPacket extends GenericPacket {

    public static final int PGN = 65253;

    public static EngineHoursPacket create(int address, long engineHours) {
        long hours = (long) (engineHours / 0.05);
        int[] data = ParsedPacket.toInts(hours);
        return new EngineHoursPacket(Packet.create(PGN, address, data));
    }
    public EngineHoursPacket(Packet packet) {
        super(packet);
    }

    public double getEngineHours() {
        return getSpnValue(247).findFirst().orElse(NOT_AVAILABLE);
    }

}
