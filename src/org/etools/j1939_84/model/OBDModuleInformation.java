/*
 * Copyright (c) 2019. Equipment & Tool Institute
 */
package org.etools.j1939_84.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.etools.j1939_84.bus.j1939.Lookup;
import org.etools.j1939_84.bus.j1939.packets.DM19CalibrationInformationPacket.CalibrationInformation;
import org.etools.j1939_84.bus.j1939.packets.DM24SPNSupportPacket;
import org.etools.j1939_84.bus.j1939.packets.DM27AllPendingDTCsPacket;
import org.etools.j1939_84.bus.j1939.packets.GenericPacket;
import org.etools.j1939_84.bus.j1939.packets.MonitoredSystem;
import org.etools.j1939_84.bus.j1939.packets.PerformanceRatio;
import org.etools.j1939_84.bus.j1939.packets.ScaledTestResult;
import org.etools.j1939_84.bus.j1939.packets.SupportedSPN;

/**
 * @author Matt Gumbel (matt@soliddesign.net)
 */
public class OBDModuleInformation implements Cloneable {

    private final List<CalibrationInformation> calibrationInformation = new ArrayList<>();
    private final List<MonitoredSystem> monitoredSystems = new ArrayList<>();
    private final Set<PerformanceRatio> performanceRatios = new HashSet<>();
    private final List<ScaledTestResult> scaledTestResults = new ArrayList<>();
    private final List<ScaledTestResult> nonInitializedTests = new ArrayList<>();
    private final int sourceAddress;
    private final List<SupportedSPN> supportedSPNs = new ArrayList<>();
    /** These SPNs represent SP which appear in multiple PGs. */
    private final List<Integer> omittedSPNs = new ArrayList<>(List.of(588, 1213, 1220, 12675, 12730, 12783, 12797));
    private ComponentIdentification componentIdentification = null;
    private int function;
    private int ignitionCycleCounterValue = -1;
    private byte obdCompliance;
    private String engineFamilyName = "";
    private String modelYear = "";
    private PacketArchive packetArchive = new PacketArchive();

    public OBDModuleInformation(int sourceAddress) {
        this.sourceAddress = sourceAddress;
    }

    @Override
    public OBDModuleInformation clone() {
        try {
            super.clone();
        } catch (CloneNotSupportedException ignored) {
        }
        OBDModuleInformation obdInfo = new OBDModuleInformation(getSourceAddress());
        obdInfo.setCalibrationInformation(getCalibrationInformation());
        obdInfo.setComponentIdentification(getComponentIdentification());
        obdInfo.setFunction(getFunction());
        obdInfo.setIgnitionCycleCounterValue(getIgnitionCycleCounterValue());
        obdInfo.setMonitoredSystems(getMonitoredSystems());
        obdInfo.setObdCompliance(getObdCompliance());
        obdInfo.setPerformanceRatios(getPerformanceRatios());
        obdInfo.setScaledTestResults(getScaledTestResults());
        obdInfo.setSupportedSPNs(getSupportedSPNs());
        obdInfo.setEngineFamilyName(getEngineFamilyName());
        obdInfo.setModelYear(getModelYear());
        obdInfo.setNonInitializedTests(getNonInitializedTests());
        obdInfo.packetArchive = packetArchive;

        return obdInfo;
    }

    public int getIgnitionCycleCounterValue() {
        return ignitionCycleCounterValue;
    }

    public void setIgnitionCycleCounterValue(int ignitionCycleCounterValue) {
        this.ignitionCycleCounterValue = ignitionCycleCounterValue;
    }

    public List<CalibrationInformation> getCalibrationInformation() {
        return calibrationInformation;
    }

    public void setCalibrationInformation(List<CalibrationInformation> calibrationInformation) {
        this.calibrationInformation.clear();
        this.calibrationInformation.addAll(calibrationInformation);
    }

    public ComponentIdentification getComponentIdentification() {
        return componentIdentification;
    }

    public void setComponentIdentification(ComponentIdentification componentIdentification) {
        this.componentIdentification = componentIdentification;
    }

    public List<SupportedSPN> getDataStreamSPNs() {
        return getSupportedSPNs().stream()
                                 .filter(SupportedSPN::supportsDataStream)
                                 .collect(Collectors.toList());
    }

