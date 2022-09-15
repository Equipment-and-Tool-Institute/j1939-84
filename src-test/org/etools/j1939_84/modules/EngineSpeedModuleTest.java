/*
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.modules;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.etools.j1939_84.model.KeyState.KEY_OFF;
import static org.etools.j1939_84.model.KeyState.KEY_ON_ENGINE_OFF;
import static org.etools.j1939_84.model.KeyState.KEY_ON_ENGINE_RUNNING;
import static org.etools.j1939_84.model.KeyState.UNKNOWN;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;

import org.etools.j1939_84.utils.CollectionUtils;
import org.etools.j1939tools.bus.Either;
import org.etools.j1939tools.bus.Packet;
import org.etools.j1939tools.j1939.J1939;
import org.etools.j1939tools.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939tools.j1939.packets.EngineSpeedPacket;
import org.etools.j1939tools.j1939.packets.GenericPacket;
import org.etools.j1939tools.j1939.packets.ParsedPacket;
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
@TestDoc(@TestItem(description = "Verify correct interpretation of PGN 61444."))
public class EngineSpeedModuleTest {

    private static final int TIMEOUT = 1200;

    private EngineSpeedModule instance;

    @Mock
    private J1939 j1939;

    private static EngineSpeedPacket engineSpeedPacket(int speed) {
        return EngineSpeedPacket.create(0, speed);
    }

    private static EngineSpeedPacket engineSpeedPacket(int speed, LocalDateTime timestamp) {
        int[] data = new int[] { 0xFF, 0xFF, 0xFF };
        data = CollectionUtils.join(data, ParsedPacket.to2Ints(speed * 8)); // Bytes 4 & 5
        data = CollectionUtils.join(data, new int[] { 0xFF, 0xFF, 0xFF });
        Packet packet = Packet.create(timestamp, 0, EngineSpeedPacket.PGN, 0, false, data);
        return new EngineSpeedPacket(packet);
    }

    private static GenericPacket idleSpeedPacket(int idleSpeed) {
        int[] data = new int[38];
        Arrays.fill(data, 0xFF);
        data = CollectionUtils.join(ParsedPacket.to2Ints(idleSpeed * 8), data);

        return new GenericPacket(Packet.create(65251, 0, data));
    }

    private static GenericPacket pedalPositionPacket(double pedalPosition, double auxPedalPosition) {
        int pedal1 = Double.valueOf((pedalPosition / 0.4)).intValue() & 0xFF;
        int pedal2 = Double.valueOf((auxPedalPosition / 0.4)).intValue() & 0xFF;
        return new GenericPacket(Packet.create(61443, 0, 0xFF, pedal1, 0xFF, 0xFF, pedal2, 0xFF, 0xFF, 0xFF));
    }

    private static Optional<Either<EngineSpeedPacket, AcknowledgmentPacket>> optionalSpeedOf(int speed) {
        return Optional.of(new Either<>(engineSpeedPacket(speed), null));
    }

    @Before
    public void setUp() throws Exception {
        instance = new EngineSpeedModule();
        instance.setJ1939(j1939);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(j1939);
    }

    @Test
    @TestDoc(@TestItem(description = "Verify KOEO is identified when PGN 61444 speed 0 is on the bus."))
    public void testEngineKOEO_0() {
        when(j1939.read(EngineSpeedPacket.class, 0x00, TIMEOUT, MILLISECONDS)).thenReturn(optionalSpeedOf(0));
        assertEquals(KEY_ON_ENGINE_OFF, instance.getKeyState());
        assertEquals("0.0 RPMs", instance.getEngineSpeedAsString());
        verify(j1939, atLeastOnce()).read(EngineSpeedPacket.class, 0x00, TIMEOUT, MILLISECONDS);
    }

    @Test
    @TestDoc(@TestItem(description = "Verify KOEO is identified when PGN 61444 speed 300 is on the bus."))
    public void testEngineKOEO_300() {
        when(j1939.read(EngineSpeedPacket.class, 0x00, TIMEOUT, MILLISECONDS)).thenReturn(optionalSpeedOf(300));
        assertEquals(KEY_ON_ENGINE_OFF, instance.getKeyState());
        assertEquals("300.0 RPMs", instance.getEngineSpeedAsString());
        verify(j1939, atLeastOnce()).read(EngineSpeedPacket.class, 0x00, TIMEOUT, MILLISECONDS);
    }

    @Test
    @TestDoc(@TestItem(description = "Verify KOER is identified when PGN 61444 speed 301 is on the bus."))
    public void testEngineKOER_301() {
        when(j1939.read(EngineSpeedPacket.class, 0x00, TIMEOUT, MILLISECONDS)).thenReturn(optionalSpeedOf(301));
        assertEquals(KEY_ON_ENGINE_RUNNING, instance.getKeyState());
        assertEquals("301.0 RPMs", instance.getEngineSpeedAsString());
        verify(j1939, atLeastOnce()).read(EngineSpeedPacket.class, 0x00, TIMEOUT, MILLISECONDS);
    }

    @Test
    @TestDoc(@TestItem(description = "Verify key off (no communication, but engine may be running) identified when there is no traffic on the bus."))
    public void testEngineNotCommunicating() {
        when(j1939.read(EngineSpeedPacket.class, 0x00, TIMEOUT, MILLISECONDS)).thenReturn(Optional.empty());
        assertEquals(KEY_OFF, instance.getKeyState());
        assertEquals("Key Off", instance.getEngineSpeedAsString());
        verify(j1939, atLeastOnce()).read(EngineSpeedPacket.class, 0x00, TIMEOUT, MILLISECONDS);
    }

    @Test
    public void testIsEngineRunning() {
        when(j1939.read(EngineSpeedPacket.class, 0x00, TIMEOUT, MILLISECONDS)).thenReturn(optionalSpeedOf(1500));
        assertEquals(KEY_ON_ENGINE_RUNNING, instance.getKeyState());
        assertEquals("1500.0 RPMs", instance.getEngineSpeedAsString());
        verify(j1939, atLeastOnce()).read(EngineSpeedPacket.class, 0x00, TIMEOUT, MILLISECONDS);
    }

    @Test
    @TestDoc(@TestItem(description = "Verify KOER is identified when PGN 61444 speed (0xFE00-1)/8 RPM is on the bus."))
    public void testKOER_2500() {
        when(j1939.read(EngineSpeedPacket.class, 0x00, TIMEOUT, MILLISECONDS)).thenReturn(optionalSpeedOf(0xFE00 - 1));
        assertEquals(KEY_ON_ENGINE_RUNNING, instance.getKeyState());
        assertEquals("7679.0 RPMs", instance.getEngineSpeedAsString());
        verify(j1939, atLeastOnce()).read(EngineSpeedPacket.class, 0x00, TIMEOUT, MILLISECONDS);
    }

    @Test
    @TestDoc(@TestItem(description = "Verify KOER is identified when PGN 61444 speed error is on the bus."))
    public void testKOER_error() {
        when(j1939.read(EngineSpeedPacket.class, 0x00, TIMEOUT, MILLISECONDS)).thenReturn(optionalSpeedOf(0xFEFF / 8));
        assertEquals(UNKNOWN, instance.getKeyState());
        assertEquals("Error RPMs", instance.getEngineSpeedAsString());
        verify(j1939, atLeastOnce()).read(EngineSpeedPacket.class, 0x00, TIMEOUT, MILLISECONDS);
    }

    @Test
    @TestDoc(@TestItem(description = "Verify KOER is identified when PGN 61444 speed 'not available' is on the bus."))
    public void testKOER_not_available() {
        when(j1939.read(EngineSpeedPacket.class, 0x00, TIMEOUT, MILLISECONDS)).thenReturn(optionalSpeedOf(0xFFFF / 8));
        assertEquals(UNKNOWN, instance.getKeyState());
        assertEquals("N/A RPMs", instance.getEngineSpeedAsString());
        verify(j1939, atLeastOnce()).read(EngineSpeedPacket.class, 0x00, TIMEOUT, MILLISECONDS);
    }

    @Test
    public void testWeightedAverageEngineSpeed() throws InterruptedException {

        // Set the initial engine speed
        when(j1939.read(EngineSpeedPacket.class, 0, TIMEOUT, MILLISECONDS)).thenReturn(optionalSpeedOf(650));

        List<GenericPacket> packets = new ArrayList<>();

        packets.add(pedalPositionPacket(25, 0)); // Pedal is pressed
        packets.add(idleSpeedPacket(650)); // Set Idle Speed to 650

        LocalDateTime time = LocalDateTime.now();

        // Ramp the speed up from idle
        int speed = 650;
        while (speed < 1400) {
            time = time.plus(50, ChronoUnit.MILLIS);
            speed += 10;
            packets.add(engineSpeedPacket(speed, time));
            packets.add(pedalPositionPacket(25, 0)); // Pedal is pressed
        }

        // Run at speed for a while
        speed = 1400;
        for (int i = 0; i < 300; i++) {
            time = time.plus(50, ChronoUnit.MILLIS);
            packets.add(engineSpeedPacket(1400, time));
            packets.add(pedalPositionPacket(0, 25)); // Pedal is pressed
        }

        // Allow engine to return to idle
        while (speed > 650) {
            speed -= 10;
            time = time.plus(50, ChronoUnit.MILLIS);
            packets.add(engineSpeedPacket(speed, time));
            packets.add(pedalPositionPacket(0, 0)); // Aux Pedal is pressed
        }

        // Run at idle for a while
        speed = 650;
        for (int i = 0; i < 1000; i++) {
            time = time.plus(50, ChronoUnit.MILLIS);
            packets.add(engineSpeedPacket(speed, time));
            packets.add(pedalPositionPacket(0.4, 0.4)); // Neither pedal is pressed
        }

        when(j1939.readGenericPacket(any())).thenReturn(packets.stream());

        // Check initial values
        assertEquals(0.0, instance.averagedEngineSpeed(), 0.0);
        assertEquals(0, instance.secondsAtSpeed());
        assertEquals(0, instance.secondsAtIdle());
        assertEquals(0, instance.currentEngineSpeed(), 0.0);
        assertEquals(0, instance.pedalPosition(), 0.0);

        instance.startMonitoringEngineSpeed(Executors.newSingleThreadExecutor(), e -> true);

        // Let the packets be processed
        LocalDateTime endTime = LocalDateTime.now().plusSeconds(5);
        while (instance.secondsAtIdle() < 45 && LocalDateTime.now().isBefore(endTime)) {
            Thread.sleep(100);
        }

        verify(j1939).readGenericPacket(any());
        verify(j1939).read(EngineSpeedPacket.class, 0, TIMEOUT, MILLISECONDS);

        // Check final values
        assertEquals(17, instance.secondsAtSpeed());
        assertEquals(45, instance.secondsAtIdle());
        assertEquals(650, instance.averagedEngineSpeed(), 1.0);
        assertEquals(650, instance.currentEngineSpeed(), 1.0);
        assertEquals(0.4, instance.pedalPosition(), 0.01);
    }

}
