/*
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.modules;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.controllers.ResultsListener.NOOP;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import org.etools.j1939_84.bus.Bus;
import org.etools.j1939_84.bus.BusException;
import org.etools.j1939_84.bus.Either;
import org.etools.j1939_84.bus.Packet;
import org.etools.j1939_84.bus.j1939.BusResult;
import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.packets.AddressClaimPacket;
import org.etools.j1939_84.bus.j1939.packets.ComponentIdentificationPacket;
import org.etools.j1939_84.bus.j1939.packets.DM19CalibrationInformationPacket;
import org.etools.j1939_84.bus.j1939.packets.DM56EngineFamilyPacket;
import org.etools.j1939_84.bus.j1939.packets.EngineHoursPacket;
import org.etools.j1939_84.bus.j1939.packets.HighResVehicleDistancePacket;
import org.etools.j1939_84.bus.j1939.packets.TotalVehicleDistancePacket;
import org.etools.j1939_84.bus.j1939.packets.VehicleIdentificationPacket;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.RequestResult;
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
@SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT",
                    justification = "The values returned are properly ignored on verify statements.")
@RunWith(MockitoJUnitRunner.class)
public class VehicleInformationModuleTest {

    private static final int BUS_ADDR = 0xA5;

    private static final Charset UTF8 = StandardCharsets.UTF_8;

    private VehicleInformationModule instance;

    @Spy
    private J1939 j1939;

    private final TestResultsListener listener = new TestResultsListener();

    @Before
    public void setup() {
        DateTimeModule.setInstance(new TestDateTimeModule());
        instance = new VehicleInformationModule();
        instance.setJ1939(j1939);
        DataRepository.clearInstance();
    }

    @After
    public void tearDown() {
        // using spy now verifyNoMoreInteractions(j1939);
    }

    @Test
    @TestDoc(description = "Verify that engine family from the DM56 is correctly cached.",
             dependsOn = "DM56EngineFamilyPacketTest")
    public void testGetEngineFamilyName() throws Exception {
        DM56EngineFamilyPacket response = mock(DM56EngineFamilyPacket.class);
        when(response.getFamilyName()).thenReturn("family");
        doReturn(new RequestResult<>(false, response)).when(j1939)
                .requestGlobal(null, DM56EngineFamilyPacket.class, NOOP
                );

        String actual = instance.getEngineFamilyName();
        instance.getEngineFamilyName(); // Make sure it's cached
        assertEquals("family", actual);

        verify(j1939).requestGlobal(null, DM56EngineFamilyPacket.class, NOOP);
    }

    @Test
    @TestDoc(description = "Verify that engine family from a missing DM56 is correctly not detected.",
             dependsOn = "DM56EngineFamilyPacketTest")
    public void testGetEngineFamilyNameNoResponse() {
        doReturn(RequestResult.empty()).when(j1939).requestGlobal(null, DM56EngineFamilyPacket.class, NOOP
        );

        try {
            instance.getEngineFamilyName();
            fail("An exception should have been thrown");
        } catch (IOException e) {
            assertEquals("Timeout Error Reading Engine Family", e.getMessage());
        }

        verify(j1939).requestGlobal(null, DM56EngineFamilyPacket.class, NOOP);
    }

    @Test
    @TestDoc(description = "Verify that multiple family names that are not equal results in error.",
             dependsOn = "DM56EngineFamilyPacketTest")
    public void testGetEngineFamilyNameWithDifferentResponses() {
        DM56EngineFamilyPacket response1 = mock(DM56EngineFamilyPacket.class);
        when(response1.getFamilyName()).thenReturn("name1");
        DM56EngineFamilyPacket response2 = mock(DM56EngineFamilyPacket.class);
        when(response2.getFamilyName()).thenReturn("name2");
        doReturn(new RequestResult<>(false, response1, response2)).when(j1939)
                .requestGlobal(null, DM56EngineFamilyPacket.class, NOOP);

        try {
            instance.getEngineFamilyName();
            fail("An exception should have been thrown");
        } catch (IOException e) {
            assertEquals("Different Engine Families Received", e.getMessage());
        }

        verify(j1939).requestGlobal(null, DM56EngineFamilyPacket.class, NOOP);
    }

    @Test
    @TestDoc(description = "Verify that engine model year from the DM56 is correctly cached.",
             dependsOn = "DM56EngineFamilyPacketTest")
    public void testGetEngineModelYear() throws Exception {
        DM56EngineFamilyPacket response = mock(DM56EngineFamilyPacket.class);
        when(response.getEngineModelYear()).thenReturn(123);
        doReturn(new RequestResult<>(false, response)).when(j1939)
                .requestGlobal(null, DM56EngineFamilyPacket.class, NOOP
                );

        Integer actual = instance.getEngineModelYear();
        instance.getEngineModelYear(); // Make sure it's cached
        assertEquals(Integer.valueOf(123), actual);

        verify(j1939).requestGlobal(null, DM56EngineFamilyPacket.class, NOOP);
    }

    @Test
    @TestDoc(description = "Verify that a failure is generated when there is no response to the request for DM56.")
    public void testGetEngineModelYearNoResponse() {
        doReturn(RequestResult.empty()).when(j1939).requestGlobal(null, DM56EngineFamilyPacket.class, NOOP
        );

        try {
            instance.getEngineModelYear();
            fail("An exception should have been thrown");
        } catch (IOException e) {
            assertEquals("Timeout Error Reading Engine Model Year", e.getMessage());
        }

        verify(j1939).requestGlobal(null, DM56EngineFamilyPacket.class, NOOP);
    }

    @Test
    @TestDoc(description = "Verify that multiple model years that are not equal results in error.",
             dependsOn = "DM56EngineFamilyPacketTest")
    public void testGetEngineModelYearWithDifferentResponses() {
        DM56EngineFamilyPacket response1 = mock(DM56EngineFamilyPacket.class);
        when(response1.getEngineModelYear()).thenReturn(123);
        DM56EngineFamilyPacket response2 = mock(DM56EngineFamilyPacket.class);
        when(response2.getEngineModelYear()).thenReturn(456);
        doReturn(new RequestResult<>(false, response1, response2)).when(j1939)
                .requestGlobal(null,
                               DM56EngineFamilyPacket.class, NOOP);

        try {
            instance.getEngineModelYear();
            fail("An exception should have been thrown");
        } catch (IOException e) {
            assertEquals("Different Engine Model Years Received", e.getMessage());
        }

        verify(j1939).requestGlobal(null, DM56EngineFamilyPacket.class, NOOP);
    }

    @Test
    @TestDoc(@TestItem(verifies = "6.1.5.1.a",
                       description = "Verified that the VIN is requested with a global request.",
                       dependsOn = "VehicleIdentificationPacketTest"))
    public void testGetVin() throws Exception {
        VehicleIdentificationPacket response = mock(VehicleIdentificationPacket.class);
        when(response.getVin()).thenReturn("vin");
        doReturn(new RequestResult<>(false, response)).when(j1939)
                .requestGlobal(null, VehicleIdentificationPacket.class, NOOP
                );

        String vin = instance.getVin();
        instance.getVin(); // Make sure it's cached
        assertEquals("vin", vin);

        verify(j1939).requestGlobal(null, VehicleIdentificationPacket.class, NOOP);
    }

    @Test
    @TestDoc(@TestItem(verifies = "6.1.5.2.a",
                       description = "Verify if a VIN request is not answered, error is thrown.",
                       dependsOn = "VehicleIdentificationPacketTest"))
    public void testGetVinNoResponse() {
        doReturn(RequestResult.empty()).when(j1939).requestGlobal(null, VehicleIdentificationPacket.class, NOOP
        );

        try {
            instance.getVin();
            fail("An exception should have been thrown");
        } catch (IOException e) {
            assertEquals("Timeout Error Reading VIN", e.getMessage());
        }

        verify(j1939).requestGlobal(null, VehicleIdentificationPacket.class, NOOP);
    }

    @Test
    @TestDoc(@TestItem(verifies = "6.1.5.2.b",
                       description = "Verify if a VIN request generates two different responses, error is thrown.",
                       dependsOn = "VehicleIdentificationPacketTest"))
    public void testGetVinWithDifferentResponses() {
        VehicleIdentificationPacket response1 = mock(VehicleIdentificationPacket.class);
        when(response1.getVin()).thenReturn("vin1");
        VehicleIdentificationPacket response2 = mock(VehicleIdentificationPacket.class);
        when(response2.getVin()).thenReturn("vin2");
        doReturn(new RequestResult<>(false, response1, response2))
                .when(j1939).requestGlobal(null, VehicleIdentificationPacket.class, NOOP);

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
        final int pgn = AddressClaimPacket.PGN;

        AddressClaimPacket packet1 = new AddressClaimPacket(Packet.parse("18EEFF55 10 F7 45 01 00 45 00 01"));
        AddressClaimPacket packet2 = new AddressClaimPacket(Packet.parse("18EEFF3D 00 00 00 00 00 00 00 00"));
        AddressClaimPacket packet3 = new AddressClaimPacket(Packet.parse("18EEFF00 00 00 40 05 00 00 65 14"));

        doReturn(new RequestResult<>(false, packet1, packet2, packet3))
                .when(j1939).requestGlobal("Global Request for Address Claim", AddressClaimPacket.class, listener);

        instance.reportAddressClaim(listener);
        assertEquals("", listener.getResults());

        verify(j1939).requestGlobal("Global Request for Address Claim", AddressClaimPacket.class, listener);
    }

    @Test
    public void testReportAddressClaimNoFunction0() {
        final int pgn = AddressClaimPacket.PGN;

        AddressClaimPacket packet1 = new AddressClaimPacket(Packet.parse("18EEFF55 10 F7 45 01 00 45 00 01"));
        doReturn(new RequestResult<>(false, packet1))
                .when(j1939).requestGlobal("Global Request for Address Claim", AddressClaimPacket.class, listener);

        instance.reportAddressClaim(listener);
        assertEquals("Error: No module reported Function 0" + NL, listener.getResults());
        verify(j1939).requestGlobal("Global Request for Address Claim", AddressClaimPacket.class, listener);
    }

    @Test
    public void testReportAddressClaimNoResponse() {
        final int pgn = AddressClaimPacket.PGN;

        doReturn(RequestResult.empty(), RequestResult.empty(), RequestResult.empty())
                .when(j1939).requestGlobal("Global Request for Address Claim", AddressClaimPacket.class, NOOP);
        instance.reportAddressClaim(NOOP);
        verify(j1939).requestGlobal("Global Request for Address Claim", AddressClaimPacket.class, NOOP);
    }

    @Test
    public void testReportCalibrationInformation() {
        final int pgn = DM19CalibrationInformationPacket.PGN;
        final byte[] calBytes1 = "ABCD1234567890123456".getBytes(UTF8);
        final byte[] calBytes2 = "EFGH1234567890123456".getBytes(UTF8);
        final byte[] calBytes3 = "IJKL1234567890123456".getBytes(UTF8);

        DM19CalibrationInformationPacket packet1 = new DM19CalibrationInformationPacket(
                Packet.create(pgn, 0x00, calBytes1));
        DM19CalibrationInformationPacket packet2 = new DM19CalibrationInformationPacket(
                Packet.create(pgn, 0x17, calBytes2));
        DM19CalibrationInformationPacket packet3 = new DM19CalibrationInformationPacket(
                Packet.create(pgn, 0x21, calBytes3));
        when(j1939.requestGlobal("Global DM19 Request", DM19CalibrationInformationPacket.class, NOOP))
                        .thenReturn(new RequestResult<>(false, packet1, packet2, packet3));

        instance.reportCalibrationInformation(NOOP);

        verify(j1939, times(2)).requestGlobal("Global DM19 Request", DM19CalibrationInformationPacket.class, NOOP);
    }

    @Test
    public void testReportCalibrationInformationWithAddress() {
        final int pgn = DM19CalibrationInformationPacket.PGN;
        final byte[] calBytes1 = "ABCD1234567890123456".getBytes(UTF8);

        DM19CalibrationInformationPacket packet1 = new DM19CalibrationInformationPacket(
                Packet.create(pgn, 0x00, calBytes1));

        doReturn(new BusResult<>(false, packet1)).when(j1939).requestDS(
                "Destination Specific DM19 Request to Engine #1 (0)",  DM19CalibrationInformationPacket.class, 0x00, NOOP);

        instance.reportCalibrationInformation(NOOP, 0x00);

        verify(j1939).requestDS("Destination Specific DM19 Request to Engine #1 (0)", DM19CalibrationInformationPacket.class, 0x00, NOOP);
    }

    @Test
    public void testReportCalibrationInformationWithAddressWithoutResponse() {

        doReturn(BusResult.empty()).when(j1939).requestDS(
                "Destination Specific DM19 Request to Engine #1 (0)",  DM19CalibrationInformationPacket.class, 0x00, NOOP);

        instance.reportCalibrationInformation(NOOP, 0x00);

        verify(j1939).requestDS("Destination Specific DM19 Request to Engine #1 (0)",  DM19CalibrationInformationPacket.class, 0x00, NOOP);
    }

    @Test
    public void testReportCalibrationInformationWithNoResponses() {
        doReturn(RequestResult.empty()).when(j1939)
                .requestGlobal("Global DM19 Request", DM19CalibrationInformationPacket.class, NOOP);

        instance.reportCalibrationInformation(NOOP);

        verify(j1939).requestGlobal("Global DM19 Request", DM19CalibrationInformationPacket.class, NOOP);
    }

    @Test
    public void testReportComponentIdentification() {
        final int pgn = ComponentIdentificationPacket.PGN;
        final byte[] bytes1 = "Make1*Model1*SerialNumber1**".getBytes(UTF8);
        final byte[] bytes2 = "****".getBytes(UTF8);
        final byte[] bytes3 = "Make3*Model3***".getBytes(UTF8);

        Packet requestPacket = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, 0xFF);

        ComponentIdentificationPacket packet1 = new ComponentIdentificationPacket(Packet.create(pgn, 0x00, bytes1));
        ComponentIdentificationPacket packet2 = new ComponentIdentificationPacket(Packet.create(pgn, 0x17, bytes2));
        ComponentIdentificationPacket packet3 = new ComponentIdentificationPacket(Packet.create(pgn, 0x21, bytes3));
        when(j1939.requestGlobal("Global Component Identification Request", ComponentIdentificationPacket.class, NOOP))
                        .thenReturn(new RequestResult<>(false, packet1, packet2, packet3));

        instance.reportComponentIdentification(NOOP);

        verify(j1939).createRequestPacket(pgn, 0xFF);
        verify(j1939, times(2)).requestGlobal("Global Component Identification Request",
                                              ComponentIdentificationPacket.class,
                                              NOOP);
    }

    @Test
    public void testReportComponentIdentificationWithNoResponse() {
        final int pgn = ComponentIdentificationPacket.PGN;

        doReturn(RequestResult.empty()).when(j1939)
                .requestGlobal("Global Component Identification Request", ComponentIdentificationPacket.class, NOOP);
        instance.reportComponentIdentification(NOOP);

        verify(j1939).requestGlobal("Global Component Identification Request",
                                    ComponentIdentificationPacket.class,
                                    NOOP);
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
    public void testReportEngineFamily() {
        final int pgn = DM56EngineFamilyPacket.PGN;
        final byte[] bytes = "2015MY-EUS HD ODB   *".getBytes(UTF8);

        DM56EngineFamilyPacket packet1 = new DM56EngineFamilyPacket(Packet.create(pgn, 0x00, bytes));
        DM56EngineFamilyPacket packet2 = new DM56EngineFamilyPacket(Packet.create(pgn, 0x17, bytes));
        DM56EngineFamilyPacket packet3 = new DM56EngineFamilyPacket(Packet.create(pgn, 0x21, bytes));
        doReturn(new RequestResult<>(false, packet1, packet2, packet3))
                .when(j1939).requestGlobal("Global DM56 Request", DM56EngineFamilyPacket.class, NOOP);

        List<DM56EngineFamilyPacket> packets = instance.requestDM56(NOOP);
        assertEquals(3, packets.size());
        assertEquals(packet1, packets.get(0));
        assertEquals(packet2, packets.get(1));
        assertEquals(packet3, packets.get(2));

        verify(j1939).requestGlobal("Global DM56 Request", DM56EngineFamilyPacket.class, NOOP);
    }

    @Test
    public void testReportEngineFamilyWithNoResponses() {

        doReturn(RequestResult.empty()).when(j1939).requestGlobal("Global DM56 Request", DM56EngineFamilyPacket.class,
                                                                  NOOP);

        List<DM56EngineFamilyPacket> packets = instance.requestDM56(NOOP);
        assertEquals(0, packets.size());

        verify(j1939).requestGlobal("Global DM56 Request", DM56EngineFamilyPacket.class, NOOP);
    }

    @Test
    public void testReportEngineHours() {
        final int pgn = EngineHoursPacket.PGN;

        EngineHoursPacket packet1 = new EngineHoursPacket(Packet.create(pgn, 0x00, 1, 2, 3, 4, 5, 6, 7, 8));
        EngineHoursPacket packet2 = new EngineHoursPacket(Packet.create(pgn, 0x01, 8, 7, 6, 5, 4, 3, 2, 1));
        doReturn(new RequestResult<>(false, packet1, packet2)).when(j1939)
                .requestGlobal("Engine Hours Request", EngineHoursPacket.class, NOOP);

        instance.reportEngineHours(NOOP);

        verify(j1939).requestGlobal("Engine Hours Request", EngineHoursPacket.class, NOOP);
    }

    @Test
    public void testReportEngineHoursWithNoResponse() {
        final int pgn = EngineHoursPacket.PGN;

        doReturn(RequestResult.empty()).when(j1939)
                .requestGlobal("Engine Hours Request", EngineHoursPacket.class, NOOP);

        instance.reportEngineHours(NOOP);

        verify(j1939).requestGlobal("Engine Hours Request", EngineHoursPacket.class, NOOP);
    }

    @Test
    public void testReportVehicleDistanceWithHiRes() {
        final int pgn = HighResVehicleDistancePacket.PGN;
        HighResVehicleDistancePacket packet0 = new HighResVehicleDistancePacket(
                Packet.create(pgn, 0x00, 0, 0, 0, 0, 0, 0, 0, 0));
        HighResVehicleDistancePacket packet1 = new HighResVehicleDistancePacket(
                Packet.create(pgn, 0x01, 1, 1, 1, 1, 1, 1, 1, 1));
        HighResVehicleDistancePacket packet2 = new HighResVehicleDistancePacket(
                Packet.create(pgn, 0x02, 2, 2, 2, 2, 2, 2, 2, 2));
        HighResVehicleDistancePacket packetFF = new HighResVehicleDistancePacket(
                Packet.create(pgn, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF));

        doReturn(Stream.of(packet0, packet1, packet2, packetFF).map(p -> new Either<>(p, null))).when(j1939)
                .read(HighResVehicleDistancePacket.class, 3, TimeUnit.SECONDS);

        String expected = "";
        expected += "10:15:30.0000 Vehicle Distance" + NL;
        expected += "High Resolution Vehicle Distance from Turbocharger (2): " + NL;
        expected += "  SPN   917, Total Vehicle Distance (High Resolution): 168430090.000000 m" + NL;
        expected += "  SPN   918, Trip Distance (High Resolution): 168430090.000000 m" + NL;
        expected += NL;

        TestResultsListener listener = new TestResultsListener();
        instance.reportVehicleDistance(listener);
        assertEquals(expected, listener.getResults());

        verify(j1939).read(HighResVehicleDistancePacket.class, 3, TimeUnit.SECONDS);
    }

    @Test
    public void testReportVehicleDistanceWithLoRes() {
        doReturn(Stream.empty()).when(j1939).read(HighResVehicleDistancePacket.class, 3, TimeUnit.SECONDS);
        final int pgn = TotalVehicleDistancePacket.PGN;
        TotalVehicleDistancePacket packet0 = new TotalVehicleDistancePacket(
                Packet.create(pgn, 0x00, 0, 0, 0, 0, 0, 0, 0, 0));
        TotalVehicleDistancePacket packet1 = new TotalVehicleDistancePacket(
                Packet.create(pgn, 0x01, 1, 1, 1, 1, 1, 1, 1, 1));
        TotalVehicleDistancePacket packet2 = new TotalVehicleDistancePacket(
                Packet.create(pgn, 0x02, 2, 2, 2, 2, 2, 2, 2, 2));
        TotalVehicleDistancePacket packetFF = new TotalVehicleDistancePacket(
                Packet.create(pgn, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF));

        doReturn(Stream.of(packet2, packet1, packet0, packetFF).map(p -> new Either<>(p, null))).when(j1939)
                .read(TotalVehicleDistancePacket.class, 300, TimeUnit.MILLISECONDS);
        String expected = "";
        expected += "10:15:30.0000 Vehicle Distance" + NL;
        expected += "Total Vehicle Distance from Turbocharger (2): " + NL;
        expected += "  SPN   244, Trip Distance: 4210752.250000 km" + NL;
        expected += "  SPN   245, Total Vehicle Distance: 4210752.250000 km" + NL;
        expected += NL;

        TestResultsListener listener = new TestResultsListener();
        instance.reportVehicleDistance(listener);
        assertEquals(expected, listener.getResults());

        verify(j1939).read(HighResVehicleDistancePacket.class, 3, TimeUnit.SECONDS);
        verify(j1939).read(TotalVehicleDistancePacket.class, 300, TimeUnit.MILLISECONDS);
    }

    @Test
    public void testReportVehicleDistanceWithNoResponse() {
        doReturn(Stream.empty()).when(j1939).read(HighResVehicleDistancePacket.class, 3, TimeUnit.SECONDS);
        doReturn(Stream.empty()).when(j1939).read(TotalVehicleDistancePacket.class, 300, TimeUnit.MILLISECONDS);

        String expected = "";
        expected += "10:15:30.0000 Vehicle Distance" + NL;
        expected += "Error: Timeout - No Response." + NL;

        TestResultsListener listener = new TestResultsListener();
        instance.reportVehicleDistance(listener);
        assertEquals(expected, listener.getResults());

        verify(j1939).read(HighResVehicleDistancePacket.class, 3, TimeUnit.SECONDS);
        verify(j1939).read(TotalVehicleDistancePacket.class, 300, TimeUnit.MILLISECONDS);
    }

    @Test
    public void testReportVin() {
        final int pgn = VehicleIdentificationPacket.PGN;
        final byte[] vinBytes = "12345678901234567890*".getBytes(UTF8);

        VehicleIdentificationPacket packet1 = new VehicleIdentificationPacket(Packet.create(pgn, 0x00, vinBytes));
        VehicleIdentificationPacket packet2 = new VehicleIdentificationPacket(Packet.create(pgn, 0x17, vinBytes));
        VehicleIdentificationPacket packet3 = new VehicleIdentificationPacket(Packet.create(pgn, 0x21, vinBytes));
        doReturn(new RequestResult<>(false, packet1, packet2, packet3))
                .when(j1939).requestGlobal("Global VIN Request", VehicleIdentificationPacket.class, NOOP);

        List<VehicleIdentificationPacket> packets = instance.reportVin(NOOP);
        assertEquals(3, packets.size());
        assertEquals(packet1, packets.get(0));
        assertEquals(packet2, packets.get(1));
        assertEquals(packet3, packets.get(2));

        verify(j1939).requestGlobal("Global VIN Request", VehicleIdentificationPacket.class, NOOP);
    }

    @Test
    public void testReportVinWithNoResponses() throws BusException {
        final int pgn = VehicleIdentificationPacket.PGN;
        Packet requestPacket = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, 0xFF);

        doReturn(Stream.empty(), Stream.empty(), Stream.empty()).when(j1939).read(anyLong(), any());

        String expected = "10:15:30.0000 Global VIN Request" + NL;
        expected += "10:15:30.0000 18EAFFA5 [3] EC FE 00 (TX)" + NL;
        expected += "Error: Timeout - No Response." + NL;

        List<VehicleIdentificationPacket> packets = instance.reportVin(listener);
        assertEquals(0, packets.size());
        assertEquals(expected, listener.getResults());
        verify(j1939).createRequestPacket(pgn, 0xFF);
        verify(j1939).read(anyLong(), any());
    }

}
