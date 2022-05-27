/*
 * Copyright (c) 2022. Equipment & Tool Institute
 */

package org.etools.j1939tools.j1939.model;

import static org.etools.j1939tools.j1939.model.ObdCompliance.EMD;
import static org.etools.j1939tools.j1939.model.ObdCompliance.EMD_PLUS;
import static org.etools.j1939tools.j1939.model.ObdCompliance.EOBD;
import static org.etools.j1939tools.j1939.model.ObdCompliance.EOBD_AND_OBD;
import static org.etools.j1939tools.j1939.model.ObdCompliance.EOBD_AND_OBD_II;
import static org.etools.j1939tools.j1939.model.ObdCompliance.EOBD_OBD_AND_OBD_II;
import static org.etools.j1939tools.j1939.model.ObdCompliance.ERROR;
import static org.etools.j1939tools.j1939.model.ObdCompliance.EURO_VI;
import static org.etools.j1939tools.j1939.model.ObdCompliance.HD_EOBD;
import static org.etools.j1939tools.j1939.model.ObdCompliance.HD_OBD;
import static org.etools.j1939tools.j1939.model.ObdCompliance.HD_OBD_P;
import static org.etools.j1939tools.j1939.model.ObdCompliance.HEAVY_DUTY_VEHICLES_EURO_EEC_C_GAS_ENGINES;
import static org.etools.j1939tools.j1939.model.ObdCompliance.HEAVY_DUTY_VEHICLES_EURO_IV_B1;
import static org.etools.j1939tools.j1939.model.ObdCompliance.HEAVY_DUTY_VEHICLES_EURO_V_B2;
import static org.etools.j1939tools.j1939.model.ObdCompliance.JOBD;
import static org.etools.j1939tools.j1939.model.ObdCompliance.JOBD_AND_EOB;
import static org.etools.j1939tools.j1939.model.ObdCompliance.JOBD_AND_OBD_II;
import static org.etools.j1939tools.j1939.model.ObdCompliance.JOBD_EOBD_AND_OBD_II;
import static org.etools.j1939tools.j1939.model.ObdCompliance.NOT_AVAILABLE;
import static org.etools.j1939tools.j1939.model.ObdCompliance.NOT_INTENDED_TO_MEET_OBD_II_REQUIREMENTS;
import static org.etools.j1939tools.j1939.model.ObdCompliance.OBD;
import static org.etools.j1939tools.j1939.model.ObdCompliance.OBD_AND_OBD_II;
import static org.etools.j1939tools.j1939.model.ObdCompliance.OBD_I;
import static org.etools.j1939tools.j1939.model.ObdCompliance.OBD_II;
import static org.etools.j1939tools.j1939.model.ObdCompliance.OBD_II_22;
import static org.etools.j1939tools.j1939.model.ObdCompliance.OBD_M_SI_SD_I;
import static org.etools.j1939tools.j1939.model.ObdCompliance.OBD_OBD_II_HD_OBD;
import static org.etools.j1939tools.j1939.model.ObdCompliance.OBD_OBD_II_HD_OBD_P;
import static org.etools.j1939tools.j1939.model.ObdCompliance.RESERVED_FOR_SAE_UNKNOWN;
import static org.etools.j1939tools.j1939.model.ObdCompliance.VALUE_251;
import static org.etools.j1939tools.j1939.model.ObdCompliance.VALUE_252;
import static org.etools.j1939tools.j1939.model.ObdCompliance.VALUE_253;
import static org.etools.j1939tools.j1939.model.ObdCompliance.WWH_OBD;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Unit test for the {@link ObdCompliance} class
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 */

public class ObdComplianceTest {

    @Test
    public void getValue() {
        assertEquals(0x01, OBD_II.getValue());
        assertEquals(0x02, OBD.getValue());
        assertEquals(0x03, OBD_AND_OBD_II.getValue());
        assertEquals(0x04, OBD_I.getValue());
        assertEquals(0x05, NOT_INTENDED_TO_MEET_OBD_II_REQUIREMENTS.getValue());
        assertEquals(0x06, EOBD.getValue());
        assertEquals(0x07, EOBD_AND_OBD_II.getValue());
        assertEquals(0x08, EOBD_AND_OBD.getValue());
        assertEquals(0x09, EOBD_OBD_AND_OBD_II.getValue());
        assertEquals(0x0A, JOBD.getValue());
        assertEquals(0x0B, JOBD_AND_OBD_II.getValue());
        assertEquals(0x0C, JOBD_AND_EOB.getValue());
        assertEquals(0x0D, JOBD_EOBD_AND_OBD_II.getValue());
        assertEquals(0x0E, HEAVY_DUTY_VEHICLES_EURO_IV_B1.getValue());
        assertEquals(0x0F, HEAVY_DUTY_VEHICLES_EURO_V_B2.getValue());
        assertEquals(0x10,  HEAVY_DUTY_VEHICLES_EURO_EEC_C_GAS_ENGINES.getValue());
        assertEquals(0x11, EMD.getValue());
        assertEquals(0x12, EMD_PLUS.getValue());
        assertEquals(0x13, HD_OBD_P.getValue());
        assertEquals(0x14, HD_OBD.getValue());
        assertEquals(0x15, WWH_OBD.getValue());
        assertEquals(0x16, OBD_II_22.getValue());
        assertEquals(0x17, HD_EOBD.getValue());

        assertEquals(0x19, OBD_M_SI_SD_I.getValue());
        assertEquals(0x1A, EURO_VI.getValue());

        assertEquals(0x22, OBD_OBD_II_HD_OBD.getValue());
        assertEquals(0x23, OBD_OBD_II_HD_OBD_P.getValue());

        assertEquals((byte) 0xFB, VALUE_251.getValue());
        assertEquals((byte) 0xFC, VALUE_252.getValue());
        assertEquals((byte) 0xFD, VALUE_253.getValue());
        assertEquals((byte) 0xFE, ERROR.getValue());
        assertEquals( (byte) 0xFF,   NOT_AVAILABLE.getValue());
        assertEquals(0x00, RESERVED_FOR_SAE_UNKNOWN.getValue());
    }

