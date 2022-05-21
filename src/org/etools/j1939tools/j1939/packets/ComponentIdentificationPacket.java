/*
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939tools.j1939.packets;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939tools.utils.StringUtils.stripLeadingAndTrailingNulls;

import org.etools.j1939tools.bus.Packet;
import org.etools.j1939tools.j1939.model.ComponentIdentification;

/**
 * Parses the Component Identification Packet
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 */
public class ComponentIdentificationPacket extends GenericPacket {

    public static final int PGN = 65259;
    /**
     * Holds the different parts of the component identification:
     *
     * <pre>
     * 0 - Make
     * 1 - Model
     * 2 - Serial Number
     * 3 - Unit Number
     * </pre>
     */
    final private String[] parts = new String[4];

    /**
     * Constructor
     *
     * @param packet
     *                   the {@link Packet} to parse
     */
    public ComponentIdentificationPacket(Packet packet) {
        super(packet);
        String str = new String(packet.getBytes(), UTF_8);
        String[] array = str.split("\\*", -1);
        for (int i = 0; i < 4 && i < array.length; i++) {
            parts[i] = array[i];
        }
    }

    /*
     * Helper method for unit testing purposes. Allows us to easily create
     * a packet from the expected human readable data type. String are joined
     * together to create a byte representation of the data values joined together
     * with an '*" for parsing.
     */
    public static ComponentIdentificationPacket create(int sourceAddress,
                                                       String make,
                                                       String model,
                                                       String serialNumber,
                                                       String unitNumber) {

        byte[] bytes = (make + "*" + model + "*" + serialNumber + "*" + unitNumber + "*").getBytes(UTF_8);
        return new ComponentIdentificationPacket(Packet.create(PGN, sourceAddress, bytes));
    }

    public String getMake() {
        return parts[0];
    }

    public String getModel() {
        return parts[1];
    }

    @Override
    public String getName() {
        return "Component Identification";
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
    @Override
    public String toString() {
        String result = getStringPrefix() + "{" + NL;
        String make = getMake();
        result += "  Make: " + (make == null ? "" : make.trim()) + NL;
        String model = getModel();
        result += "  Model: " + (model == null ? "" : model.trim()) + NL;
        String serialNumber = getSerialNumber();
        // no need for null check here as getSerialNumer() handles null
        result += "  Serial: " + serialNumber + NL;
        String unitNumber = getUnitNumber();
        result += "  Unit: " + (unitNumber == null ? "" : unitNumber.trim()) + NL;
        result += "}" + NL;
        return result;
    }

    public String getSerialNumber() {
        return stripLeadingAndTrailingNulls(parts[2]);
    }

    public String getUnitNumber() {
        return parts[3];
    }

    public ComponentIdentification getComponentIdentification() {
        return (new ComponentIdentification(this));
    }
}
