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
import java.util.List;
import java.util.concurrent.Executor;

import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.packets.DM24SPNSupportPacket;
import org.etools.j1939_84.bus.j1939.packets.ParsedPacket;
import org.etools.j1939_84.bus.j1939.packets.SupportedSPN;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.FuelType;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.model.Outcome;
import org.etools.j1939_84.model.PartResultFactory;
import org.etools.j1939_84.model.RequestResult;
import org.etools.j1939_84.model.VehicleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticReadinessModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.OBDTestsModule;
import org.etools.j1939_84.modules.ReportFileModule;
import org.etools.j1939_84.modules.SupportedSpnModule;
import org.etools.j1939_84.modules.TestDateTimeModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.testdoc.TestDoc;
import org.etools.testdoc.TestItem;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * The unit test for {@link Step04Controller}
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 *
 */
@RunWith(MockitoJUnitRunner.class)
@TestDoc(items = @TestItem(value = "Part 1 Step 4", description = "DM24: SPN support"))
public class Step04ControllerTest {

    @Mock
    private BannerModule bannerModule;

    @Mock
    private DataRepository dataRepository;

    private DateTimeModule dateTimeModule;

    @Mock
    private DiagnosticReadinessModule diagnosticReadinessModule;

    @Mock
    private EngineSpeedModule engineSpeedModule;

    @Mock
    private Executor executor;

    private Step04Controller instance;

    @Mock
    private J1939 j1939;

    private TestResultsListener listener;

    @Mock
    private ResultsListener mockListener;

    private final String NL = System.lineSeparator();

    @Mock
    private OBDTestsModule obdTestsModule;

    @Mock
    private PartResultFactory partResultFactory;

    @Mock
    private ReportFileModule reportFileModule;

    @Mock
    private SupportedSpnModule supportedSpnModule;

    @Mock
    private VehicleInformationModule vehicleInformationModule;

    @Before
    public void setUp() throws Exception {
        listener = new TestResultsListener(mockListener);
        dateTimeModule = new TestDateTimeModule();

        instance = new Step04Controller(
                executor,
                engineSpeedModule,
                bannerModule,
                dateTimeModule,
                vehicleInformationModule,
                partResultFactory,
                obdTestsModule,
                supportedSpnModule,
                dataRepository);
    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(executor,
                engineSpeedModule,
                bannerModule,
                vehicleInformationModule,
                partResultFactory,
                obdTestsModule,
                dataRepository,
                mockListener,
                supportedSpnModule,
                reportFileModule);
    }

    @Test
    // Testing the object will all possible errors
    @TestDoc(items = {
            @TestItem("6.1.4.2.a"),
            @TestItem("6.1.4.2.b"),
            @TestItem("6.1.4.2.c") },
            description = "Fail if retry was required to obtain DM24 response."
                    + "<br>"
                    + "Fail if one or more minimum expected SPNs for data stream not supported per section A.1, Minimum Support Table, from the OBD ECU(s)."
                    + "<br>"
                    + "Fail if one or more minimum expected SPNs for freeze frame not supported per section A.2, Criteria for Freeze Frame Evaluation, from the OBD ECU(s).")

