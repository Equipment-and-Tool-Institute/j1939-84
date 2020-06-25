/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.j1939.packets;

import org.etools.j1939_84.bus.Packet;

/**
 * The {@link ParsedPacket} for Emission Increasing Auxiliary Emission Control
 * Device Active Time (DM33)
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 *
 * The total engine run time while each of the Emission Increasing Auxiliary
 * Emission Control Devices (EI-AECDs) is active.
 */
public class DM33EmissionIncreasingAuxiliaryEmissionControlDeviceActiveTime extends ParsedPacket {
    // Hex value of PGN = 00A100
    public static final int PGN = 41216;

    public DM33EmissionIncreasingAuxiliaryEmissionControlDeviceActiveTime(Packet packet) {
        super(packet);
    }

    @Override
    public String getName() {
        return "DM33";
    }
}
