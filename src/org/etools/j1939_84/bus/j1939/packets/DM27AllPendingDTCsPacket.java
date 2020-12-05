/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.j1939.packets;

import org.etools.j1939_84.bus.Packet;
import org.etools.j1939_84.bus.j1939.J1939DaRepository;

/**
 * The {@link DiagnosticTroubleCodePacket} for the All Pending Diagnostic
 * Trouble Codes (DM27)
 *
 * @author Marianne Schaefer (marianne.schaefer@gmail.com)
 *
 */
public class DM27AllPendingDTCsPacket extends DiagnosticTroubleCodePacket {
    public static final int PGN = 64898; // 0x00FD82

    public DM27AllPendingDTCsPacket(Packet packet) {
        super(packet, new J1939DaRepository().findPgnDefinition(PGN));
    }

    @Override
    public String getName() {
        return "DM27";
    }

}
