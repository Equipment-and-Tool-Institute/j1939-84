package org.etools.j1939_84.bus.j1939.packets;

import java.util.Arrays;

import org.etools.j1939_84.J1939_84;
import org.etools.j1939_84.bus.Packet;

public class DM56EngineFamilyPacket extends ParsedPacket {

    public static final String NAME = "Model Year and Certification Engine Family";

    public static final int PGN = 64711;

    private String familyName = null;

    private String modelYear = null;

    public DM56EngineFamilyPacket(Packet packet) {
        super(packet);
    }

    public Integer getEngineModelYear() {
        return isEngineModelYear() ? getModelYearInt() : null;
    }

    public String getFamilyName() {
        if (familyName == null) {
            byte[] bytes = getPacket().getBytes();
            byte[] data = Arrays.copyOfRange(bytes, 8, bytes.length);
            familyName = parseField(data, false);
        }
        return familyName;
    }

    /**
     * Returns the Engine Model Year Field (XXXX[E/V]-MY)
     *
     * @return the Model Year Field as a String
     *
     */
    public String getModelYearField() {
        if (modelYear == null) {
            modelYear = format(Arrays.copyOf(getPacket().getBytes(), 8)).trim();
        }
        return modelYear;
    }

    private int getModelYearInt() {
        return Integer.valueOf(getModelYearField().substring(0, 4));
    }

    @Override
    public String getName() {
        return NAME;
    }

    public Integer getVehicleModelYear() {
        return !isEngineModelYear() ? getModelYearInt() : null;
    }

    private boolean isEngineModelYear() {
        return getModelYearField().contains("E-MY");
    }

    @Override
    public String toString() {
        return getStringPrefix() + J1939_84.NL +
                "Model Year: " + getModelYearField() + J1939_84.NL +
                "Family Name: " + getFamilyName();
    }
}
