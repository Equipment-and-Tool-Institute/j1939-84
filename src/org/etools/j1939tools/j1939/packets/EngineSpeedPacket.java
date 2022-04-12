/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939tools.j1939.packets;

import org.etools.j1939tools.bus.Packet;
import org.etools.j1939tools.utils.CollectionUtils;

/**
 * The {@link ParsedPacket} responsible for translating Engine Speed (SPN 190)
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class EngineSpeedPacket extends GenericPacket {

    public static final int PGN = 61444;

    public static EngineSpeedPacket create(int sourceAddress, int engineRPMs) {
        int[] data = new int[] { 0xFF, 0xFF, 0xFF };
        data = CollectionUtils.join(data, to2Ints(engineRPMs * 8)); // Bytes 4 & 5
        data = CollectionUtils.join(data, new int[] { 0xFF, 0xFF, 0xFF });
        return new EngineSpeedPacket(Packet.create(PGN, sourceAddress, data));
    }

    private final double engineSpeed;

    public EngineSpeedPacket(Packet packet) {
        super(packet);
        engineSpeed = getScaledShortValue(3, 8.0);
    }

    /**
     * Returns the Engine Speed as Revolutions Per Minute (RPM)
     *
     * @return RPMs of the Engine
     */
    public double getEngineSpeed() {
        return engineSpeed;
    }

    @Override
    public String getName() {
        return "Engine Speed";
    }

    /**
     * Return true if the value returned indicates the Engine Speed Signal is
     * Errored
     *
     * @return boolean
     */
    public boolean isError() {
        return isError(getEngineSpeed());
    }

    /**
     * Returns true if the value returned indicate the Engine could not read the
     * Engine Speed
     *
     * @return boolean
     */
    public boolean isNotAvailable() {
        return isNotAvailable(getEngineSpeed());
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
