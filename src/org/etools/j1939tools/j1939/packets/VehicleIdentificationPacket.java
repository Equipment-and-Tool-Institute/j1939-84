/*
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939tools.j1939.packets;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.etools.j1939tools.bus.Packet;

/**
 * Parses the Vehicle Identification Packet (PGN 65260)
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class VehicleIdentificationPacket extends GenericPacket {

    public static final String NAME = "Vehicle Identification";

    public static final int PGN = 65260;

    private String vin = null;

    public VehicleIdentificationPacket(Packet packet) {
        super(packet);
    }

    public static VehicleIdentificationPacket create(int source, String vin) {
        return new VehicleIdentificationPacket(Packet.create(PGN, source, vin.getBytes(StandardCharsets.UTF_8)));
    }
    /**
     * Returns the data, if any, that exist beyond the asterisk
     * NOTE: this is not defined in an SAE Document as a valid SPN
     *
     * @return String of any additional data
     */
    public String getManufacturerData() {
        byte[] data = getPacket().getBytes();
        int index = getAsteriskOrNullIndex(data);
        return index == -1 || index == data.length - 1 ? ""
                : format(Arrays.copyOfRange(data, index + 1, data.length));
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String toString() {
        return getStringPrefix() + getVin();
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
    /**
     * Returns the Vehicle Identification Number
     *
     * @return the VIN as a String
     *
     */
    public String getVin() {
        if (vin == null) {
            vin = parseField(getPacket().getBytes());
        }
        return vin;
    }
}
