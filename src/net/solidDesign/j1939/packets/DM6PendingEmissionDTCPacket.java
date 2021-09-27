/**
 * Copyright 2019 Equipment & Tool Institute
 */
package net.solidDesign.j1939.packets;

import org.etools.j1939_84.bus.Packet;

/**
 * The {@link ParsedPacket} for the Emission Related Pending Diagnostic Trouble
 * Codes (DM6)
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 */
public class DM6PendingEmissionDTCPacket extends DiagnosticTroubleCodePacket {
    public static final int PGN = 65231;

    public DM6PendingEmissionDTCPacket(Packet packet) {
        super(packet);
    }

    public static DM6PendingEmissionDTCPacket create(int address,
                                                     LampStatus mil,
                                                     LampStatus stop,
                                                     LampStatus amber,
                                                     LampStatus protect,
                                                     DiagnosticTroubleCode... dtcs) {

        return new DM6PendingEmissionDTCPacket(create(address, PGN, mil, stop, amber, protect, dtcs));
    }

    @Override
    public String getName() {
        return "DM6";
    }

}
