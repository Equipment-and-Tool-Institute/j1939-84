/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.modules;

import static org.etools.j1939_84.J1939_84.NL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

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
import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.packets.AddressClaimPacket;
import org.etools.j1939_84.bus.j1939.packets.ComponentIdentificationPacket;
import org.etools.j1939_84.bus.j1939.packets.DM19CalibrationInformationPacket;
import org.etools.j1939_84.bus.j1939.packets.DM56EngineFamilyPacket;
import org.etools.j1939_84.bus.j1939.packets.EngineHoursPacket;
import org.etools.j1939_84.bus.j1939.packets.HighResVehicleDistancePacket;
import org.etools.j1939_84.bus.j1939.packets.TotalVehicleDistancePacket;
import org.etools.j1939_84.bus.j1939.packets.VehicleIdentificationPacket;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.testdoc.TestDoc;
import org.etools.testdoc.TestItem;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Unit tests for the {@link VehicleInformationModule} class
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
@SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT",
                    justification = "The values returned are properly ignored on verify statements.")
@RunWith(MockitoJUnitRunner.class)
public class VehicleInformationModuleTest {

    private static final int BUS_ADDR = 0xA5;

    private static final Charset UTF8 = StandardCharsets.UTF_8;

    private final DateTimeModule dateTimeModule = new TestDateTimeModule();

    private VehicleInformationModule instance;

    @Mock
    private J1939 j1939;

    @Before
    public void setup() {
        instance = new VehicleInformationModule(dateTimeModule);
        instance.setJ1939(j1939);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(j1939);
    }

    @Test
    @TestDoc(description = "Verify that engine family from the DM56 is correctly cached.",
             dependsOn = "DM56EngineFamilyPacketTest")
    public void testGetEngineFamilyName() throws Exception {
        DM56EngineFamilyPacket response = mock(DM56EngineFamilyPacket.class);
        when(response.getFamilyName()).thenReturn("family");
        when(j1939.requestMultiple(DM56EngineFamilyPacket.class)).thenReturn(Stream.of(new Either<>(response, null)));

        String actual = instance.getEngineFamilyName();
        instance.getEngineFamilyName(); // Make sure it's cached
        assertEquals("family", actual);

        verify(j1939).requestMultiple(DM56EngineFamilyPacket.class);
    }

    @Test
    @TestDoc(description = "Verify that engine family from a missing DM56 is correctly not detected.",
             dependsOn = "DM56EngineFamilyPacketTest")
    public void testGetEngineFamilyNameNoResponse() throws Exception {
        when(j1939.requestMultiple(DM56EngineFamilyPacket.class)).thenReturn(Stream.empty());

        try {
            instance.getEngineFamilyName();
            fail("An exception should have been thrown");
        } catch (IOException e) {
            assertEquals("Timeout Error Reading Engine Family", e.getMessage());
        }

        verify(j1939).requestMultiple(DM56EngineFamilyPacket.class);
    }

    @Test
    @TestDoc(description = "Verify that multiple family names that are not equal results in error.",
             dependsOn = "DM56EngineFamilyPacketTest")
    public void testGetEngineFamilyNameWithDifferentResponses() throws Exception {
        DM56EngineFamilyPacket response1 = mock(DM56EngineFamilyPacket.class);
        when(response1.getFamilyName()).thenReturn("name1");
        DM56EngineFamilyPacket response2 = mock(DM56EngineFamilyPacket.class);
        when(response2.getFamilyName()).thenReturn("name2");
        when(j1939.requestMultiple(DM56EngineFamilyPacket.class))
                .thenReturn(Stream.of(new Either<>(response1, null), new Either<>(response2, null)));

        try {
            instance.getEngineFamilyName();
            fail("An exception should have been thrown");
        } catch (IOException e) {
            assertEquals("Different Engine Families Received", e.getMessage());
        }

        verify(j1939).requestMultiple(DM56EngineFamilyPacket.class);
    }

    @Test
    @TestDoc(description = "Verify that engine model year from the DM56 is correctly cached.",
             dependsOn = "DM56EngineFamilyPacketTest")
    public void testGetEngineModelYear() throws Exception {
        DM56EngineFamilyPacket response = mock(DM56EngineFamilyPacket.class);
        when(response.getEngineModelYear()).thenReturn(123);
        when(j1939.requestMultiple(DM56EngineFamilyPacket.class)).thenReturn(Stream.of(new Either<>(response, null)));

        Integer actual = instance.getEngineModelYear();
        instance.getEngineModelYear(); // Make sure it's cached
        assertEquals(Integer.valueOf(123), actual);

        verify(j1939).requestMultiple(DM56EngineFamilyPacket.class);
    }

