/*
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.j1939.packets;

import org.etools.j1939_84.bus.Packet;
import org.etools.j1939_84.bus.j1939.J1939DaRepository;

/**
 * The {@link ParsedPacket} for Emission-Related Malfunction Indicator Lamp On
 * Diagnostic Trouble Codes (DM12)
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class DM12MILOnEmissionDTCPacket extends DiagnosticTroubleCodePacket {
    public static final int PGN = 65236; // 0x00FED4

    public DM12MILOnEmissionDTCPacket(Packet packet) {
        super(packet, new J1939DaRepository().findPgnDefinition(PGN));
    }

    @Override
    public String getName() {
        return "DM12";
    }

}
