/*
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939tools.j1939.packets;

import org.etools.j1939tools.bus.Packet;

/**
 * The {@link DiagnosticTroubleCodePacket} for the All Pending Diagnostic
 * Trouble Codes (DM27)
 *
 * @author Marianne Schaefer (marianne.schaefer@gmail.com)
 */
public class DM27AllPendingDTCsPacket extends DiagnosticTroubleCodePacket {
    public static final int PGN = 64898; // 0xFD82

    public DM27AllPendingDTCsPacket(Packet packet) {
        super(packet);
    }

    public static DM27AllPendingDTCsPacket create(int address,
                                                  LampStatus mil,
                                                  LampStatus stop,
                                                  LampStatus amber,
                                                  LampStatus protect,
                                                  DiagnosticTroubleCode... dtcs) {
        return new DM27AllPendingDTCsPacket(create(address, PGN, mil, stop, amber, protect, dtcs));
    }

    @Override
    public String getName() {
        return "DM27";
    }

}
