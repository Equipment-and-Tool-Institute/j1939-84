/**
 * Copyright (c) 2019. Equipment & Tool Institute
 */
package org.etools.j1939_84.model;

import static org.etools.j1939_84.J1939_84.NL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.etools.testdoc.TestDoc;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit Test for the {@link VehicleInformation} class
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
@TestDoc(description = "Verifies that all elelments of Vehicle Information are accounted for when comparing.")
public class VehicleInformationTest {

    private VehicleInformation instance;

    private VehicleInformation instance2;

    private VehicleInformation instance3;

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

        instance3 = instance.clone();
    }

    @Test
    @TestDoc(description = "Verify equals on null, an Object that is not a VehicleInformation, this and an object that is the same as this.")
    public void testEquals() {
        assertFalse(instance.equals(null));
        assertFalse(instance.equals(new Object()));
        assertTrue(instance.equals(instance));
        assertTrue(instance.equals(instance2));
        assertTrue(instance.equals(instance3));
    }

    @Test
    @TestDoc(description = "Verify hashcodes are repeatable and equal for this and an object the same as this.")
    public void testHashCode() {
        assertTrue(instance.hashCode() == instance.hashCode());
        assertTrue(instance.hashCode() == instance2.hashCode());
        assertTrue(instance.hashCode() == instance3.hashCode());
    }

    @Test
    @TestDoc(description = "Verify a certification is detected in comparison.")
    public void testNotEqualsCertificationIntent() {
        instance2.setCertificationIntent(null);
        assertFalse(instance.equals(instance2));
    }

    @Test
    @TestDoc(description = "Verify a emission units is detected in comparison.")
    public void testNotEqualsEmissionUnits() {
        instance2.setEmissionUnits(0);
        assertFalse(instance.equals(instance2));
    }

    @Test
    @TestDoc(description = "Verify a engine model year is detected in comparison.")
    public void testNotEqualsEngineModelYear() {
        instance2.setEngineModelYear(0);
        assertFalse(instance.equals(instance2));
    }

    @Test
    @TestDoc(description = "Verify a fuel type is detected in comparison.")
    public void testNotEqualsFuelType() {
        instance2.setFuelType(null);
        assertFalse(instance.equals(instance2));
    }

    @Test
    @TestDoc(description = "Verify a vehicle module year is detected in comparison.")
    public void testNotEqualsVehicleModelYear() {
        instance2.setVehicleModelYear(0);
        assertFalse(instance.equals(instance2));
    }

    @Test
    @TestDoc(description = "Verify a vin is detected in comparison.")
    public void testNotEqualsVin() {
        instance2.setVin(null);
        assertFalse(instance.equals(instance2));
    }

    @Test
    @TestDoc(description = "Detect regressions in the display of VehicleInformation.")
    public void testToString() {
        String actual = instance.toString();
        String expected = "User Data Entry: " + NL + NL +
                "Engine Model Emissions Year: 1" + NL +
                "Number of Emissions ECUs Expected: 4" + NL +
                "Number of CAL IDs Expected: 0" + NL +
                "Fuel Type: Diesel vehicle" + NL +
                "Ignition Type: Compression" + NL +
                "Number of Trips for Fault B Implant: 0" + NL + NL +
                "Vehicle Information:" + NL +
                "VIN: vin" + NL +
                "Vehicle MY: 2" + NL +
                "Engine MY: 1" + NL +
                "Cert. Engine Family: cert" + NL +
                "Number of OBD ECUs Found: 0" + NL + NL +
                "Number of CAL IDs Found: 0" + NL + NL;
        assertEquals(expected, actual);
    }
}
