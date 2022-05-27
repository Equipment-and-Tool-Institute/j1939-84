/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939tools.j1939.packets;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939tools.utils.CollectionUtils.join;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.etools.j1939tools.bus.Packet;
import org.etools.j1939tools.j1939.Lookup;


/**
 * Parses the SPN Support (DM24) Packet
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 */
public class DM24SPNSupportPacket extends GenericPacket {

    public static final int PGN = 64950; // 0xFDB6
    private List<SupportedSPN> spns;
    private List<SupportedSPN> freezeFrameSPNs;

    public DM24SPNSupportPacket(Packet packet) {
        super(packet);
    }

    public static DM24SPNSupportPacket create(int source, SupportedSPN... spns) {

        int[] data = new int[0];
        for (SupportedSPN spn : spns) {
            data = join(data, spn.getData());
        }

        return new DM24SPNSupportPacket(Packet.create(PGN, source, data));
    }

    public static DM24SPNSupportPacket create(int source, List<SupportedSPN> spns) {

        int[] data = new int[0];
        for (SupportedSPN spn : spns) {
            data = join(data, spn.getData());
        }

        return new DM24SPNSupportPacket(Packet.create(PGN, source, data));
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
    public String getName() {
        return "DM24";
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getStringPrefix()).append("[").append(NL);

        sb.append("  D F T D F").append(NL);
        sb.append("  a r e M F").append(NL);
        sb.append("  t F s 5 l").append(NL);
        sb.append("  a r t 8 n  SPN — SP Name").append(NL);
        sb.append("  ------------------------").append(NL);
        getSupportedSpns().forEach(supportedSPN -> sb.append(createRow(supportedSPN)).append(NL));
        sb.append("]").append(NL);
        return sb.toString();
    }

    private static String createRow(SupportedSPN supportedSPN) {
        String result = "  ";
        if (supportedSPN.supportsDataStream()) {
            result += "D ";
        } else {
            result += "  ";
        }

        if (supportedSPN.supportsExpandedFreezeFrame()) {
            result += "F ";
        } else {
            result += "  ";
        }

        if (supportedSPN.supportsScaledTestResults()) {
            result += "T ";
        } else {
            result += "  ";
        }

        if (supportedSPN.supportsRationalityFaultData()) {
            result += "R ";
        } else {
            result += "  ";
        }

        result += supportedSPN.getLength();
        if (supportedSPN.getLength() >= 10) {
            result += " ";
        } else {
            result += "  ";
        }

        result += supportedSPN.toString();
        return result;
    }

    /**
     * Returns the {@link List} of {@link SupportedSPN}
     *
     * @return {@link List}
     */
    public List<SupportedSPN> getSupportedSpns() {
        if (spns == null) {
            spns = new ArrayList<>();
            parsePacket();
            spns.sort(Comparator.comparingInt(SupportedSPN::getSpn));
        }
        return spns;
    }

    public List<SupportedSPN> getFreezeFrameSPNsInOrder() {
        if (freezeFrameSPNs == null) {
            freezeFrameSPNs = new ArrayList<>();
            int length = getPacket().getLength();
            for (int i = 0; i + 3 < length; i = i + 4) {
                SupportedSPN parsedSpn = parseSpn(i);
                if (parsedSpn.getSpn() != 0 && parsedSpn.supportsExpandedFreezeFrame()) {
                    freezeFrameSPNs.add(parsedSpn);
                }
            }
        }
        return freezeFrameSPNs;
    }

    public String printFreezeFrameSPNsInOrder() {
        StringBuilder sb = new StringBuilder();
        sb.append("SPs Supported in Expanded Freeze Frame from ")
          .append(Lookup.getAddressName(getSourceAddress()))
          .append(": [")
          .append(NL);
        sb.append("  LN  SPN — SP Name").append(NL);
        sb.append("  -----------------").append(NL);
        getFreezeFrameSPNsInOrder().forEach(supportedSPN -> sb.append(createFreezeFrameRow(supportedSPN)).append(NL));
        sb.append("]").append(NL);
        return sb.toString();
    }

    private static String createFreezeFrameRow(SupportedSPN supportedSPN) {
        return String.format("  %1$2s  " + supportedSPN.toString(), supportedSPN.getLength());
    }

    /**
     * Parses the packet to populate all the {@link SupportedSPN}s
     */
    private void parsePacket() {
        int length = getPacket().getLength();
        for (int i = 0; i + 3 < length; i = i + 4) {
            SupportedSPN parsedSpn = parseSpn(i);
            if (parsedSpn.getSpn() != 0) {
                spns.add(parsedSpn);
            }
        }
    }

    /**
     * Parses a portion of the packet to create a {@link SupportedSPN}
     *
     * @param  index
     *                   the index at which the parsing starts
     * @return       a {@link SupportedSPN}
     */
    private SupportedSPN parseSpn(int index) {
        int[] data = getPacket().getData(index, index + 4);
        return new SupportedSPN(data);
    }

}