    @Test
    @TestDoc(description = "Verify that a failure is generated when there is no response to the request for DM56.")
    public void testGetEngineModelYearNoResponse() throws Exception {
        when(j1939.requestMultiple(DM56EngineFamilyPacket.class)).thenReturn(Stream.empty());

        try {
            instance.getEngineModelYear();
            fail("An exception should have been thrown");
        } catch (IOException e) {
            assertEquals("Timeout Error Reading Engine Model Year", e.getMessage());
        }

        verify(j1939).requestMultiple(DM56EngineFamilyPacket.class);
    }

    @Test
    @TestDoc(description = "Verify that multiple model years that are not equal results in error.",
             dependsOn = "DM56EngineFamilyPacketTest")
    public void testGetEngineModelYearWithDifferentResponses() throws Exception {
        DM56EngineFamilyPacket response1 = mock(DM56EngineFamilyPacket.class);
        when(response1.getEngineModelYear()).thenReturn(123);
        DM56EngineFamilyPacket response2 = mock(DM56EngineFamilyPacket.class);
        when(response2.getEngineModelYear()).thenReturn(456);
        when(j1939.requestMultiple(DM56EngineFamilyPacket.class))
                .thenReturn(Stream.of(new Either<>(response1, null), new Either<>(response2, null)));

        try {
            instance.getEngineModelYear();
            fail("An exception should have been thrown");
        } catch (IOException e) {
            assertEquals("Different Engine Model Years Received", e.getMessage());
        }

        verify(j1939).requestMultiple(DM56EngineFamilyPacket.class);
    }

    @Test
    @TestDoc(@TestItem(verifies = "6.1.5.1.a",
                       description = "Verified that the VIN is requested with a global request.",
                       dependsOn = "VehicleIdentificationPacketTest"))
    public void testGetVin() throws Exception {
        VehicleIdentificationPacket response = mock(VehicleIdentificationPacket.class);
        when(response.getVin()).thenReturn("vin");
        when(j1939.requestMultiple(VehicleIdentificationPacket.class))
                .thenReturn(Stream.of(new Either<>(response, null)));

        String vin = instance.getVin();
        instance.getVin(); // Make sure it's cached
        assertEquals("vin", vin);

        verify(j1939).requestMultiple(VehicleIdentificationPacket.class);
    }

    @Test
    @TestDoc(@TestItem(verifies = "6.1.5.2.a",
                       description = "Verify if a VIN request is not answered, error is thrown.",
                       dependsOn = "VehicleIdentificationPacketTest"))
    public void testGetVinNoResponse() throws Exception {
        when(j1939.requestMultiple(VehicleIdentificationPacket.class)).thenReturn(Stream.empty());

        try {
            instance.getVin();
            fail("An exception should have been thrown");
        } catch (IOException e) {
            assertEquals("Timeout Error Reading VIN", e.getMessage());
        }

        verify(j1939).requestMultiple(VehicleIdentificationPacket.class);
    }

    @Test
    @TestDoc(@TestItem(verifies = "6.1.5.2.b",
                       description = "Verify if a VIN request generates two different responses, error is thrown.",
                       dependsOn = "VehicleIdentificationPacketTest"))
    public void testGetVinWithDifferentResponses() throws Exception {
        VehicleIdentificationPacket response1 = mock(VehicleIdentificationPacket.class);
        when(response1.getVin()).thenReturn("vin1");
        VehicleIdentificationPacket response2 = mock(VehicleIdentificationPacket.class);
        when(response2.getVin()).thenReturn("vin2");
        when(j1939.requestMultiple(VehicleIdentificationPacket.class))
                .thenReturn(Stream.of(new Either<>(response1, null), new Either<>(response2, null)));

        try {
            instance.getVin();
            fail("An exception should have been thrown");
        } catch (IOException e) {
            assertEquals("Different VINs Received", e.getMessage());
        }

        verify(j1939).requestMultiple(VehicleIdentificationPacket.class);
    }

