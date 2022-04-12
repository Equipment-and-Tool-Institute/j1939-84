/*
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939tools.j1939.packets;

import org.etools.j1939tools.bus.Packet;

/**
 * The DM3 Packet. This isn't used to parse any packets as it will only be sent
 * to the vehicle. Responses will be {@link AcknowledgmentPacket}
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class DM3DiagnosticDataClearPacket extends GenericPacket {

    public static final int PGN = 65228; // 0xFECC

    public DM3DiagnosticDataClearPacket(Packet packet) {
        super(packet);
    }

    @Override
    public String getName() {
        return "DM3";
    }

}
