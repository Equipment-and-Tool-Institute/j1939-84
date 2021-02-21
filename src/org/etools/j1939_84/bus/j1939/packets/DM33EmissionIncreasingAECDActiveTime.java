/*
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.j1939.packets;

import static org.etools.j1939_84.J1939_84.NL;

import java.util.ArrayList;
import java.util.List;

import org.etools.j1939_84.bus.Packet;
import org.etools.j1939_84.utils.CollectionUtils;

/**
 * The {@link ParsedPacket} for Emission Increasing Auxiliary Emission Control
 * Device Active Time (DM33)
 * The total engine run time while each of the Emission Increasing
 * Auxiliary Emission Control Devices (EI-AECDs) is active.
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 */
public class DM33EmissionIncreasingAECDActiveTime extends GenericPacket {
    public static final int PGN = 41216; // 0xA100

    public static DM33EmissionIncreasingAECDActiveTime create(int source, EngineHoursTimer... timers) {
        int[] data = new int[0];
        for (EngineHoursTimer timer : timers) {
            data = CollectionUtils.join(data, timer.getData());
        }
        return new DM33EmissionIncreasingAECDActiveTime(Packet.create(PGN, source, data));
    }

    private List<EngineHoursTimer> eiAecdEngineHoursTimers;

    public DM33EmissionIncreasingAECDActiveTime(Packet packet) {
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
        return "DM33 Emission Increasing AECD Active Time";
    }

    private void parsePacket() {
        eiAecdEngineHoursTimers = new ArrayList<>();
        final int length = getPacket().getLength();
        for (int i = 0; i + 8 < length; i = i + 9) {
            int[] copyOfRange = getPacket().getData(i, i + 9);
            eiAecdEngineHoursTimers.add(new EngineHoursTimer(copyOfRange));
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getStringPrefix()).append("{");
        getEiAecdEngineHoursTimers().forEach(timer -> sb.append(NL).append(timer.toString()));
        sb.append(NL).append("}").append(NL);
        return sb.toString();
    }

}
