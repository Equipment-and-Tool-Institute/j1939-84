/*
 * Copyright (c) 2022. Equipment & Tool Institute
 */

package org.etools.j1939tools.j1939.model;

import java.util.Arrays;

public enum ObdCompliance {

    OBD_II((byte) 0x01, "OBD II"),
    OBD((byte) 0x02, "OBD"),
    OBD_AND_OBD_II((byte) 0x03, "OBD and OBD II"),
    OBD_I((byte) 0x04, "OBD I"),
    NOT_INTENDED_TO_MEET_OBD_II_REQUIREMENTS((byte) 0x05, "Not intended to meet OBD II requirements"),
    EOBD((byte) 0x06, "EOBD"),
    EOBD_AND_OBD_II((byte) 0x07, "EOBD and OBD II"),
    EOBD_AND_OBD((byte) 0x08, "EOBD and OBD"),
    EOBD_OBD_AND_OBD_II((byte) 0x09, "EOBD, OBD and OBD II"),
    JOBD((byte) 0x0A, "JOBD"),
    JOBD_AND_OBD_II((byte) 0x0B, "JOBD and OBD II"),
    JOBD_AND_EOB((byte) 0x0C, "JOBD and EOB"),
    JOBD_EOBD_AND_OBD_II((byte) 0x0D, "JOBD, EOBD and OBD II"),
    HEAVY_DUTY_VEHICLES_EURO_IV_B1((byte) 0x0E, "Heavy Duty Vehicles (EURO IV) B1"),
    HEAVY_DUTY_VEHICLES_EURO_V_B2((byte) 0x0F, "Dual Fuel vehicle – Diesel and LNG"),
    HEAVY_DUTY_VEHICLES_EURO_EEC_C_GAS_ENGINES((byte) 0x10, "Heavy Duty Vehicles (EURO EEC) C (gas engines)"),
    EMD((byte) 0x11, "EMD"),
    EMD_PLUS((byte) 0x12, "EMD+"),
    HD_OBD_P((byte) 0x13, "HD OBD P"),
    HD_OBD((byte) 0x14, "HD OBD"),
    WWH_OBD((byte) 0x15, "WWH OBD"),
    OBD_II_22((byte) 0x16, "OBD II"),
    HD_EOBD((byte) 0x17, "HD EOBD"),

    OBD_M_SI_SD_I((byte) 0x19, "OBD-M (SI-SD/I)"),
    EURO_VI((byte) 0x1A, "EURO VI"),

    OBD_OBD_II_HD_OBD((byte) 0x22, "OBD, OBD II, HD OBD"),
    OBD_OBD_II_HD_OBD_P((byte) 0x23, "OBD, OBD II, HD OBD P"),

    VALUE_251((byte) 0xFB, "value 251"),
    VALUE_252((byte) 0xFC, "value 252"),
    VALUE_253((byte) 0xFD, "value 253"),
    ERROR((byte) 0xFE, "Error"),
    NOT_AVAILABLE((byte) 0xFF, "Not available"),
    RESERVED_FOR_SAE_UNKNOWN((byte) 0x00, "Reserved for SAE/Unknown");

    /**
     * The display name which may be shown to the user
     */
    private final String string;

    /**
     * The SAE defined value for the OBD Compliance Type
     */
    private final byte value;

    /**
     * Constructor
     *
     * @param value
     *                   the SAE defined value for the OBD Compliance Type
     * @param string
     *                   the display name which may be shown to the user
     */
    ObdCompliance(byte value, String string) {
        this.string = string;
        this.value = value;
    }

    /**
     * Returns the SAE defined value for the OBD Compliance Type
     *
     * @return int
     */
    public byte getValue() {
        return value;
    }

    @Override
    public String toString() {
        return string;
    }

    public static ObdCompliance resolveObdCompliance(byte byteValue) {
        return Arrays.stream(ObdCompliance.values())
                     .filter(aEnum -> aEnum.getValue() == byteValue)
                     .findFirst()
                     .orElse(RESERVED_FOR_SAE_UNKNOWN);
    }
}