    /**
     * Returns the List of SupportedSPNs filtering out 'dis-allowed' SPNs
     */
    public List<SupportedSPN> getFilteredDataStreamSPNs() {
        return getDataStreamSPNs().stream()
                                  .filter(s -> !getOmittedDataStreamSPNs().contains(s.getSpn()))
                                  .collect(Collectors.toList());
    }

    public List<Integer> getOmittedDataStreamSPNs() {
        return omittedSPNs.stream().distinct().sorted().collect(Collectors.toList());
    }

    public void addOmittedDataStreamSPN(int spn) {
        omittedSPNs.add(spn);
    }

    public List<SupportedSPN> getFreezeFrameSPNs() {
        return getSupportedSPNs().stream()
                                 .filter(SupportedSPN::supportsExpandedFreezeFrame)
                                 .collect(Collectors.toList());
    }

    public int getFunction() {
        return function;
    }

    public void setFunction(int function) {
        this.function = function;
    }

    public List<MonitoredSystem> getMonitoredSystems() {
        return monitoredSystems;
    }

    public void setMonitoredSystems(List<MonitoredSystem> monitoredSystems) {
        this.monitoredSystems.clear();
        this.monitoredSystems.addAll(monitoredSystems);
    }

    public byte getObdCompliance() {
        return obdCompliance;
    }

    public void setObdCompliance(byte obdCompliance) {
        this.obdCompliance = obdCompliance;
    }

    public Set<PerformanceRatio> getPerformanceRatios() {
        return performanceRatios;
    }

    public void setPerformanceRatios(Collection<PerformanceRatio> performanceRatios) {
        this.performanceRatios.clear();
        this.performanceRatios.addAll(performanceRatios);
    }

    public List<ScaledTestResult> getScaledTestResults() {
        return scaledTestResults;
    }

    public void setScaledTestResults(List<ScaledTestResult> scaledTestResults) {
        this.scaledTestResults.clear();
        this.scaledTestResults.addAll(scaledTestResults);
    }

    public int getSourceAddress() {
        return sourceAddress;
    }

    public String getModuleName() {
        return Lookup.getAddressName(getSourceAddress());
    }

    public List<SupportedSPN> getSupportedSPNs() {
        return (get(DM24SPNSupportPacket.class) == null ? supportedSPNs
                : get(DM24SPNSupportPacket.class).getSupportedSpns()).stream()
                                                                     .sorted(Comparator.comparingInt(SupportedSPN::getSpn))
                                                                     .collect(Collectors.toList());
    }

    public void setSupportedSPNs(List<SupportedSPN> supportedSPNs) {
        this.supportedSPNs.clear();
        this.supportedSPNs.addAll(supportedSPNs);
    }

    public List<SupportedSPN> getTestResultSPNs() {
        return getSupportedSPNs().stream()
                                 .filter(SupportedSPN::supportsScaledTestResults)
                                 .collect(Collectors.toList());
    }

    public List<ScaledTestResult> getNonInitializedTests() {
        return nonInitializedTests;
    }

    public void setNonInitializedTests(List<ScaledTestResult> tests) {
        nonInitializedTests.clear();
        nonInitializedTests.addAll(tests);
    }

    public String getEngineFamilyName() {
        return engineFamilyName;
    }

    public void setEngineFamilyName(String engineFamilyName) {
        this.engineFamilyName = engineFamilyName;
    }

    public String getModelYear() {
        return modelYear;
    }

    public void setModelYear(String modelYear) {
        this.modelYear = modelYear;
    }

    public <T extends GenericPacket> void remove(Class<T> clazz) {
        packetArchive.remove(clazz);
    }

    public void set(GenericPacket packet) {
        packetArchive.put(packet);
    }

    public <T extends GenericPacket> T get(Class<T> clazz) {
        return packetArchive.get(clazz);
    }

    public boolean supportsDM27() {
        return get(DM27AllPendingDTCsPacket.class) != null;
    }

    private static class PacketArchive {

        private final Map<Class<? extends GenericPacket>, GenericPacket> packetArchive = new HashMap<>();

        public void put(GenericPacket packet) {
            packetArchive.put(packet.getClass(), packet);
        }

        public <T extends GenericPacket> T get(Class<T> clazz) {
            return (T) packetArchive.get(clazz);
        }

        public <T extends GenericPacket> void remove(Class<T> clazz) {
            packetArchive.remove(clazz);
        }
    }

}
