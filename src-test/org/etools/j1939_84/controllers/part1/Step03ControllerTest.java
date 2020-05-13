/**
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part1;

import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939_84.model.Outcome.WARN;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executor;

import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket.Response;
import org.etools.j1939_84.bus.j1939.packets.DM5DiagnosticReadinessPacket;
import org.etools.j1939_84.bus.j1939.packets.ParsedPacket;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.model.PartResultFactory;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticReadinessModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.ReportFileModule;
import org.etools.j1939_84.modules.TestDateTimeModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.testdoc.TestDoc;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * The unit test for {@link Step03Controller}
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 *
 */
@TestDoc(verifies = "Part 1, Step 3", description = "DM5: Diagnostic readiness 1")
@RunWith(MockitoJUnitRunner.class)
public class Step03ControllerTest {

    @Mock
    private AcknowledgmentPacket acknowledgmentPacket;

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

    private Step03Controller instance;

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

    @Before
    public void setUp() throws Exception {
        listener = new TestResultsListener(mockListener);
        dateTimeModule = new TestDateTimeModule();

        instance = new Step03Controller(
                executor,
                engineSpeedModule,
                bannerModule,
                dateTimeModule,
                vehicleInformationModule,
                partResultFactory,
                diagnosticReadinessModule,
                dataRepository);
    }

    @After
    public void tearDown() throws Exception {

        verifyNoMoreInteractions(executor,
                engineSpeedModule,
                bannerModule,
                vehicleInformationModule,
                partResultFactory,
                diagnosticReadinessModule,
                dataRepository,
                mockListener,
                reportFileModule);
    }

    @Test
    @SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT", justification = "The method is called just to get some exception.")
    /**
     * Includes addWarning() verification for distinctCount > 1
     */
    public void testBadECUValue() {
        List<ParsedPacket> packets = new ArrayList<>();
        when(diagnosticReadinessModule.requestDM5Packets(any(), eq(true))).thenReturn(packets);

        ParsedPacket packet1 = mock(ParsedPacket.class);
        packets.add(packet1);

        AcknowledgmentPacket packet2 = mock(AcknowledgmentPacket.class);
        when(packet2.getResponse()).thenReturn(Response.ACK);
        packets.add(packet2);

        AcknowledgmentPacket packet3 = mock(AcknowledgmentPacket.class);
        when(packet3.getResponse()).thenReturn(Response.NACK);
        packets.add(packet3);

        DM5DiagnosticReadinessPacket packet4 = mock(DM5DiagnosticReadinessPacket.class);
        when(packet4.isObd()).thenReturn(true);
        when(packet4.getSourceAddress()).thenReturn(0);
        when(packet4.getOBDCompliance()).thenReturn((byte) 4);
        packets.add(packet4);

        DM5DiagnosticReadinessPacket packet5 = mock(DM5DiagnosticReadinessPacket.class);
        when(packet5.isObd()).thenReturn(true);
        when(packet5.getSourceAddress()).thenReturn(17);
        when(packet5.getOBDCompliance()).thenReturn((byte) 5);
        packets.add(packet5);

        Collection<OBDModuleInformation> obdInfoList = new ArrayList<>();

        OBDModuleInformation obdInfo1 = new OBDModuleInformation(0);
        obdInfo1.setObdCompliance((byte) 4);
        obdInfoList.add(obdInfo1);

        OBDModuleInformation obdInfo2 = new OBDModuleInformation(17);
        obdInfo2.setObdCompliance((byte) 5);
        obdInfoList.add(obdInfo2);

        when(dataRepository.getObdModules()).thenReturn(obdInfoList);

        instance.execute(listener, j1939, reportFileModule);
        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(executor).execute(runnableCaptor.capture());
        runnableCaptor.getValue().run();

        verify(diagnosticReadinessModule).setJ1939(j1939);
        verify(engineSpeedModule).setJ1939(j1939);
        verify(vehicleInformationModule).setJ1939(j1939);
        verify(dataRepository).getObdModules();
        verify(diagnosticReadinessModule).requestDM5Packets(any(), eq(true));
        verify(dataRepository).putObdModule(0, obdInfo1);
        verify(mockListener).addOutcome(1, 3, FAIL, "6.1.3.2.b - The request for DM5 was NACK'ed");
        verify(reportFileModule).addOutcome(1, 3, FAIL, "6.1.3.2.b - The request for DM5 was NACK'ed");
        verify(reportFileModule).onResult("FAIL: 6.1.3.2.b - The request for DM5 was NACK'ed");

        verify(dataRepository).putObdModule(17, obdInfo2);
        verify(mockListener).addOutcome(1, 3, FAIL, "6.1.3.2.b - The request for DM5 was NACK'ed");

        verify(mockListener).addOutcome(1,
                3,
                WARN,
                "6.1.3.3.a - An ECU responded with a value for OBD Compliance that was not identical to other ECUs");
        verify(reportFileModule).addOutcome(1,
                3,
                WARN,
                "6.1.3.3.a - An ECU responded with a value for OBD Compliance that was not identical to other ECUs");
        verify(reportFileModule).onResult(
                "WARN: 6.1.3.3.a - An ECU responded with a value for OBD Compliance that was not identical to other ECUs");

        verify(reportFileModule).onProgress(0,
                1,
                "");
        verify(reportFileModule).onResult("FAIL: 6.1.3.2.b - The request for DM5 was NACK'ed");

        String expectedObd = "OBD Module Information:\n";
        expectedObd += "sourceAddress is : 0\n";
        expectedObd += "obdCompliance is : 4\n";
        expectedObd += "Supported SPNs: \n";
        assertEquals(expectedObd, obdInfo1.toString());
    }