    @Test
    public void testOrder() {
        ObdCompliance[] values = ObdCompliance.values();
        assertEquals(OBD_II, values[0]);
        assertEquals(OBD, values[1]);
        assertEquals(OBD_AND_OBD_II, values[2]);
        assertEquals(OBD_I, values[3]);
        assertEquals(NOT_INTENDED_TO_MEET_OBD_II_REQUIREMENTS, values[4]);
        assertEquals(EOBD, values[5]);
        assertEquals(EOBD_AND_OBD_II, values[6]);
        assertEquals(EOBD_AND_OBD, values[7]);
        assertEquals(EOBD_OBD_AND_OBD_II, values[8]);
        assertEquals(JOBD, values[9]);
        assertEquals(JOBD_AND_OBD_II, values[10]);
        assertEquals(JOBD_AND_EOB, values[11]);
        assertEquals(JOBD_EOBD_AND_OBD_II, values[12]);
        assertEquals(HEAVY_DUTY_VEHICLES_EURO_IV_B1, values[13]);
        assertEquals(HEAVY_DUTY_VEHICLES_EURO_V_B2, values[14]);
        assertEquals(HEAVY_DUTY_VEHICLES_EURO_EEC_C_GAS_ENGINES, values[15]);
        assertEquals(EMD, values[16]);
        assertEquals(EMD_PLUS, values[17]);
        assertEquals(HD_OBD_P, values[18]);
        assertEquals(HD_OBD, values[19]);
        assertEquals(WWH_OBD, values[20]);
        assertEquals(OBD_II_22, values[21]);
        assertEquals(HD_EOBD, values[22]);

        assertEquals(OBD_M_SI_SD_I, values[23]);
        assertEquals(EURO_VI, values[24]);

        assertEquals(OBD_OBD_II_HD_OBD, values[25]);
        assertEquals(OBD_OBD_II_HD_OBD_P, values[26]);

        assertEquals(VALUE_251, values[27]);
        assertEquals(VALUE_252, values[28]);
        assertEquals(VALUE_253, values[29]);
        assertEquals(ERROR, values[30]);
        assertEquals(    NOT_AVAILABLE, values[31]);
        assertEquals(RESERVED_FOR_SAE_UNKNOWN, values[32]);
        assertEquals(33, values.length);
    }

    @Test
    public void testToString() {
        assertEquals("OBD II", OBD_II.toString());
        assertEquals("OBD", OBD.toString());
        assertEquals("OBD and OBD II", OBD_AND_OBD_II.toString());
        assertEquals("OBD I", OBD_I.toString());
        assertEquals("Not intended to meet OBD II requirements", NOT_INTENDED_TO_MEET_OBD_II_REQUIREMENTS.toString());
        assertEquals("EOBD", EOBD.toString());
        assertEquals("EOBD and OBD II", EOBD_AND_OBD_II.toString());
        assertEquals("EOBD and OBD", EOBD_AND_OBD.toString());
        assertEquals("EOBD, OBD and OBD II", EOBD_OBD_AND_OBD_II.toString());
        assertEquals("JOBD", JOBD.toString());
        assertEquals("JOBD and OBD II", JOBD_AND_OBD_II.toString());
        assertEquals("JOBD and EOB", JOBD_AND_EOB.toString());
        assertEquals("JOBD, EOBD and OBD II", JOBD_EOBD_AND_OBD_II.toString());
        assertEquals("Heavy Duty Vehicles (EURO IV) B1", HEAVY_DUTY_VEHICLES_EURO_IV_B1.toString());
        assertEquals("Dual Fuel vehicle – Diesel and LNG", HEAVY_DUTY_VEHICLES_EURO_V_B2.toString());
        assertEquals("Heavy Duty Vehicles (EURO EEC) C (gas engines)", HEAVY_DUTY_VEHICLES_EURO_EEC_C_GAS_ENGINES.toString());
        assertEquals("EMD", EMD.toString());
        assertEquals("EMD+", EMD_PLUS.toString());
        assertEquals("HD OBD P", HD_OBD_P.toString());
        assertEquals("HD OBD", HD_OBD.toString());
        assertEquals("WWH OBD", WWH_OBD.toString());
        assertEquals("OBD II", OBD_II_22.toString());
        assertEquals("HD EOBD", HD_EOBD.toString());


        assertEquals("OBD-M (SI-SD/I)", OBD_M_SI_SD_I.toString());
        assertEquals("EURO VI", EURO_VI.toString() );

        assertEquals("OBD, OBD II, HD OBD", OBD_OBD_II_HD_OBD.toString());
        assertEquals("OBD, OBD II, HD OBD P", OBD_OBD_II_HD_OBD_P.toString());

        assertEquals("value 251", VALUE_251.toString());
        assertEquals("value 252", VALUE_252.toString());
        assertEquals("value 253", VALUE_253.toString());
        assertEquals("Error", ERROR.toString());
        assertEquals("Not available", NOT_AVAILABLE.toString());
    }

