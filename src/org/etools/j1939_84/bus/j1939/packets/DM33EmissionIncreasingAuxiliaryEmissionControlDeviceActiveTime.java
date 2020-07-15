/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.j1939.packets;

import static org.etools.j1939_84.J1939_84.NL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.etools.j1939_84.bus.Packet;

/**
 * The {@link ParsedPacket} for Emission Increasing Auxiliary Emission Control
 * Device Active Time (DM33)
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 *
 *         The total engine run time while each of the Emission Increasing
 *         Auxiliary Emission Control Devices (EI-AECDs) is active.
 */
public class DM33EmissionIncreasingAuxiliaryEmissionControlDeviceActiveTime extends ParsedPacket {
    // Hex value of PGN = 00A100
    public static final int PGN = 41216;

    private List<EngineHoursTimer> eiAecdEngineHoursTimers;

    public DM33EmissionIncreasingAuxiliaryEmissionControlDeviceActiveTime(Packet packet) {
        super(packet);
    }

    public List<EngineHoursTimer> getEiAecdEngineHoursTimers() {
        if (eiAecdEngineHoursTimers == null) {
            parsePacket();
        }
        return eiAecdEngineHoursTimers;
    }

    @Override
    public String getName() {
        return "DM33";
    }

    private void parsePacket() {
        eiAecdEngineHoursTimers = new ArrayList<>();
        final int length = getPacket().getLength();
        for (int i = 0; i + 8 < length; i = i + 9) {
            byte[] copyOfRange = Arrays.copyOfRange(getPacket().getBytes(), i, i + 9);
            eiAecdEngineHoursTimers.add(new EngineHoursTimer(copyOfRange));
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("DM33EmissionIncreasingAuxiliaryEmissionControlDeviceActiveTime");
        getEiAecdEngineHoursTimers().forEach(timer -> {
            sb.append(NL).append(timer.toString());
        });
        return sb.toString();
    }

}