    @Test
    public void testReportAddressClaim() {
        final int pgn = AddressClaimPacket.PGN;

        Packet requestPacket = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        when(j1939.createRequestPacket(pgn, 0xFF)).thenReturn(requestPacket);

        AddressClaimPacket packet1 = new AddressClaimPacket(Packet.parse("18EEFF55 10 F7 45 01 00 45 00 01"));
        AddressClaimPacket packet2 = new AddressClaimPacket(Packet.parse("18EEFF3D 00 00 00 00 00 00 00 00"));
        AddressClaimPacket packet3 = new AddressClaimPacket(Packet.parse("18EEFF00 00 00 40 05 00 00 65 14"));
        when(j1939.requestMultiple(AddressClaimPacket.class, requestPacket))
                .thenReturn(Stream.of(new Either<>(packet1, null), new Either<>(packet2, null),
                        new Either<>(packet3, null)));

        String expected = "";
        expected += "10:15:30.000 Global Request for Address Claim" + NL;
        expected += "10:15:30.000 18EAFFA5 00 EE 00 (TX)" + NL;
        expected += "10:15:30.000 18EEFF55 10 F7 45 01 00 45 00 01" + NL;
        expected += "Diesel Particulate Filter Controller (85) reported as: {" + NL;
        expected += "  Industry Group: Global" + NL;
        expected += "  Vehicle System: Non-specific System, System Instance: 1" + NL;
        expected += "  Function: Engine Emission Aftertreatment System, Functional Instance: 0, ECU Instance: 0" + NL;
        expected += "  Manufactured by: Cummins Inc, Identity Number: 390928" + NL;
        expected += "  Is not arbitrary address capable." + NL;
        expected += "}" + NL;
        expected += "10:15:30.000 18EEFF3D 00 00 00 00 00 00 00 00" + NL;
        expected += "Exhaust Emission Controller (61) reported as: {" + NL;
        expected += "  Industry Group: Global" + NL;
        expected += "  Vehicle System: Non-specific System, System Instance: 0" + NL;
        expected += "  Function: Engine, Functional Instance: 0, ECU Instance: 0" + NL;
        expected += "  Manufactured by: Reserved, Identity Number: 0" + NL;
        expected += "  Is not arbitrary address capable." + NL;
        expected += "}" + NL;
        expected += "10:15:30.000 18EEFF00 00 00 40 05 00 00 65 14" + NL;
        expected += "Engine #1 (0) reported as: {" + NL;
        expected += "  Industry Group: On-Highway Equipment" + NL;
        expected += "  Vehicle System: Unknown System (50), System Instance: 4" + NL;
        expected += "  Function: Unknown Function (0), Functional Instance: 0, ECU Instance: 0" + NL;
        expected += "  Manufactured by: International Truck and Engine Corporation - Engine Electronics, Identity Number: 0"
                + NL;
        expected += "  Is not arbitrary address capable." + NL;
        expected += "}" + NL;
        TestResultsListener listener = new TestResultsListener();
        instance.reportAddressClaim(listener);
        assertEquals(expected, listener.getResults());
        verify(j1939).createRequestPacket(pgn, 0xFF);
        verify(j1939).requestMultiple(AddressClaimPacket.class, requestPacket);
    }

    @Test
    public void testReportAddressClaimNoFunction0() {
        final int pgn = AddressClaimPacket.PGN;

        Packet requestPacket = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        when(j1939.createRequestPacket(pgn, 0xFF)).thenReturn(requestPacket);

        AddressClaimPacket packet1 = new AddressClaimPacket(Packet.parse("18EEFF55 10 F7 45 01 00 45 00 01"));
        when(j1939.requestMultiple(AddressClaimPacket.class, requestPacket))
                .thenReturn(Stream.of(new Either<>(packet1, null)));

        String expected = "";
        expected += "10:15:30.000 Global Request for Address Claim" + NL;
        expected += "10:15:30.000 18EAFFA5 00 EE 00 (TX)" + NL;
        expected += "10:15:30.000 18EEFF55 10 F7 45 01 00 45 00 01" + NL;
        expected += "Diesel Particulate Filter Controller (85) reported as: {" + NL;
        expected += "  Industry Group: Global" + NL;
        expected += "  Vehicle System: Non-specific System, System Instance: 1" + NL;
        expected += "  Function: Engine Emission Aftertreatment System, Functional Instance: 0, ECU Instance: 0" + NL;
        expected += "  Manufactured by: Cummins Inc, Identity Number: 390928" + NL;
        expected += "  Is not arbitrary address capable." + NL;
        expected += "}" + NL;
        expected += "Error: No module reported Function 0" + NL;
        TestResultsListener listener = new TestResultsListener();
        instance.reportAddressClaim(listener);
        assertEquals(expected, listener.getResults());
        verify(j1939).createRequestPacket(pgn, 0xFF);
        verify(j1939).requestMultiple(AddressClaimPacket.class, requestPacket);
    }

