/*
 * Copyright (c) 2022. Equipment & Tool Institute
 */
package org.etools.j1939tools.j1939.packets;

import static org.etools.j1939_84.J1939_84.NL;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.etools.j1939tools.bus.Packet;
import org.etools.j1939tools.j1939.model.ObdCompliance;

/**
 * The {@link ParsedPacket} for Diagnostic Readiness #1 (DM5)
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 */
public class DM5DiagnosticReadinessPacket extends DiagnosticReadinessPacket {

    public static final int PGN = 65230; // 0xFECE
    private static final List<Byte> notObdValues = Arrays.asList(new Byte[] { 0, 5, (byte) 0xFB, (byte) 0xFC,
            (byte) 0xFD, (byte) 0xFE, (byte) 0xFF });
    private final byte activeCount;
    private final byte obdCompliance;
    private final byte previousCount;

    public byte getContinuouslyMonSysStat() {
        return continuouslyMonSysStat;
    }

    private final byte continuouslyMonSysStat;

    public DM5DiagnosticReadinessPacket(Packet packet) {
        super(packet);
        activeCount = getByte(0);
        previousCount = getByte(1);
        obdCompliance = getByte(2);
        continuouslyMonSysStat = getByte(4);
    }

    public static DM5DiagnosticReadinessPacket create(int sourceAddress,
                                                      int activeCount,
                                                      int previouslyActiveCount,
                                                      int obdCompliance) {
        return create(sourceAddress, activeCount, previouslyActiveCount, obdCompliance, List.of(), List.of());
    }

    public static DM5DiagnosticReadinessPacket create(int sourceAddress,
                                                      int activeCount,
                                                      int previouslyActiveCount,
                                                      int obdCompliance,
                                                      List<CompositeSystem> supportedSystems,
                                                      List<CompositeSystem> completeSystems) {
        int[] data = new int[8];
        data[0] = (byte) activeCount;
        data[1] = (byte) previouslyActiveCount;
        data[2] = (byte) obdCompliance;

        for (CompositeSystem systemId : CompositeSystem.values()) {
            boolean isEnabled = supportedSystems.contains(systemId);
            boolean isComplete = completeSystems.contains(systemId);
            populateData(systemId, isComplete, isEnabled, data);
        }

        return new DM5DiagnosticReadinessPacket(Packet.create(PGN, sourceAddress, data));
    }

    /**
     * Returns the number of active DTCs
     *
     * @return byte
     */
    public byte getActiveCodeCount() {
        return activeCount;
    }

    @Override
    public String getName() {
        return "DM5";
    }

    @Override
    public String toString() {
        byte obd = getOBDCompliance();
        return getStringPrefix() + "OBD Compliance: " + ObdCompliance.resolveObdCompliance(obd) + " (" + (obd & 0xFF)
                + "), "
                + "Active Codes: " + getValueWithUnits(getActiveCodeCount(), null) + ", Previously Active Codes: "
                + getValueWithUnits(getPreviouslyActiveCodeCount(), null)
                + NL + "Continuously Monitored System Support/Status:"
                + NL + getContinuouslyMonitoredSystems().stream()
                                                        .sorted()
                                                        .map(t -> t.toString())
                                                        .collect(Collectors.joining(NL))
                + NL + "Non-continuously Monitored System Support/Status:"
                + NL + super.getNonContinuouslyMonitoredSystems().stream()
                                                                 .sorted()
                                                                 .map(t -> t.toString())
                                                                 .collect(Collectors.joining(NL));
    }

    /**
     * Returns the value of the OBD Compliance
     *
     * @return byte
     */
    public byte getOBDCompliance() {
        return obdCompliance;
    }

    /**
     * Returns the number of previously active DTCs
     *
     * @return byte
     */
    public byte getPreviouslyActiveCodeCount() {
        return previousCount;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getActiveCodeCount(), getPreviouslyActiveCodeCount(), getOBDCompliance(), super.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof DM5DiagnosticReadinessPacket)) {
            return false;
        }

        DM5DiagnosticReadinessPacket that = (DM5DiagnosticReadinessPacket) obj;
        return getActiveCodeCount() == that.getActiveCodeCount()
                && getPreviouslyActiveCodeCount() == that.getPreviouslyActiveCodeCount()
                && getOBDCompliance() == that.getOBDCompliance() && super.equals(obj);
    }

    /**
     * Returns true if this module reported that it supports HD OBD
     *
     * @return boolean
     */
    public boolean isHdObd() {
        return getOBDCompliance() == 19 || getOBDCompliance() == 20;
    }

    public boolean isObd() {
        return !notObdValues.contains(getOBDCompliance());
    }

    @Override
    public List<Value> getSpnValues(int spnId) {
        switch (spnId) {
            case 1218:
            case 1219:
            case 1220:
                return List.of(getSpn(spnId).map(s -> (Value) new SpnValue(s)).orElseThrow());
            case 1221:
                return getContinuouslyMonitoredSystems().stream()
                                                        .flatMap(s -> Stream.of(s.getSupportValue(),
                                                                                s.getStatusValue()))
                                                        .collect(Collectors.toList());
            case 1222:
                return getNonContinuouslyMonitoredSystems().stream()
                                                           .flatMap(s -> Stream.of(s.getSupportValue(),
                                                                                   s.getStatusValue()))
                                                           .collect(Collectors.toList());
        }
        throw new IllegalArgumentException("Invalid SPN:" + spnId);
    }
}
