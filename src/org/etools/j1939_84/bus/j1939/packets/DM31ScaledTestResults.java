/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.j1939.packets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.etools.j1939_84.bus.Packet;

/**
 * The {@link ParsedPacket} for Diagnostic Trouble Code to Lamp Associations
 * (DM31)
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 *
 * DTC to Lamp Association
 */
public class DM31ScaledTestResults extends DiagnosticTroubleCodePacket {
    // Hex value of PGN = 00A300
    public static final int PGN = 41728;
    private List<DiagnosticTroubleCodePacket> dtcPackets;

    public DM31ScaledTestResults(Packet packet) {
        super(packet);
    }

    /**
     * @return the dtcPackets
     */
    public List<DiagnosticTroubleCodePacket> getDtcPackets() {
        if (dtcPackets == null) {
            parsePacket();
        }
        return dtcPackets;
    }

    @Override
    public String getName() {
        return "DM31";
    }

    /**
     * Parses the packet to populate all the member variables
     */
    private void parsePacket() {
        final int length = getPacket().getLength();
        dtcPackets = new ArrayList<>();
        for (int i = 0; i + 5 < length; i = i + 6) {
            dtcPackets.add(new DiagnosticTroubleCodePacket(Packet.create(getPacket().getId(),
                    getPacket().getSource(),
                    Arrays.copyOfRange(getPacket().getBytes(), i, i + 6))));
        }
    }

}