    @Test
    public void testReportAddressClaimNoResponse() {
        final int pgn = AddressClaimPacket.PGN;

        Packet requestPacket = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        when(j1939.createRequestPacket(pgn, 0xFF)).thenReturn(requestPacket);

        when(j1939.requestMultiple(AddressClaimPacket.class, requestPacket)).thenReturn(Stream.empty())
                .thenReturn(Stream.empty()).thenReturn(Stream.empty());

        String expected = "";
        expected += "10:15:30.000 Global Request for Address Claim" + NL;
        expected += "10:15:30.000 18EAFFA5 00 EE 00 (TX)" + NL;
        expected += "Error: Timeout - No Response." + NL;
        TestResultsListener listener = new TestResultsListener();
        instance.reportAddressClaim(listener);
        assertEquals(expected, listener.getResults());
        verify(j1939).createRequestPacket(pgn, 0xFF);
        verify(j1939).requestMultiple(AddressClaimPacket.class, requestPacket);
    }

    @Test
    public void testReportCalibrationInformation() {
        final int pgn = DM19CalibrationInformationPacket.PGN;
        final byte[] calBytes1 = "ABCD1234567890123456".getBytes(UTF8);
        final byte[] calBytes2 = "EFGH1234567890123456".getBytes(UTF8);
        final byte[] calBytes3 = "IJKL1234567890123456".getBytes(UTF8);

        Packet requestPacket = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        when(j1939.createRequestPacket(pgn, 0xFF)).thenReturn(requestPacket);

        DM19CalibrationInformationPacket packet1 = new DM19CalibrationInformationPacket(
                Packet.create(pgn, 0x00, calBytes1));
        DM19CalibrationInformationPacket packet2 = new DM19CalibrationInformationPacket(
                Packet.create(pgn, 0x17, calBytes2));
        DM19CalibrationInformationPacket packet3 = new DM19CalibrationInformationPacket(
                Packet.create(pgn, 0x21, calBytes3));
        when(j1939.requestMultiple(DM19CalibrationInformationPacket.class, requestPacket))
                .thenReturn(Stream.of(packet1, packet2, packet3).map(p -> new Either<>(p, null)));

        String expected = "";
        expected += "10:15:30.000 Global DM19 (Calibration Information) Request" + NL;
        expected += "10:15:30.000 18EAFFA5 00 D3 00 (TX)" + NL;
        expected += "10:15:30.000 18D30000 41 42 43 44 31 32 33 34 35 36 37 38 39 30 31 32 33 34 35 36" + NL;
        expected += "DM19 from Engine #1 (0): CAL ID of 1234567890123456 and CVN of 0x44434241" + NL;
        expected += "10:15:30.000 18D30017 45 46 47 48 31 32 33 34 35 36 37 38 39 30 31 32 33 34 35 36" + NL;
        expected += "DM19 from Instrument Cluster #1 (23): CAL ID of 1234567890123456 and CVN of 0x48474645" + NL;
        expected += "10:15:30.000 18D30021 49 4A 4B 4C 31 32 33 34 35 36 37 38 39 30 31 32 33 34 35 36" + NL;
        expected += "DM19 from Body Controller (33): CAL ID of 1234567890123456 and CVN of 0x4C4B4A49" + NL;
        TestResultsListener listener = new TestResultsListener();
        instance.reportCalibrationInformation(listener);
        assertEquals(expected, listener.getResults());
        verify(j1939).createRequestPacket(pgn, 0xFF);
        verify(j1939).requestMultiple(DM19CalibrationInformationPacket.class, requestPacket);
    }

    @Test
    public void testReportCalibrationInformationWithAddress() throws InterruptedException {
        final int pgn = DM19CalibrationInformationPacket.PGN;
        final byte[] calBytes1 = "ABCD1234567890123456".getBytes(UTF8);

        Packet requestPacket = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        when(j1939.createRequestPacket(pgn, 0x00)).thenReturn(requestPacket);

        DM19CalibrationInformationPacket packet1 = new DM19CalibrationInformationPacket(
                Packet.create(pgn, 0x00, calBytes1));
        DM19CalibrationInformationPacket packet2 = new DM19CalibrationInformationPacket(
                Packet.create(pgn, 0x17, new byte[] {}));
        DM19CalibrationInformationPacket packet3 = new DM19CalibrationInformationPacket(
                Packet.create(pgn, 0x21, new byte[] {}));

        when(j1939.requestRaw(DM19CalibrationInformationPacket.class, requestPacket, 5500, TimeUnit.MILLISECONDS))
                .thenReturn(Stream.of(packet1, packet2, packet3).map(p -> new Either<>(p, null)));

        String expected = "";
        expected += "10:15:30.000 DS DM19 (Calibration Information) Request to 00" + NL;
        expected += "10:15:30.000 18EAFFA5 00 D3 00 (TX)" + NL;
        expected += "10:15:30.000 18D30000 41 42 43 44 31 32 33 34 35 36 37 38 39 30 31 32 33 34 35 36" + NL;
        // why were we expecting calibration packets from the wrong addresses?
        // expected += "10:15:30.000 18D30017 " + NL;
        // expected += "10:15:30.000 18D30021 " + NL;
        TestResultsListener listener = new TestResultsListener();
        instance.reportCalibrationInformation(listener, 0x00);
        assertEquals(expected, listener.getResults());
        verify(j1939).createRequestPacket(pgn, 0x00);
        verify(j1939).requestRaw(DM19CalibrationInformationPacket.class, requestPacket, 5500, TimeUnit.MILLISECONDS);
    }

