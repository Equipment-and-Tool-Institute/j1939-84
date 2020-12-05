/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.j1939.packets;

import org.etools.j1939_84.bus.Packet;
import org.etools.j1939_84.bus.j1939.J1939DaRepository;

/**
 * The DM1 Active Diagnostic Trouble Codes
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 *
 */
public class DM1ActiveDTCsPacket extends DiagnosticTroubleCodePacket {

    public static final int PGN = 65226; // (0x00FECA);

    public DM1ActiveDTCsPacket(Packet packet) {
        super(packet, new J1939DaRepository().findPgnDefinition(PGN));
    }

    @Override
    public String getName() {
        return "DM1";
    }

}
