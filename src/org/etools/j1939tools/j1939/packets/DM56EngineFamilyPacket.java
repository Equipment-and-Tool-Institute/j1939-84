package org.etools.j1939tools.j1939.packets;

import static org.etools.j1939_84.J1939_84.NL;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.etools.j1939tools.bus.Packet;
import org.etools.j1939tools.utils.CollectionUtils;

public class DM56EngineFamilyPacket extends GenericPacket {

    public static final int PGN = 64711;

    public static DM56EngineFamilyPacket create(int address, int modelYear, boolean isEngine, String familyName) {
        byte[] data = new byte[0];
        if (isEngine) {
            data = CollectionUtils.join(data, String.format("%1$dE-MY", modelYear).getBytes(StandardCharsets.UTF_8));
        } else {
            data = CollectionUtils.join(data, String.format("%1$dV-MY", modelYear).getBytes(StandardCharsets.UTF_8));
        }
        data = CollectionUtils.join(data, familyName.getBytes(StandardCharsets.UTF_8));
        return new DM56EngineFamilyPacket(Packet.create(PGN, address, data));
    }

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

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
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
        return Integer.parseInt(getModelYearField().substring(0, 4));
    }

    @Override
    public String getName() {
        return "Model Year and Certification Engine Family";
    }

    @Override
    public String toString() {
        return getStringPrefix() + NL +
                "Model Year: " + getModelYearField() + NL +
                "Family Name: " + getFamilyName();
    }

    public Integer getVehicleModelYear() {
        return !isEngineModelYear() ? getModelYearInt() : null;
    }

    private boolean isEngineModelYear() {
        return getModelYearField().contains("E-MY");
    }
}
