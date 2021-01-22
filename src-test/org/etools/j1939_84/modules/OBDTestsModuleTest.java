/*
 * Copyright 2017 Equipment & Tool Institute
 */
package org.etools.j1939_84.modules;

import static org.etools.j1939_84.J1939_84.NL;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.etools.j1939_84.bus.Packet;
import org.etools.j1939_84.bus.j1939.BusResult;
import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.packets.DM24SPNSupportPacket;
import org.etools.j1939_84.bus.j1939.packets.DM30ScaledTestResultsPacket;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Unit tests for {@link OBDTestsModule}
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 */
@SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT",
        justification = "The values returned are properly ignored on verify statements.")
@RunWith(MockitoJUnitRunner.class)
public class OBDTestsModuleTest {

    private static final int BUS_ADDR = 0xA5;

    private OBDTestsModule instance;

    @Mock
    private J1939 j1939;

    private TestResultsListener listener;

    @Before
    public void setUp() throws Exception {
        // when(j1939.getBusAddress()).thenReturn(0xA5);
        instance = new OBDTestsModule();
        instance.setJ1939(j1939);
        listener = new TestResultsListener();
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(j1939);
    }

    @Test
    public void testReportOBDTestsMultipleModulesMultipleScaledTestResultsRequestsOnlyScaledTests() {
        when(j1939.getBusAddress()).thenReturn(BUS_ADDR);
        Packet dm24RequestPacket1 = Packet.create(0xEA00, BUS_ADDR, true, 0xB6, 0xFD, 0x00);
        when(j1939.createRequestPacket(64950, 0x00)).thenReturn(dm24RequestPacket1);

        DM24SPNSupportPacket engineDm24Packet = new DM24SPNSupportPacket(Packet.create(64950,
                0x00,
                0x66,
                0x00,
                0x1B,
                0x01,
                0x5C,
                0x00,
                0x1F,
                0x01,
                0x00,
                0x02,
                0x1B,
                0x01,
                0x9C,
                0xF0,
                0xFB,
                0x00));

        when(j1939.requestDS("Direct DM24 Request to Engine #1 (0)", listener, true, DM24SPNSupportPacket.class,
                dm24RequestPacket1))
                .thenReturn((new BusResult<>(false, engineDm24Packet)));

        DM30ScaledTestResultsPacket engineDm30PacketSpn102 = new DM30ScaledTestResultsPacket(
                Packet.create(0xA400, 0x00, 0xF7, 0x66, 0x00, 0x12, 0xD0, 0x00, 0x00, 0xFB, 0xFF, 0xFF, 0xFF, 0xFF));
        when(j1939.requestDm7("Direct DM30 Requests to Engine #1 (0)", listener,
                Packet.create(0xE300, BUS_ADDR, true, 0xF7, 0x66, 0x00, 0x1F, 0xFF, 0xFF, 0xFF, 0xFF)))
                .thenReturn(new BusResult<>(false, engineDm30PacketSpn102));

        DM30ScaledTestResultsPacket engineDm30PacketSpn512 = new DM30ScaledTestResultsPacket(
                Packet.create(0xA400, 0x00, 0xF7, 0x00, 0x02, 0x12, 0xD0, 0x00, 0x00, 0xFB, 0xFF, 0xFF, 0xFF, 0xFF));
        when(j1939.requestDm7("Direct DM30 Requests to Engine #1 (0)", listener,
                Packet.create(0xE300, BUS_ADDR, true, 0xF7, 0x00, 0x02, 0x1F, 0xFF, 0xFF, 0xFF, 0xFF)))
                .thenReturn(new BusResult<>(false, engineDm30PacketSpn512));

        DM30ScaledTestResultsPacket engineDm30PacketSpn520348 = new DM30ScaledTestResultsPacket(
                Packet.create(0xA400, 0x00, 0xF7, 0x9C, 0xF0, 0xFF, 0xD0, 0x00, 0x00, 0xFB, 0xFF, 0xFF, 0xFF, 0xFF));
        when(j1939.requestDm7("Direct DM30 Requests to Engine #1 (0)", listener,
                Packet.create(0xE300, BUS_ADDR, true, 0xF7, 0x9C, 0xF0, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF)))
                .thenReturn(new BusResult<>(false, engineDm30PacketSpn520348));

        Packet dm24RequestPacket2 = Packet.create(0xEA55, BUS_ADDR, true, 0xB6, 0xFD, 0x00);
        when(j1939.createRequestPacket(64950, 0x55)).thenReturn(dm24RequestPacket2);
        DM24SPNSupportPacket atDm24Packet = new DM24SPNSupportPacket(
                Packet.create(64950, 0x55, 0xA7, 0x13, 0x1C, 0x00, 0x0C, 0x11, 0x18, 0x00, 0x9A, 0x0C, 0x18, 0x00));
        when(j1939.requestDS("Direct DM24 Request to DPF Controller (85)", listener, true,
                DM24SPNSupportPacket.class,
                dm24RequestPacket2))
                .thenReturn(new BusResult<>(false, atDm24Packet));

        DM30ScaledTestResultsPacket atDm30PacketSpn4364 = new DM30ScaledTestResultsPacket(
                Packet.create(0xA400, 0x55, 0xF7, 0x0C, 0x11, 0x00, 0xFB, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF));
        when(j1939.requestDm7("Direct DM30 Requests to DPF Controller (85)", listener,
                Packet.create(0xE355, BUS_ADDR, true, 0xF7, 0x0C, 0x11, 0x1F, 0xFF, 0xFF, 0xFF, 0xFF)))
                .thenReturn(new BusResult<>(false, atDm30PacketSpn4364));

        DM30ScaledTestResultsPacket atDm30PacketSpn3226 = new DM30ScaledTestResultsPacket(
                Packet.create(0xA400, 0x55, 0xF7, 0x9A, 0x0C, 0x0A, 0x00, 0x01, 0x00, 0xFB, 0xFF, 0xFF, 0xFF, 0xFF));
        when(j1939.requestDm7("Direct DM30 Requests to DPF Controller (85)", listener,
                Packet.create(0xE355, BUS_ADDR, true, 0xF7, 0x9A, 0x0C, 0x1F, 0xFF, 0xFF, 0xFF, 0xFF)))
                .thenReturn(new BusResult<>(false, atDm30PacketSpn3226));

        List<Integer> obdModules = Arrays.asList(0x00, 0x55);
        instance.requestSupportedSpnPackets(listener, obdModules);

        String expected = "";
        expected += "DM24 from Engine #1 (0): " + NL;
        expected += "(Supporting Scaled Test Results) [" + NL;
        expected += "  SPN 102 - Engine Intake Manifold #1 Pressure" + NL;
        expected += "  SPN 512 - Driver's Demand Engine - Percent Torque" + NL;
        expected += "  SPN 520348 - Manufacturer Assignable SPN" + NL;
        expected += "]" + NL;
        expected += "(Supports Data Stream Results) [" + NL;
        expected += "]" + NL;
        expected += "(Supports Freeze Frame Results) [" + NL;
        expected += "]" + NL;
        expected += "" + NL;
        expected += NL;
        expected += "DM24 from DPF Controller (85): " + NL;
        expected += "(Supporting Scaled Test Results) [" + NL;
        expected += "  SPN 3226 - AFT 1 Outlet NOx 1" + NL;
        expected += "  SPN 4364 - AFT 1 SCR Conversion Efficiency" + NL;
        expected += "]" + NL;
        expected += "(Supports Data Stream Results) [" + NL;
        expected += "  SPN 3226 - AFT 1 Outlet NOx 1" + NL;
        expected += "  SPN 4364 - AFT 1 SCR Conversion Efficiency" + NL;
        expected += "  SPN 5031 - AFT 1 Outlet NOx Sensor Heater Ratio" + NL;
        expected += "]" + NL;
        expected += "(Supports Freeze Frame Results) [" + NL;
        expected += "  SPN 3226 - AFT 1 Outlet NOx 1" + NL;
        expected += "  SPN 4364 - AFT 1 SCR Conversion Efficiency" + NL;
        expected += "  SPN 5031 - AFT 1 Outlet NOx Sensor Heater Ratio" + NL;
        expected += "]" + NL;
        expected += "" + NL;
        expected += NL;
        expected += "DM30 from 0: SPN 102 FMI 18 (SLOT 208) Result: Test Not Complete." + NL;
        expected += "" + NL;
        expected += "DM30 from 0: SPN 512 FMI 18 (SLOT 208) Result: Test Not Complete." + NL;
        expected += "" + NL;
        expected += "DM30 from 0: SPN 520348 FMI 31 (SLOT 208) Result: Test Not Complete." + NL;
        expected += "" + NL;
        expected += "DM30 from 85: SPN 3226 FMI 10 (SLOT 256) Result: Test Not Complete." + NL;
        expected += "" + NL;
        expected += "DM30 from 85: SPN 4364 FMI 0 (SLOT 65531) Result: Test Passed. Min: N/A, Value: 65,535, Max: N/A" + NL;
        expected += "" + NL;
        expected += "Incomplete Tests: [" + NL;
        expected += "  DPF Controller (85): SPN 3226 FMI 10 (SLOT 256) Result: Test Not Complete." + NL;
        expected += "  Engine #1 (0): SPN 102 FMI 18 (SLOT 208) Result: Test Not Complete." + NL;
        expected += "  Engine #1 (0): SPN 512 FMI 18 (SLOT 208) Result: Test Not Complete." + NL;
        expected += "  Engine #1 (0): SPN 520348 FMI 31 (SLOT 208) Result: Test Not Complete." + NL;
        expected += "]" + NL;
        expected += "4 Incomplete Tests" + NL;

        assertEquals(expected, listener.getResults());

        verify(j1939, times(5)).getBusAddress();
        verify(j1939).createRequestPacket(64950, 0x00);
        verify(j1939).createRequestPacket(64950, 0x55);
        verify(j1939).requestDS("Direct DM24 Request to Engine #1 (0)", listener, true, DM24SPNSupportPacket.class,
                dm24RequestPacket1);
        verify(j1939).requestDS("Direct DM24 Request to DPF Controller (85)", listener, true,
                DM24SPNSupportPacket.class, dm24RequestPacket2);
        verify(j1939, times(5)).requestDm7(any(), eq(listener), any(Packet.class));
    }