    public void testErroredObject() {
        List<ParsedPacket> packets = new ArrayList<>();
        DM24SPNSupportPacket packet1 = mock(DM24SPNSupportPacket.class);
        when(packet1.getSourceAddress()).thenReturn(0);
        packets.add(packet1);

        List<SupportedSPN> supportedSpns = new ArrayList<>();
        SupportedSPN spn1 = mock(SupportedSPN.class);
        supportedSpns.add(spn1);
        when(packet1.getSupportedSpns()).thenReturn(supportedSpns);

        OBDModuleInformation obdInfo = new OBDModuleInformation(0);
        Collection<OBDModuleInformation> obdInfoList = new ArrayList<>();
        obdInfo.setObdCompliance((byte) 4);
        obdInfoList.add(obdInfo);
        obdInfoList.add(obdInfo);
        when(dataRepository.getObdModule(0)).thenReturn(obdInfo);
        when(dataRepository.getObdModule(1)).thenReturn(obdInfo);

        DM24SPNSupportPacket packet4 = mock(DM24SPNSupportPacket.class);
        when(packet4.getSourceAddress()).thenReturn(1);
        when(packet4.getSupportedSpns()).thenReturn(supportedSpns);
        packets.add(packet4);

        when(obdTestsModule.requestDM24Packets(any())).thenReturn(new RequestResult(true, packets));

        List<SupportedSPN> expectedSPNs = new ArrayList<>();
        SupportedSPN supportedSpn = new SupportedSPN(new int[] { 0x00, 0x00, 0x00, 0x00 });
        SupportedSPN supportedSpn2 = new SupportedSPN(new int[] { 0xFE, 0xFE, 0xFE, 0xFE });
        expectedSPNs.add(supportedSpn);
        expectedSPNs.add(supportedSpn2);
        when(dataRepository.getObdModules()).thenReturn(obdInfoList);

        VehicleInformation vehicleInfo = mock(VehicleInformation.class);
        when(dataRepository.getVehicleInformation()).thenReturn(vehicleInfo);
        when(dataRepository.getVehicleInformation().getFuelType()).thenReturn(FuelType.BI_GAS);

        instance.execute(listener, j1939, reportFileModule);
        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(executor).execute(runnableCaptor.capture());
        runnableCaptor.getValue().run();

        verify(obdTestsModule).setJ1939(j1939);
        verify(obdTestsModule).requestDM24Packets(any());

        verify(dataRepository).getObdModule(0);
        verify(dataRepository).getObdModule(1);
        verify(dataRepository).putObdModule(0, obdInfo);
        verify(dataRepository).putObdModule(1, obdInfo);
        verify(dataRepository, atLeastOnce()).getObdModules();
        verify(dataRepository, atLeastOnce()).getVehicleInformation();

        verify(engineSpeedModule).setJ1939(j1939);

        verify(mockListener).addOutcome(1, 4, Outcome.FAIL, "6.1.4.2.a - Retry was required to obtain DM24 response");
        verify(mockListener)
                .addOutcome(1, 4, Outcome.FAIL, "6.1.4.2.b - One or more SPNs for data stream is not supported");
        verify(mockListener)
                .addOutcome(1, 4, Outcome.FAIL, "6.1.4.2.c - One or more SPNs for freeze frame are not supported");

        verify(reportFileModule).onProgress(0, 1, "");
        verify(reportFileModule)
                .addOutcome(1, 4, Outcome.FAIL, "6.1.4.2.a - Retry was required to obtain DM24 response");
        verify(reportFileModule).onResult("FAIL: 6.1.4.2.a - Retry was required to obtain DM24 response");
        verify(reportFileModule).onProgress(0, 1, "");
        verify(reportFileModule)
                .addOutcome(1, 4, Outcome.FAIL, "6.1.4.2.b - One or more SPNs for data stream is not supported");
        verify(reportFileModule).onResult("FAIL: 6.1.4.2.b - One or more SPNs for data stream is not supported");
        verify(reportFileModule)
                .addOutcome(1, 4, Outcome.FAIL, "6.1.4.2.c - One or more SPNs for freeze frame are not supported");
        verify(reportFileModule).onResult("FAIL: 6.1.4.2.c - One or more SPNs for freeze frame are not supported");

        verify(supportedSpnModule).validateDataStreamSpns(any(), any(), any());
        verify(supportedSpnModule).validateFreezeFrameSpns(any(), any());

        verify(vehicleInformationModule).setJ1939(j1939);
    }

    @Test
    public void testGetDisplayName() {
        assertEquals("Display Name", "Part 1 Step 4", instance.getDisplayName());
    }

    @Test
    public void testGetTotalSteps() {
        assertEquals("Total Steps", 1, instance.getTotalSteps());
    }

