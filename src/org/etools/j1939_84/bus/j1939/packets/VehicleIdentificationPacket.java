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
        int index = -1;
        for (int i = 0; i < data.length; i++) {
            if (data[i] == ASTERISK) {
                index = i;
                break;
            }
        }

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