    @Test
    public void testReportOBDTestsNoResponse() {
        final Packet requestPacket = Packet.create(0xEA00, BUS_ADDR, true, 0xB6, 0xFD, 0x00);
        when(j1939.createRequestPacket(64950, 0x00)).thenReturn(requestPacket);
        when(j1939.requestDS("Direct DM24 Request to Engine #1 (0)", listener, true,
                DM24SPNSupportPacket.class, requestPacket))
                .thenReturn(new BusResult<>(true, Optional.empty()));
        List<Integer> obdModules = Collections.singletonList(0x00);

        instance.requestSupportedSpnPackets(listener, obdModules);
        String expected = NL;
        expected += "ERROR No tests results returned" + NL;

        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(64950, 0x00);
        verify(j1939).requestDS("Direct DM24 Request to Engine #1 (0)", listener, true,
                DM24SPNSupportPacket.class, requestPacket);
    }

    @Test
    public void testReportOBDTestsOneScaledTestResults() {
        when(j1939.getBusAddress()).thenReturn(BUS_ADDR);
        final Packet dm24RequestPacket = Packet.create(0xEA00, BUS_ADDR, true, 0xB6, 0xFD, 0x00);
        when(j1939.createRequestPacket(64950, 0x00)).thenReturn(dm24RequestPacket);

        DM30ScaledTestResultsPacket engineDm30Packet = new DM30ScaledTestResultsPacket(
                Packet.create(0xA400, 0x00, 0xF7, 0x66, 0x00, 0x12, 0xD0, 0x00, 0x00, 0xFA, 0xFF, 0xFF, 0xFF, 0xFF));

        when(j1939.requestDm7(eq("Direct DM30 Requests to Engine #1 (0)"), eq(listener), any(Packet.class)))
                .thenReturn(new BusResult<>(false, engineDm30Packet));

        DM24SPNSupportPacket engineDm24Packet = new DM24SPNSupportPacket(
                Packet.create(64950, 0x00, 0x66, 0x00, 0x1B, 0x01));
        when(j1939.requestDS("Direct DM24 Request to Engine #1 (0)", listener, true, DM24SPNSupportPacket.class,
                dm24RequestPacket))
                .thenReturn((new BusResult<>(false, engineDm24Packet)));
        List<Integer> obdModules = List.of(0x00);
        instance.requestSupportedSpnPackets(listener, obdModules);

        String expected = "";
        expected += "DM24 from Engine #1 (0): " + NL;
        expected += "(Supporting Scaled Test Results) [" + NL;
        expected += "  SPN 102 - Engine Intake Manifold #1 Pressure" + NL;
        expected += "]" + NL;
        expected += "(Supports Data Stream Results) [" + NL;
        expected += "]" + NL;
        expected += "(Supports Freeze Frame Results) [" + NL;
        expected += "]" + NL;
        expected += NL;
        expected += NL;
        expected += "DM30 from 0: SPN 102 FMI 18 (SLOT 208) Result: Test Passed. Min: N/A, Value: 64,000, Max: N/A count" + NL;
        expected += "" + NL;
        expected += "All Tests Complete" + NL;

        assertEquals(expected, listener.getResults());

        verify(j1939).getBusAddress();
        verify(j1939).createRequestPacket(64950, 0x00);
        verify(j1939).requestDS("Direct DM24 Request to Engine #1 (0)", listener, true, DM24SPNSupportPacket.class,
                dm24RequestPacket);
        verify(j1939).requestDm7(eq("Direct DM30 Requests to Engine #1 (0)"), eq(listener), any(Packet.class));
    }

