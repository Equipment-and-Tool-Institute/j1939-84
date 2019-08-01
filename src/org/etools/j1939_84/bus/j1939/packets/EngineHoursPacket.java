/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.j1939.packets;

import org.etools.j1939_84.bus.Packet;

/**
 * The {@link ParsedPacket} responsible for translating Engine Hours (SPN 247)
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class EngineHoursPacket extends ParsedPacket {

    public static final int PGN = 65253;

    private final double engineHours;

    public EngineHoursPacket(Packet packet) {
        super(packet);
        engineHours = getScaledIntValue(0, 20.0);
    }

    public double getEngineHours() {
        return engineHours;
    }

    @Override
    public String getName() {
        return "Engine Hours";
    }

    @Override
    public String toString() {
        return getStringPrefix() + getValueWithUnits(getEngineHours(), "hours");
    }

}
