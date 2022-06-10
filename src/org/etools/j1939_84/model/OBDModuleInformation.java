/*
 * Copyright (c) 2019. Equipment & Tool Institute
 */
package org.etools.j1939_84.model;

import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.etools.j1939tools.j1939.Lookup;
import org.etools.j1939tools.j1939.packets.DM24SPNSupportPacket;
import org.etools.j1939tools.j1939.packets.DM27AllPendingDTCsPacket;
import org.etools.j1939tools.j1939.packets.GenericPacket;
import org.etools.j1939tools.j1939.packets.ScaledTestResult;
import org.etools.j1939tools.j1939.packets.SupportedSPN;

/**
 * @author Matt Gumbel (matt@soliddesign.net)
 */
public class OBDModuleInformation implements Cloneable {

    private final int sourceAddress;

    private final int function;

    /** These SPNs represent SP which appear in multiple PGs. */
    private final List<Integer> omittedSPNs = new ArrayList<>(List.of(588,
                                                                      976,
                                                                      1213,
                                                                      1220,
                                                                      12675,
                                                                      12691,
                                                                      12730,
                                                                      12783,
                                                                      12797));

    // TODO this should be removed and tests re-factored to save a DM24 instead
    private final List<SupportedSPN> supportedSPNs = new ArrayList<>();

    private final List<ScaledTestResult> scaledTestResults = new ArrayList<>();

    private final List<ScaledTestResult> nonInitializedTests = new ArrayList<>();

    private Double deltaEngineStart = null;

    private PacketArchive packetArchive = new PacketArchive();

    public OBDModuleInformation(int sourceAddress) {
        this(sourceAddress, -1);
    }

    public OBDModuleInformation(int sourceAddress, int function) {
        this.sourceAddress = sourceAddress;
        this.function = function;
    }

    @Override
    public OBDModuleInformation clone() {
        OBDModuleInformation obdInfo = new OBDModuleInformation(getSourceAddress(), getFunction());
        obdInfo.setScaledTestResults(getScaledTestResults());
        obdInfo.setSupportedSPNs(getSupportedSPNs());
        obdInfo.setNonInitializedTests(getNonInitializedTests());
        obdInfo.setDeltaEngineStart(getDeltaEngineStart());
        obdInfo.packetArchive = packetArchive;

        return obdInfo;
    }

    public int getSourceAddress() {
        return sourceAddress;
    }

    public String getModuleName() {
        return Lookup.getAddressName(getSourceAddress());
    }

    public int getFunction() {
        return function;
    }

    /**
     * @deprecated Use a DM24 to set the support SPNs
     */
    @Deprecated
    public void setSupportedSPNs(List<SupportedSPN> supportedSPNs) {
        this.supportedSPNs.clear();
        this.supportedSPNs.addAll(supportedSPNs);
    }

    public boolean supportsSpn(int spn) {
        return getSupportedSPNs()
                                 .stream()
                                 .map(SupportedSPN::getSpn)
                                 .anyMatch(s -> s == spn);
    }

    public List<SupportedSPN> getSupportedSPNs() {
        DM24SPNSupportPacket dm24 = get(DM24SPNSupportPacket.class, 1);
        List<SupportedSPN> spns = dm24 == null ? supportedSPNs : dm24.getSupportedSpns();
        return spns.stream().sorted(comparingInt(SupportedSPN::getSpn)).collect(toList());
    }

    public List<SupportedSPN> getDataStreamSPNs() {
        return getSupportedSPNs().stream()
                                 .filter(SupportedSPN::supportsDataStream)
                                 .collect(toList());
    }

    /**
     * Returns the List of SupportedSPNs filtering out 'dis-allowed' SPNs
     */
    public List<SupportedSPN> getFilteredDataStreamSPNs() {
        return getDataStreamSPNs().stream()
                                  .filter(s -> !getOmittedDataStreamSPNs().contains(s.getSpn()))
                                  .collect(toList());
    }

    public List<SupportedSPN> getFreezeFrameSPNs() {
        return getSupportedSPNs().stream()
                                 .filter(SupportedSPN::supportsExpandedFreezeFrame)
                                 .collect(toList());
    }

    public List<SupportedSPN> getTestResultSPNs() {
        return getSupportedSPNs().stream()
                                 .filter(SupportedSPN::supportsScaledTestResults)
                                 .collect(toList());
    }

    public List<Integer> getOmittedDataStreamSPNs() {
        return omittedSPNs.stream().distinct().sorted().collect(toList());
    }

    public void addOmittedDataStreamSPN(int spn) {
        omittedSPNs.add(spn);
    }

    public List<ScaledTestResult> getScaledTestResults() {
        return scaledTestResults;
    }

    public void setScaledTestResults(List<ScaledTestResult> scaledTestResults) {
        this.scaledTestResults.clear();
        this.scaledTestResults.addAll(scaledTestResults);
        Collections.sort(this.scaledTestResults);
    }

    public List<ScaledTestResult> getNonInitializedTests() {
        return nonInitializedTests;
    }

    public void setNonInitializedTests(List<ScaledTestResult> tests) {
        nonInitializedTests.clear();
        nonInitializedTests.addAll(tests);
    }

    public void set(GenericPacket packet, int partNumber) {
        packetArchive.put(packet, partNumber);
    }

    @Deprecated
    public <T extends GenericPacket> T get(Class<T> clazz, int partNumber) {
        return get(getPg(clazz), partNumber);
    }

    public <T extends GenericPacket> T get(int pg, int partNumber) {
        return packetArchive.get(pg, partNumber);
    }

    public <T extends GenericPacket> T getLatest(Class<T> clazz) {
        return packetArchive.getLatest(clazz);
    }

    public boolean supportsDM27() {
        return getLatest(DM27AllPendingDTCsPacket.class) != null;
    }

    public Double getDeltaEngineStart() {
        return deltaEngineStart;
    }

    public void setDeltaEngineStart(Double deltaEngineStart) {
        this.deltaEngineStart = deltaEngineStart;
    }

    private static class PacketArchive {

        private final Map<Integer, GenericPacket[]> packetArchive = new HashMap<>();

        public void put(GenericPacket packet, int partNumber) {
            if (partNumber == 0) {
                throw new IllegalArgumentException("0 is not a valid partNumber");
            }

            int id = packet.getPgnDefinition().getId();
            var packets = getPackets(id);
            packets[partNumber] = packet;
            packetArchive.put(id, packets);
        }

        public <T extends GenericPacket> T getLatest(Class<T> clazz) {
            for (int i = 12; i > 0; i--) {
                var packet = get(getPg(clazz), i);
                if (packet != null) {
                    return (T) packet;
                }
            }
            return null;

        }

        public <T extends GenericPacket> T get(int pg, int partNumber) {
            if (partNumber == 0) {
                throw new IllegalArgumentException("0 is not a valid partNumber");
            }

            return (T) getPackets(pg)[partNumber];
        }

        private <T extends GenericPacket> GenericPacket[] getPackets(int pg) {
            return packetArchive.getOrDefault(pg, new GenericPacket[13]);
        }

    }

    // Helper method to get the pg for the class object
    private static int getPg(Class<? extends GenericPacket> clazz) {
        int pg = 0;
        try {
            pg = clazz.getField("PGN").getInt(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pg;
    }

}
