/*
 * Copyright (c) 2019. Equipment & Tool Institute
 */
package org.etools.j1939_84.model;

import static org.etools.j1939_84.J1939_84.NL;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.etools.j1939_84.bus.j1939.packets.DM19CalibrationInformationPacket.CalibrationInformation;
import org.etools.j1939_84.bus.j1939.packets.DM24SPNSupportPacket;
import org.etools.j1939_84.bus.j1939.packets.DM25ExpandedFreezeFrame;
import org.etools.j1939_84.bus.j1939.packets.DM26TripDiagnosticReadinessPacket;
import org.etools.j1939_84.bus.j1939.packets.DM27AllPendingDTCsPacket;
import org.etools.j1939_84.bus.j1939.packets.DiagnosticTroubleCode;
import org.etools.j1939_84.bus.j1939.packets.MonitoredSystem;
import org.etools.j1939_84.bus.j1939.packets.PerformanceRatio;
import org.etools.j1939_84.bus.j1939.packets.ScaledTestResult;
import org.etools.j1939_84.bus.j1939.packets.SupportedSPN;

/**
 * @author Matt Gumbel (matt@soliddesign.net)
 */
public class OBDModuleInformation implements Cloneable {

    private final List<CalibrationInformation> calibrationInformation = new ArrayList<>();

    private ComponentIdentification componentIdentification = null;

    private int function;

    private int ignitionCycleCounterValue;

    private final List<MonitoredSystem> monitoredSystems = new ArrayList<>();

    private byte obdCompliance;

    private final Set<PerformanceRatio> performanceRatios = new HashSet<>();

    private final List<ScaledTestResult> scaledTestResults = new ArrayList<>();

    private final int sourceAddress;

    private final List<SupportedSPN> supportedSpns = new ArrayList<>();

    private String engineFamilyName = "";

    private String modelYear = "";

    private DM25ExpandedFreezeFrame lastDM25;

    private DM26TripDiagnosticReadinessPacket lastDM26;

    private DM27AllPendingDTCsPacket lastDM27;

    private DM24SPNSupportPacket dm24;

    private final List<DiagnosticTroubleCode> emissionDTCs = new ArrayList<>();

    /** These SPNs represent SP which appear in multiple PGs. */
    private final List<Integer> omittedSPNs = new ArrayList<>(List.of(588, 1213, 1220, 12675, 12730, 12783, 12797));

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
        obdInfo.setSupportedSpns(getSupportedSpns());
        obdInfo.setEngineFamilyName(getEngineFamilyName());
        obdInfo.setModelYear(getModelYear());
        obdInfo.setLastDM26(getLastDM26());
        obdInfo.setLastDM27(getLastDM27());
        obdInfo.setEmissionDTCs(getEmissionDTCs());
        obdInfo.setDm24(getDm24());

