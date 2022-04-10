/*
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939tools.j1939.packets;

import org.etools.j1939tools.bus.Packet;

/**
 * The {@link ParsedPacket} for Emission-Related Malfunction Indicator Lamp On
 * Diagnostic Trouble Codes (DM12)
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 */
public class DM12MILOnEmissionDTCPacket extends DiagnosticTroubleCodePacket {
    public static final int PGN = 65236; // 0xFED4

    public DM12MILOnEmissionDTCPacket(Packet packet) {
        super(packet);
    }

    public static DM12MILOnEmissionDTCPacket create(int address,
                                                    LampStatus mil,
                                                    LampStatus stop,
                                                    LampStatus amber,
                                                    LampStatus protect,
                                                    DiagnosticTroubleCode... dtcs) {
        return new DM12MILOnEmissionDTCPacket(create(address, PGN, mil, stop, amber, protect, dtcs));
    }

    @Override
    public String getName() {
        return "DM12";
    }

}