    @Test
    public void testReportOBDTestsScaledTestResultsTimeout() {
        when(j1939.getBusAddress()).thenReturn(BUS_ADDR);
        final Packet requestPacket = Packet.create(0xEA00, BUS_ADDR, true, 0xB6, 0xFD, 0x00);
        when(j1939.createRequestPacket(64950, 0x00)).thenReturn(requestPacket);
        DM24SPNSupportPacket engineDm24Packet = new DM24SPNSupportPacket(
                Packet.create(64950, 0x00, 0x66, 0x00, 0x1B, 0x01));
        when(j1939.requestDS("Direct DM24 Request to Engine #1 (0)", listener, true, DM24SPNSupportPacket.class,
                requestPacket))
                .thenReturn((new BusResult<>(false, engineDm24Packet)));

        when(j1939.requestDm7(eq("Direct DM30 Requests to Engine #1 (0)"), eq(listener), any(Packet.class)))
                .thenReturn(BusResult.empty());

        List<Integer> obdModules = List.of(0x00);
        instance.requestSupportedSpnPackets(listener, obdModules);

        String expected = "";
        expected += "DM24 from Engine #1 (0): " + NL;
        expected += "(Supporting Scaled Test Results) [" + NL;
        expected += "  SPN 102 - Engine Intake Manifold #1 Pressure" + NL;
        expected += "]" + NL;
        expected += "(Supports Data Stream Results) [" + NL;
        expected += "]" + NL;
        expected += "(Supports Freeze Frame Results) [" + NL;
        expected += "]" + NL;
        expected += NL;
        expected += NL;
        expected += "" + NL;
        expected += "No Scaled Tests Results from Engine #1 (0)" + NL;
        expected += "" + NL;
        expected += "ERROR No tests results returned" + NL;

        assertEquals(expected, listener.getResults());

        verify(j1939).getBusAddress();
        verify(j1939).createRequestPacket(64950, 0x00);
        verify(j1939).requestDS("Direct DM24 Request to Engine #1 (0)", listener, true, DM24SPNSupportPacket.class,
                requestPacket);
        verify(j1939).requestDm7(eq("Direct DM30 Requests to Engine #1 (0)"), eq(listener), any(Packet.class));
    }