        return obdInfo;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof OBDModuleInformation)) {
            return false;
        }

        OBDModuleInformation that = (OBDModuleInformation) obj;

        return Objects.equals(calibrationInformation, that.calibrationInformation)
                && Objects.equals(this.componentIdentification, that.componentIdentification)
                && Objects.equals(function, that.function)
                && Objects.equals(ignitionCycleCounterValue, that.ignitionCycleCounterValue)
                && Objects.equals(monitoredSystems, that.monitoredSystems)
                && Objects.equals(obdCompliance, that.obdCompliance)
                && Objects.equals(performanceRatios, that.performanceRatios)
                && Objects.equals(scaledTestResults, that.scaledTestResults)
                && Objects.equals(sourceAddress, that.sourceAddress)
                && Objects.equals(supportedSpns, that.supportedSpns)
                && Objects.equals(engineFamilyName, that.engineFamilyName)
                && Objects.equals(modelYear, that.modelYear)
                && Objects.equals(lastDM25, that.lastDM25)
                && Objects.equals(lastDM26, that.lastDM26)
                && Objects.equals(lastDM27, that.lastDM27)
                && Objects.equals(emissionDTCs, that.emissionDTCs)
                && Objects.equals(dm24, that.dm24);
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

    public ComponentIdentification getComponentIdentification() {
        return componentIdentification;
    }

    public List<SupportedSPN> getDataStreamSpns() {
        return getSupportedSpns().stream()
                .filter(SupportedSPN::supportsDataStream)
                .collect(Collectors.toList());
    }

    /**
     * Returns the List of SupportedSPNs filtering out 'dis-allowed' SPNs
     */
    public List<SupportedSPN> getFilteredDataStreamSPNs() {
        return getDataStreamSpns().stream()
                .filter(s -> !getOmittedDataStreamSPNs().contains(s.getSpn()))
                .collect(Collectors.toList());
    }

    public List<Integer> getOmittedDataStreamSPNs() {
        return omittedSPNs.stream().distinct().sorted().collect(Collectors.toList());
    }

    public void addOmittedDataStreamSPN(int spn) {
        omittedSPNs.add(spn);
    }

    public List<SupportedSPN> getFreezeFrameSpns() {
        return getSupportedSpns().stream()
                .filter(SupportedSPN::supportsExpandedFreezeFrame)
                .collect(Collectors.toList());
    }

    public int getFunction() {
        return function;
    }

    public List<MonitoredSystem> getMonitoredSystems() {
        return monitoredSystems;
    }

    public byte getObdCompliance() {
        return obdCompliance;
    }

    public Set<PerformanceRatio> getPerformanceRatios() {
        return performanceRatios;
    }

    public List<ScaledTestResult> getScaledTestResults() {
        return scaledTestResults;
    }

    public int getSourceAddress() {
        return sourceAddress;
    }

    public List<SupportedSPN> getSupportedSpns() {
        return (dm24 == null ? supportedSpns : dm24.getSupportedSpns()).stream()
                .sorted(Comparator.comparingInt(SupportedSPN::getSpn))
                .collect(Collectors.toList());
    }

    public List<SupportedSPN> getTestResultSpns() {
        return getSupportedSpns().stream()
                .filter(SupportedSPN::supportsScaledTestResults)
                .collect(Collectors.toList());
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceAddress,
                            calibrationInformation,
                            function,
                            ignitionCycleCounterValue,
                            monitoredSystems,
                            obdCompliance,
                            performanceRatios,
                            scaledTestResults,
                            supportedSpns,
                            engineFamilyName,
                            modelYear,
                            lastDM25,
                            lastDM26,
                            lastDM27,
                            emissionDTCs,
                            dm24);
    }

    public void setCalibrationInformation(List<CalibrationInformation> calibrationInformation) {
        this.calibrationInformation.clear();
        this.calibrationInformation.addAll(calibrationInformation);
    }

    public void setComponentIdentification(ComponentIdentification componentIdentification) {
        this.componentIdentification = componentIdentification;
    }

    public void setFunction(int function) {
        this.function = function;
    }

    public void setMonitoredSystems(List<MonitoredSystem> monitoredSystems) {
        this.monitoredSystems.clear();
        this.monitoredSystems.addAll(monitoredSystems);
    }

    public void setObdCompliance(byte obdCompliance) {
        this.obdCompliance = obdCompliance;
    }

    public void setPerformanceRatios(Collection<PerformanceRatio> performanceRatios) {
        this.performanceRatios.clear();
        this.performanceRatios.addAll(performanceRatios);
    }

    public void setScaledTestResults(List<ScaledTestResult> scaledTestResults) {
        this.scaledTestResults.clear();
        this.scaledTestResults.addAll(scaledTestResults);
    }

    public void setSupportedSpns(List<SupportedSPN> supportedSpns) {
        this.supportedSpns.clear();
        this.supportedSpns.addAll(supportedSpns);
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

    public DM25ExpandedFreezeFrame getLastDM25() {
        return lastDM25;
    }

    public void setLastDM25(DM25ExpandedFreezeFrame lastDM25) {
        this.lastDM25 = lastDM25;
    }

    public DM26TripDiagnosticReadinessPacket getLastDM26() {
        return lastDM26;
    }

    public void setLastDM26(DM26TripDiagnosticReadinessPacket lastDM26) {
        this.lastDM26 = lastDM26;
    }

    public DM27AllPendingDTCsPacket getLastDM27() {
        return lastDM27;
    }

    public void setLastDM27(DM27AllPendingDTCsPacket lastDM27) {
        this.lastDM27 = lastDM27;
    }

    public List<DiagnosticTroubleCode> getEmissionDTCs() {
        return emissionDTCs;
    }

    public void setEmissionDTCs(List<DiagnosticTroubleCode> emissionDTCs) {
        this.emissionDTCs.clear();
        this.emissionDTCs.addAll(emissionDTCs);
    }

    public DM24SPNSupportPacket getDm24() {
        return dm24;
    }

    public void setDm24(DM24SPNSupportPacket dm24) {
        this.dm24 = dm24;
    }

    @Override
    public String toString() {
        String result = "OBD Module Information: " + NL;
        result += "sourceAddress is : " + sourceAddress + NL;
        result += "obdCompliance is : " + getObdCompliance() + NL;
        result += "function is : " + getFunction() + NL;
        result += "ignition cycles is : " + getIgnitionCycleCounterValue() + NL;
        result += "engine family name is : " + getEngineFamilyName() + NL;
        result += "model year is : " + getModelYear() + NL;
        result += "Scaled Test Results: " + getScaledTestResults() + NL;
        result += "Performance Ratios: " + getPerformanceRatios() + NL;
        result += "Monitored Systems: " + getMonitoredSystems() + NL;
        result += "Supported SPNs: " + NL + formattedSupportedSpns();
        return result;
    }

    private String formattedSupportedSpns() {
        return getSupportedSpns().stream().map(SupportedSPN::toString).collect(Collectors.joining(", "));
    }

}
