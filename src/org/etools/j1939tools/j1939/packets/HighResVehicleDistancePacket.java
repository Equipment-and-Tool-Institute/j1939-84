/*
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939tools.j1939.packets;

import org.etools.j1939tools.bus.Packet;

/**
 * The {@link ParsedPacket} responsible for translating Total Vehicle Distance
 * (High Resolution) (SPN 917)
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class HighResVehicleDistancePacket extends GenericPacket {

    public static final int PGN = 65217;

    private final double distance;

    public HighResVehicleDistancePacket(Packet packet) {
        super(packet);
        distance = getScaledIntValue(0, 200);
    }

    @Override
    public String getName() {
        return "High Resolution Vehicle Distance";
    }

    /**
     * Returns the Total Vehicle Distance in kilometers
     *
     * @return the vehicle distance in km
     */
    public double getTotalVehicleDistance() {
        return distance;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
