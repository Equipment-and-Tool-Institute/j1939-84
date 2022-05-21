/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.modules;

import static org.etools.j1939_84.controllers.ResultsListener.NOOP;
import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939tools.j1939.J1939.GLOBAL_ADDR;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Stream;

import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939tools.bus.Bus;
import org.etools.j1939tools.bus.BusException;
import org.etools.j1939tools.bus.Packet;
import org.etools.j1939tools.bus.RequestResult;
import org.etools.j1939tools.j1939.J1939;
import org.etools.j1939tools.j1939.packets.AddressClaimPacket;
import org.etools.j1939tools.j1939.packets.DM56EngineFamilyPacket;
import org.etools.j1939tools.j1939.packets.DM5DiagnosticReadinessPacket;
import org.etools.j1939tools.j1939.packets.VehicleIdentificationPacket;
import org.etools.j1939tools.modules.DateTimeModule;
import org.etools.testdoc.TestDoc;
import org.etools.testdoc.TestItem;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
/**
 * Unit tests for the {@link VehicleInformationModule} class
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 */
@RunWith(MockitoJUnitRunner.class)
public class VehicleInformationModuleTest {

    private static final Charset UTF8 = StandardCharsets.UTF_8;
    private final TestResultsListener listener = new TestResultsListener();
    private VehicleInformationModule instance;
    @Spy
    private J1939 j1939;

    @Before
    public void setup() {
        DateTimeModule.setInstance(new TestDateTimeModule());
        instance = new VehicleInformationModule();
        instance.setJ1939(j1939);
    }

    @After
    public void tearDown() {
        // using spy now verifyNoMoreInteractions(j1939tools);
    }

    @Test
    @TestDoc(description = "Verify that engine family from the DM56 is correctly cached.", dependsOn = "DM56EngineFamilyPacketTest")
    public void testGetEngineFamilyName() throws Exception {
        DM56EngineFamilyPacket response = DM56EngineFamilyPacket.create(0x00, 2021, true, "family");
        doReturn(new RequestResult<>(false, response)).when(j1939)
                                                      .requestGlobal(null,
                                                                     DM56EngineFamilyPacket.class,
                                                                     NOOP);

        String actual = instance.getEngineFamilyName();
        instance.getEngineFamilyName(); // Make sure it's cached
        assertEquals("family", actual);

        verify(j1939).requestGlobal(null, DM56EngineFamilyPacket.class, NOOP);
    }

    @Test
    @TestDoc(description = "Verify that engine family from a missing DM56 is correctly not detected.", dependsOn = "DM56EngineFamilyPacketTest")
    public void testGetEngineFamilyNameNoResponse() {
        doReturn(RequestResult.empty()).when(j1939).requestGlobal(null, DM56EngineFamilyPacket.class, NOOP);

        try {
            instance.getEngineFamilyName();
            fail("An exception should have been thrown");
        } catch (IOException e) {
            assertEquals("Timeout Error Reading Engine Family", e.getMessage());
        }

        verify(j1939).requestGlobal(null, DM56EngineFamilyPacket.class, NOOP);
    }

    @Test
    @TestDoc(description = "Verify that multiple family names that are not equal results in error.", dependsOn = "DM56EngineFamilyPacketTest")
    public void testGetEngineFamilyNameWithDifferentResponses() {
        DM56EngineFamilyPacket response1 = mock(DM56EngineFamilyPacket.class);
        when(response1.getFamilyName()).thenReturn("name1");
        DM56EngineFamilyPacket response2 = mock(DM56EngineFamilyPacket.class);
        when(response2.getFamilyName()).thenReturn("name2");
        doReturn(new RequestResult<>(false, response1, response2)).when(j1939)
                                                                  .requestGlobal(null,
                                                                                 DM56EngineFamilyPacket.class,
                                                                                 NOOP);

        try {
            instance.getEngineFamilyName();
            fail("An exception should have been thrown");
        } catch (IOException e) {
            assertEquals("Different Engine Families Received", e.getMessage());
        }

        verify(j1939).requestGlobal(null, DM56EngineFamilyPacket.class, NOOP);
    }

    @Test
    @TestDoc(description = "Verify that engine model year from the DM56 is correctly cached.", dependsOn = "DM56EngineFamilyPacketTest")
    public void testGetEngineModelYear() throws Exception {
        DM56EngineFamilyPacket response = mock(DM56EngineFamilyPacket.class);
        when(response.getEngineModelYear()).thenReturn(123);
        doReturn(new RequestResult<>(false, response)).when(j1939)
                                                      .requestGlobal(null, DM56EngineFamilyPacket.class, NOOP);

        Integer actual = instance.getEngineModelYear();
        instance.getEngineModelYear(); // Make sure it's cached
        assertEquals(Integer.valueOf(123), actual);

        verify(j1939).requestGlobal(null, DM56EngineFamilyPacket.class, NOOP);
    }

