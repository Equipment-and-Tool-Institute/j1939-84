/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.j1939.packets;

import org.etools.j1939_84.bus.Packet;

/**
 * The {@link ParsedPacket} for the Emission Related Pending Diagnostic Trouble
 * Codes (DM6)
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class DM6PendingEmissionDTCPacket extends DiagnosticTroubleCodePacket {
    public static final int PGN = 65231;

    public DM6PendingEmissionDTCPacket(Packet packet) {
        super(packet);
    }

    @Override
    public String getName() {
        return "DM6";
    }

}
