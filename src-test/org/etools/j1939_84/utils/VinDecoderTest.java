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
		assertEquals(2010, instance.getModelYear("LPU918TW3AS9H90U7"));
		assertEquals(2011, instance.getModelYear("C772J2XJ3B7ESG0VP"));
		assertEquals(2012, instance.getModelYear("PEFFDD4Y3C8AYT0KP"));
		assertEquals(2013, instance.getModelYear("3DRWN9784DZBMGPG7"));
		assertEquals(2014, instance.getModelYear("7RM06PXG6EF9Z1YY1"));
		assertEquals(2015, instance.getModelYear("B6GL6DN95FCJE8U0W"));
		assertEquals(2016, instance.getModelYear("1SEGU7GT5G4ZEXS2R"));
		assertEquals(2017, instance.getModelYear("7B00Z54Z5H2NYESSY"));
		assertEquals(2018, instance.getModelYear("P00PMXW05JPGCTL72"));
		assertEquals(2019, instance.getModelYear("5EKRP8F85KKK77XUK"));
		assertEquals(2020, instance.getModelYear("9UD0ZKSD9LYWL9MCZ"));
		assertEquals(2021, instance.getModelYear("WY99YFFT9M1AV3W8V"));
		assertEquals(2022, instance.getModelYear("4E5JNFX60NCBHTA70"));
		assertEquals(2023, instance.getModelYear("9KPV2PCWXP8WERUHD"));
		assertEquals(2024, instance.getModelYear("PX0W9XD10RZL37HD7"));
		assertEquals(2025, instance.getModelYear("XN1J5DTM2S4XKDYGF"));
		assertEquals(2026, instance.getModelYear("7ZS6H3H54TUZ9WUZS"));
		assertEquals(2027, instance.getModelYear("1B0JLLYZ7V3VZA2SC"));
		assertEquals(2028, instance.getModelYear("HGMKPG8U4WXPJNYEE"));
		assertEquals(2029, instance.getModelYear("57AE75W58X9JWWW1V"));
		assertEquals(2030, instance.getModelYear("F2FJ23E95Y8702102"));
		assertEquals(2031, instance.getModelYear("K4F6V97D719T8T25S"));
		assertEquals(2032, instance.getModelYear("TAPH8XWE323R5AUY1"));
		assertEquals(2033, instance.getModelYear("66G5ZS6C93ZE96UAK"));
		assertEquals(2034, instance.getModelYear("NT6PWENT64CEN9TMD"));
		assertEquals(2035, instance.getModelYear("UE2W812K75HJ4RH5G"));
		assertEquals(2036, instance.getModelYear("14Z1YS83X6PZE13BX"));
		assertEquals(2037, instance.getModelYear("1J8FF57W67HRLJKHT"));
		assertEquals(2038, instance.getModelYear("NWPSN256382KRTURV"));
		assertEquals(2039, instance.getModelYear("P5Z1FPK699SRBDPJ0"));
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
