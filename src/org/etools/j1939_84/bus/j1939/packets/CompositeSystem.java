/**
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.j1939.packets;

/**
 * The Composite Monitoring Systems
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 *
 */
public enum CompositeSystem {

    AC_SYSTEM_REFRIGERANT("A/C system refrigerant", 4, 0x10),
    BOOST_PRESSURE_CONTROL_SYS("Boost pressure control sys", 5, 0x02),
    CATALYST("Catalyst", 4, 0x01),
    COLD_START_AID_SYSTEM("Cold start aid system", 5, 0x01),
    COMPREHENSIVE_COMPONENT("Comprehensive component", 0, 0x40),
    DIESEL_PARTICULATE_FILTER("Diesel Particulate Filter", 5, 0x04),
    EGR_VVT_SYSTEM("EGR/VVT system", 4, 0x80),
    EVAPORATIVE_SYSTEM("Evaporative system", 4, 0x04),
    EXHAUST_GAS_SENSOR("Exhaust Gas Sensor", 4, 0x20),
    EXHAUST_GAS_SENSOR_HEATER("Exhaust Gas Sensor heater", 4, 0x40),
    FUEL_SYSTEM("Fuel System", 0, 0x20),
    HEATED_CATALYST("Heated catalyst", 4, 0x02),
    MISFIRE("Misfire", 0, 0x10),
    NMHC_CONVERTING_CATALYST("NMHC converting catalyst", 5, 0x10),
    NOX_CATALYST_ABSORBER("NOx catalyst/adsorber", 5, 0x08),
    SECONDARY_AIR_SYSTEM("Secondary air system", 4, 0x08);

    private final int lowerByte;
    private final int mask;
    private final String name;

    private CompositeSystem(String name, int lowerByte, int mask) {
        this.name = name;
        this.lowerByte = lowerByte;
        this.mask = mask;
    }

    /**
     * @return the address
     */
    public int getLowerByte() {
        return lowerByte;
    }

    /**
     * @return the lowerByte
     */
    public int getMask() {
        return mask;
    }

    /**
     * @return the name
     */
    public String getName() {
        return String.format("%-26s", name);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder(String.format("%-30s", getName()))
                .append(String.format("%-17s", String.valueOf("Lower Byte: " + getMask())))
                .append(String.format("%-10s", String.valueOf("Address: " + getLowerByte())));
        return stringBuilder.toString();
    }

}
