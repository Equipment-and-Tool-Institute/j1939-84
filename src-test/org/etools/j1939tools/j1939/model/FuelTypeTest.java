/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939tools.j1939.model;

import static org.etools.j1939tools.j1939.model.FuelType.BATT_ELEC;
import static org.etools.j1939tools.j1939.model.FuelType.BI_CNG;
import static org.etools.j1939tools.j1939.model.FuelType.BI_DSL;
import static org.etools.j1939tools.j1939.model.FuelType.BI_ELEC;
import static org.etools.j1939tools.j1939.model.FuelType.BI_ETH;
import static org.etools.j1939tools.j1939.model.FuelType.BI_GAS;
import static org.etools.j1939tools.j1939.model.FuelType.BI_LPG;
import static org.etools.j1939tools.j1939.model.FuelType.BI_METH;
import static org.etools.j1939tools.j1939.model.FuelType.BI_MIX;
import static org.etools.j1939tools.j1939.model.FuelType.BI_NG;
import static org.etools.j1939tools.j1939.model.FuelType.BI_PROP;
import static org.etools.j1939tools.j1939.model.FuelType.CNG;
import static org.etools.j1939tools.j1939.model.FuelType.DSL;
import static org.etools.j1939tools.j1939.model.FuelType.DSL_CNG;
import static org.etools.j1939tools.j1939.model.FuelType.DSL_LNG;
import static org.etools.j1939tools.j1939.model.FuelType.ETH;
import static org.etools.j1939tools.j1939.model.FuelType.GAS;
import static org.etools.j1939tools.j1939.model.FuelType.HYB_DSL;
import static org.etools.j1939tools.j1939.model.FuelType.HYB_ELEC;
import static org.etools.j1939tools.j1939.model.FuelType.HYB_ETH;
import static org.etools.j1939tools.j1939.model.FuelType.HYB_GAS;
import static org.etools.j1939tools.j1939.model.FuelType.HYB_MIX;
import static org.etools.j1939tools.j1939.model.FuelType.HYB_REG;
import static org.etools.j1939tools.j1939.model.FuelType.LPG;
import static org.etools.j1939tools.j1939.model.FuelType.METH;
import static org.etools.j1939tools.j1939.model.FuelType.NG;
import static org.etools.j1939tools.j1939.model.FuelType.NGV;
import static org.etools.j1939tools.j1939.model.FuelType.NONE;
import static org.etools.j1939tools.j1939.model.FuelType.PROP;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.etools.testdoc.TestDoc;
import org.junit.Test;

@TestDoc(description = "Verify that the FuelType constants match the specification")
public class FuelTypeTest {

    @Test
    public void testCount() {
        assertEquals(29, FuelType.values().length);
    }

    @Test
    public void testOrder() {
        FuelType[] values = FuelType.values();
        assertEquals(BATT_ELEC, values[0]);
        assertEquals(BI_CNG, values[1]);
        assertEquals(BI_DSL, values[2]);
        assertEquals(BI_ELEC, values[3]);
        assertEquals(BI_ETH, values[4]);
        assertEquals(BI_GAS, values[5]);
        assertEquals(BI_LPG, values[6]);
        assertEquals(BI_METH, values[7]);
        assertEquals(BI_MIX, values[8]);
        assertEquals(BI_NG, values[9]);
        assertEquals(BI_PROP, values[10]);
        assertEquals(CNG, values[11]);
        assertEquals(DSL, values[12]);
        assertEquals(DSL_CNG, values[13]);
        assertEquals(DSL_LNG, values[14]);
        assertEquals(ETH, values[15]);
        assertEquals(GAS, values[16]);
        assertEquals(HYB_DSL, values[17]);
        assertEquals(HYB_ELEC, values[18]);
        assertEquals(HYB_ETH, values[19]);
        assertEquals(HYB_GAS, values[20]);
        assertEquals(HYB_MIX, values[21]);
        assertEquals(HYB_REG, values[22]);
        assertEquals(LPG, values[23]);
        assertEquals(METH, values[24]);
        assertEquals(NG, values[25]);
        assertEquals(NGV, values[26]);
        assertEquals(NONE, values[27]);
        assertEquals(PROP, values[28]);
    }

