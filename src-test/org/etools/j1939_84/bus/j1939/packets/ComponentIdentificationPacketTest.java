/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.j1939.packets;

import static org.junit.Assert.assertEquals;

import java.nio.charset.StandardCharsets;

import org.etools.j1939_84.bus.Packet;
import org.junit.Test;

/**
 * Unit test for the {@link ComponentIdentificationPacket} class
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class ComponentIdentificationPacketTest {
	private static final String MAKE = "Solid Design";
	private static final String MODEL = "J1939-84 Tool";
	private static final String SN = "000001";
	private static final String UN = "1234567890";

	@Test
	public void testMake() {
		byte[] data = (MAKE + "****").getBytes(StandardCharsets.UTF_8);
		Packet packet = Packet.create(0, 0, data);
		ComponentIdentificationPacket instance = new ComponentIdentificationPacket(packet);
		assertEquals(MAKE, instance.getMake());
		assertEquals("", instance.getModel());
		assertEquals("", instance.getSerialNumber());
		assertEquals("", instance.getUnitNumber());
		String expected = "Found Engine #1 (0): Make: Solid Design, Model: , Serial: , Unit: ";
		assertEquals(expected, instance.toString());
	}

	@Test
	public void testMakeModel() {
		byte[] data = (MAKE + "*" + MODEL + "***").getBytes(StandardCharsets.UTF_8);
		Packet packet = Packet.create(0, 0, data);
		ComponentIdentificationPacket instance = new ComponentIdentificationPacket(packet);
		assertEquals(MAKE, instance.getMake());
		assertEquals(MODEL, instance.getModel());
		assertEquals("", instance.getSerialNumber());
		assertEquals("", instance.getUnitNumber());
		String expected = "Found Engine #1 (0): Make: Solid Design, Model: J1939-84 Tool, Serial: , Unit: ";
		assertEquals(expected, instance.toString());
	}

	@Test
	public void testMakeModelSerialNumberUnitNumber() {
		byte[] data = (MAKE + "*" + MODEL + "*" + SN + "*" + UN + "*").getBytes(StandardCharsets.UTF_8);
		Packet packet = Packet.create(0, 0, data);
		ComponentIdentificationPacket instance = new ComponentIdentificationPacket(packet);
		assertEquals(MAKE, instance.getMake());
		assertEquals(MODEL, instance.getModel());
		assertEquals(SN, instance.getSerialNumber());
		assertEquals(UN, instance.getUnitNumber());
		String expected = "Found Engine #1 (0): Make: Solid Design, Model: J1939-84 Tool, Serial: 000001, Unit: 1234567890";
		assertEquals(expected, instance.toString());
	}

	@Test
	public void testMakeModelUnitNumber() {
		byte[] data = (MAKE + "*" + MODEL + "**" + UN + "*").getBytes(StandardCharsets.UTF_8);
		Packet packet = Packet.create(0, 0, data);
		ComponentIdentificationPacket instance = new ComponentIdentificationPacket(packet);
		assertEquals(MAKE, instance.getMake());
		assertEquals(MODEL, instance.getModel());
		assertEquals("", instance.getSerialNumber());
		assertEquals(UN, instance.getUnitNumber());
		String expected = "Found Engine #1 (0): Make: Solid Design, Model: J1939-84 Tool, Serial: , Unit: 1234567890";
		assertEquals(expected, instance.toString());
	}

	@Test
	public void testMakeSerialNumber() {
		byte[] data = (MAKE + "**" + SN + "**").getBytes(StandardCharsets.UTF_8);
		Packet packet = Packet.create(0, 0, data);
		ComponentIdentificationPacket instance = new ComponentIdentificationPacket(packet);
		assertEquals(MAKE, instance.getMake());
		assertEquals("", instance.getModel());
		assertEquals(SN, instance.getSerialNumber());
		assertEquals("", instance.getUnitNumber());
		String expected = "Found Engine #1 (0): Make: Solid Design, Model: , Serial: 000001, Unit: ";
		assertEquals(expected, instance.toString());
	}

	@Test
	public void testMakeSerialNumberUnitNumber() {
		byte[] data = (MAKE + "**" + SN + "*" + UN + "*").getBytes(StandardCharsets.UTF_8);
		Packet packet = Packet.create(0, 0, data);
		ComponentIdentificationPacket instance = new ComponentIdentificationPacket(packet);
		assertEquals(MAKE, instance.getMake());
		assertEquals("", instance.getModel());
		assertEquals(SN, instance.getSerialNumber());
		assertEquals(UN, instance.getUnitNumber());
		String expected = "Found Engine #1 (0): Make: Solid Design, Model: , Serial: 000001, Unit: 1234567890";
		assertEquals(expected, instance.toString());
	}

	@Test
	public void testMakeUnitNumber() {
		byte[] data = (MAKE + "***" + UN + "*").getBytes(StandardCharsets.UTF_8);
		Packet packet = Packet.create(0, 0, data);
		ComponentIdentificationPacket instance = new ComponentIdentificationPacket(packet);
		assertEquals(MAKE, instance.getMake());
		assertEquals("", instance.getModel());
		assertEquals("", instance.getSerialNumber());
		assertEquals(UN, instance.getUnitNumber());
		String expected = "Found Engine #1 (0): Make: Solid Design, Model: , Serial: , Unit: 1234567890";
		assertEquals(expected, instance.toString());
	}

	@Test
	public void testModel() {
		byte[] data = ("*" + MODEL + "***").getBytes(StandardCharsets.UTF_8);
		Packet packet = Packet.create(0, 0, data);
		ComponentIdentificationPacket instance = new ComponentIdentificationPacket(packet);
		assertEquals("", instance.getMake());
		assertEquals(MODEL, instance.getModel());
		assertEquals("", instance.getSerialNumber());
		assertEquals("", instance.getUnitNumber());
		String expected = "Found Engine #1 (0): Make: , Model: J1939-84 Tool, Serial: , Unit: ";
		assertEquals(expected, instance.toString());
	}

	@Test
	public void testModelSerialNumber() {
		byte[] data = ("*" + MODEL + "*" + SN + "**").getBytes(StandardCharsets.UTF_8);
		Packet packet = Packet.create(0, 0, data);
		ComponentIdentificationPacket instance = new ComponentIdentificationPacket(packet);
		assertEquals("", instance.getMake());
		assertEquals(MODEL, instance.getModel());
		assertEquals(SN, instance.getSerialNumber());
		assertEquals("", instance.getUnitNumber());
		String expected = "Found Engine #1 (0): Make: , Model: J1939-84 Tool, Serial: 000001, Unit: ";
		assertEquals(expected, instance.toString());
	}

	@Test
	public void testModelSerialNumberUnitNumber() {
		byte[] data = ("*" + MODEL + "*" + SN + "*" + UN + "*").getBytes(StandardCharsets.UTF_8);
		Packet packet = Packet.create(0, 0, data);
		ComponentIdentificationPacket instance = new ComponentIdentificationPacket(packet);
		assertEquals("", instance.getMake());
		assertEquals(MODEL, instance.getModel());
		assertEquals(SN, instance.getSerialNumber());
		assertEquals(UN, instance.getUnitNumber());
		String expected = "Found Engine #1 (0): Make: , Model: J1939-84 Tool, Serial: 000001, Unit: 1234567890";
		assertEquals(expected, instance.toString());
	}

	@Test
	public void testModelUnitNumber() {
		byte[] data = ("*" + MODEL + "**" + UN + "*").getBytes(StandardCharsets.UTF_8);
		Packet packet = Packet.create(0, 0, data);
		ComponentIdentificationPacket instance = new ComponentIdentificationPacket(packet);
		assertEquals("", instance.getMake());
		assertEquals(MODEL, instance.getModel());
		assertEquals("", instance.getSerialNumber());
		assertEquals(UN, instance.getUnitNumber());
		String expected = "Found Engine #1 (0): Make: , Model: J1939-84 Tool, Serial: , Unit: 1234567890";
		assertEquals(expected, instance.toString());
	}

	@Test
	public void testNoStars() {
		byte[] data = (MAKE + MODEL + SN + UN).getBytes(StandardCharsets.UTF_8);
		Packet packet = Packet.create(0, 0, data);
		ComponentIdentificationPacket instance = new ComponentIdentificationPacket(packet);
		assertEquals(MAKE + MODEL + SN + UN, instance.getMake());
		assertEquals(null, instance.getModel());
		assertEquals(null, instance.getSerialNumber());
		assertEquals(null, instance.getUnitNumber());
		String expected = "Found Engine #1 (0): Make: Solid DesignJ1939-84 Tool0000011234567890, Model: null, Serial: null, Unit: null";
		assertEquals(expected, instance.toString());
	}

	@Test
	public void testNothing() {
		byte[] data = ("****").getBytes(StandardCharsets.UTF_8);
		Packet packet = Packet.create(0, 0, data);
		ComponentIdentificationPacket instance = new ComponentIdentificationPacket(packet);
		assertEquals("", instance.getMake());
		assertEquals("", instance.getModel());
		assertEquals("", instance.getSerialNumber());
		assertEquals("", instance.getUnitNumber());
		String expected = "Found Engine #1 (0): Make: , Model: , Serial: , Unit: ";
		assertEquals(expected, instance.toString());
	}

	@Test
	public void testOneStar() {
		byte[] data = (MAKE + "*" + MODEL + SN + UN).getBytes(StandardCharsets.UTF_8);
		Packet packet = Packet.create(0, 0, data);
		ComponentIdentificationPacket instance = new ComponentIdentificationPacket(packet);
		assertEquals(MAKE, instance.getMake());
		assertEquals(MODEL + SN + UN, instance.getModel());
		assertEquals(null, instance.getSerialNumber());
		assertEquals(null, instance.getUnitNumber());
		String expected = "Found Engine #1 (0): Make: Solid Design, Model: J1939-84 Tool0000011234567890, Serial: null, Unit: null";
		assertEquals(expected, instance.toString());
	}

	@Test
	public void testPGN() {
		assertEquals(65259, ComponentIdentificationPacket.PGN);
	}

	@Test
	public void testSerialNumber() {
		byte[] data = ("**" + SN + "**").getBytes(StandardCharsets.UTF_8);
		Packet packet = Packet.create(0, 0, data);
		ComponentIdentificationPacket instance = new ComponentIdentificationPacket(packet);
		assertEquals("", instance.getMake());
		assertEquals("", instance.getModel());
		assertEquals(SN, instance.getSerialNumber());
		assertEquals("", instance.getUnitNumber());
		String expected = "Found Engine #1 (0): Make: , Model: , Serial: 000001, Unit: ";
		assertEquals(expected, instance.toString());
	}

	@Test
	public void testSerialNumberUnitNumber() {
		byte[] data = ("**" + SN + "*" + UN + "*").getBytes(StandardCharsets.UTF_8);
		Packet packet = Packet.create(0, 0, data);
		ComponentIdentificationPacket instance = new ComponentIdentificationPacket(packet);
		assertEquals("", instance.getMake());
		assertEquals("", instance.getModel());
		assertEquals(SN, instance.getSerialNumber());
		assertEquals(UN, instance.getUnitNumber());
		String expected = "Found Engine #1 (0): Make: , Model: , Serial: 000001, Unit: 1234567890";
		assertEquals(expected, instance.toString());
	}

	@Test
	public void testThreeStars() {
		byte[] data = (MAKE + "*" + MODEL + "*" + SN + "*" + UN).getBytes(StandardCharsets.UTF_8);
		Packet packet = Packet.create(0, 0, data);
		ComponentIdentificationPacket instance = new ComponentIdentificationPacket(packet);
		assertEquals(MAKE, instance.getMake());
		assertEquals(MODEL, instance.getModel());
		assertEquals(SN, instance.getSerialNumber());
		assertEquals(UN, instance.getUnitNumber());
		String expected = "Found Engine #1 (0): Make: Solid Design, Model: J1939-84 Tool, Serial: 000001, Unit: 1234567890";
		assertEquals(expected, instance.toString());
	}

	@Test
	public void testTwoStars() {
		byte[] data = (MAKE + "*" + MODEL + "*" + SN + UN).getBytes(StandardCharsets.UTF_8);
		Packet packet = Packet.create(0, 0, data);
		ComponentIdentificationPacket instance = new ComponentIdentificationPacket(packet);
		assertEquals(MAKE, instance.getMake());
		assertEquals(MODEL, instance.getModel());
		assertEquals(SN + UN, instance.getSerialNumber());
		assertEquals(null, instance.getUnitNumber());
		String expected = "Found Engine #1 (0): Make: Solid Design, Model: J1939-84 Tool, Serial: 0000011234567890, Unit: null";
		assertEquals(expected, instance.toString());
	}

	@Test
	public void testUnitNumber() {
		byte[] data = ("***" + UN + "*").getBytes(StandardCharsets.UTF_8);
		Packet packet = Packet.create(0, 0, data);
		ComponentIdentificationPacket instance = new ComponentIdentificationPacket(packet);
		assertEquals("", instance.getMake());
		assertEquals("", instance.getModel());
		assertEquals("", instance.getSerialNumber());
		assertEquals(UN, instance.getUnitNumber());
		String expected = "Found Engine #1 (0): Make: , Model: , Serial: , Unit: 1234567890";
		assertEquals(expected, instance.toString());
	}

}
