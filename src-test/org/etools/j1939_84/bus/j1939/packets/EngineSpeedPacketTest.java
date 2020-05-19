/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.j1939.packets;

import static org.junit.Assert.assertEquals;

import org.etools.j1939_84.bus.Packet;
import org.etools.testdoc.TestDoc;
import org.junit.Test;

/**
 * Unit tests for the {@link EngineSpeedPacket} class
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
@TestDoc(description = "Verify the correct interpretation of PGN 61444 as engine speed.")
public class EngineSpeedPacketTest {

	@Test
	@TestDoc(
			description = "Verify that data 0x11, 0x22, 0x33, 0x60, 0x09, 0x66, 0x77, 0x88 is interpretted as 300 RPM.")
	public void testGetEngineSpeedAndToStringAt300() {
		int[] data = new int[] { 0x11, 0x22, 0x33, 0x60, 0x09, 0x66, 0x77, 0x88 };
		Packet packet = Packet.create(0, 0, data);
		EngineSpeedPacket instance = new EngineSpeedPacket(packet);
		assertEquals(300, instance.getEngineSpeed(), 0.0);
		assertEquals("Engine Speed from Engine #1 (0): 300 RPM", instance.toString());
	}

	@Test
	@TestDoc(
			description = "Verify that data 0x11, 0x22, 0x33, 0xFF, 0xFE, 0x66, 0x77, 0x88 is interpretted as an error.")
	public void testGetEngineSpeedAndToStringAtError() {
		int[] data = new int[] { 0x11, 0x22, 0x33, 0xFF, 0xFE, 0x66, 0x77, 0x88 };
		Packet packet = Packet.create(0, 0, data);
		EngineSpeedPacket instance = new EngineSpeedPacket(packet);
		assertEquals(ParsedPacket.ERROR, instance.getEngineSpeed(), 0.0);
		assertEquals("Engine Speed from Engine #1 (0): error", instance.toString());
	}

	@Test
	@TestDoc(
			description = "Verify that data 0x11, 0x22, 0x33, 0xFF, 0xFF, 0x66, 0x77, 0x88 is interpretted as not available.")
	public void testGetEngineSpeedAndToStringAtNotAvailable() {
		int[] data = new int[] { 0x11, 0x22, 0x33, 0xFF, 0xFF, 0x66, 0x77, 0x88 };
		Packet packet = Packet.create(0, 0, data);
		EngineSpeedPacket instance = new EngineSpeedPacket(packet);
		assertEquals(ParsedPacket.NOT_AVAILABLE, instance.getEngineSpeed(), 0.0);
		assertEquals("Engine Speed from Engine #1 (0): not available", instance.toString());
	}

	@Test
	@TestDoc(
			description = "Verify that data 0x11, 0x22, 0x33, 0xFF, 0xFA, 0x66, 0x77, 0x88 is interpretted as 8031.875.")
	public void testGetEngineSpeedAtMax() {
		int[] data = new int[] { 0x11, 0x22, 0x33, 0xFF, 0xFA, 0x66, 0x77, 0x88 };
		Packet packet = Packet.create(0, 0, data);
		EngineSpeedPacket instance = new EngineSpeedPacket(packet);
		assertEquals(8031.875, instance.getEngineSpeed(), 0.0);
	}

	@Test
	@TestDoc(
			description = "Verify that data 0x11, 0x22, 0x33, 0x00, 0x00, 0x66, 0x77, 0x88 is interpretted as 0.")
	public void testGetEngineSpeedAtZero() {
		int[] data = new int[] { 0x11, 0x22, 0x33, 0x00, 0x00, 0x66, 0x77, 0x88 };
		Packet packet = Packet.create(0, 0, data);
		EngineSpeedPacket instance = new EngineSpeedPacket(packet);
		assertEquals(0, instance.getEngineSpeed(), 0.0);
	}

	@Test
	@TestDoc(description = "Verify that the PGN is 61444.")
	public void testPGN() {
		assertEquals(61444, EngineSpeedPacket.PGN);
	}

}
