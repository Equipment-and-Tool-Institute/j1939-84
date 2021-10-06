/*
 * Copyright 2019 Equipment & Tool Institute
 */
package net.solidDesign.j1939.packets;

import java.util.List;
import java.util.Objects;

import org.etools.j1939_84.bus.Packet;

/**
 * The {@link ParsedPacket} for Trip Diagnostic Readiness (DM26)
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class DM26TripDiagnosticReadinessPacket extends DiagnosticReadinessPacket {

    public static final int PGN = 64952; // 0xFDB8

    public static DM26TripDiagnosticReadinessPacket create(int address, int secondsSCC, int warmUpsSCC) {
        return DM26TripDiagnosticReadinessPacket.create(address, secondsSCC, warmUpsSCC, List.of(), List.of());
    }

    public static DM26TripDiagnosticReadinessPacket create(int address,
                                                           int secondsSCC,
                                                           int warmUpsSCC,
                                                           List<CompositeSystem> enabledSystems,
                                                           List<CompositeSystem> completeSystems) {
        int[] data = new int[8];
        data[0] = secondsSCC & 0xFF;
        data[1] = (secondsSCC >> 8) & 0xFF;
        data[2] = warmUpsSCC & 0xFF;

        for (CompositeSystem systemId : CompositeSystem.values()) {
            boolean isEnabled = enabledSystems.contains(systemId);
            boolean isComplete = completeSystems.contains(systemId);
            populateData(systemId, isComplete, isEnabled, data);
        }

        return new DM26TripDiagnosticReadinessPacket(Packet.create(PGN, address, data));
    }

    private final double timeRunning;
    private final byte warmUps;

    public DM26TripDiagnosticReadinessPacket(Packet packet) {
        super(packet);
        warmUps = getByte(2);
        timeRunning = getScaledShortValue(0, 1.0);
    }

    @Override
    public String getName() {
        return "DM26";
    }

    @Override
    public String toString() {
        return getStringPrefix() + "Warm-ups: " + getValueWithUnits(getWarmUpsSinceClear(), null)
                + ", Time Since Engine Start: " + getValueWithUnits(getTimeSinceEngineStart(), "seconds");
    }

    /**
     * Returns the number of seconds the engine has been running since the key
     * was turned on
     *
     * @return int
     */
    public double getTimeSinceEngineStart() {
        return timeRunning;
    }

    /**
     * Returns the Number of Warm-up cycles since all DTCs were cleared
     *
     * @return int
     */
    public byte getWarmUpsSinceClear() {
        return warmUps;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getWarmUpsSinceClear(), getTimeSinceEngineStart(), super.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof DM26TripDiagnosticReadinessPacket)) {
            return false;
        }

        DM26TripDiagnosticReadinessPacket that = (DM26TripDiagnosticReadinessPacket) obj;
        return getWarmUpsSinceClear() == that.getWarmUpsSinceClear()
                && getTimeSinceEngineStart() == that.getTimeSinceEngineStart() && super.equals(obj);
    }

}
