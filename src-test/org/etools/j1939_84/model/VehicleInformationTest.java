/**
 * Copyright (c) 2019. Equipment & Tool Institute
 */
package org.etools.j1939_84.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit Test for the {@link VehicleInformation} class
 * 
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class VehicleInformationTest {

	private VehicleInformation instance;

	private VehicleInformation instance2;

	@Before
	public void setUp() {
		instance = new VehicleInformation();
		instance.setCertificationIntent("cert");
		instance.setEmissionUnits(4);
		instance.setEngineModelYear(1);
		instance.setFuelType(FuelType.DSL);
		instance.setVehicleModelYear(2);
		instance.setVin("vin");

		instance2 = new VehicleInformation();
		instance2.setCertificationIntent("cert");
		instance2.setEmissionUnits(4);
		instance2.setEngineModelYear(1);
		instance2.setFuelType(FuelType.DSL);
		instance2.setVehicleModelYear(2);
		instance2.setVin("vin");
	}

	@Test
	public void testEquals() {
		assertFalse(instance.equals(new Object()));
		assertTrue(instance.equals(instance));
		assertTrue(instance.equals(instance2));
	}

	@Test
	public void testHashCode() {
		assertTrue(instance.hashCode() == instance2.hashCode());
		assertTrue(instance.hashCode() == instance2.hashCode());
	}

	@Test
	public void testNotEqualsCertificationIntent() {
		instance2.setCertificationIntent(null);
		assertFalse(instance.equals(instance2));
	}

	@Test
	public void testNotEqualsEmissionUnits() {
		instance2.setEmissionUnits(0);
		assertFalse(instance.equals(instance2));
	}

	@Test
	public void testNotEqualsEngineModelYear() {
		instance2.setEngineModelYear(0);
		assertFalse(instance.equals(instance2));
	}

	@Test
	public void testNotEqualsFuelType() {
		instance2.setFuelType(null);
		assertFalse(instance.equals(instance2));
	}

	@Test
	public void testNotEqualsVehicleModelYear() {
		instance2.setVehicleModelYear(0);
		assertFalse(instance.equals(instance2));
	}

	@Test
	public void testNotEqualsVin() {
		instance2.setVin(null);
		assertFalse(instance.equals(instance2));
	}

	@Test
	public void testToString() {
		String actual = instance.toString();
		String expected = "VehicleInformation:\n" + "Certification: cert\n" + "Emissions: 4\n" + "Engine MY: 1\n"
				+ "FuelType: Diesel vehicle\n" + "Vehicle MY: 2\n" + "VIN: vin";
		assertEquals(expected, actual);
	}
}
