/**
 * Copyright (c) 2019. Equipment & Tool Institute
 */
package org.etools.j1939_84.model;

import java.util.List;
import java.util.stream.Collectors;

import org.etools.j1939_84.bus.j1939.packets.SupportedSPN;

/**
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class OBDModuleInformation {

    private byte obdCompliance;

    private final int sourceAddress;

    private List<SupportedSPN> supportedSpns;

    /**
     *
     */
    public OBDModuleInformation(int sourceAddress) {
        this.sourceAddress = sourceAddress;
    }

    public List<SupportedSPN> getDataStreamSpns() {
        return getSupportedSpns().stream().filter(s -> s.supportsDataStream()).collect(Collectors.toList());
    }

    public List<SupportedSPN> getFreezeFrameSpns() {
        return getSupportedSpns().stream().filter(s -> s.supportsExpandedFreezeFrame()).collect(Collectors.toList());
    }

    /**
     * @return the obdCompliance
     */
    public byte getObdCompliance() {
        return obdCompliance;
    }

    /**
     * @return the supportedSpns
     */
    public List<SupportedSPN> getSupportedSpns() {
        return supportedSpns;
    }

    public List<SupportedSPN> getTestResultSpns() {
        return getSupportedSpns().stream().filter(s -> s.supportsScaledTestResults()).collect(Collectors.toList());
    }

    /**
     * @param obdCompliance the obdCompliance to set
     */
    public void setObdCompliance(byte obdCompliance) {
        this.obdCompliance = obdCompliance;
    }

    /**
     * @param supportedSpns the supportedSpns to set
     */
    public void setSupportedSpns(List<SupportedSPN> supportedSpns) {
        this.supportedSpns = supportedSpns;
    }

}