    @Test
    @TestDoc(description = "Verify that a failure is generated when there is no response to the request for DM56.")
    public void testGetEngineModelYearNoResponse() {
        doReturn(RequestResult.empty()).when(j1939).requestGlobal(null, DM56EngineFamilyPacket.class, NOOP);

        try {
            instance.getEngineModelYear();
            fail("An exception should have been thrown");
        } catch (IOException e) {
            assertEquals("Timeout Error Reading Engine Model Year", e.getMessage());
        }

        verify(j1939).requestGlobal(null, DM56EngineFamilyPacket.class, NOOP);
    }

    @Test
    @TestDoc(description = "Verify that multiple model years that are not equal results in error.", dependsOn = "DM56EngineFamilyPacketTest")
    public void testGetEngineModelYearWithDifferentResponses() {
        DM56EngineFamilyPacket response1 = mock(DM56EngineFamilyPacket.class);
        when(response1.getEngineModelYear()).thenReturn(123);
        DM56EngineFamilyPacket response2 = mock(DM56EngineFamilyPacket.class);
        when(response2.getEngineModelYear()).thenReturn(456);
        doReturn(new RequestResult<>(false, response1, response2)).when(j1939)
                                                                  .requestGlobal(null,
                                                                                 DM56EngineFamilyPacket.class,
                                                                                 NOOP);

        try {
            instance.getEngineModelYear();
            fail("An exception should have been thrown");
        } catch (IOException e) {
            assertEquals("Different Engine Model Years Received", e.getMessage());
        }

        verify(j1939).requestGlobal(null, DM56EngineFamilyPacket.class, NOOP);
    }

    @Test
    @TestDoc(@TestItem(verifies = "6.1.5.1.a", description = "Verified that the VIN is requested with a global request.", dependsOn = "VehicleIdentificationPacketTest"))
    public void testGetVin() throws Exception {
        VehicleIdentificationPacket response = mock(VehicleIdentificationPacket.class);
        when(response.getVin()).thenReturn("vin");
        doReturn(new RequestResult<>(false, response)).when(j1939)
                                                      .requestGlobal(null, VehicleIdentificationPacket.class, NOOP);

        String vin = instance.getVin();
        instance.getVin(); // Make sure it's cached
        assertEquals("vin", vin);

        verify(j1939).requestGlobal(null, VehicleIdentificationPacket.class, NOOP);
    }

    @Test
    @TestDoc(@TestItem(verifies = "6.1.5.2.a", description = "Verify if a VIN request is not answered, error is thrown.", dependsOn = "VehicleIdentificationPacketTest"))
    public void testGetVinNoResponse() {
        doReturn(RequestResult.empty()).when(j1939).requestGlobal(null, VehicleIdentificationPacket.class, NOOP);

        try {
            instance.getVin();
            fail("An exception should have been thrown");
        } catch (IOException e) {
            assertEquals("Timeout Error Reading VIN", e.getMessage());
        }

        verify(j1939).requestGlobal(null, VehicleIdentificationPacket.class, NOOP);
    }

    @Test
    @TestDoc(@TestItem(verifies = "6.1.5.2.b", description = "Verify if a VIN request generates two different responses, error is thrown.", dependsOn = "VehicleIdentificationPacketTest"))
    public void testGetVinWithDifferentResponses() {
        VehicleIdentificationPacket response1 = mock(VehicleIdentificationPacket.class);
        when(response1.getVin()).thenReturn("vin1");
        VehicleIdentificationPacket response2 = mock(VehicleIdentificationPacket.class);
        when(response2.getVin()).thenReturn("vin2");
        doReturn(new RequestResult<>(false, response1, response2))
                                                                  .when(j1939)
                                                                  .requestGlobal(null,
                                                                                 VehicleIdentificationPacket.class,
                                                                                 NOOP);

        try {
            instance.getVin();
            fail("An exception should have been thrown");
        } catch (IOException e) {
            assertEquals("Different VINs Received", e.getMessage());
        }

        verify(j1939).requestGlobal(null, VehicleIdentificationPacket.class, NOOP);
    }

    @Test
    public void testReportAddressClaim() {
        AddressClaimPacket packet1 = new AddressClaimPacket(Packet.parse("18EEFF55 10 F7 45 01 00 45 00 01"));
        AddressClaimPacket packet2 = new AddressClaimPacket(Packet.parse("18EEFF3D 00 00 00 00 00 00 00 00"));
        AddressClaimPacket packet3 = new AddressClaimPacket(Packet.parse("18EEFF00 00 00 40 05 00 00 65 14"));

        doReturn(new RequestResult<>(false, packet1, packet2, packet3))
                                                                       .when(j1939)
                                                                       .requestGlobal("Global Request for Address Claim",
                                                                                      AddressClaimPacket.class,
                                                                                      listener);

        instance.reportAddressClaim(listener);
        assertEquals("", listener.getResults());

        verify(j1939).requestGlobal("Global Request for Address Claim", AddressClaimPacket.class, listener);
    }

