/*
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.j1939.packets;

import org.etools.j1939_84.bus.Packet;

/**
 * The {@link ParsedPacket} for Emission-Related Permanent Diagnostic Trouble
 * Codes (DM28)
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 */
public class DM28PermanentEmissionDTCPacket extends DiagnosticTroubleCodePacket {

    public static final int PGN = 64896; // 0xFD80

    public static DM28PermanentEmissionDTCPacket create(int address,
                                                        LampStatus mil,
                                                        LampStatus stop,
                                                        LampStatus amber,
                                                        LampStatus protect,
                                                        DiagnosticTroubleCode... dtcs) {
        return new DM28PermanentEmissionDTCPacket(create(address, PGN, mil, stop, amber, protect, dtcs));
    }

    public DM28PermanentEmissionDTCPacket(Packet packet) {
        super(packet);
    }

    @Override
    public String getName() {
        return "DM28";
    }

}