    @Test
    // Testing object without any errors.
    @TestDoc(items = {
            @TestItem("6.1.4.1.a"),
            @TestItem("6.1.4.1.b"),
            @TestItem("6.1.4.1.c"),
            @TestItem("6.1.4.1.d"),
            @TestItem("6.1.4.1.e") },
            description = "Destination Specific (DS) DM24 (send Request (PGN 59904) for PGN 64950 (SPNs 3297, 4100-4103)) to each OBD ECU.6"
                    + "<br>"
                    + "If no response (transport protocol RTS or NACK(Busy) in 220 ms), then retry DS DM24 request to the OBD ECU."
                    + "<br>"
                    + "[Do not attempt retry for NACKs that indicate not supported]."
                    + "<br>"
                    + "Create vehicle list of supported SPNs for data stream."
                    + "<br>"
                    + "Create ECU specific list of supported SPNs for test results."
                    + "<br>"
                    + "Create ECU specific list of supported freeze frame SPNs.")
    public void testGoodObjects() {
        List<DM24SPNSupportPacket> packets = new ArrayList<>();
        DM24SPNSupportPacket packet1 = mock(DM24SPNSupportPacket.class);
        when(packet1.getSourceAddress()).thenReturn(0);
        packets.add(packet1);

        List<SupportedSPN> supportedSpns = new ArrayList<>();
        SupportedSPN spn1 = mock(SupportedSPN.class);
        supportedSpns.add(spn1);
        when(packet1.getSupportedSpns()).thenReturn(supportedSpns);

        OBDModuleInformation obdInfo = new OBDModuleInformation(0);
        Collection<OBDModuleInformation> obdInfoList = new ArrayList<>();
        obdInfo.setObdCompliance((byte) 4);
        obdInfoList.add(obdInfo);
        obdInfoList.add(obdInfo);
        when(dataRepository.getObdModule(0)).thenReturn(obdInfo);
        when(dataRepository.getObdModule(1)).thenReturn(obdInfo);

        DM24SPNSupportPacket packet4 = mock(DM24SPNSupportPacket.class);
        when(packet4.getSourceAddress()).thenReturn(1);
        when(packet4.getSupportedSpns()).thenReturn(supportedSpns);
        packets.add(packet4);

        RequestResult<ParsedPacket> result = new RequestResult(false, packets);
        when(obdTestsModule.requestDM24Packets(any())).thenReturn(result);

        List<SupportedSPN> expectedSPNs = new ArrayList<>();
        SupportedSPN supportedSpn = new SupportedSPN(new int[] { 0x00, 0x00, 0x00, 0x00 });
        SupportedSPN supportedSpn2 = new SupportedSPN(new int[] { 0xFE, 0xFE, 0xFE, 0xFE });
        expectedSPNs.add(supportedSpn);
        expectedSPNs.add(supportedSpn2);
        when(dataRepository.getObdModules()).thenReturn(obdInfoList);

        VehicleInformation vehicleInfo = mock(VehicleInformation.class);
        when(dataRepository.getVehicleInformation()).thenReturn(vehicleInfo);
        when(dataRepository.getVehicleInformation().getFuelType()).thenReturn(FuelType.BI_GAS);

        when(supportedSpnModule.validateDataStreamSpns(any(), any(), any())).thenReturn(true);
        when(supportedSpnModule.validateFreezeFrameSpns(any(), any())).thenReturn(true);

        instance.execute(listener, j1939, reportFileModule);
        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(executor).execute(runnableCaptor.capture());
        runnableCaptor.getValue().run();

        verify(obdTestsModule).setJ1939(j1939);
        verify(obdTestsModule).requestDM24Packets(any());

        verify(dataRepository).getObdModule(0);
        verify(dataRepository).getObdModule(1);
        verify(dataRepository).putObdModule(0, obdInfo);
        verify(dataRepository).putObdModule(1, obdInfo);
        verify(dataRepository, atLeastOnce()).getObdModules();
        verify(dataRepository, atLeastOnce()).getVehicleInformation();

        verify(engineSpeedModule).setJ1939(j1939);

        verify(reportFileModule).onProgress(0, 1, "");
        verify(reportFileModule).onProgress(0, 1, "");

        verify(supportedSpnModule).validateDataStreamSpns(any(), any(), any());
        verify(supportedSpnModule).validateFreezeFrameSpns(any(), any());

        verify(vehicleInformationModule).setJ1939(j1939);
    }

}
