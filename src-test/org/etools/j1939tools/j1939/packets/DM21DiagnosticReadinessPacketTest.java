/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939tools.j1939.packets;

import static org.etools.j1939_84.J1939_84.NL;
import static org.junit.Assert.assertEquals;

import org.etools.j1939tools.bus.Packet;
import org.junit.Test;

/**
 * Unit Test for {@link DM21DiagnosticReadinessPacket} class
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 */
public class DM21DiagnosticReadinessPacketTest {

    @Test
    public void testError() {
        Packet packet = Packet.create(0, 0, 0x00, 0xFE, 0x00, 0xFE, 0x00, 0xFE, 0x00, 0xFE);
        DM21DiagnosticReadinessPacket instance = new DM21DiagnosticReadinessPacket(packet);
        assertEquals(Double.MIN_VALUE, instance.getKmSinceDTCsCleared(), 0.0);
        assertEquals(Double.MIN_VALUE, instance.getMinutesSinceDTCsCleared(), 0.0);
        assertEquals(Double.MIN_VALUE, instance.getKmWhileMILIsActivated(), 0.0);
        assertEquals(Double.MIN_VALUE, instance.getMinutesWhileMILIsActivated(), 0.0);
        String expected = "DM21 from Engine #1 (0): [" + NL + "  Distance Traveled While MIL is Activated:     error"
                + NL + "  Time Run by Engine While MIL is Activated:    error" + NL
                + "  Distance Since DTCs Cleared:                  error" + NL
                + "  Time Since DTCs Cleared:                      error" + NL + "]";
        assertEquals(expected, instance.toString());
    }

    @Test
    public void testMax() {
        Packet packet = Packet.create(0, 0, 0xFF, 0xFA, 0xFF, 0xFA, 0xFF, 0xFA, 0xFF, 0xFA);
        DM21DiagnosticReadinessPacket instance = new DM21DiagnosticReadinessPacket(packet);
        assertEquals(64255, instance.getKmSinceDTCsCleared(), 0.0);
        assertEquals(39926.20581345, instance.getMilesSinceDTCsCleared(), 0.0);
        assertEquals(64255, instance.getMinutesSinceDTCsCleared(), 0.0);
        assertEquals(64255, instance.getKmWhileMILIsActivated(), 0.0);
        assertEquals(39926.20581345, instance.getMilesWhileMILIsActivated(), 0.0);
        assertEquals(64255, instance.getMinutesWhileMILIsActivated(), 0.0);
        String expected = "DM21 from Engine #1 (0): [" + NL
                + "  Distance Traveled While MIL is Activated:     64,255 km (39,926.206 mi)" + NL
                + "  Time Run by Engine While MIL is Activated:    64,255 minutes" + NL
                + "  Distance Since DTCs Cleared:                  64,255 km (39,926.206 mi)" + NL
                + "  Time Since DTCs Cleared:                      64,255 minutes" + NL + "]";
        assertEquals(expected, instance.toString());
    }

    @Test
    public void testMin() {
        Packet packet = Packet.create(0, 0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00);
        DM21DiagnosticReadinessPacket instance = new DM21DiagnosticReadinessPacket(packet);
        assertEquals(0.0, instance.getKmSinceDTCsCleared(), 0.0);
        assertEquals(0.0, instance.getMilesSinceDTCsCleared(), 0.0);
        assertEquals(0.0, instance.getMinutesSinceDTCsCleared(), 0.0);
        assertEquals(0.0, instance.getKmWhileMILIsActivated(), 0.0);
        assertEquals(0.0, instance.getMilesWhileMILIsActivated(), 0.0);
        assertEquals(0.0, instance.getMinutesWhileMILIsActivated(), 0.0);
        String expected = "DM21 from Engine #1 (0): [" + NL
                + "  Distance Traveled While MIL is Activated:     0 km (0 mi)" + NL
                + "  Time Run by Engine While MIL is Activated:    0 minutes" + NL
                + "  Distance Since DTCs Cleared:                  0 km (0 mi)" + NL
                + "  Time Since DTCs Cleared:                      0 minutes" + NL + "]";
        assertEquals(expected, instance.toString());
    }

    @Test
    public void testNotAvailable() {
        Packet packet = Packet.create(0, 0, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF);
        DM21DiagnosticReadinessPacket instance = new DM21DiagnosticReadinessPacket(packet);
        assertEquals(Double.MAX_VALUE, instance.getKmSinceDTCsCleared(), 0.0);
        assertEquals(Double.MAX_VALUE, instance.getMinutesSinceDTCsCleared(), 0.0);
        assertEquals(Double.MAX_VALUE, instance.getKmWhileMILIsActivated(), 0.0);
        assertEquals(Double.MAX_VALUE, instance.getMinutesWhileMILIsActivated(), 0.0);
        String expected = "DM21 from Engine #1 (0): [" + NL
                + "  Distance Traveled While MIL is Activated:     not available" + NL
                + "  Time Run by Engine While MIL is Activated:    not available" + NL
                + "  Distance Since DTCs Cleared:                  not available" + NL
                + "  Time Since DTCs Cleared:                      not available" + NL + "]";
        assertEquals(expected, instance.toString());
    }

    @Test
    public void testPGN() {
        assertEquals(49408, DM21DiagnosticReadinessPacket.PGN);
    }

    @Test
    public void testValue() {
        DM21DiagnosticReadinessPacket instance = DM21DiagnosticReadinessPacket.create(0, 0, 10000, 20000, 30000, 40000);
        assertEquals(20000, instance.getKmSinceDTCsCleared(), 0.0);
        assertEquals(12427.423799999999, instance.getMilesSinceDTCsCleared(), 0.0);
        assertEquals(40000, instance.getMinutesSinceDTCsCleared(), 0.0);
        assertEquals(10000, instance.getKmWhileMILIsActivated(), 0.0);
        assertEquals(6213.711899999999, instance.getMilesWhileMILIsActivated(), 0.0);
        assertEquals(30000, instance.getMinutesWhileMILIsActivated(), 0.0);
        String expected = "DM21 from Engine #1 (0): [" + NL
                + "  Distance Traveled While MIL is Activated:     10,000 km (6,213.712 mi)" + NL
                + "  Time Run by Engine While MIL is Activated:    30,000 minutes" + NL
                + "  Distance Since DTCs Cleared:                  20,000 km (12,427.424 mi)" + NL
                + "  Time Since DTCs Cleared:                      40,000 minutes" + NL + "]";
        assertEquals(expected, instance.toString());
    }
}
