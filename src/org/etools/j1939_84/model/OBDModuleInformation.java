/**
 * Copyright (c) 2019. Equipment & Tool Institute
 */
package org.etools.j1939_84.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.etools.j1939_84.bus.j1939.packets.DM19CalibrationInformationPacket.CalibrationInformation;
import org.etools.j1939_84.bus.j1939.packets.PerformanceRatio;
import org.etools.j1939_84.bus.j1939.packets.SupportedSPN;

/**
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class OBDModuleInformation {

    private List<CalibrationInformation> calibrationInformation;

    private int function;

    private byte obdCompliance;

    private final Set<PerformanceRatio> performanceRatios = new HashSet<>();

    private final int sourceAddress;

    private List<SupportedSPN> supportedSpns;

    public OBDModuleInformation(int sourceAddress) {
        this.sourceAddress = sourceAddress;
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
                && Objects.equals(obdCompliance, that.obdCompliance)
                && Objects.equals(function, that.function)
                && Objects.equals(calibrationInformation, that.calibrationInformation);
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
        return getSupportedSpns().stream().filter(s -> s.supportsDataStream()).collect(Collectors.toList());
    }

    /**
     * @return the freezeFrameSpns
     */
    public List<SupportedSPN> getFreezeFrameSpns() {
        return getSupportedSpns().stream().filter(s -> s.supportsExpandedFreezeFrame()).collect(Collectors.toList());
    }

    /**
     * @return the function
     */
    public int getFunction() {
        return function;
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

    public int getSourceAddress() {
        return sourceAddress;
    }

    /**
     * @return the supportedSpns
     */
    public List<SupportedSPN> getSupportedSpns() {
        if (supportedSpns == null) {
            supportedSpns = new ArrayList<>();
        }
        return supportedSpns;
    }

    public List<SupportedSPN> getTestResultSpns() {
        return getSupportedSpns().stream().filter(s -> s.supportsScaledTestResults()).collect(Collectors.toList());
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceAddress,
                supportedSpns,
                obdCompliance,
                calibrationInformation);
    }

    /**
     * @param calibrationInformation the calibrationInformation to set
     */
    public void setCalibrationInformation(List<CalibrationInformation> calibrationInformation) {
        this.calibrationInformation = calibrationInformation;
    }

    /**
     * @param function the function to set
     */
    public void setFunction(int function) {
        this.function = function;
    }

    /**
     * @param obdCompliance the obdCompliance to set
     */
    public void setObdCompliance(byte obdCompliance) {
        this.obdCompliance = obdCompliance;
    }

    /**
     * @param performanceRatios the performanceRatios to set
     */
    public void setPerformanceRatios(Collection<PerformanceRatio> performanceRatios) {
        this.performanceRatios.clear();
        this.performanceRatios.addAll(performanceRatios);
    }

    /**
     * @param supportedSpns the supportedSpns to set
     */
    public void setSupportedSpns(List<SupportedSPN> supportedSpns) {
        this.supportedSpns = supportedSpns;
    }

    @Override
    public String toString() {
        String result = "OBD Module Information:\n";
        result += "sourceAddress is : " + sourceAddress + "\n";
        result += "obdCompliance is : " + getObdCompliance() + "\n";
        result += "function is : " + getFunction() + "\n";
        result += "Supported SPNs: \n"
                + getSupportedSpns().stream().map(i -> i.toString()).collect(Collectors.joining(","));
        return result;
    }

}
