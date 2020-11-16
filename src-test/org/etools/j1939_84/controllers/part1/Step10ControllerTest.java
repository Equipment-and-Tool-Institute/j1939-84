package org.etools.j1939_84.controllers.part1;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.model.Outcome.WARN;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;

import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket.Response;
import org.etools.j1939_84.bus.j1939.packets.DM11ClearActiveDTCsPacket;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.PartResultFactory;
import org.etools.j1939_84.model.RequestResult;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DTCModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticReadinessModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.OBDTestsModule;
import org.etools.j1939_84.modules.ReportFileModule;
import org.etools.j1939_84.modules.TestDateTimeModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939_84.utils.AbstractControllerTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * The unit test for {@link Step10Controller}
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class Step10ControllerTest extends AbstractControllerTest {

    @Mock
    private BannerModule bannerModule;

    @Mock
    private DataRepository dataRepository;

    private DateTimeModule dateTimeModule;

    @Mock
    private DiagnosticReadinessModule diagnosticReadinessModule;

    @Mock
    private DTCModule dtcModule;

    @Mock
    private EngineSpeedModule engineSpeedModule;

    @Mock
    private Executor executor;

    private Step10Controller instance;

    @Mock
    private J1939 j1939;

    private TestResultsListener listener;

    @Mock
    private ResultsListener mockListener;

    @Mock
    private OBDTestsModule obdTestsModule;

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

        instance = new Step10Controller(executor,
                engineSpeedModule,
                bannerModule,
                dateTimeModule,
                vehicleInformationModule,
                dtcModule,
                partResultFactory,
                diagnosticReadinessModule,
                obdTestsModule,
                dataRepository);
        setup(instance, listener, j1939, engineSpeedModule, reportFileModule, executor, vehicleInformationModule);
    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(executor,
                engineSpeedModule,
                bannerModule,
                vehicleInformationModule,
                dtcModule,
                partResultFactory,
                obdTestsModule,
                dataRepository,
                diagnosticReadinessModule,
                mockListener);
    }

    @Test
    public void testError() {

        ArrayList<Integer> obdModuleAddresses = new ArrayList<>() {
            {
                add(0x00);
                add(0x17);
                add(0x21);
            }
        };
        when(dataRepository.getObdModuleAddresses()).thenReturn(obdModuleAddresses);

        AcknowledgmentPacket ackPacket = mock(AcknowledgmentPacket.class);
        when(ackPacket.getResponse()).thenReturn(Response.ACK);
        AcknowledgmentPacket nackPacket = mock(AcknowledgmentPacket.class);
        when(nackPacket.getResponse()).thenReturn(Response.NACK);
        List<AcknowledgmentPacket> acknowledgmentPackets = new ArrayList<>() {
            {
                add(ackPacket);
                add(nackPacket);
            }
        };

        when(dtcModule.requestDM11(any()))
                .thenReturn(new RequestResult<>(false, Collections.emptyList(),
                        acknowledgmentPackets));
        runTest();

        verify(dataRepository).getObdModuleAddresses();

        verify(diagnosticReadinessModule).setJ1939(j1939);

        verify(dtcModule).setJ1939(j1939);
        verify(dtcModule).requestDM11(any());

        verify(mockListener).addOutcome(1, 10, WARN, "6.1.10.3.a - The request for DM11 was NACK'ed");
        verify(mockListener).addOutcome(1, 10, WARN, "6.1.10.3.a - The request for DM11 was ACK'ed");

        verify(obdTestsModule).setJ1939(j1939);

        StringBuilder expectedResults = new StringBuilder("WARN: 6.1.10.3.a - The request for DM11 was NACK'ed" + NL);
        expectedResults.append("WARN: 6.1.10.3.a - The request for DM11 was ACK'ed" + NL);
        assertEquals(expectedResults.toString(), listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
    }

    @Test
    public void testGetDisplayName() {
        assertEquals("Display Name", "Part 1 Step 10", instance.getDisplayName());
    }

    @Test
    public void testGetStepNumber() {
        assertEquals("Step Number", 10, instance.getStepNumber());
    }

    @Test
    public void testGetTotalSteps() {
        assertEquals("Total Steps", 1, instance.getTotalSteps());
    }

    @Test
    public void testNoError() {

        AcknowledgmentPacket acknowledgmentPacket = mock(AcknowledgmentPacket.class);
        when(acknowledgmentPacket.getResponse()).thenReturn(Response.BUSY);

        DM11ClearActiveDTCsPacket dm11Packet = mock(DM11ClearActiveDTCsPacket.class);

        when(dtcModule.requestDM11(any()))
                .thenReturn(new RequestResult<>(false, Collections.singletonList(dm11Packet),
                        Collections.singletonList(acknowledgmentPacket)));

        runTest();

        verify(dataRepository).getObdModuleAddresses();

        verify(diagnosticReadinessModule).setJ1939(j1939);

        verify(dtcModule).setJ1939(j1939);
        verify(dtcModule).requestDM11(any());

        verify(obdTestsModule).setJ1939(j1939);

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());
    }

}
