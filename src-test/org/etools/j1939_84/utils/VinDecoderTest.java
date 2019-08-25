/**
 * Copyright (c) 2019. Equipment & Tool Institute
 */
package org.etools.j1939_84.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class VinDecoderTest {

    private VinDecoder instance;

    @Before
    public void setUp() {
        instance = new VinDecoder();
    }

    @Test
    public void testGetModelYear() {
        assertEquals(2010, instance.getModelYear("726PLAGW0A01D2339"));
        assertEquals(2011, instance.getModelYear("BPW309DY4BRML7951"));
        assertEquals(2012, instance.getModelYear("PS32AHRF9CMV68772"));
        assertEquals(2013, instance.getModelYear("890WY5GL8D7U36134"));
        assertEquals(2014, instance.getModelYear("Y9WTKEP13E50R5246"));
        assertEquals(2015, instance.getModelYear("LLADWX593FLVJ5065"));
        assertEquals(2016, instance.getModelYear("L61A8UXM6GWR21346"));
        assertEquals(2017, instance.getModelYear("TV5UD90M3H6DU0452"));
        assertEquals(2018, instance.getModelYear("SS54HF012JKJ89958"));
        assertEquals(2019, instance.getModelYear("RV2M3HLC8KRVY8256"));
        assertEquals(2020, instance.getModelYear("9R3SDLYJ2LE3A0368"));
        assertEquals(2021, instance.getModelYear("DY0TWE1J4MN9F3376"));
        assertEquals(2022, instance.getModelYear("M828RYKLXNPB25823"));
        assertEquals(2023, instance.getModelYear("N7JDSGR32PSLS6495"));
        assertEquals(2024, instance.getModelYear("HTVLG7V8XR0658779"));
        assertEquals(2025, instance.getModelYear("JDFKATNW2SNHE2645"));
        assertEquals(2026, instance.getModelYear("R37P8T4B0TD0W9194"));
        assertEquals(2027, instance.getModelYear("36JFM6U57VWJE6400"));
        assertEquals(2028, instance.getModelYear("9B8KNEZY8WYCE4680"));
        assertEquals(2029, instance.getModelYear("EKCKCEPB5XCC03539"));
        assertEquals(2030, instance.getModelYear("K3CV77CM5Y58S3528"));
        assertEquals(2031, instance.getModelYear("8V0E8GDU81G132838"));
        assertEquals(2032, instance.getModelYear("MK4FDL0Y42K8Y9930"));
        assertEquals(2033, instance.getModelYear("WZRTJ90MX3UWB3530"));
        assertEquals(2034, instance.getModelYear("CNH003FP34F285830"));
        assertEquals(2035, instance.getModelYear("2MRKRPW295S6V7673"));
        assertEquals(2036, instance.getModelYear("2Y9DWG7U96PPC9940"));
        assertEquals(2037, instance.getModelYear("BTWB7VUP475RP2031"));
        assertEquals(2038, instance.getModelYear("6PRJF3VS08H9R3889"));
        assertEquals(2039, instance.getModelYear("92K46JCC09K7Z3062"));
    }

    @Test
    public void testIsVinValid() {
        assertFalse("Empty not allowed", instance.isVinValid(""));
        assertFalse("Null not allowed", instance.isVinValid(null));

        assertFalse("Too short", instance.isVinValid("1234567890123456"));
        assertFalse("Too long", instance.isVinValid("2G1WB5E37E11105673"));

        assertFalse("Letters and numbers only", instance.isVinValid("~!@#$%^&*()_+`=[]"));
        assertFalse("Letters and numbers only", instance.isVinValid("09876543210_+`=[]"));
        assertFalse("Letters and numbers only", instance.isVinValid("{}<>,./?\\|:;'1234"));

        assertFalse("I is not allowed", instance.isVinValid("IIIIIIIIIIIIIIIII"));
        assertFalse("O is not allowed", instance.isVinValid("OOOOOOOOOOOOOOOOO"));
        assertFalse("Q is not allowed", instance.isVinValid("QQQQQQQQQQQQQQQQQ"));

        assertFalse("Bad Check digit", instance.isVinValid("2G1WB5E36E1110567"));

        assertFalse("Non-numeric sequence", instance.isVinValid("PX0W9XD10RZL37HD7"));

        assertTrue("VIN is valid", instance.isVinValid("2G1WB5E37E1110567"));
    }

    @Test
    public void testSanitize() {
        String actual = instance.sanitize(
                "`1234567890-=~!@#$%^&*()_+qwertyuiop[]\\QWERTYUIOP{}|asdfghjkl;'ASDFGHJKL:\"zxcvbnm,./ZXCVBNM<>?");
        String expected = "1234567890WERTYUPWERTYUPASDFGHJKLASDFGHJKLZXCVBNMZXCVBNM";
        assertEquals(expected, actual);
    }
}
