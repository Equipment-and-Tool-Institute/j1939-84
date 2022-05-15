/*
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939tools.j1939.packets;


import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939tools.utils.CollectionUtils.join;

import org.etools.j1939tools.bus.Packet;

/**
 * Parses the DM21 Diagnostic Readiness Packet
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 */
public class DM21DiagnosticReadinessPacket extends GenericPacket {

    public static final int PGN = 49408; // 0xC100

    public DM21DiagnosticReadinessPacket(Packet packet) {
        super(packet);
    }

    public static DM21DiagnosticReadinessPacket create(int source,
                                                       int destination,
                                                       int kmWithMIL,
                                                       int kmSinceCodeClear,
                                                       int minutesWithMIL,
                                                       int minutesSinceCodeClear) {
        byte[] bytes = join(to2Bytes(kmWithMIL),
                            to2Bytes(kmSinceCodeClear),
                            to2Bytes(minutesWithMIL),
                            to2Bytes(minutesSinceCodeClear));
        return new DM21DiagnosticReadinessPacket(Packet.create(PGN | destination, source, bytes));
    }

    private String getDistanceSinceDTCsClearedAsString() {
        return getValuesWithUnits(getKmSinceDTCsCleared(), "km", getMilesSinceDTCsCleared(), "mi");
    }

    private String getDistanceWithMILActiveAsString() {
        return getValuesWithUnits(getKmWhileMILIsActivated(), "km", getMilesWhileMILIsActivated(), "mi");
    }

    /**
     * Returns the total number of kilometers the vehicle has traveled since the
     * Diagnostic Trouble Codes were last cleared
     *
     * @return kilometers as a double
     */
    public double getKmSinceDTCsCleared() {
        return getScaledShortValue(2, 1);
    }

    /**
     * Returns the total number of kilometers the vehicle has traveled while the
     * Malfunction Indicator Lamp has been active
     *
     * @return kilometers as a double
     */
    public double getKmWhileMILIsActivated() {
        return getScaledShortValue(0, 1);
    }

    /**
     * Returns the total number of miles the vehicle has traveled since the
     * Diagnostic Trouble Codes were last cleared
     *
     * @return miles as a double
     */
    public double getMilesSinceDTCsCleared() {
        return getKmSinceDTCsCleared() * KM_TO_MILES_FACTOR;
    }

    /**
     * Returns the total number of miles the vehicle has traveled while the
     * Malfunction Indicator Lamp has been active
     *
     * @return miles as a double
     */
    public double getMilesWhileMILIsActivated() {
        return getKmWhileMILIsActivated() * KM_TO_MILES_FACTOR;
    }

    /**
     * Returns the total number of minutes the engine has been running since the
     * Diagnostic Trouble Codes were last cleared.
     *
     * @return minutes as a double
     */
    public double getMinutesSinceDTCsCleared() {
        return getScaledShortValue(6, 1);
    }

    /**
     * Returns the total number of minutes the engine has been running while the
     * Malfunction Indicator Lamp has been active
     *
     * @return minutes as a double
     */
    public double getMinutesWhileMILIsActivated() {
        return getScaledShortValue(4, 1);
    }

    @Override
    public String getName() {
        return "DM21";
    }

    @Override
    public String toString() {
        String result = getStringPrefix() + "[" + NL;
        result += "  Distance Traveled While MIL is Activated:     " + getDistanceWithMILActiveAsString() + NL;
        result += "  Time Run by Engine While MIL is Activated:    " + getTimeWithMILActiveAsString() + NL;
        result += "  Distance Since DTCs Cleared:                  " + getDistanceSinceDTCsClearedAsString() + NL;
        result += "  Time Since DTCs Cleared:                      " + getTimeSinceDTCsClearedAsString() + NL;
        result += "]";
        return result;
    }

    private String getTimeSinceDTCsClearedAsString() {
        return getValueWithUnits(getMinutesSinceDTCsCleared(), "minutes");
    }

    private String getTimeWithMILActiveAsString() {
        return getValueWithUnits(getMinutesWhileMILIsActivated(), "minutes");
    }
}
