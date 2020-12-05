/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.j1939.packets;

import static org.etools.j1939_84.J1939_84.NL;

import java.util.ArrayList;
import java.util.List;
import org.etools.j1939_84.bus.Packet;
import org.etools.j1939_84.bus.j1939.J1939DaRepository;

/**
 * The {@link ParsedPacket} for Diagnostic Trouble Code to Lamp Associations
 * (DM31)
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 *
 *         DTC to Lamp Association
 */
public class DM31DtcToLampAssociation extends GenericPacket {
    // Hex value of PGN = 00A300
    public static final int PGN = 41728;
    private List<DTCLampStatus> dtcLampStatuses;

    public DM31DtcToLampAssociation(Packet packet) {
        super(packet, new J1939DaRepository().findPgnDefinition(PGN));
    }

    /**
     * @return the dtcLampStatuses
     */
    public List<DTCLampStatus> getDtcLampStatuses() {
        if (dtcLampStatuses == null) {
            parsePacket();
        }
        return dtcLampStatuses;
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
        dtcLampStatuses = new ArrayList<>();
        for (int i = 0; i + 6 <= length; i = i + 6) {
            dtcLampStatuses.add(new DTCLampStatus(getPacket().getData(i, i + 6)));
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getStringPrefix() + NL);
        sb.append("DTC Lamp Statuses: [" + NL);
        for (DTCLampStatus dtcLampStatus : getDtcLampStatuses()) {
            sb.append(dtcLampStatus + NL);
        }
        sb.append("]");

        return sb.toString();
    }

}
