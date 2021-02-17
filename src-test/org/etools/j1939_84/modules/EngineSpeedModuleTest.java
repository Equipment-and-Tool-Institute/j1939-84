/*
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.modules;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.etools.j1939_84.bus.Either;
import org.etools.j1939_84.bus.Packet;
import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.packets.EngineSpeedPacket;
import org.etools.testdoc.TestDoc;
import org.etools.testdoc.TestItem;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * The Unit tests for the {@link EngineSpeedModule} class
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
@RunWith(MockitoJUnitRunner.class)
@TestDoc(@TestItem(description = "Verify correct interpretation of PGN 61444.",
                   dependsOn = { "EngineSpeedPacketTest" }))
public class EngineSpeedModuleTest {

    private static final int PGN = EngineSpeedPacket.PGN;

    private EngineSpeedModule instance;

    @Mock
    private J1939 j1939;

    /**
     * Creates an {@link EngineSpeedPacket} with the given engine speed
     *
     * @param speed
     *            the engine speed in 1/8 RPMs
     * @return an {@link EngineSpeedPacket}
     */
    private EngineSpeedPacket getEngineSpeedPacket(int speed) {
        return new EngineSpeedPacket(Packet.create(PGN, 0x00, 0, 0, 0, speed & 0xFF, (speed >> 8) & 0xFF, 0, 0, 0));
    }

    @Before
    public void setUp() throws Exception {
        instance = new EngineSpeedModule();
        instance.setJ1939(j1939);
    }

    @After
    public void tearDown() {
        // verify that isEngineCommunicating() and isEngineNotRunning() are both
        // called
        verify(j1939, times(4)).read(EngineSpeedPacket.class, 0x00, 300, TimeUnit.MILLISECONDS);
        verifyNoMoreInteractions(j1939);
    }

    @Test
    @TestDoc(@TestItem(description = "Verify KOEO is identified when PGN 61444 speed 0 is on the bus.", dependsOn = {
            "EngineSpeedPacketTest.testGetEngineSpeedAtZero" }))
    public void testEngineKOEO_0() {
        EngineSpeedPacket packet = getEngineSpeedPacket(0);
        when(j1939.read(EngineSpeedPacket.class, 0x00, 300, TimeUnit.MILLISECONDS))
                .thenReturn(Optional.of(new Either<>(packet, null)));
        assertFalse(instance.isEngineRunning());
        assertTrue(instance.isEngineCommunicating());
        assertTrue(instance.isEngineNotRunning());
        assertEquals("Engine speed", Math.floor(0.0), instance.getEngineSpeed(), 1);
    }

    @Test
    @TestDoc(@TestItem(description = "Verify KOEO is identified when PGN 61444 speed 300 is on the bus.", dependsOn = {
            "EngineSpeedPacketTest.testGetEngineSpeedAndToStringAt300" }))
    public void testEngineKOEO_300() {
        EngineSpeedPacket packet = getEngineSpeedPacket(300 * 8);
        when(j1939.read(EngineSpeedPacket.class, 0x00, 300, TimeUnit.MILLISECONDS))
                .thenReturn(Optional.of(new Either<>(packet, null)));
        assertTrue(instance.isEngineRunning());
        assertTrue(instance.isEngineCommunicating());
        assertTrue(instance.isEngineNotRunning());
        assertEquals("Engine speed", Math.floor(300.0), instance.getEngineSpeed(), 1);
    }

    @Test
    @TestDoc(@TestItem(description = "Verify KOER is identified when PGN 61444 speed 301 is on the bus."))
    public void testEngineKOER_301() {
        EngineSpeedPacket packet = getEngineSpeedPacket(301 * 8);
        when(j1939.read(EngineSpeedPacket.class, 0x00, 300, TimeUnit.MILLISECONDS))
                .thenReturn(Optional.of(new Either<>(packet, null)));
        assertTrue(instance.isEngineRunning());
        assertTrue(instance.isEngineCommunicating());
        assertFalse(instance.isEngineNotRunning());
        assertEquals("Engine speed", Math.floor(301.0), instance.getEngineSpeed(), 1);
    }

    @Test
    @TestDoc(@TestItem(description = "Verify key off (no communication, but engine may be running) identified when there is no traffic on the bus."))
    public void testEngineNotCommunicating() {
        // should we send non F004 traffic?
        when(j1939.read(EngineSpeedPacket.class, 0x00, 300, TimeUnit.MILLISECONDS)).thenReturn(Optional.empty());
        assertFalse(instance.isEngineCommunicating());
        assertFalse(instance.isEngineNotRunning());
        assertFalse(instance.isEngineRunning());
        assertNull(instance.getEngineSpeed());
    }

    /**
     * Test method for {@link EngineSpeedModule#isEngineRunning()} ()}.
     */
    @Test
    public void testIsEngineRunning() {
        EngineSpeedPacket packet = getEngineSpeedPacket(0xFFFF);
        when(j1939.read(EngineSpeedPacket.class, 0x00, 300, TimeUnit.MILLISECONDS))
                .thenReturn(Optional.of(new Either<>(packet, null)));
        assertFalse(instance.isEngineRunning());
        assertTrue(instance.isEngineCommunicating());
        assertFalse(instance.isEngineNotRunning());
        assertEquals("Engine speed", Math.floor(1.7976931348623157E308), instance.getEngineSpeed(), 1);
    }

    @Test
    @TestDoc(@TestItem(description = "Verify KOER is identified when PGN 61444 speed (0xFE00-1)/8 RPM is on the bus."))
    public void testKOER_2500() {
        EngineSpeedPacket packet = getEngineSpeedPacket(0xFE00 - 1);
        when(j1939.read(EngineSpeedPacket.class, 0x00, 300, TimeUnit.MILLISECONDS))
                .thenReturn(Optional.of(new Either<>(packet, null)));
        assertTrue(instance.isEngineCommunicating());
        assertFalse(instance.isEngineNotRunning());
        assertTrue(instance.isEngineRunning());
        assertEquals("Engine speed", Math.floor(8127.875), instance.getEngineSpeed(), 1);
    }

    @Test
    @TestDoc(@TestItem(description = "Verify KOER is identified when PGN 61444 speed error is on the bus.",
                       dependsOn = {
                               "EngineSpeedPacketTest.testGetEngineSpeedAndToStringAtError" }))
    public void testKOER_error() {
        EngineSpeedPacket packet = getEngineSpeedPacket(0xFE00);
        when(j1939.read(EngineSpeedPacket.class, 0x00, 300, TimeUnit.MILLISECONDS))
                .thenReturn(Optional.of(new Either<>(packet, null)));
        assertTrue(instance.isEngineCommunicating());
        assertFalse(instance.isEngineNotRunning());
        assertFalse(instance.isEngineRunning());
        assertEquals("Engine speed", Math.floor(4.9E-324), instance.getEngineSpeed(), 1);
    }

    @Test
    @TestDoc(@TestItem(description = "Verify KOER is identified when PGN 61444 speed 'not available' is on the bus.",
                       dependsOn = {
                               "EngineSpeedPacketTest.testGetEngineSpeedAndToStringAtNotAvailable" }))
    public void testKOER_not_available() {
        EngineSpeedPacket packet = getEngineSpeedPacket(0xFFFF);
        when(j1939.read(EngineSpeedPacket.class, 0x00, 300, TimeUnit.MILLISECONDS))
                .thenReturn(Optional.of(new Either<>(packet, null)));
        assertTrue(instance.isEngineCommunicating());
        assertFalse(instance.isEngineNotRunning());
        assertFalse(instance.isEngineRunning());
        assertEquals("Engine speed", Math.floor(1.7976931348623157E308), instance.getEngineSpeed(), 1);
    }

}
