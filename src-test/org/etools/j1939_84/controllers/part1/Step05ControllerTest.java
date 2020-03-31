/**
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part1;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;

import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939_84.bus.j1939.packets.VehicleIdentificationPacket;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.model.Outcome;
import org.etools.j1939_84.model.PartResultFactory;
import org.etools.j1939_84.model.VehicleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.ReportFileModule;
import org.etools.j1939_84.modules.TestDateTimeModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939_84.utils.VinDecoder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * The unit test for {@link Step05Controller}
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class Step05ControllerTest {

    @Mock
    private AcknowledgmentPacket acknowledgmentPacket;

    @Mock
    private BannerModule bannerModule;

    @Mock
    private DataRepository dataRepository;

    private DateTimeModule dateTimeModule;

    @Mock
    private EngineSpeedModule engineSpeedModule;

    @Mock
    private Executor executor;

    private Step05Controller instance;

    @Mock
    private J1939 j1939;

    private TestResultsListener listener;

    @Mock
    private ResultsListener mockListener;

    @Mock
    private PartResultFactory partResultFactory;

    @Mock
    private ReportFileModule reportFileModule;

    @Mock
    private VehicleInformationModule vehicleInformationModule;

    @Mock
    private VinDecoder vinDecoder;

    private void runTest() {
        instance.execute(listener, j1939, reportFileModule);
        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(executor).execute(runnableCaptor.capture());
        runnableCaptor.getValue().run();

        verify(engineSpeedModule).setJ1939(j1939);
    }

    @Before
    public void setUp() throws Exception {
        listener = new TestResultsListener(mockListener);
        dateTimeModule = new TestDateTimeModule();

        instance = new Step05Controller(
                executor,
                engineSpeedModule,
                bannerModule,
                dateTimeModule,
                vehicleInformationModule,
                partResultFactory,
                vinDecoder,
                dataRepository);
    }

    @After
    public void tearDown() throws Exception {

        verifyNoMoreInteractions(executor,
                engineSpeedModule,
                bannerModule,
                vehicleInformationModule,
                partResultFactory,
                vinDecoder,
                dataRepository,
                mockListener);
    }

    @Test
    public void testError() {
        List<VehicleIdentificationPacket> packets = new ArrayList<>();
        VehicleIdentificationPacket packet = mock(VehicleIdentificationPacket.class);
        when(packet.getSourceAddress()).thenReturn(1);
        packets.add(packet);
        packets.add(packet);
        when(packet.getVin()).thenReturn("vin");
        when(packet.getManufacturerData()).thenReturn("NightHawk");

        VehicleIdentificationPacket packet2 = mock(VehicleIdentificationPacket.class);
        when(packet.getSourceAddress()).thenReturn(3);
        packets.add(packet2);

        Collection<OBDModuleInformation> obdModuleInformations = new ArrayList<>();
        OBDModuleInformation obdModule1 = new OBDModuleInformation(0);
        OBDModuleInformation obdModule2 = new OBDModuleInformation(0);
        obdModuleInformations.add(obdModule1);
        obdModuleInformations.add(obdModule2);

        Set<Integer> obdModuleAddresses = new HashSet<>();
        obdModuleAddresses.add(1);
        obdModuleAddresses.add(2);
        obdModuleAddresses.add(3);

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setVin("78654321345667889");
        vehicleInformation.setVehicleModelYear(2006);

        when(vehicleInformationModule.reportVin(any())).thenReturn(packets);
        when(dataRepository.getObdModuleAddresses()).thenReturn(obdModuleAddresses);
        when(dataRepository.getVehicleInformation()).thenReturn(vehicleInformation);

        runTest();

        verify(dataRepository, atLeastOnce()).getObdModuleAddresses();
        verify(dataRepository, atLeastOnce()).getVehicleInformation();

        verify(engineSpeedModule).setJ1939(j1939);

        verify(mockListener).addOutcome(1,
                5,
                Outcome.FAIL,
                "6.1.5.2.b - More than one OBD ECU responded with VIN");
        verify(mockListener).addOutcome(1,
                5,
                Outcome.FAIL,
                "6.1.5.2.c - VIN does not match user entered VIN");
        verify(mockListener).addOutcome(1,
                5,
                Outcome.FAIL,
                "6.1.5.2.d - VIN Model Year does not match user entered Vehicle Model Year");
        verify(mockListener).addOutcome(1,
                5,
                Outcome.FAIL,
                "6.1.5.2.e - VIN is not valid (not 17 legal chars, incorrect checksum, or non-numeric sequence");
        verify(mockListener).addOutcome(1,
                5,
                Outcome.WARN,
                "6.1.5.3.a - Non-OBD ECU responded with VIN");
        verify(mockListener).addOutcome(1,
                5,
                Outcome.WARN,
                "6.1.5.3.b - More than one VIN response from an ECU");

        verify(mockListener).addOutcome(1,
                5,
                Outcome.WARN,
                "6.1.5.3.d - Manufacturer defined data follows the VIN");

        verify(vehicleInformationModule).setJ1939(j1939);
        verify(vehicleInformationModule).reportVin(any());

        verify(vinDecoder).getModelYear("vin");
        verify(vinDecoder).isVinValid("vin");
    }

    @Test
    public void testGetDisplayName() {
        assertEquals("Display Name", "Part 1 Step 5", instance.getDisplayName());
    }

    @Test
    public void testGetTotalSteps() {
        assertEquals("Total Steps", 1, instance.getTotalSteps());
    }

    @Test
    // This test will have an error on the nonObdResponses due to error combination
    // restrictions/requirements
    public void testNonError() {
        List<VehicleIdentificationPacket> packets = new ArrayList<>();
        VehicleIdentificationPacket packet = mock(VehicleIdentificationPacket.class);
        when(packet.getSourceAddress()).thenReturn(1);
        packets.add(packet);
        packets.add(packet);
        when(packet.getVin()).thenReturn("vin");
        when(packet.getManufacturerData()).thenReturn("NightHawk");

        VehicleIdentificationPacket packet2 = mock(VehicleIdentificationPacket.class);
        when(packet.getSourceAddress()).thenReturn(3);
        packets.add(packet2);

        Collection<OBDModuleInformation> obdModuleInformations = new ArrayList<>();
        OBDModuleInformation obdModule1 = new OBDModuleInformation(0);
        OBDModuleInformation obdModule2 = new OBDModuleInformation(0);
        obdModuleInformations.add(obdModule1);
        obdModuleInformations.add(obdModule2);

        Set<Integer> obdModuleAddresses = new HashSet<>();
        obdModuleAddresses.add(1);
        obdModuleAddresses.add(2);
        obdModuleAddresses.add(3);

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setVin("78654321345667889");
        vehicleInformation.setVehicleModelYear(2006);

        when(vehicleInformationModule.reportVin(any())).thenReturn(packets);
        // when(dataRepository.getObdModuleAddresses()).thenReturn(obdModuleAddresses);
        when(dataRepository.getVehicleInformation()).thenReturn(vehicleInformation);

        runTest();

        verify(dataRepository, atLeastOnce()).getObdModuleAddresses();
        verify(dataRepository, atLeastOnce()).getVehicleInformation();

        verify(engineSpeedModule).setJ1939(j1939);

        verify(mockListener).addOutcome(1,
                5,
                Outcome.FAIL,
                "6.1.5.2.c - VIN does not match user entered VIN");
        verify(mockListener).addOutcome(1,
                5,
                Outcome.FAIL,
                "6.1.5.2.d - VIN Model Year does not match user entered Vehicle Model Year");
        verify(mockListener).addOutcome(1,
                5,
                Outcome.FAIL,
                "6.1.5.2.e - VIN is not valid (not 17 legal chars, incorrect checksum, or non-numeric sequence");
        verify(mockListener).addOutcome(1,
                5,
                Outcome.WARN,
                "6.1.5.3.a - Non-OBD ECU responded with VIN");
        verify(mockListener).addOutcome(1,
                5,
                Outcome.WARN,
                "6.1.5.3.b - More than one VIN response from an ECU");

        verify(mockListener).addOutcome(1,
                5,
                Outcome.WARN,
                "6.1.5.3.c - VIN provided from more than one non-OBD ECU");

        verify(mockListener).addOutcome(1,
                5,
                Outcome.WARN,
                "6.1.5.3.d - Manufacturer defined data follows the VIN");

        verify(vehicleInformationModule).setJ1939(j1939);
        verify(vehicleInformationModule).reportVin(any());

        verify(vinDecoder).getModelYear("vin");
        verify(vinDecoder).isVinValid("vin");
    }

    @Test
    public void testPacketsEmpty() {
        List<VehicleIdentificationPacket> packets = new ArrayList<>();
        Collection<OBDModuleInformation> obdModuleInformations = new ArrayList<>();
        OBDModuleInformation obdModule1 = new OBDModuleInformation(0);
        OBDModuleInformation obdModule2 = new OBDModuleInformation(0);
        obdModuleInformations.add(obdModule1);
        obdModuleInformations.add(obdModule2);

        Collection<Integer> obdModuleAddresses = new HashSet<>();
        obdModuleAddresses.add(1);
        obdModuleAddresses.add(2);

        when(vehicleInformationModule.reportVin(any())).thenReturn(packets);

        runTest();

        verify(mockListener).addOutcome(1, 5, Outcome.FAIL, "6.1.5.2.a - No VIN was provided");

        verify(vehicleInformationModule).setJ1939(j1939);
        verify(vehicleInformationModule).reportVin(any());
    }

}
