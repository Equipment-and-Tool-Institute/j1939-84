/*
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939tools.j1939.packets;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939tools.utils.CollectionUtils.join;

import java.util.ArrayList;
import java.util.List;

import org.etools.j1939tools.bus.Packet;


/**
 * The {@link ParsedPacket} for Diagnostic Trouble Code to Lamp Associations
 * (DM31)
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 *         <p>
 *         DTC to Lamp Association
 */
public class DM31DtcToLampAssociation extends GenericPacket {
    public static final int PGN = 41728; // 0xA300
    private List<DTCLampStatus> dtcLampStatuses;

    public DM31DtcToLampAssociation(Packet packet) {
        super(packet);
    }

    public static DM31DtcToLampAssociation create(int sourceAddress, int destination, DTCLampStatus... lampStatuses) {
        int[] data = new int[0];
        if (lampStatuses.length > 0) {
            for (DTCLampStatus dtcLampStatus : lampStatuses) {
                data = join(data, dtcLampStatus.getData());
            }
        } else {
            data = new int[] { 0, 0, 0, 0, 0, 0xFF, 0xFF, 0xFF };
        }
        return new DM31DtcToLampAssociation(Packet.create(PGN | destination, sourceAddress, data));
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

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getStringPrefix()).append(NL);
        sb.append("DTC Lamp Statuses: [").append(NL);
        for (DTCLampStatus dtcLampStatus : getDtcLampStatuses()) {
            sb.append(dtcLampStatus).append(NL);
        }
        sb.append("]");

        return sb.toString();
    }

    public DTCLampStatus findLampStatusForDTC(DiagnosticTroubleCode dtc) {
        return getDtcLampStatuses().stream()
                                   .filter(l -> l.getDtc()
                                                 .getSuspectParameterNumber() == dtc.getSuspectParameterNumber())
                                   .filter(l -> l.getDtc().getFailureModeIndicator() == dtc.getFailureModeIndicator())
                                   .findFirst()
                                   .orElse(null);
    }

    /**
     * Parses the packet to populate all the member variables
     */
    private void parsePacket() {
        int length = getPacket().getLength();
        dtcLampStatuses = new ArrayList<>();
        for (int i = 0; i + 6 <= length; i = i + 6) {
            if (getPacket().get32(0) != 0) {
                dtcLampStatuses.add(new DTCLampStatus(getPacket().getData(i, i + 6)));
            }
        }
    }

}