    @Test
    public void testReportCalibrationInformationWithAddressWithoutResponse() {
        final int pgn = DM19CalibrationInformationPacket.PGN;

        Packet requestPacket = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        when(j1939.createRequestPacket(pgn, 0x00)).thenReturn(requestPacket);

        when(j1939.requestRaw(DM19CalibrationInformationPacket.class, requestPacket, 5500, TimeUnit.MILLISECONDS))
                .thenReturn(Stream.empty())
                .thenReturn(Stream.empty())
                .thenReturn(Stream.empty());

        String expected = "";
        expected += "10:15:30.000 DS DM19 (Calibration Information) Request to 00" + NL;
        expected += "10:15:30.000 18EAFFA5 00 D3 00 (TX)" + NL;
        expected += "Error: Timeout - No Response." + NL;

        TestResultsListener listener = new TestResultsListener();
        instance.reportCalibrationInformation(listener, 0x00);

        assertEquals(expected, listener.getResults());
        verify(j1939).createRequestPacket(pgn, 0x00);
        verify(j1939, times(3))
                .requestRaw(DM19CalibrationInformationPacket.class, requestPacket, 5500, TimeUnit.MILLISECONDS);
    }

    @Test
    public void testReportCalibrationInformationWithNoResponses() {
        final int pgn = DM19CalibrationInformationPacket.PGN;

        Packet requestPacket = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        when(j1939.createRequestPacket(pgn, 0xFF)).thenReturn(requestPacket);

        when(j1939.requestMultiple(DM19CalibrationInformationPacket.class, requestPacket)).thenReturn(Stream.empty());

        String expected = "";
        expected += "10:15:30.000 Global DM19 (Calibration Information) Request" + NL;
        expected += "10:15:30.000 18EAFFA5 00 D3 00 (TX)" + NL;
        expected += "Error: Timeout - No Response." + NL;
        TestResultsListener listener = new TestResultsListener();
        instance.reportCalibrationInformation(listener);
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, 0xFF);
        verify(j1939).requestMultiple(DM19CalibrationInformationPacket.class, requestPacket);
    }

    @Test
    public void testReportComponentIdentification() {
        final int pgn = ComponentIdentificationPacket.PGN;
        final byte[] bytes1 = "Make1*Model1*SerialNumber1**".getBytes(UTF8);
        final byte[] bytes2 = "****".getBytes(UTF8);
        final byte[] bytes3 = "Make3*Model3***".getBytes(UTF8);

        Packet requestPacket = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        when(j1939.createRequestPacket(pgn, 0xFF)).thenReturn(requestPacket);

        ComponentIdentificationPacket packet1 = new ComponentIdentificationPacket(Packet.create(pgn, 0x00, bytes1));
        ComponentIdentificationPacket packet2 = new ComponentIdentificationPacket(Packet.create(pgn, 0x17, bytes2));
        ComponentIdentificationPacket packet3 = new ComponentIdentificationPacket(Packet.create(pgn, 0x21, bytes3));
        when(j1939.requestMultiple(ComponentIdentificationPacket.class, requestPacket))
                .thenReturn(Stream.of(packet1, packet2, packet3).map(p -> new Either<>(p, null)));

        String expected = "";
        expected += "10:15:30.000 Global Component Identification Request" + NL;
        expected += "10:15:30.000 18EAFFA5 EB FE 00 (TX)" + NL;
        expected += "10:15:30.000 18FEEB00 4D 61 6B 65 31 2A 4D 6F 64 65 6C 31 2A 53 65 72 69 61 6C 4E 75 6D 62 65 72 31 2A 2A"
                + NL;
        expected += "Found Engine #1 (0): Make: Make1, Model: Model1, Serial: SerialNumber1, Unit: " + NL;
        expected += "10:15:30.000 18FEEB17 2A 2A 2A 2A" + NL;
        expected += "Found Instrument Cluster #1 (23): Make: , Model: , Serial: , Unit: " + NL;
        expected += "10:15:30.000 18FEEB21 4D 61 6B 65 33 2A 4D 6F 64 65 6C 33 2A 2A 2A" + NL;
        expected += "Found Body Controller (33): Make: Make3, Model: Model3, Serial: , Unit: " + NL;

        TestResultsListener listener = new TestResultsListener();
        instance.reportComponentIdentification(listener);
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, 0xFF);
        verify(j1939).requestMultiple(ComponentIdentificationPacket.class, requestPacket);
    }

    @Test
    public void testReportComponentIdentificationWithNoResponse() {
        final int pgn = ComponentIdentificationPacket.PGN;

        Packet requestPacket = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        when(j1939.createRequestPacket(pgn, 0xFF)).thenReturn(requestPacket);

        when(j1939.requestMultiple(ComponentIdentificationPacket.class, requestPacket)).thenReturn(Stream.empty());

        String expected = "";
        expected += "10:15:30.000 Global Component Identification Request" + NL;
        expected += "10:15:30.000 18EAFFA5 EB FE 00 (TX)" + NL;
        expected += "Error: Timeout - No Response." + NL;

        TestResultsListener listener = new TestResultsListener();
        instance.reportComponentIdentification(listener);
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, 0xFF);
        verify(j1939).requestMultiple(ComponentIdentificationPacket.class, requestPacket);
    }

    @Test
    public void testReportConnectionSpeed() throws Exception {
        Bus bus = mock(Bus.class);
        when(j1939.getBus()).thenReturn(bus);
        when(bus.getConnectionSpeed()).thenReturn(250000);

        TestResultsListener listener = new TestResultsListener();
        instance.reportConnectionSpeed(listener);

        String expected = "10:15:30.000 Baud Rate: 250,000 bps" + NL;
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

        String expected = "10:15:30.000 Baud Rate: Could not be determined" + NL;
        assertEquals(expected, listener.getResults());
        verify(j1939).getBus();
    }

    @Test
    public void testReportEngineFamily() {
        final int pgn = DM56EngineFamilyPacket.PGN;
        final byte[] bytes = "2015MY-EUS HD ODB   *".getBytes(UTF8);

        Packet requestPacket = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        when(j1939.createRequestPacket(pgn, 0xFF)).thenReturn(requestPacket);

        DM56EngineFamilyPacket packet1 = new DM56EngineFamilyPacket(Packet.create(pgn, 0x00, bytes));
        DM56EngineFamilyPacket packet2 = new DM56EngineFamilyPacket(Packet.create(pgn, 0x17, bytes));
        DM56EngineFamilyPacket packet3 = new DM56EngineFamilyPacket(Packet.create(pgn, 0x21, bytes));
        when(j1939.requestMultiple(DM56EngineFamilyPacket.class, requestPacket))
                .thenReturn(Stream.of(packet1, packet2, packet3).map(p -> new Either<>(p, null)));

        String expected = "";
        expected += "10:15:30.000 Global DM56 Request" + NL;
        expected += "10:15:30.000 18EAFFA5 C7 FC 00 (TX)" + NL;
        expected += "10:15:30.000 18FCC700 32 30 31 35 4D 59 2D 45 55 53 20 48 44 20 4F 44 42 20 20 20 2A" + NL;
        expected += "Model Year and Certification Engine Family from Engine #1 (0): " + NL;
        expected += "Model Year: 2015MY-E" + NL;
        expected += "Family Name: US HD ODB   " + NL;
        expected += "10:15:30.000 18FCC717 32 30 31 35 4D 59 2D 45 55 53 20 48 44 20 4F 44 42 20 20 20 2A" + NL;
        expected += "Model Year and Certification Engine Family from Instrument Cluster #1 (23): " + NL;
        expected += "Model Year: 2015MY-E" + NL;
        expected += "Family Name: US HD ODB   " + NL;
        expected += "10:15:30.000 18FCC721 32 30 31 35 4D 59 2D 45 55 53 20 48 44 20 4F 44 42 20 20 20 2A" + NL;
        expected += "Model Year and Certification Engine Family from Body Controller (33): " + NL;
        expected += "Model Year: 2015MY-E" + NL;
        expected += "Family Name: US HD ODB   " + NL;

        TestResultsListener listener = new TestResultsListener();
        List<DM56EngineFamilyPacket> packets = instance.reportEngineFamily(listener);
        assertEquals(3, packets.size());
        assertEquals(packet1, packets.get(0));
        assertEquals(packet2, packets.get(1));
        assertEquals(packet3, packets.get(2));
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, 0xFF);
        verify(j1939).requestMultiple(DM56EngineFamilyPacket.class, requestPacket);
    }

    @Test
    public void testReportEngineFamilyWithNoResponses() {
        final int pgn = DM56EngineFamilyPacket.PGN;

        Packet requestPacket = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        when(j1939.createRequestPacket(pgn, 0xFF)).thenReturn(requestPacket);

        when(j1939.requestMultiple(DM56EngineFamilyPacket.class, requestPacket)).thenReturn(Stream.empty());

        String expected = "";
        expected += "10:15:30.000 Global DM56 Request" + NL;
        expected += "10:15:30.000 18EAFFA5 C7 FC 00 (TX)" + NL;
        expected += "Error: Timeout - No Response." + NL;

        TestResultsListener listener = new TestResultsListener();
        List<DM56EngineFamilyPacket> packets = instance.reportEngineFamily(listener);
        assertEquals(0, packets.size());
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, 0xFF);
        verify(j1939).requestMultiple(DM56EngineFamilyPacket.class, requestPacket);
    }

    @Test
    public void testReportEngineHours() {
        final int pgn = EngineHoursPacket.PGN;

        Packet requestPacket = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        when(j1939.createRequestPacket(pgn, 0xFF)).thenReturn(requestPacket);

        EngineHoursPacket packet1 = new EngineHoursPacket(Packet.create(pgn, 0x00, 1, 2, 3, 4, 5, 6, 7, 8));
        EngineHoursPacket packet2 = new EngineHoursPacket(Packet.create(pgn, 0x01, 8, 7, 6, 5, 4, 3, 2, 1));
        when(j1939.requestMultiple(EngineHoursPacket.class, requestPacket))
                .thenReturn(Stream.of(packet1, packet2).map(p -> new Either<>(p, null)));

        String expected = "";
        expected += "10:15:30.000 Engine Hours Request" + NL;
        expected += "10:15:30.000 18EAFFA5 E5 FE 00 (TX)" + NL;
        expected += "10:15:30.000 18FEE500 01 02 03 04 05 06 07 08" + NL;
        expected += "Engine Hours from Engine #1 (0): 3,365,299.25 hours" + NL;
        expected += "10:15:30.000 18FEE501 08 07 06 05 04 03 02 01" + NL;
        expected += "Engine Hours from Engine #2 (1): 4,214,054.8 hours" + NL;

        TestResultsListener listener = new TestResultsListener();
        instance.reportEngineHours(listener);
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, 0xFF);
        verify(j1939).requestMultiple(EngineHoursPacket.class, requestPacket);
    }

    @Test
    public void testReportEngineHoursWithNoResponse() {
        final int pgn = EngineHoursPacket.PGN;

        Packet requestPacket = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        when(j1939.createRequestPacket(pgn, 0xFF)).thenReturn(requestPacket);

        when(j1939.requestMultiple(EngineHoursPacket.class, requestPacket)).thenReturn(Stream.empty());

        String expected = "";
        expected += "10:15:30.000 Engine Hours Request" + NL;
        expected += "10:15:30.000 18EAFFA5 E5 FE 00 (TX)" + NL;
        expected += "Error: Timeout - No Response." + NL;

        TestResultsListener listener = new TestResultsListener();
        instance.reportEngineHours(listener);
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, 0xFF);
        verify(j1939).requestMultiple(EngineHoursPacket.class, requestPacket);
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

        when(j1939.read(HighResVehicleDistancePacket.class, 3, TimeUnit.SECONDS))
                .thenReturn(Stream.of(packet0, packet1, packet2, packetFF).map(p -> new Either<>(p, null)));

        String expected = "";
        expected += "10:15:30.000 Vehicle Distance" + NL;
        expected += "10:15:30.000 18FEC102 02 02 02 02 02 02 02 02" + NL;
        expected += "High Resolution Vehicle Distance from Turbocharger (2): 168,430.09 km (104,657.605 mi)" + NL;

        TestResultsListener listener = new TestResultsListener();
        instance.reportVehicleDistance(listener);
        assertEquals(expected, listener.getResults());

        verify(j1939).read(HighResVehicleDistancePacket.class, 3, TimeUnit.SECONDS);
    }

    @Test
    public void testReportVehicleDistanceWithLoRes() {
        when(j1939.read(HighResVehicleDistancePacket.class, 3, TimeUnit.SECONDS)).thenReturn(Stream.empty());
        final int pgn = TotalVehicleDistancePacket.PGN;
        TotalVehicleDistancePacket packet0 = new TotalVehicleDistancePacket(
                Packet.create(pgn, 0x00, 0, 0, 0, 0, 0, 0, 0, 0));
        TotalVehicleDistancePacket packet1 = new TotalVehicleDistancePacket(
                Packet.create(pgn, 0x01, 1, 1, 1, 1, 1, 1, 1, 1));
        TotalVehicleDistancePacket packet2 = new TotalVehicleDistancePacket(
                Packet.create(pgn, 0x02, 2, 2, 2, 2, 2, 2, 2, 2));
        TotalVehicleDistancePacket packetFF = new TotalVehicleDistancePacket(
                Packet.create(pgn, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF));

        when(j1939.read(TotalVehicleDistancePacket.class, 300, TimeUnit.MILLISECONDS))
                .thenReturn(Stream.of(packet2, packet1, packet0, packetFF).map(p -> new Either<>(p, null)));

        String expected = "";
        expected += "10:15:30.000 Vehicle Distance" + NL;
        expected += "10:15:30.000 18FEE002 02 02 02 02 02 02 02 02" + NL;
        expected += "Total Vehicle Distance from Turbocharger (2): 4,210,752.25 km (2,616,440.136 mi)" + NL;

        TestResultsListener listener = new TestResultsListener();
        instance.reportVehicleDistance(listener);
        assertEquals(expected, listener.getResults());

        verify(j1939).read(HighResVehicleDistancePacket.class, 3, TimeUnit.SECONDS);
        verify(j1939).read(TotalVehicleDistancePacket.class, 300, TimeUnit.MILLISECONDS);
    }

    @Test
    public void testReportVehicleDistanceWithNoResponse() {
        when(j1939.read(HighResVehicleDistancePacket.class, 3, TimeUnit.SECONDS)).thenReturn(Stream.empty());
        when(j1939.read(TotalVehicleDistancePacket.class, 300, TimeUnit.MILLISECONDS)).thenReturn(Stream.empty());

        String expected = "";
        expected += "10:15:30.000 Vehicle Distance" + NL;
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

        Packet requestPacket = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        when(j1939.createRequestPacket(pgn, 0xFF)).thenReturn(requestPacket);

        VehicleIdentificationPacket packet1 = new VehicleIdentificationPacket(Packet.create(pgn, 0x00, vinBytes));
        VehicleIdentificationPacket packet2 = new VehicleIdentificationPacket(Packet.create(pgn, 0x17, vinBytes));
        VehicleIdentificationPacket packet3 = new VehicleIdentificationPacket(Packet.create(pgn, 0x21, vinBytes));
        when(j1939.requestMultiple(VehicleIdentificationPacket.class, requestPacket))
                .thenReturn(Stream.of(packet1, packet2, packet3).map(p -> new Either<>(p, null)));

        String expected = "";
        expected += "10:15:30.000 Global VIN Request" + NL;
        expected += "10:15:30.000 18EAFFA5 EC FE 00 (TX)" + NL;
        expected += "10:15:30.000 18FEEC00 31 32 33 34 35 36 37 38 39 30 31 32 33 34 35 36 37 38 39 30 2A" + NL;
        expected += "Vehicle Identification from Engine #1 (0): 12345678901234567890" + NL;
        expected += "10:15:30.000 18FEEC17 31 32 33 34 35 36 37 38 39 30 31 32 33 34 35 36 37 38 39 30 2A" + NL;
        expected += "Vehicle Identification from Instrument Cluster #1 (23): 12345678901234567890" + NL;
        expected += "10:15:30.000 18FEEC21 31 32 33 34 35 36 37 38 39 30 31 32 33 34 35 36 37 38 39 30 2A" + NL;
        expected += "Vehicle Identification from Body Controller (33): 12345678901234567890" + NL;

        TestResultsListener listener = new TestResultsListener();
        List<VehicleIdentificationPacket> packets = instance.reportVin(listener);
        assertEquals(3, packets.size());
        assertEquals(packet1, packets.get(0));
        assertEquals(packet2, packets.get(1));
        assertEquals(packet3, packets.get(2));
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, 0xFF);
        verify(j1939).requestMultiple(VehicleIdentificationPacket.class, requestPacket);
    }

    @Test
    public void testReportVinWithNoResponses() {
        final int pgn = VehicleIdentificationPacket.PGN;

        Packet requestPacket = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        when(j1939.createRequestPacket(pgn, 0xFF)).thenReturn(requestPacket);

        when(j1939.requestMultiple(VehicleIdentificationPacket.class, requestPacket)).thenReturn(Stream.empty());

        String expected = "";
        expected += "10:15:30.000 Global VIN Request" + NL;
        expected += "10:15:30.000 18EAFFA5 EC FE 00 (TX)" + NL;
        expected += "Error: Timeout - No Response." + NL;

        TestResultsListener listener = new TestResultsListener();
        List<VehicleIdentificationPacket> packets = instance.reportVin(listener);
        assertEquals(0, packets.size());
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, 0xFF);
        verify(j1939).requestMultiple(VehicleIdentificationPacket.class, requestPacket);
    }

}
