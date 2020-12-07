/*
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.j1939.packets;

import java.util.Arrays;
import org.etools.j1939_84.bus.Packet;
import org.etools.j1939_84.bus.j1939.J1939DaRepository;

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
        super(packet, new J1939DaRepository().findPgnDefinition(PGN));
    }

    /**
     * Returns the data, if any, that exist beyond the asterisk
     * NOTE: this is not defined in an SAE Document as a valid SPN
     *
     * @return String of any additional data
     */
    public String getManufacturerData() {
        byte[] data = getPacket().getBytes();
        int index = getAsteriskIndex(data);
        return index == -1 || index == data.length - 1 ? ""
                : format(Arrays.copyOfRange(data, index + 1, data.length));
    }

    @Override
    public String getName() {
        return NAME;
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

    @Override
    public String toString() {
        return getStringPrefix() + getVin();
    }
}
