/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939tools.j1939.packets;

/**
 * The Composite Monitoring Systems
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 */
public enum CompositeSystem {

    AC_SYSTEM_REFRIGERANT("A/C system refrigerant", 4, 0x10, false),
    BOOST_PRESSURE_CONTROL_SYS("Boost pressure control sys", 5, 0x02, false),
    CATALYST("Catalyst", 4, 0x01, false),
    COLD_START_AID_SYSTEM("Cold start aid system", 5, 0x01, false),
    COMPREHENSIVE_COMPONENT("Comprehensive component", 3, 0x40, true),
    DIESEL_PARTICULATE_FILTER("Diesel Particulate Filter", 5, 0x04, false),
    EGR_VVT_SYSTEM("EGR/VVT system", 4, 0x80, false),
    EVAPORATIVE_SYSTEM("Evaporative system", 4, 0x04, false),
    EXHAUST_GAS_SENSOR("Exhaust Gas Sensor", 4, 0x20, false),
    EXHAUST_GAS_SENSOR_HEATER("Exhaust Gas Sensor heater", 4, 0x40, false),
    FUEL_SYSTEM("Fuel System", 3, 0x20, true),
    HEATED_CATALYST("Heated catalyst", 4, 0x02, false),
    MISFIRE("Misfire", 3, 0x10, true),
    NMHC_CONVERTING_CATALYST("NMHC converting catalyst", 5, 0x10, false),
    NOX_CATALYST_ABSORBER("NOx catalyst/adsorber", 5, 0x08, false),
    SECONDARY_AIR_SYSTEM("Secondary air system", 4, 0x08, false);

    private final int lowerByte;
    private final int mask;
    private final String name;
    private final boolean isContinuouslyMonitored;

    CompositeSystem(String name, int lowerByte, int mask, boolean isContinuouslyMonitored) {
        this.name = name;
        this.lowerByte = lowerByte;
        this.mask = mask;
        this.isContinuouslyMonitored = isContinuouslyMonitored;
    }

    public int getLowerByte() {
        return lowerByte;
    }

    public int getMask() {
        return mask;
    }

    public boolean isContinuouslyMonitored() {
        return isContinuouslyMonitored;
    }

    public String getName() {
        return String.format("%-26s", name);
    }

    @Override
    public String toString() {
        return String.format("%-30s", getName()) +
                String.format("%-17s", "Lower Byte: " + getMask()) +
                String.format("%-10s", "Address: " + getLowerByte());
    }

}
