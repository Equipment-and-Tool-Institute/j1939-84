/*
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.j1939.packets;

import org.etools.j1939_84.bus.Packet;

/**
 * The DM1 Active Diagnostic Trouble Codes
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 */
public class DM1ActiveDTCsPacket extends DiagnosticTroubleCodePacket {

    public static final int PGN = 65226; // 0xFECA

    public static DM1ActiveDTCsPacket create(int address,
                                             LampStatus mil,
                                             LampStatus stop,
                                             LampStatus amber,
                                             LampStatus protect,
                                             DiagnosticTroubleCode... dtcs) {
        return new DM1ActiveDTCsPacket(create(address, PGN, mil, stop, amber, protect, dtcs));
    }

    public DM1ActiveDTCsPacket(Packet packet) {
        super(packet);
    }

    @Override
    public String getName() {
        return "DM1";
    }

}
