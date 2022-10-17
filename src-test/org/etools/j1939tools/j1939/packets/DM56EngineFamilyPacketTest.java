package org.etools.j1939tools.j1939.packets;

import static org.etools.j1939_84.J1939_84.NL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.nio.charset.StandardCharsets;

import org.etools.j1939tools.bus.Packet;
import org.etools.testdoc.TestDoc;
import org.junit.Test;

/**
 * The Unit tests for the {@link DM56EngineFamilyPacketTest} class
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
@TestDoc(description = "Verify DM56 decoding.")
public class DM56EngineFamilyPacketTest {

    @Test
    public void testEngineModelYear() {
        String bytes = "2011E-MYUS HD OBD";
        Packet packet = Packet.create(0, 0, bytes.getBytes(StandardCharsets.UTF_8));
        DM56EngineFamilyPacket instance = new DM56EngineFamilyPacket(packet);
        assertEquals("2011E-MY", instance.getModelYearField());
        assertEquals(Integer.valueOf(2011), instance.getEngineModelYear());
        assertNull(instance.getVehicleModelYear());
    }

    @Test
    public void testFamilyNameWithAsterisk() {
        String bytes = "2039V-MYCALIF HD OBD*";
        Packet packet = Packet.create(0, 0, bytes.getBytes(StandardCharsets.UTF_8));
        DM56EngineFamilyPacket instance = new DM56EngineFamilyPacket(packet);
        assertEquals("CALIF HD OBD", instance.getFamilyName());
    }

    @Test
    public void testFamilyNameWithoutAsterisk() {
        String bytes = "2039V-MYEURO VI/UNECE R49";
        Packet packet = Packet.create(0, 0, bytes.getBytes(StandardCharsets.UTF_8));
        DM56EngineFamilyPacket instance = new DM56EngineFamilyPacket(packet);
        assertEquals("EURO VI/UNECE R49", instance.getFamilyName());
    }

    @Test
    public void testToString() {
        DM56EngineFamilyPacket instance = DM56EngineFamilyPacket.create(0, 2018, false, "US HD OBD");
        String expected = "";
        expected += "Model Year and Certification Engine Family from Engine #1 (0): " + NL;
        expected += "Model Year: 2018V-MY" + NL;
        expected += "Family Name: US HD OBD";
        assertEquals(expected, instance.toString());
    }

    @Test
    public void testVehicleModelYear() {
        String bytes = "2039V-MYCALIF HD OBD";
        Packet packet = Packet.create(0, 0, bytes.getBytes(StandardCharsets.UTF_8));
        DM56EngineFamilyPacket instance = new DM56EngineFamilyPacket(packet);
        assertEquals("2039V-MY", instance.getModelYearField());
        assertNull(instance.getEngineModelYear());
        assertEquals(Integer.valueOf(2039), instance.getVehicleModelYear());
    }

    @Test
    public void testReal1() {
        String bytes = "2024E-MYRPCRH12.9.21*000";
        Packet packet = Packet.create(0, 0, bytes.getBytes(StandardCharsets.UTF_8));
        DM56EngineFamilyPacket instance = new DM56EngineFamilyPacket(packet);
        assertEquals("2024E-MY", instance.getModelYearField());
        assertEquals("RPCRH12.9.21", instance.getFamilyName());
        assertEquals(12, instance.getFamilyName().length());
    }

    @Test
    public void testReal2() {
        String bytes = "2024E-MYRPCRH12.9.21";
        Packet packet = Packet.create(0, 0, bytes.getBytes(StandardCharsets.UTF_8));
        DM56EngineFamilyPacket instance = new DM56EngineFamilyPacket(packet);
        assertEquals("2024E-MY", instance.getModelYearField());
        assertEquals("RPCRH12.9.21", instance.getFamilyName());
        assertEquals(12, instance.getFamilyName().length());
    }

    @Test
    public void testReal4() {
        String bytes = "2024E-MYRPCRH12.9.21XXX*\0";
        Packet packet = Packet.create(0, 0, bytes.getBytes(StandardCharsets.UTF_8));
        DM56EngineFamilyPacket instance = new DM56EngineFamilyPacket(packet);
        assertEquals("2024E-MY", instance.getModelYearField());
        assertEquals("RPCRH12.9.21XXX", instance.getFamilyName());
        assertEquals(15, instance.getFamilyName().length());
    }

    @Test
    public void testReal3() {
        String bytes = "2024E-MYRPCRH12.9.21\0\0\0";
        Packet packet = Packet.create(0, 0, bytes.getBytes(StandardCharsets.UTF_8));
        DM56EngineFamilyPacket instance = new DM56EngineFamilyPacket(packet);
        assertEquals("2024E-MY", instance.getModelYearField());
        assertEquals("RPCRH12.9.21", instance.getFamilyName());
        assertEquals(12, instance.getFamilyName().length());
    }
}
