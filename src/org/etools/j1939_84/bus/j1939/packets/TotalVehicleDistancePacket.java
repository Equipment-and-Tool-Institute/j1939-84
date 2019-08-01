/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.j1939.packets;

import org.etools.j1939_84.bus.Packet;

/**
 * The {@link ParsedPacket} responsible for translating Total Vehicle Distance
 * (SPN 245)
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class TotalVehicleDistancePacket extends ParsedPacket {

    public static final int PGN = 65248;

    private final double distance;

    public TotalVehicleDistancePacket(Packet packet) {
        super(packet);
        distance = getScaledIntValue(4, 8.0);
    }

    @Override
    public String getName() {
        return "Total Vehicle Distance";
    }

    /**
     * Returns the Total Vehicle Distance in kilometers
     *
     * @return the vehicle distance in km
     */
    public double getTotalVehicleDistance() {
        return distance;
    }

    /**
     * Returns the Total Vehicle Distance in miles
     *
     * @return the vehicle distance in miles
     */
    public double getTotalVehicleDistanceAsMiles() {
        return getTotalVehicleDistance() * KM_TO_MILES_FACTOR;
    }

    private String getVehicleDistanceAsString() {
        return getValuesWithUnits(getTotalVehicleDistance(), "km", getTotalVehicleDistanceAsMiles(), "mi");
    }

    @Override
    public String toString() {
        return getStringPrefix() + getVehicleDistanceAsString();
    }

}
