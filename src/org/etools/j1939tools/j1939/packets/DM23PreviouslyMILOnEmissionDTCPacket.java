/*
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939tools.j1939.packets;

import org.etools.j1939tools.bus.Packet;

/**
 * The {@link ParsedPacket} for Emission-Related Previously Malfunction
 * Indicator Lamp On Diagnostic Trouble Codes (DM12)
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 */
public class DM23PreviouslyMILOnEmissionDTCPacket extends DiagnosticTroubleCodePacket {
    public static final int PGN = 64949;

    public DM23PreviouslyMILOnEmissionDTCPacket(Packet packet) {
        super(packet);
    }

    public static DM23PreviouslyMILOnEmissionDTCPacket create(int address,
                                                              LampStatus mil,
                                                              LampStatus stop,
                                                              LampStatus amber,
                                                              LampStatus protect,
                                                              DiagnosticTroubleCode... dtcs) {
        return new DM23PreviouslyMILOnEmissionDTCPacket(create(address, PGN, mil, stop, amber, protect, dtcs));
    }

    @Override
    public String getName() {
        return "DM23";
    }

}
