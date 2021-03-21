/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.j1939.packets;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.utils.CollectionUtils.join;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.etools.j1939_84.bus.Packet;

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

    private String createListingOfSpnForReporting(List<SupportedSPN> supportedSPNs, String reportTitle) {
        StringBuilder sb = new StringBuilder();
        sb.append("(").append(reportTitle).append(")").append(" [").append(NL);
        if (supportedSPNs.isEmpty()) {
            sb.append("  No Supported SPNs").append(NL);
        } else {
            supportedSPNs.forEach(supportedSPN -> {
                sb.append("  ").append(supportedSPN).append(NL);
            });
        }
        sb.append("]").append(NL);
        return sb.toString();
    }

    @Override
    public String getName() {
        return "DM24";
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getStringPrefix()).append(NL);
        List<SupportedSPN> scaledResults = getSupportedSpns()
                                                             .stream()
                                                             .filter(SupportedSPN::supportsScaledTestResults)
                                                             .collect(Collectors.toList());

        sb.append(createListingOfSpnForReporting(scaledResults, "Supporting Scaled Test Results"));

        List<SupportedSPN> supportsDataStreamsResults = getSupportedSpns()
                                                                          .stream()
                                                                          .filter(SupportedSPN::supportsDataStream)
                                                                          .collect(Collectors.toList());

        sb.append(createListingOfSpnForReporting(supportsDataStreamsResults, "Supports Data Stream Results"));

        List<SupportedSPN> supportsFreezeFrameResults = getSupportedSpns()
                                                                          .stream()
                                                                          .filter(SupportedSPN::supportsExpandedFreezeFrame)
                                                                          .collect(Collectors.toList());

        sb.append(createListingOfSpnForReporting(supportsFreezeFrameResults, "Supports Freeze Frame Results"));
        return sb.toString();
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