    @Test
    public void testReportAddressClaimNoFunction0() {
        AddressClaimPacket packet1 = new AddressClaimPacket(Packet.parse("18EEFF55 10 F7 45 01 00 45 00 01"));
        doReturn(new RequestResult<>(false, packet1))
                                                     .when(j1939)
                                                     .requestGlobal("Global Request for Address Claim",
                                                                    AddressClaimPacket.class,
                                                                    listener);

        instance.reportAddressClaim(listener);
        assertEquals("Error: No ECU reported Function 0" + NL, listener.getResults());
        verify(j1939).requestGlobal("Global Request for Address Claim", AddressClaimPacket.class, listener);
    }

    @Test
    public void testReportAddressClaimNoResponse() {
        doReturn(RequestResult.empty(), RequestResult.empty(), RequestResult.empty())
                                                                                     .when(j1939)
                                                                                     .requestGlobal("Global Request for Address Claim",
                                                                                                    AddressClaimPacket.class,
                                                                                                    NOOP);
        instance.reportAddressClaim(NOOP);
        verify(j1939).requestGlobal("Global Request for Address Claim", AddressClaimPacket.class, NOOP);
    }

    @Test
    public void testReportConnectionSpeed() throws Exception {
        Bus bus = mock(Bus.class);
        when(j1939.getBus()).thenReturn(bus);
        when(bus.getConnectionSpeed()).thenReturn(250000);

        TestResultsListener listener = new TestResultsListener();
        instance.reportConnectionSpeed(listener);

        String expected = "10:15:30.0000 Baud Rate: 250,000 bps" + NL;
        assertEquals(expected, listener.getResults());
        verify(j1939).getBus();
    }

    @Test
    public void testReportConnectionSpeedWithException() throws Exception {
        Bus bus = mock(Bus.class);
        when(j1939.getBus()).thenReturn(bus);
        when(bus.getConnectionSpeed()).thenThrow(new BusException("Surprise"));

        TestResultsListener listener = new TestResultsListener();
        instance.reportConnectionSpeed(listener);

        String expected = "10:15:30.0000 Baud Rate: Could not be determined" + NL;
        assertEquals(expected, listener.getResults());
        verify(j1939).getBus();
    }

    @Test
    public void testGetOBDModules() throws BusException {
        final int pgn = DM5DiagnosticReadinessPacket.PGN;

        DM5DiagnosticReadinessPacket packet1 = new DM5DiagnosticReadinessPacket(
                                                                                Packet.create(pgn,
                                                                                              0x00,
                                                                                              0x11,
                                                                                              0x22,
                                                                                              20,
                                                                                              0x44,
                                                                                              0x55,
                                                                                              0x66,
                                                                                              0x77,
                                                                                              0x88));
        DM5DiagnosticReadinessPacket packet11 = new DM5DiagnosticReadinessPacket(
                                                                                 Packet.create(pgn,
                                                                                               0x00,
                                                                                               0x11,
                                                                                               0x22,
                                                                                               20,
                                                                                               0x44,
                                                                                               0x55,
                                                                                               0x66,
                                                                                               0x77,
                                                                                               0x88));
        DM5DiagnosticReadinessPacket packet2 = new DM5DiagnosticReadinessPacket(
                                                                                Packet.create(pgn,
                                                                                              0x17,
                                                                                              0x01,
                                                                                              0x02,
                                                                                              0x03,
                                                                                              0x04,
                                                                                              0x05,
                                                                                              0x06,
                                                                                              0x07,
                                                                                              0x08));
        DM5DiagnosticReadinessPacket packet22 = new DM5DiagnosticReadinessPacket(
                                                                                 Packet.create(pgn,
                                                                                               0x17,
                                                                                               0x01,
                                                                                               0x02,
                                                                                               0x03,
                                                                                               0x04,
                                                                                               0x05,
                                                                                               0x06,
                                                                                               0x07,
                                                                                               0x08));
        DM5DiagnosticReadinessPacket packet3 = new DM5DiagnosticReadinessPacket(
                                                                                Packet.create(pgn,
                                                                                              0x21,
                                                                                              0x10,
                                                                                              0x20,
                                                                                              19,
                                                                                              0x40,
                                                                                              0x50,
                                                                                              0x60,
                                                                                              0x70,
                                                                                              0x80));
        doReturn(Stream.of(packet1.getPacket(),
                           packet11.getPacket(),
                           packet2.getPacket(),
                           packet22.getPacket(),
                           packet3.getPacket())).when(j1939).read(anyLong(), any());

        List<Integer> results = instance.getOBDModules(NOOP);
        assertEquals(2, results.size());
        assertTrue(results.contains(0x00));
        assertTrue(results.contains(0x21));

        verify(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        verify(j1939).read(anyLong(), any());
    }
}