    @Test
    public void testValues() {
        assertEquals(EOBD_AND_OBD, ObdCompliance.resolveObdCompliance((byte) 0x08));
        assertEquals(OBD_II, ObdCompliance.resolveObdCompliance((byte) 0x01));
        assertEquals(OBD, ObdCompliance.resolveObdCompliance((byte) 0x02));
        assertEquals(OBD_AND_OBD_II, ObdCompliance.resolveObdCompliance((byte) 0x03));
        assertEquals(OBD_I, ObdCompliance.resolveObdCompliance((byte) 0x04));
        assertEquals(NOT_INTENDED_TO_MEET_OBD_II_REQUIREMENTS, ObdCompliance.resolveObdCompliance((byte) 0x05));
        assertEquals(EOBD, ObdCompliance.resolveObdCompliance((byte) 0x06));
        assertEquals(EOBD_AND_OBD_II, ObdCompliance.resolveObdCompliance((byte) 0x07));
        assertEquals(EOBD_AND_OBD, ObdCompliance.resolveObdCompliance((byte) 0x08));
        assertEquals(EOBD_OBD_AND_OBD_II, ObdCompliance.resolveObdCompliance((byte) 0x09));
        assertEquals(JOBD, ObdCompliance.resolveObdCompliance((byte) 0x0A));
        assertEquals(JOBD_AND_OBD_II, ObdCompliance.resolveObdCompliance((byte) 0x0B));
        assertEquals(JOBD_AND_EOB, ObdCompliance.resolveObdCompliance((byte) 0x0C));
        assertEquals(JOBD_EOBD_AND_OBD_II, ObdCompliance.resolveObdCompliance((byte) 0x0D));
        assertEquals(HEAVY_DUTY_VEHICLES_EURO_IV_B1, ObdCompliance.resolveObdCompliance((byte) 0x0E));
        assertEquals(HEAVY_DUTY_VEHICLES_EURO_V_B2, ObdCompliance.resolveObdCompliance((byte) 0x0F));
        assertEquals(HEAVY_DUTY_VEHICLES_EURO_EEC_C_GAS_ENGINES, ObdCompliance.resolveObdCompliance((byte) 0x10));
        assertEquals(EMD, ObdCompliance.resolveObdCompliance((byte) 0x11));
        assertEquals(EMD_PLUS, ObdCompliance.resolveObdCompliance((byte) 0x12));
        assertEquals(HD_OBD_P, ObdCompliance.resolveObdCompliance((byte) 0x13));
        assertEquals(HD_OBD, ObdCompliance.resolveObdCompliance((byte) 0x14));
        assertEquals(WWH_OBD, ObdCompliance.resolveObdCompliance((byte) 0x15));
        assertEquals(OBD_II_22, ObdCompliance.resolveObdCompliance((byte) 0x16));
        assertEquals(HD_EOBD, ObdCompliance.resolveObdCompliance((byte) 0x17));

        assertEquals(OBD_M_SI_SD_I, ObdCompliance.resolveObdCompliance((byte) 0x19));
        assertEquals(EURO_VI, ObdCompliance.resolveObdCompliance((byte) 0x1A));

        assertEquals(OBD_OBD_II_HD_OBD, ObdCompliance.resolveObdCompliance((byte) 0x22));
        assertEquals(OBD_OBD_II_HD_OBD_P, ObdCompliance.resolveObdCompliance((byte) 0x23));

        assertEquals(VALUE_251, ObdCompliance.resolveObdCompliance((byte) 0xFB));
        assertEquals(VALUE_252, ObdCompliance.resolveObdCompliance((byte) 0xFC));
        assertEquals(VALUE_253, ObdCompliance.resolveObdCompliance((byte) 0xFD));
        assertEquals(ERROR, ObdCompliance.resolveObdCompliance((byte) 0xFE));
        assertEquals(NOT_AVAILABLE, ObdCompliance.resolveObdCompliance((byte) 0xFF));
        assertEquals(RESERVED_FOR_SAE_UNKNOWN, ObdCompliance.resolveObdCompliance((byte) 0x00));

    }

    @Test
    public void testResolveObdCompliance() {
        assertEquals(RESERVED_FOR_SAE_UNKNOWN, ObdCompliance.resolveObdCompliance((byte)0x00));
    }
}