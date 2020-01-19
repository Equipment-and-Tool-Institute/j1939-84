package org.etools.j1939_84.bus.j1939.packets;

import static org.junit.Assert.assertEquals;

import java.nio.charset.StandardCharsets;

import org.etools.j1939_84.bus.Packet;
import org.junit.Test;

/**
 * The Unit tests for the {@link DM56EngineFamilyPacketTest} class
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class DM56EngineFamilyPacketTest {

    @Test
    public void testEngineModelYear() {
        String bytes = "2011E-MYUS HD OBD";
        Packet packet = Packet.create(0, 0, bytes.getBytes(StandardCharsets.UTF_8));
        DM56EngineFamilyPacket instance = new DM56EngineFamilyPacket(packet);
        assertEquals("2011E-MY", instance.getModelYearField());
        assertEquals(new Integer(2011), instance.getEngineModelYear());
        assertEquals(null, instance.getVehicleModelYear());
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
        String bytes = "2018V-MYUS HD OBD";
        Packet packet = Packet.create(0, 0, bytes.getBytes(StandardCharsets.UTF_8));
        DM56EngineFamilyPacket instance = new DM56EngineFamilyPacket(packet);
        String expected = "";
        expected += "Model Year and Certification Engine Family from Engine #1 (0): \n";
        expected += "Model Year: 2018V-MY\n";
        expected += "Family Name: US HD OBD";
        assertEquals(expected, instance.toString());
    }

    @Test
    public void testVehicleModelYear() {
        String bytes = "2039V-MYCALIF HD OBD";
        Packet packet = Packet.create(0, 0, bytes.getBytes(StandardCharsets.UTF_8));
        DM56EngineFamilyPacket instance = new DM56EngineFamilyPacket(packet);
        assertEquals("2039V-MY", instance.getModelYearField());
        assertEquals(null, instance.getEngineModelYear());
        assertEquals(new Integer(2039), instance.getVehicleModelYear());
    }
}
