/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.j1939.packets;

import java.util.Arrays;

import org.etools.j1939_84.bus.Packet;

/**
 * Parses the Vehicle Identification Packet (PGN 65260)
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class VehicleIdentificationPacket extends ParsedPacket {

    /**
     * The ASCII code for a *. It denotes the end of the VIN
     */
    private static final byte ASTERISK = 42;

    public static final String NAME = "Vehicle Identification";

    public static final int PGN = 65260;

    private String vin = null;

    public VehicleIdentificationPacket(Packet packet) {
        super(packet);
    }

    /**
     * Finds and returns the index of the asterisk in the data
     *
     * @param data the data of interest
     * @return the index of the asterisk, -1 if there is no asterisk
     */
    private int getAsteriskIndex(byte[] data) {
        int index = -1;
        for (int i = 0; i < data.length; i++) {
            if (data[i] == ASTERISK) {
                index = i;
                break;
            }
        }
        return index;
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
            vin = parseVin();
        }
        return vin;
    }

    private String parseVin() {
        byte[] data = getPacket().getBytes();

        // Find the location of the *
        int index = getAsteriskIndex(data);

        if (index >= 0) {
            // It has a *, return just the VIN
            byte[] vinBytes = Arrays.copyOf(data, index);
            return format(vinBytes).trim();
        } else {
            // It doesn't have a *, return the entire thing
            return format(data).trim();
        }
    }

    @Override
    public String toString() {
        return getStringPrefix() + getVin();
    }

}
