/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939tools.j1939.packets;

import org.etools.j1939tools.bus.Packet;

/**
 * The {@link ParsedPacket} for the Previously ActiveDTC Diagnostic Trouble
 * Codes (DM2)
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 */
public class DM2PreviouslyActiveDTC extends DiagnosticTroubleCodePacket {
    public static final int PGN = 65227; // 0xFECB

    public DM2PreviouslyActiveDTC(Packet packet) {
        super(packet);
    }

    public static DM2PreviouslyActiveDTC create(int address,
                                                LampStatus mil,
                                                LampStatus stop,
                                                LampStatus amber,
                                                LampStatus protect,
                                                DiagnosticTroubleCode... dtcs) {
        return new DM2PreviouslyActiveDTC(create(address, PGN, mil, stop, amber, protect, dtcs));
    }

    @Override
    public String getName() {
        return "DM2";
    }

}