    @Test
    public void testReportOBDTestsWithNoScaledTestResults() {
        Packet requestPacket = Packet.create(0xEA00, BUS_ADDR, true, 0xB6, 0xFD, 0x00);
        when(j1939.createRequestPacket(64950, 0x00)).thenReturn(requestPacket);
        DM24SPNSupportPacket engineDm24Packet = new DM24SPNSupportPacket(
                Packet.create(64950, 0x00, 0x66, 0x00, 0x1C, 0x01));
        when(j1939.requestDS("Direct DM24 Request to Engine #1 (0)", listener, true, DM24SPNSupportPacket.class,
                requestPacket))
                .thenReturn((new BusResult<>(false, engineDm24Packet)));

        List<Integer> obdModules = Collections.singletonList(0x00);
        instance.requestSupportedSpnPackets(listener, obdModules);

        String expected = "";
        expected += "DM24 from Engine #1 (0): " + NL;
        expected += "(Supporting Scaled Test Results) [" + NL;
        expected += "]" + NL;
        expected += "(Supports Data Stream Results) [" + NL;
        expected += "  SPN 102 - Engine Intake Manifold #1 Pressure" + NL;
        expected += "]" + NL;
        expected += "(Supports Freeze Frame Results) [" + NL;
        expected += "  SPN 102 - Engine Intake Manifold #1 Pressure" + NL;
        expected += "]" + NL;
        expected += NL;
        expected += NL;
        expected += "Engine #1 (0) does not have any tests that support scaled tests results" + NL;
        expected += "" + NL;
        expected += "ERROR No tests results returned" + NL;

        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(64950, 0x00);
        verify(j1939).requestDS("Direct DM24 Request to Engine #1 (0)", listener, true, DM24SPNSupportPacket.class,
                requestPacket);
    }

}
