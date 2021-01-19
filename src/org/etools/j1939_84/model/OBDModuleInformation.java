/*
 * Copyright (c) 2019. Equipment & Tool Institute
 */
package org.etools.j1939_84.model;

import static org.etools.j1939_84.J1939_84.NL;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.etools.j1939_84.bus.j1939.packets.DM19CalibrationInformationPacket.CalibrationInformation;
import org.etools.j1939_84.bus.j1939.packets.MonitoredSystem;
import org.etools.j1939_84.bus.j1939.packets.PerformanceRatio;
import org.etools.j1939_84.bus.j1939.packets.ScaledTestResult;
import org.etools.j1939_84.bus.j1939.packets.SupportedSPN;

/**
 * @author Matt Gumbel (matt@soliddesign.net)
 */
public class OBDModuleInformation implements Cloneable {

    private final List<CalibrationInformation> calibrationInformation = new ArrayList<>();

    private int function;

    public int getIgnitionCycleCounterValue() {
        return ignitionCycleCounterValue;
    }

    public void setIgnitionCycleCounterValue(int ignitionCycleCounterValue) {
        this.ignitionCycleCounterValue = ignitionCycleCounterValue;
    }

    private int ignitionCycleCounterValue;

    private final Set<MonitoredSystem> monitoredSystems = new HashSet<>();

    private byte obdCompliance;

    private final Set<PerformanceRatio> performanceRatios = new HashSet<>();

    private final List<ScaledTestResult> scaledTestResults = new ArrayList<>();

    private final int sourceAddress;

    private final List<SupportedSPN> supportedSpns = new ArrayList<>();

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
        obdInfo.setFunction(getFunction());
        obdInfo.setObdCompliance(getObdCompliance());
        obdInfo.setPerformanceRatios(getPerformanceRatios());
        obdInfo.setScaledTestResults(getScaledTestResults());
        obdInfo.setSupportedSpns(getSupportedSpns());
        obdInfo.setMonitoredSystems(getMonitoredSystems());

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
        return Objects.equals(sourceAddress, that.sourceAddress)
                && Objects.equals(supportedSpns, that.supportedSpns)
                && Objects.equals(scaledTestResults, that.scaledTestResults)
                && Objects.equals(obdCompliance, that.obdCompliance)
                && Objects.equals(function, that.function)
                && Objects.equals(calibrationInformation, that.calibrationInformation)
                && Objects.equals(monitoredSystems, that.monitoredSystems);
    }

    /**
     * @return the calibrationInformation
     */
    public List<CalibrationInformation> getCalibrationInformation() {
        return calibrationInformation;
    }

    /**
     * @return the supportedSpns
     */
    public List<SupportedSPN> getDataStreamSpns() {
        return getSupportedSpns().stream().filter(SupportedSPN::supportsDataStream).collect(Collectors.toList());
    }

    /**
     * @return the freezeFrameSpns
     */
    public List<SupportedSPN> getFreezeFrameSpns() {
        return getSupportedSpns().stream()
                .filter(SupportedSPN::supportsExpandedFreezeFrame)
                .collect(Collectors.toList());
    }

    /**
     * @return the function
     */
    public int getFunction() {
        return function;
    }

    /**
     * @return the monitoredSystems
     */
    public Set<MonitoredSystem> getMonitoredSystems() {
        return monitoredSystems;
    }

    /**
     * @return the obdCompliance
     */
    public byte getObdCompliance() {
        return obdCompliance;
    }

    /**
     * @return the performanceRatios
     */
    public Set<PerformanceRatio> getPerformanceRatios() {
        return performanceRatios;
    }

    public List<ScaledTestResult> getScaledTestResults() {
        return scaledTestResults;
    }

    public int getSourceAddress() {
        return sourceAddress;
    }

    /**
     * @return the supportedSpns
     */
    public List<SupportedSPN> getSupportedSpns() {
        return supportedSpns;
    }

    public List<SupportedSPN> getTestResultSpns() {
        return getSupportedSpns().stream().filter(SupportedSPN::supportsScaledTestResults).collect(Collectors.toList());
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceAddress,
                            scaledTestResults,
                            supportedSpns,
                            obdCompliance,
                            calibrationInformation,
                            monitoredSystems);
    }

    /**
     * @param calibrationInformation
     *         the calibrationInformation to set
     */
    public void setCalibrationInformation(List<CalibrationInformation> calibrationInformation) {
        this.calibrationInformation.clear();
        this.calibrationInformation.addAll(calibrationInformation);
    }

    /**
     * @param function
     *         the function to set
     */
    public void setFunction(int function) {
        this.function = function;
    }

    /**
     * @param monitoredSystems
     *         the calibrationInformation to set
     */
    public void setMonitoredSystems(Set<MonitoredSystem> monitoredSystems) {
        this.monitoredSystems.clear();
        this.monitoredSystems.addAll(monitoredSystems);
    }

    /**
     * @param obdCompliance
     *         the obdCompliance to set
     */
    public void setObdCompliance(byte obdCompliance) {
        this.obdCompliance = obdCompliance;
    }

    /**
     * @param performanceRatios
     *         the performanceRatios to set
     */
    public void setPerformanceRatios(Collection<PerformanceRatio> performanceRatios) {
        this.performanceRatios.clear();
        this.performanceRatios.addAll(performanceRatios);
    }

    public void setScaledTestResults(List<ScaledTestResult> scaledTestResults) {
        this.scaledTestResults.clear();
        this.scaledTestResults.addAll(scaledTestResults);
    }

    /**
     * @param supportedSpns
     *         the supportedSpns to set
     */
    public void setSupportedSpns(List<SupportedSPN> supportedSpns) {
        this.supportedSpns.clear();
        this.supportedSpns.addAll(supportedSpns);
    }

    @Override
    public String toString() {
        String result = "OBD Module Information: " + NL;
        result += "sourceAddress is : " + sourceAddress + NL;
        result += "obdCompliance is : " + getObdCompliance() + NL;
        result += "function is : " + getFunction() + NL;
        result += "Supported SPNs: " + NL
                + getSupportedSpns().stream().map(SupportedSPN::toString).collect(Collectors.joining(","));
        return result;
    }

}