    @Test
    public void testToString() {
        assertEquals("Battery/electric vehicle", BATT_ELEC.toString());
        assertEquals("Bi-fuel vehicle using CNG", BI_CNG.toString());
        assertEquals("Bi-fuel vehicle using diesel", BI_DSL.toString());
        assertEquals("Bi-fuel vehicle using battery", BI_ELEC.toString());
        assertEquals("Bi-fuel vehicle using ethanol", BI_ETH.toString());
        assertEquals("Bi-fuel vehicle using gasoline", BI_GAS.toString());
        assertEquals("Bi-fuel vehicle using LPG", BI_LPG.toString());
        assertEquals("Bi-fuel vehicle using methanol", BI_METH.toString());
        assertEquals("Bi-fuel vehicle using battery and combustion engine", BI_MIX.toString());
        assertEquals("Bi-fuel vehicle using NG", BI_NG.toString());
        assertEquals("Bi-fuel vehicle using propane", BI_PROP.toString());
        assertEquals("Compressed Natural Gas vehicle", CNG.toString());
        assertEquals("Diesel vehicle", DSL.toString());
        assertEquals("Dual Fuel vehicle – Diesel and CNG", DSL_CNG.toString());
        assertEquals("Dual Fuel vehicle – Diesel and LNG", DSL_LNG.toString());
        assertEquals("Ethanol vehicle", ETH.toString());
        assertEquals("Gasoline/petrol vehicle", GAS.toString());
        assertEquals("Hybrid vehicle using diesel engine", HYB_DSL.toString());
        assertEquals("Hybrid vehicle using battery", HYB_ELEC.toString());
        assertEquals("Hybrid vehicle using gasoline engine on ethanol", HYB_ETH.toString());
        assertEquals("Hybrid vehicle using gasoline engine", HYB_GAS.toString());
        assertEquals("Hybrid vehicle using battery and combustion engine", HYB_MIX.toString());
        assertEquals("Hybrid vehicle in regeneration mode", HYB_REG.toString());
        assertEquals("Liquefied Petroleum Gas vehicle", LPG.toString());
        assertEquals("Methanol vehicle", METH.toString());
        assertEquals("Natural Gas vehicle (CNG or LNG)", NG.toString());
        assertEquals("Natural Gas vehicle", NGV.toString());
        assertEquals("Not available", NONE.toString());
        assertEquals("Propane vehicle", PROP.toString());
    }

    @Test
    public void testValues() {
        assertEquals(0x08, BATT_ELEC.getValue());
        assertEquals(0x0D, BI_CNG.getValue());
        assertEquals(0x19, BI_DSL.getValue());
        assertEquals(0x0F, BI_ELEC.getValue());
        assertEquals(0x0B, BI_ETH.getValue());
        assertEquals(0x09, BI_GAS.getValue());
        assertEquals(0x0C, BI_LPG.getValue());
        assertEquals(0x0A, BI_METH.getValue());
        assertEquals(0x10, BI_MIX.getValue());
        assertEquals(0x18, BI_NG.getValue());
        assertEquals(0x0E, BI_PROP.getValue());
        assertEquals(0x06, CNG.getValue());
        assertEquals(0x04, DSL.getValue());
        assertEquals(0x1B, DSL_CNG.getValue());
        assertEquals(0x1C, DSL_LNG.getValue());
        assertEquals(0x03, ETH.getValue());
        assertEquals(0x01, GAS.getValue());
        assertEquals(0x13, HYB_DSL.getValue());
        assertEquals(0x14, HYB_ELEC.getValue());
        assertEquals(0x12, HYB_ETH.getValue());
        assertEquals(0x11, HYB_GAS.getValue());
        assertEquals(0x15, HYB_MIX.getValue());
        assertEquals(0x16, HYB_REG.getValue());
        assertEquals(0x05, LPG.getValue());
        assertEquals(0x02, METH.getValue());
        assertEquals(0x1A, NG.getValue());
        assertEquals(0x17, NGV.getValue());
        assertEquals(0x00, NONE.getValue());
        assertEquals(0x07, PROP.getValue());
    }

    @Test
    public void isCompressionIgnition() {
        assertTrue(BI_DSL.isCompressionIgnition());
        assertFalse(BI_PROP.isCompressionIgnition());
    }

    @Test
    public void isSparkIgnition() {
        assertTrue(BI_METH.isSparkIgnition());
        assertFalse(BI_ELEC.isSparkIgnition());
    }

    @Test
    public void isHybrid() {
        assertTrue(HYB_ELEC.isHybrid());
        assertFalse(BI_GAS.isHybrid());
    }

    @Test
    public void isElectric() {
        assertTrue(BATT_ELEC.isElectric());
        assertFalse(BI_GAS.isElectric());
    }

}
