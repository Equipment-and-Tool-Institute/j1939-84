/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.j1939.packets;

import org.etools.j1939_84.bus.Packet;
import org.etools.j1939_84.bus.j1939.J1939DaRepository;

/**
 * The {@link ParsedPacket} for the Previously ActiveDTC Diagnostic Trouble
 * Codes (DM2)
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 *
 */
public class DM2PreviouslyActiveDTC extends DiagnosticTroubleCodePacket {
    public static final int PGN = 65227;

    public DM2PreviouslyActiveDTC(Packet packet) {
        super(packet, new J1939DaRepository().findPgnDefinition(PGN));
    }

    @Override
    public String getName() {
        return "DM2";
    }

}