    @Test
    public void testGetDisplayName() {
        assertEquals("Display Name", "Part 1 Step 3", instance.getDisplayName());
    }

    @Test
    public void testGetTotalSteps() {
        assertEquals("Total Steps", 1, instance.getTotalSteps());
    }

    @Test
    @SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT", justification = "The method is called just to get some exception.")
    @TestDoc(verifies = { "6.1.3.2.a",
            "6.1.3.2.b" }, description = "There needs to be at least one OBD Module & The request for DM5 was NACK'ed")
    public void testModulesEmpty() {
        List<ParsedPacket> packets = new ArrayList<>();
        ParsedPacket packet1 = mock(ParsedPacket.class);
        packets.add(packet1);

        AcknowledgmentPacket packet2 = mock(AcknowledgmentPacket.class);
        when(packet2.getResponse()).thenReturn(Response.ACK);
        packets.add(packet2);

        AcknowledgmentPacket packet3 = mock(AcknowledgmentPacket.class);
        when(packet3.getResponse()).thenReturn(Response.NACK);
        packets.add(packet3);
        mock(DM5DiagnosticReadinessPacket.class);

        Collection<OBDModuleInformation> obdInfoList = new ArrayList<>();
        when(dataRepository.getObdModules()).thenReturn(obdInfoList);
        when(diagnosticReadinessModule.requestDM5Packets(any(), eq(true))).thenReturn(packets);

        instance.execute(listener, j1939, reportFileModule);
        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(executor).execute(runnableCaptor.capture());
        runnableCaptor.getValue().run();

        verify(dataRepository).getObdModules();

        verify(diagnosticReadinessModule).setJ1939(j1939);
        verify(diagnosticReadinessModule).requestDM5Packets(any(), eq(true));

        verify(engineSpeedModule).setJ1939(j1939);

        verify(mockListener).addOutcome(1, 3, FAIL, "6.1.3.2.a - There needs to be at least one OBD Module");
        verify(mockListener).addOutcome(1, 3, FAIL, "6.1.3.2.b - The request for DM5 was NACK'ed");

        verify(reportFileModule).addOutcome(1, 3, FAIL, "6.1.3.2.a - There needs to be at least one OBD Module");
        verify(reportFileModule).onResult("FAIL: 6.1.3.2.a - There needs to be at least one OBD Module");
        verify(reportFileModule).addOutcome(1,
                3,
                FAIL,
                "6.1.3.2.a - There needs to be at least one OBD Module");
        verify(reportFileModule).addOutcome(1, 3, FAIL, "6.1.3.2.b - The request for DM5 was NACK'ed");
        verify(reportFileModule).onResult("FAIL: 6.1.3.2.b - The request for DM5 was NACK'ed");
        verify(reportFileModule).addOutcome(1,
                3,
                FAIL,
                "6.1.3.2.b - The request for DM5 was NACK'ed");
        verify(reportFileModule).onProgress(0,
                1,
                "");

        verify(vehicleInformationModule).setJ1939(j1939);

    }

    @Test
    @SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT", justification = "The method is called just to get some exception.")
    @TestDoc(verifies = { "6.1.3.2.b" }, description = "The request for DM5 was NACK'ed")
    public void testRun() {
        List<ParsedPacket> packets = new ArrayList<>();
        ParsedPacket packet1 = mock(ParsedPacket.class);
        packets.add(packet1);

        AcknowledgmentPacket packet2 = mock(AcknowledgmentPacket.class);
        when(packet2.getResponse()).thenReturn(Response.ACK);
        packets.add(packet2);

        AcknowledgmentPacket packet3 = mock(AcknowledgmentPacket.class);
        when(packet3.getResponse()).thenReturn(Response.NACK);
        packets.add(packet3);

        DM5DiagnosticReadinessPacket packet4 = mock(DM5DiagnosticReadinessPacket.class);
        OBDModuleInformation obdInfo = new OBDModuleInformation(0);
        Collection<OBDModuleInformation> obdInfoList = new ArrayList<>();
        obdInfo.setObdCompliance((byte) 4);
        obdInfoList.add(obdInfo);
        obdInfoList.add(obdInfo);

        when(dataRepository.getObdModules()).thenReturn(obdInfoList);
        when(packet4.isObd()).thenReturn(true);
        when(packet4.getSourceAddress()).thenReturn(0);
        when(packet4.getOBDCompliance()).thenReturn((byte) 4);
        packets.add(packet4);
        when(diagnosticReadinessModule.requestDM5Packets(any(), eq(true))).thenReturn(packets);

        instance.execute(listener, j1939, reportFileModule);
        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(executor).execute(runnableCaptor.capture());
        runnableCaptor.getValue().run();

        verify(dataRepository).getObdModules();
        verify(dataRepository).putObdModule(0, obdInfo);

        verify(diagnosticReadinessModule).setJ1939(j1939);
        verify(diagnosticReadinessModule).requestDM5Packets(any(), eq(true));

        verify(engineSpeedModule).setJ1939(j1939);

        verify(mockListener).addOutcome(1, 3, FAIL, "6.1.3.2.b - The request for DM5 was NACK'ed");

        verify(reportFileModule).addOutcome(1, 3, FAIL, "6.1.3.2.b - The request for DM5 was NACK'ed");
        verify(reportFileModule).onProgress(0,
                1,
                "");
        verify(reportFileModule).onResult("FAIL: 6.1.3.2.b - The request for DM5 was NACK'ed");

        verify(vehicleInformationModule).setJ1939(j1939);
    }

}
