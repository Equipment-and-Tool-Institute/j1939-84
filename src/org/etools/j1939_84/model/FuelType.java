/**
 * Copyright (c) 2019. Equipment & Tool Institute
 */
package org.etools.j1939_84.model;

import static org.etools.j1939_84.model.IgnitionType.COMPRESSION;
import static org.etools.j1939_84.model.IgnitionType.SPARK;
import static org.etools.j1939_84.model.IgnitionType.UNKNOWN;

/**
 * The various fuel types for engines. See SPN 6317
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public enum FuelType {
    BATT_ELEC(0x08, "Battery/electric vehicle", UNKNOWN), BI_CNG(0x0D, "Bi-fuel vehicle using CNG", SPARK), BI_DSL(0x19,
            "Bi-fuel vehicle using diesel",
            COMPRESSION),
    BI_ELEC(0x0F, "Bi-fuel vehicle using battery", UNKNOWN), BI_ETH(0x0B,
            "Bi-fuel vehicle using ethanol",
            SPARK),
    BI_GAS(0x09, "Bi-fuel vehicle using gasoline", SPARK), BI_LPG(0x0C,
            "Bi-fuel vehicle using LPG",
            SPARK),
    BI_METH(0x0A, "Bi-fuel vehicle using methanol", SPARK), BI_MIX(0x10,
            "Bi-fuel vehicle using battery and combustion engine", UNKNOWN),
    BI_NG(0x18,
            "Bi-fuel vehicle using NG",
            SPARK),
    BI_PROP(0x0E, "Bi-fuel vehicle using propane", SPARK), CNG(0x06,
            "Compressed Natural Gas vehicle",
            SPARK),
    DSL(0x04, "Diesel vehicle", COMPRESSION), DSL_CNG(0x1B,
            "Dual Fuel vehicle – Diesel and CNG", COMPRESSION),
    DSL_LNG(
            0x1C, "Dual Fuel vehicle – Diesel and LNG",
            COMPRESSION),
    ETH(0x03, "Ethanol vehicle",
            SPARK),
    GAS(0x01, "Gasoline/petrol vehicle",
            SPARK),
    HYB_DSL(0x13,
            "Hybrid vehicle using diesel engine",
            COMPRESSION),
    HYB_ELEC(0x14,
            "Hybrid vehicle using battery",
            UNKNOWN),
    HYB_ETH(
            0x12,
            "Hybrid vehicle using gasoline engine on ethanol",
            SPARK),
    HYB_GAS(
            0x11,
            "Hybrid vehicle using gasoline engine",
            SPARK),
    HYB_MIX(
            0x15,
            "Hybrid vehicle using battery and combustion engine",
            UNKNOWN),
    HYB_REG(
            0x16,
            "Hybrid vehicle in regeneration mode",
            UNKNOWN),
    LPG(
            0x05,
            "Liquefied Petroleum Gas vehicle",
            SPARK),
    METH(
            0x02,
            "Methanol vehicle",
            SPARK),
    NG(
            0x1A,
            "Natural Gas vehicle (CNG or LNG)",
            SPARK),
    NGV(
            0x17,
            "Natural Gas vehicle",
            SPARK),
    NONE(
            0x00,
            "Not available",
            UNKNOWN),
    PROP(
            0x07,
            "Propane vehicle",
            SPARK);

    public final IgnitionType ignitionType;

    /**
     * The display name which may be shown to the user
     */
    private final String string;

    /**
     * The SAE defined value for the Fuel Type
     */
    private final int value;

    /**
     * Constructor
     *
     * @param value
     *            the SAE defined value for the Fuel Type
     * @param string
     *            the display name which may be shown to the user
     */
    private FuelType(int value, String string, IgnitionType ignitionType) {
        this.string = string;
        this.value = value;
        this.ignitionType = ignitionType;
    }

    /**
     * Returns the SAE defined value for the Fuel Type
     *
     * @return int
     */
    public int getValue() {
        return value;
    }

    public boolean isCompressionIgnition() {
        return ignitionType == COMPRESSION;
    }

    public boolean isSparkIgnition() {
        return ignitionType == SPARK;
    }

    @Override
    public String toString() {
        return string;
    }

}
