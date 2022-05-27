/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939tools.j1939.packets;

import static org.junit.Assert.assertEquals;

import java.nio.charset.StandardCharsets;

import org.etools.j1939tools.bus.Packet;
import org.etools.j1939tools.modules.DateTimeModule;
import org.etools.j1939tools.modules.TestDateTimeModule;
import org.etools.testdoc.TestDoc;
import org.etools.testdoc.TestItem;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * The Unit tests for the {@link VehicleIdentificationPacket} class
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class VehicleIdentificationPacketTest {

    @Before
    public void setUp() {
        DateTimeModule.setInstance(new TestDateTimeModule());
    }

    @After
    public void tearDown() {
        DateTimeModule.setInstance(null);
    }

    @Test
    @TestDoc(description = "Verify packet is parsed and the report string is generated correctly.", value = {
            @TestItem(verifies = "6.1.1.1.e.i") })
    public void testGetVinAndToString() {
        Packet packet = Packet.create(0,
                                      0,
                                      0x33,
                                      0x48,
                                      0x41,
                                      0x4D,
                                      0x4B,
                                      0x53,
                                      0x54,
                                      0x4E,
                                      0x30,
                                      0x46,
                                      0x4C,
                                      0x35,
                                      0x37,
                                      0x35,
                                      0x30,
                                      0x31,
                                      0x32,
                                      0x2A);
        VehicleIdentificationPacket instance = new VehicleIdentificationPacket(packet);
        assertEquals("3HAMKSTN0FL575012", instance.getVin());
        assertEquals("Vehicle Identification from Engine #1 (0): 3HAMKSTN0FL575012", instance.toString());
        assertEquals("", instance.getManufacturerData());
    }

    @Test
    @TestDoc(description = "Verify packet with manufacturer data is parsed out.")
    public void testGetVinWithManufacturerData() {
        String bytes = "Lorem ipsum*dolor sit amet";
        Packet packet = Packet.create(0, 0, bytes.getBytes(StandardCharsets.UTF_8));
        VehicleIdentificationPacket instance = new VehicleIdentificationPacket(packet);
        assertEquals("Lorem ipsum", instance.getVin());
        assertEquals("dolor sit amet", instance.getManufacturerData());
    }

    @Test
    @TestDoc(description = "Verify packet with no * is parsed.")
    public void testGetVinWithoutAsterisk() {
        Packet packet = Packet.create(0,
                                      0,
                                      0x33,
                                      0x48,
                                      0x41,
                                      0x4D,
                                      0x4B,
                                      0x53,
                                      0x54,
                                      0x4E,
                                      0x30,
                                      0x46,
                                      0x4C,
                                      0x35,
                                      0x37,
                                      0x35,
                                      0x30,
                                      0x31,
                                      0x32);
        VehicleIdentificationPacket instance = new VehicleIdentificationPacket(packet);
        assertEquals("3HAMKSTN0FL575012", instance.getVin());
        assertEquals("", instance.getManufacturerData());
    }

    @Test
    @TestDoc(description = "Verify packet with manufacturer data is parsed out.")
    public void testGetVinWithoutAsteriskWith200Characters() {
        String expected = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Phasellus ultrices vehicula elit, id pharetra lacus. Suspendisse justo nulla, egestas vel volutpat vel, convallis at nisl. Nulla facilisi amet";
        Packet packet = Packet.create(0, 0, (expected + "*").getBytes(StandardCharsets.UTF_8));
        VehicleIdentificationPacket instance = new VehicleIdentificationPacket(packet);
        assertEquals(expected, instance.getVin());
        assertEquals("", instance.getManufacturerData());
    }

    @Test
    public void testGetVinWithoutAsteriskWithNoCharacters() {
        Packet packet = Packet.create(0, 0, 0x2A);
        VehicleIdentificationPacket instance = new VehicleIdentificationPacket(packet);
        assertEquals("", instance.getVin());
        assertEquals("", instance.getManufacturerData());
    }
}
