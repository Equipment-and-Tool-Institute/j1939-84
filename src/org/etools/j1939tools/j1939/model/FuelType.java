/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939tools.j1939.model;

import static org.etools.j1939tools.j1939.packets.IgnitionType.COMPRESSION;
import static org.etools.j1939tools.j1939.packets.IgnitionType.SPARK;
import static org.etools.j1939tools.j1939.packets.IgnitionType.UNKNOWN;

import org.etools.j1939tools.j1939.packets.IgnitionType;

/**
 * The various fuel types for engines. See SPN 6317
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public enum FuelType {
    BATT_ELEC(0x08, "Battery/electric vehicle", UNKNOWN, true, false),
    BI_CNG(0x0D, "Bi-fuel vehicle using CNG", SPARK, false, false),
    BI_DSL(
            0x19,
            "Bi-fuel vehicle using diesel",
            COMPRESSION,
            false,
            false),
    BI_ELEC(0x0F, "Bi-fuel vehicle using battery", UNKNOWN, false, false),
    BI_ETH(
            0x0B,
            "Bi-fuel vehicle using ethanol",
            SPARK,
            false,
            false),
    BI_GAS(0x09, "Bi-fuel vehicle using gasoline", SPARK, false, false),
    BI_LPG(
            0x0C,
            "Bi-fuel vehicle using LPG",
            SPARK,
            false,
            false),
    BI_METH(0x0A, "Bi-fuel vehicle using methanol", SPARK, false, false),
    BI_MIX(
            0x10,
            "Bi-fuel vehicle using battery and combustion engine",
            UNKNOWN,
            false,
            false),
    BI_NG(
            0x18,
            "Bi-fuel vehicle using NG",
            SPARK,
            false,
            false),
    BI_PROP(0x0E, "Bi-fuel vehicle using propane", SPARK, false, false),
    CNG(
            0x06,
            "Compressed Natural Gas vehicle",
            SPARK,
            false,
            false),
    DSL(0x04, "Diesel vehicle", COMPRESSION, false, false),
    DSL_CNG(
            0x1B,
            "Dual Fuel vehicle – Diesel and CNG",
            COMPRESSION,
            false,
            false),
    DSL_LNG(
            0x1C,
            "Dual Fuel vehicle – Diesel and LNG",
            COMPRESSION,
            false,
            false),
    ETH(
            0x03,
            "Ethanol vehicle",
            SPARK,
            false,
            false),
    GAS(
            0x01,
            "Gasoline/petrol vehicle",
            SPARK,
            false,
            false),
    HYB_DSL(
            0x13,
            "Hybrid vehicle using diesel engine",
            COMPRESSION,
            false,
            true),
    HYB_ELEC(
            0x14,
            "Hybrid vehicle using battery",
            UNKNOWN,
            false,
            true),
    HYB_ETH(
            0x12,
            "Hybrid vehicle using gasoline engine on ethanol",
            SPARK,
            false,
            true),
    HYB_GAS(
            0x11,
            "Hybrid vehicle using gasoline engine",
            SPARK,
            false,
            true),
    HYB_MIX(
            0x15,
            "Hybrid vehicle using battery and combustion engine",
            UNKNOWN,
            false,
            true),
    HYB_REG(
            0x16,
            "Hybrid vehicle in regeneration mode",
            UNKNOWN,
            false,
            true),
    LPG(
            0x05,
            "Liquefied Petroleum Gas vehicle",
            SPARK,
            false,
            false),
    METH(
            0x02,
            "Methanol vehicle",
            SPARK,
            false,
            false),
    NG(
            0x1A,
            "Natural Gas vehicle (CNG or LNG)",
            SPARK,
            false,
            false),
    NGV(
            0x17,
            "Natural Gas vehicle",
            SPARK,
            false,
            false),
    NONE(
            0x00,
            "Not available",
            UNKNOWN,
            false,
            false),
    PROP(
            0x07,
            "Propane vehicle",
            SPARK,
            false,
            false);

    public final IgnitionType ignitionType;

    /**
     * The display name which may be shown to the user
     */
    private final String string;

    /**
     * The SAE defined value for the Fuel Type
     */
    private final int value;

    private final boolean isElectric;

    private final boolean isHybrid;

    FuelType(int value, String string, IgnitionType ignitionType, boolean isElectric, boolean isHybrid) {
        this.string = string;
        this.value = value;
        this.ignitionType = ignitionType;
        this.isElectric = isElectric;
        this.isHybrid = isHybrid;
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

    public boolean isElectric() {
        return isElectric;
    }

    public boolean isHybrid() {
        return isHybrid;
    }

    @Override
    public String toString() {
        return string;
    }

}
