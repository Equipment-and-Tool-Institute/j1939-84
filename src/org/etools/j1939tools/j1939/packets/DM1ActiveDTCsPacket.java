/*
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939tools.j1939.packets;

import org.etools.j1939tools.bus.Packet;

/**
 * The DM1 Active Diagnostic Trouble Codes
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 */
public class DM1ActiveDTCsPacket extends DiagnosticTroubleCodePacket {

    public static final int PGN = 65226; // 0xFECA

    public DM1ActiveDTCsPacket(Packet packet) {
        super(packet);
    }

    public static DM1ActiveDTCsPacket create(int address,
                                             LampStatus mil,
                                             LampStatus stop,
                                             LampStatus amber,
                                             LampStatus protect,
                                             DiagnosticTroubleCode... dtcs) {
        return new DM1ActiveDTCsPacket(create(address, PGN, mil, stop, amber, protect, dtcs));
    }

    @Override
    public String getName() {
        return "DM1";
    }

}
