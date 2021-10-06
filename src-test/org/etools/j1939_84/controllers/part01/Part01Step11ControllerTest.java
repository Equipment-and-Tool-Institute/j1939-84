/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part01;

import static net.solidDesign.j1939.packets.AcknowledgmentPacket.Response.NACK;
import static net.solidDesign.j1939.packets.DM21DiagnosticReadinessPacket.create;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;

import org.etools.j1939_84.bus.j1939.BusResult;
import net.solidDesign.j1939.J1939;
import net.solidDesign.j1939.packets.AcknowledgmentPacket;
import net.solidDesign.j1939.packets.DM21DiagnosticReadinessPacket;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.model.RequestResult;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import net.solidDesign.j1939.modules.CommunicationsModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.ReportFileModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939_84.utils.AbstractControllerTest;
import org.etools.testdoc.TestDoc;
import org.etools.testdoc.TestItem;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * The unit test for {@link Part01Step11Controller}
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 */
@RunWith(MockitoJUnitRunner.class)
public class Part01Step11ControllerTest extends AbstractControllerTest {

    @Mock
    private BannerModule bannerModule;

    private DataRepository dataRepository;

    @Mock
    private CommunicationsModule communicationsModule;

    @Mock
    private EngineSpeedModule engineSpeedModule;

    @Mock
    private Executor executor;

    private Part01Step11Controller instance;

    @Mock
    private J1939 j1939;

    private TestResultsListener listener;

    @Mock
    private ResultsListener mockListener;

    @Mock
    private ReportFileModule reportFileModule;

    @Mock
    private VehicleInformationModule vehicleInformationModule;

    @Before
    public void setUp() {
        listener = new TestResultsListener(mockListener);
        DateTimeModule.setInstance(null);
        dataRepository = DataRepository.newInstance();

        instance = new Part01Step11Controller(executor,
                                              engineSpeedModule,
                                              bannerModule,
                                              communicationsModule,
                                              vehicleInformationModule,
                                              dataRepository,
                                              DateTimeModule.getInstance());
        setup(instance,
              listener,
              j1939,
              executor,
              reportFileModule,
              engineSpeedModule,
              vehicleInformationModule,
              communicationsModule);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(executor,
                                 engineSpeedModule,
                                 bannerModule,
                                 communicationsModule,
                                 vehicleInformationModule,
                                 mockListener);
    }

    /**
     * Test method for {@link Part01Step11Controller#run()}.
     * Test three module responding:<br>
     * <br>
     * <p>
     * <b style="color:red">Module Responses:</b>
     * <table style="border-collapse: collapse;border-spacing: 0px;border:1px solid #ddd;">
     * <col width="25%";/>
     * <col width="45%";/>
     * <col width="30%";/>
     *
     * <thead>
     * <th colspan="1" style="text-align:center;border-bottom:2px solid #ddd;padding: 4px;word-wrap:break-word">Module
     * Address</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">Global
     * Response</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">DS
     * Response</th>
     *
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;border-bottom:1px solid #ddd;padding:3px;word-wrap:break-word">0x00</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">no response</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">good DM21 response</td>
     * </tr>
     * <tr>
     * <td style="text-align:center;border-bottom:1px solid #ddd;padding: 3px;word-wrap:break-word">0x17</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid #ddd;padding:
     * 3px;word-wrap:break-word">no response</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid #ddd;padding:
     * 3px;word-wrap:break-word">good DM21 response</td>
     * </tr>
     * <tr>
     * <td style="text-align:center;padding: 3px;word-wrap:break-word">0x21</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">no response</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM21
     * response</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.11.2.e", description = "Fail if NACK received from any HD OBD ECU."),
            @TestItem(verifies = "6.1.11.4.f", description = "Fail if NACK not received from OBD ECUs that did not respond to global query.") })
    public void testEmptyListReturnedToGlobalDm11RequestFailures() {
        dataRepository.putObdModule(new OBDModuleInformation(0x00));
        dataRepository.putObdModule(new OBDModuleInformation(0x17));
        dataRepository.putObdModule(new OBDModuleInformation(0x21));

        // return empty list
        when(communicationsModule.requestDM21(any(ResultsListener.class)))
                                                        .thenReturn(new RequestResult<>(false, List.of(), List.of()));

        DM21DiagnosticReadinessPacket packet4 = create(0x00, 0, 0, 0, 0, 0);
        when(communicationsModule.requestDM21(any(), eq(0x00)))
                                                               .thenReturn(new BusResult<>(false, packet4));

        DM21DiagnosticReadinessPacket packet5 = create(0x17, 0, 0, 0, 0, 0);
        when(communicationsModule.requestDM21(any(ResultsListener.class), eq(0x17)))
                                                                .thenReturn(new BusResult<>(false, packet5));

        AcknowledgmentPacket ackPacket = AcknowledgmentPacket.create(0x21,
                                                                     NACK,
                                                                     0,
                                                                     0x21,
                                                                     DM21DiagnosticReadinessPacket.PGN);
        when(communicationsModule.requestDM21(any(ResultsListener.class), eq(0x21)))
                                                                                       .thenReturn(new BusResult<>(false,
                                                                                                                   ackPacket));

        runTest();

        verify(communicationsModule).setJ1939(j1939);
        verify(communicationsModule).requestDM21(any(ResultsListener.class));
        verify(communicationsModule).requestDM21(any(ResultsListener.class), eq(0x00));
        verify(communicationsModule).requestDM21(any(ResultsListener.class), eq(0x17));
        verify(communicationsModule).requestDM21(any(ResultsListener.class), eq(0x21));

        verify(mockListener).addOutcome(1,
                                        11,
                                        FAIL,
                                        "6.1.11.2.e - No OBD ECU provided a DM21 message");
        verify(mockListener).addOutcome(1,
                                        11,
                                        FAIL,
                                        "6.1.11.4.f - OBD ECU Engine #1 (0) did not provide a response to Global query and did not provide a NACK for the DS query");
        verify(mockListener).addOutcome(1,
                                        11,
                                        FAIL,
                                        "6.1.11.4.f - OBD ECU Instrument Cluster #1 (23) did not provide a response to Global query and did not provide a NACK for the DS query");
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    /**
     * Test method for
     * {@link Part01Step11Controller#getDisplayName()}.
     */
    @Test
    public void testGetDisplayName() {
        assertEquals("Part 1 Step 11", instance.getDisplayName());
    }

    /**
     * Test method for
     * {@link Part01Step11Controller#getTotalSteps()}.
     */
    @Test
    public void testGetTotalSteps() {
        assertEquals(0, instance.getTotalSteps());
    }

    /**
     * Test method for {@link Part01Step11Controller#run()}.
     * Test three module responding:<br>
     * <br>
     * <p>
     * <b style="color:red">Module Responses:</b>
     * <table style="border-collapse: collapse;border-spacing: 0px;border:1px solid #ddd;">
     * <col width="25%";/>
     * <col width="45%";/>
     * <col width="30%";/>
     *
     * <thead>
     * <th colspan="1" style="text-align:center;border-bottom:2px solid #ddd;padding: 4px;word-wrap:break-word">Module
     * Address</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">Global
     * Response</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">DS
     * Response</th>
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;border-bottom:1px solid #ddd;padding:3px;word-wrap:break-word">0x00</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">good DM21 response</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">good DM21 response</td>
     * </tr>
     * <tr>
     * <td style="text-align:center;border-bottom:1px solid #ddd;padding: 3px;word-wrap:break-word">0x17</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">good DM21 response</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid #ddd;padding:
     * 3px;word-wrap:break-word">good DM21 response</td>
     * </tr>
     * <tr>
     * <td style="text-align:center;padding: 3px;word-wrap:break-word">0x21</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">no
     * response</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">no
     * response</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.11.4.f", description = "Fail if NACK not received from OBD ECUs that did not respond to global query.") })
    public void testNoResponseFromModule() {
        dataRepository.putObdModule(new OBDModuleInformation(0x00));
        dataRepository.putObdModule(new OBDModuleInformation(0x17));
        dataRepository.putObdModule(new OBDModuleInformation(0x21));

        List<DM21DiagnosticReadinessPacket> globalPackets = new ArrayList<>();

        DM21DiagnosticReadinessPacket packet0x00 = create(0x00, 0, 0, 0, 0, 0);
        when(communicationsModule.requestDM21(any(ResultsListener.class), eq(0x00)))
                                                                                       .thenReturn(new BusResult<>(false,
                                                                                                                   packet0x00));
        globalPackets.add(packet0x00);

        DM21DiagnosticReadinessPacket packet0x17 = create(0x17, 0, 0, 0, 0, 0);
        when(communicationsModule.requestDM21(any(ResultsListener.class), eq(0x17)))
                                                                                       .thenReturn(new BusResult<>(false,
                                                                                                                   packet0x17));
        globalPackets.add(packet0x17);

        when(communicationsModule.requestDM21(any(ResultsListener.class), eq(0x21)))
                                                                                       .thenReturn(new BusResult<>(false,
                                                                                                                   Optional.empty()));
        when(communicationsModule.requestDM21(any(ResultsListener.class)))
                                                                             .thenReturn(new RequestResult<>(false,
                                                                                                             globalPackets,
                                                                                                             List.of()));


        runTest();

        verify(communicationsModule).setJ1939(j1939);
        verify(communicationsModule).requestDM21(any());
        verify(communicationsModule).requestDM21(any(), eq(0x00));
        verify(communicationsModule).requestDM21(any(), eq(0x17));
        verify(communicationsModule).requestDM21(any(), eq(0x21));

        verify(mockListener).addOutcome(1,
                                        11,
                                        FAIL,
                                        "6.1.11.4.f - OBD ECU Body Controller (33) did not provide a response to Global query and did not provide a NACK for the DS query");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    /**
     * Test method for {@link Part01Step11Controller#run()}.
     * Test three module responding:<br>
     * <br>
     * <p>
     * <b style="color:red">Module Responses:</b>
     * <table style="border-collapse: collapse;border-spacing: 0px;border:1px solid #ddd;">
     * <col width="25%";/>
     * <col width="45%";/>
     * <col width="30%";/>
     *
     * <thead>
     * <th colspan="1" style="text-align:center;border-bottom:2px solid #ddd;padding: 4px;word-wrap:break-word">Module
     * Address</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">Global
     * Response</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">DS
     * Response</th>
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;border-bottom:1px solid #ddd;padding:3px;word-wrap:break-word">0x00</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">good DM21 response</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">good DM21 response</td>
     * </tr>
     * <tr>
     * <td style="text-align:center;border-bottom:1px solid #ddd;padding: 3px;word-wrap:break-word">0x17</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">good DM21 response</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid #ddd;padding:
     * 3px;word-wrap:break-word">good DM21 response;<br>
     * with non-zero min w/ MIL on</td>
     * </tr>
     * <tr>
     * <td style="text-align:center;padding: 3px;word-wrap:break-word">0x21</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">no
     * response</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">no
     * response</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.11.4.e", description = "Fail if NACK not received from OBD ECUs that did not respond to global query.") })
    public void testGlobalMessageDiffersFromDestinationSpecificMessageFailure() {
        dataRepository.putObdModule(new OBDModuleInformation(0x00));
        dataRepository.putObdModule(new OBDModuleInformation(0x17));
        dataRepository.putObdModule(new OBDModuleInformation(0x21));

        List<DM21DiagnosticReadinessPacket> globalPackets = new ArrayList<>();

        DM21DiagnosticReadinessPacket packet0x00 = create(0x00, 0, 0, 0, 0, 0);
        when(communicationsModule.requestDM21(any(ResultsListener.class), eq(0x00)))
                                                                                       .thenReturn(new BusResult<>(false,
                                                                                                                   packet0x00));
        globalPackets.add(packet0x00);

        DM21DiagnosticReadinessPacket packet0x17 = create(0x17, 0, 0, 0, 0, 0);
        globalPackets.add(packet0x17);

        DM21DiagnosticReadinessPacket dsPacket0x17 = create(0x17, 0, 0, 0, 2, 0);
        when(communicationsModule.requestDM21(any(ResultsListener.class), eq(0x17)))
                                                                                       .thenReturn(new BusResult<>(false,
                                                                                                                   dsPacket0x17));

        DM21DiagnosticReadinessPacket packet0x21 = create(0x21, 0, 0, 0, 0, 0);
        when(communicationsModule.requestDM21(any(ResultsListener.class), eq(0x21)))
                                                                                       .thenReturn(new BusResult<>(false,
                                                                                                                   packet0x21));
        globalPackets.add(packet0x21);

        when(communicationsModule.requestDM21(any(ResultsListener.class)))
                                                                             .thenReturn(new RequestResult<>(false,
                                                                                                             globalPackets,
                                                                                                             List.of()));

        runTest();

        verify(communicationsModule).setJ1939(j1939);
        verify(communicationsModule).requestDM21(any(ResultsListener.class));
        verify(communicationsModule).requestDM21(any(ResultsListener.class), eq(0x00));
        verify(communicationsModule).requestDM21(any(ResultsListener.class), eq(0x17));
        verify(communicationsModule).requestDM21(any(ResultsListener.class), eq(0x21));

        verify(mockListener).addOutcome(1,
                                        11,
                                        FAIL,
                                        "6.1.11.4.c - Instrument Cluster #1 (23) reported time with MIL on (SP 3295) is not zero");
        verify(mockListener).addOutcome(1,
                                        11,
                                        FAIL,
                                        "6.1.11.4.e - Difference compared to data received during global request from Instrument Cluster #1 (23)");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    /**
     * Test method for {@link Part01Step11Controller#run()}.
     * Test three module responding:<br>
     * <br>
     * <p>
     * <b style="color:red">Module Responses:</b>
     * <table style="border-collapse: collapse;border-spacing: 0px;border:1px solid #ddd;">
     * <col width="25%";/>
     * <col width="45%";/>
     * <col width="30%";/>
     *
     * <thead>
     * <th colspan="1" style="text-align:center;border-bottom:2px solid #ddd;padding: 4px;word-wrap:break-word">Module
     * Address</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">Global
     * Response</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">DS
     * Response</th>
     *
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;border-bottom:1px solid #ddd;padding:3px;word-wrap:break-word">0x00</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">good DM21 response</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">good DM21 response</td>
     * </tr>
     * <tr>
     * <td style="text-align:center;border-bottom:1px solid #ddd;padding:3px;word-wrap:break-word">0x09</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">no response</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">NACK response</td>
     * </tr>
     * <tr>
     * <td style="text-align:center;border-bottom:1px solid #ddd;padding: 3px;word-wrap:break-word">0x17</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">good DM21 response"</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid #ddd;padding:
     * 3px;word-wrap:break-word">good DM21 response</td>
     * </tr>
     * <tr>
     * <td style="text-align:center;padding: 3px;word-wrap:break-word">0x21</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM21
     * response</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM21
     * response</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.11.2.e", description = "Fail if NACK received from any HD OBD ECU."),
            @TestItem(verifies = "6.1.11.4.f", description = "Fail if NACK not received from OBD ECUs that did not respond to global query.") })
    public void testNackResponseToDestinationSpecificRequest() {
        dataRepository.putObdModule(new OBDModuleInformation(0x00));
        dataRepository.putObdModule(new OBDModuleInformation(0x09));
        dataRepository.putObdModule(new OBDModuleInformation(0x17));
        dataRepository.putObdModule(new OBDModuleInformation(0x21));

        List<DM21DiagnosticReadinessPacket> globalPackets = new ArrayList<>();

        DM21DiagnosticReadinessPacket packet0x00 = create(0x00, 0, 0, 0, 0, 0);
        when(communicationsModule.requestDM21(any(ResultsListener.class), eq(0x00)))
                                                                                       .thenReturn(new BusResult<>(false,
                                                                                                                   packet0x00));
        globalPackets.add(packet0x00);

        AcknowledgmentPacket ackPacket0x09 = AcknowledgmentPacket.create(0x09,
                                                                         NACK,
                                                                         0,
                                                                         0x09,
                                                                         DM21DiagnosticReadinessPacket.PGN);
        when(communicationsModule.requestDM21(any(), eq(0x09)))
                                                                  .thenReturn(new BusResult<>(false, ackPacket0x09));

        DM21DiagnosticReadinessPacket packet0x17 = create(0x17, 0, 0, 0, 0, 0);
        when(communicationsModule.requestDM21(any(), eq(0x17)))
                                                                  .thenReturn(new BusResult<>(false, packet0x17));
        globalPackets.add(packet0x17);

        DM21DiagnosticReadinessPacket packet0x21 = create(0x21, 0, 0, 0, 0, 0);
        when(communicationsModule.requestDM21(any(ResultsListener.class), eq(0x21)))
                                                                                       .thenReturn(new BusResult<>(false,
                                                                                                                   packet0x21));

        when(communicationsModule.requestDM21(any(ResultsListener.class))).thenReturn(new RequestResult<>(false,
                                                                                                          globalPackets,
                                                                                                          List.of()));

        runTest();

        verify(communicationsModule).setJ1939(j1939);
        verify(communicationsModule).requestDM21(any(ResultsListener.class));
        verify(communicationsModule).requestDM21(any(ResultsListener.class), eq(0x00));
        verify(communicationsModule).requestDM21(any(ResultsListener.class), eq(0x09));
        verify(communicationsModule).requestDM21(any(ResultsListener.class), eq(0x17));
        verify(communicationsModule).requestDM21(any(ResultsListener.class), eq(0x21));

        verify(mockListener).addOutcome(1,
                                        11,
                                        FAIL,
                                        "6.1.11.4.f - OBD ECU Body Controller (33) did not provide a response to Global query and did not provide a NACK for the DS query");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    /**
     * Test method for {@link Part01Step11Controller#run()}.
     * Test three module responding:<br>
     * <br>
     * <p>
     * <b style="color:red">Module Responses:</b>
     * <table style="border-collapse: collapse;border-spacing: 0px;border:1px solid #ddd;">
     * <col width="25%";/>
     * <col width="45%";/>
     * <col width="30%";/>
     *
     * <thead>
     * <th colspan="1" style="text-align:center;border-bottom:2px solid #ddd;padding: 4px;word-wrap:break-word">Module
     * Address</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">Global
     * Response</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">DS
     * Response</th>
     *
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;border-bottom:1px solid #ddd;padding:3px;word-wrap:break-word">0x00</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">good DM21 response</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">good DM21 response</td>
     * </tr>
     * <tr>
     * <td style="text-align:center;border-bottom:1px solid #ddd;padding:3px;word-wrap:break-word">0x09</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">good DM21 response</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">no response</td>
     * </tr>
     * <tr>
     * <td style="text-align:center;border-bottom:1px solid #ddd;padding: 3px;word-wrap:break-word">0x17</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">good DM21 response"</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid #ddd;padding:
     * 3px;word-wrap:break-word">good DM21 response</td>
     * </tr>
     * <tr>
     * <td style="text-align:center;padding: 3px;word-wrap:break-word">0x21</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM21
     * response</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM21
     * response</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    public void testResponseToGlobalAndNoResponseToDestinationSpecificDm11Request() {

        dataRepository.putObdModule(new OBDModuleInformation(0x00));
        dataRepository.putObdModule((new OBDModuleInformation(0x09)));
        dataRepository.putObdModule(new OBDModuleInformation(0x17));
        dataRepository.putObdModule(new OBDModuleInformation(0x21));

        List<DM21DiagnosticReadinessPacket> packets = new ArrayList<>();

        // Global response
        DM21DiagnosticReadinessPacket packet0x00 = create(0x00, 0, 0, 0, 0, 0);
        packets.add(packet0x00);
        // Destination Specific response
        when(communicationsModule.requestDM21(any(ResultsListener.class), eq(0x00)))
                                                                                       .thenReturn(new BusResult<>(false,
                                                                                                                   packet0x00));
        // Global response
        DM21DiagnosticReadinessPacket packet0x09 = create(0x09, 0, 0, 0, 0, 0);
        packets.add(packet0x09);
        // Destination Specific response
        when(communicationsModule.requestDM21(any(ResultsListener.class), eq(0x09)))
                                                                                       .thenReturn(new BusResult<>(false,
                                                                                                                   Optional.empty()));
        // Global response
        DM21DiagnosticReadinessPacket packet0x17 = create(0x17, 0, 0, 0, 0, 0);
        packets.add(packet0x17);
        // Destination Specific response
        when(communicationsModule.requestDM21(any(ResultsListener.class), eq(0x17)))
                                                                                       .thenReturn(new BusResult<>(false,
                                                                                                                   packet0x17));

        // Global response
        DM21DiagnosticReadinessPacket packet0x21 = create(0x21, 0, 0, 0, 0, 0);
        packets.add(packet0x21);
        // Destination Specific response
        when(communicationsModule.requestDM21(any(ResultsListener.class),
                                              eq(0x21))).thenReturn(new BusResult<>(false,
                                                                                       packet0x21));
        // returned RequestResult
        when(communicationsModule.requestDM21(any(ResultsListener.class))).thenReturn(new RequestResult<>(false,
                                                                                                          packets,
                                                                                                          List.of()));
        runTest();

        verify(communicationsModule).setJ1939(j1939);
        verify(communicationsModule).requestDM21(any(ResultsListener.class));
        verify(communicationsModule).requestDM21(any(ResultsListener.class), eq(0x00));
        verify(communicationsModule).requestDM21(any(ResultsListener.class), eq(0x09));
        verify(communicationsModule).requestDM21(any(ResultsListener.class), eq(0x17));
        verify(communicationsModule).requestDM21(any(ResultsListener.class), eq(0x21));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    /**
     * Test method for {@link Part01Step11Controller#run()}.
     * Test three module responding:<br>
     * <br>
     * <p>
     * <b style="color:red">Module Responses:</b>
     * <table style="border-collapse: collapse;border-spacing: 0px;border:1px solid #ddd;">
     * <col width="25%";/>
     * <col width="45%";/>
     * <col width="30%";/>
     *
     * <thead>
     * <th colspan="1" style="text-align:center;border-bottom:2px solid #ddd;padding: 4px;word-wrap:break-word">Module
     * Address</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">Global
     * Response</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">DS
     * Response</th>
     *
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;border-bottom:1px solid #ddd;padding:3px;word-wrap:break-word">0x00</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">good DM21 response</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">good DM21 response</td>
     * </tr>
     * <tr>
     * <td style="text-align:center;border-bottom:1px solid #ddd;padding: 3px;word-wrap:break-word">0x17</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">good DM21 response"</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid #ddd;padding:
     * 3px;word-wrap:break-word">good DM21 response</td>
     * </tr>
     * <tr>
     * <td style="text-align:center;padding: 3px;word-wrap:break-word">0x21</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM21
     * response</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM21
     * response</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.11.1.a", description = "Global DM21 ([send Request (PG 59904) for PG 49408 (SPs 3069, 3294-3296)])."),
            @TestItem(verifies = "6.1.11.3.a", description = "DS DM21 to each OBD ECU.")
    })
    public void testMatchingGlobalAndDestinationSpecificResponses() {
        dataRepository.putObdModule(new OBDModuleInformation(0x00));
        dataRepository.putObdModule(new OBDModuleInformation(0x17));
        dataRepository.putObdModule(new OBDModuleInformation(0x21));

        DM21DiagnosticReadinessPacket packet0 = DM21DiagnosticReadinessPacket.create(0x00, 0, 0, 0, 0, 0);
        DM21DiagnosticReadinessPacket packet17 = DM21DiagnosticReadinessPacket.create(0x17, 0, 0, 0, 0, 0);
        DM21DiagnosticReadinessPacket packet21 = DM21DiagnosticReadinessPacket.create(0x21, 0, 0, 0, 0, 0);

        List<DM21DiagnosticReadinessPacket> packets = new ArrayList<>();
        packets.add(packet0);
        packets.add(packet17);
        packets.add(packet21);
        // Global DM21 request response - return packets
        when(communicationsModule.requestDM21(any(ResultsListener.class)))
                                                                             .thenReturn(new RequestResult<>(false,
                                                                                                             packets,
                                                                                                             List.of()));
        // DS to 0x00 response - return packet0
        when(communicationsModule.requestDM21(any(ResultsListener.class), eq(0x00)))
                                                                                       .thenReturn(new BusResult<>(false,
                                                                                                                   packet0));
        // DS to 0x17 response - return packet17
        when(communicationsModule.requestDM21(any(ResultsListener.class), eq(0x17)))
                                                                                       .thenReturn(new BusResult<>(false,
                                                                                                                   packet17));

        when(communicationsModule.requestDM21(any(), eq(0x21)))
                                                                  .thenReturn(new BusResult<>(false, packet21));

        runTest();

        verify(communicationsModule).setJ1939(j1939);
        verify(communicationsModule).requestDM21(any());
        verify(communicationsModule).requestDM21(any(), eq(0x00));
        verify(communicationsModule).requestDM21(any(), eq(0x17));
        verify(communicationsModule).requestDM21(any(), eq(0x21));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    /**
     * Test method for {@link Part01Step11Controller#run()}.
     * Test three module responding:<br>
     * <br>
     * <p>
     * <b style="color:red">Module Responses:</b>
     * <table style="border-collapse: collapse;border-spacing: 0px;border:1px solid #ddd;">
     * <col width="25%";/>
     * <col width="45%";/>
     * <col width="30%";/>
     *
     * <thead>
     * <th colspan="1" style="text-align:center;border-bottom:2px solid #ddd;padding: 4px;word-wrap:break-word">Module
     * Address</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">Global
     * Response</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">DS
     * Response</th>
     *
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;border-bottom:1px solid #ddd;padding:3px;word-wrap:break-word">0x00</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">good DM21 response <br>
     * with non-zero km since code clear value</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">good DM21 response <br>
     * with non-zero km since code clear value;</td>
     * </tr>
     * <tr>
     * <td style="text-align:center;border-bottom:1px solid #ddd;padding: 3px;word-wrap:break-word">0x17</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid #ddd;padding:
     * 3px;word-wrap:break-word">good DM21 response</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid #ddd;padding:
     * 3px;word-wrap:break-word">good DM21 response</td>
     * </tr>
     * <tr>
     * <td style="text-align:center;padding: 3px;word-wrap:break-word">0x21</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM21
     * response</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM21
     * response</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.11.2.a", description = "Fail if any ECU reports distance with MIL on (SP 3069) is not zero."),
            @TestItem(verifies = "6.1.11.4.a", description = "Fail if any ECU reports distance with MIL on (SP 3069) is not zero.") })
    public void testKmSinceDTCsCleared() {
        dataRepository.putObdModule(new OBDModuleInformation(0x00));
        dataRepository.putObdModule(new OBDModuleInformation(0x17));
        dataRepository.putObdModule(new OBDModuleInformation(0x21));

        DM21DiagnosticReadinessPacket globalPacket0x00 = create(0x00, 0, 0, 15, 0, 0);
        DM21DiagnosticReadinessPacket globalPacket0x17 = create(0x17, 0, 0, 0, 0, 0);
        DM21DiagnosticReadinessPacket globalPacket0x21 = create(0x21, 0, 0, 0, 0, 0);

        List<DM21DiagnosticReadinessPacket> packets = new ArrayList<>();
        packets.add(globalPacket0x00);
        packets.add(globalPacket0x17);
        packets.add(globalPacket0x21);
        when(communicationsModule.requestDM21(any(ResultsListener.class)))
                                                                             .thenReturn(new RequestResult<>(false,
                                                                                                             packets,
                                                                                                             List.of()));

        when(communicationsModule.requestDM21(any(ResultsListener.class), eq(0x00)))
                                                                                       .thenReturn(new BusResult<>(false,
                                                                                                                   globalPacket0x00));

        when(communicationsModule.requestDM21(any(ResultsListener.class), eq(0x17)))
                                                                                       .thenReturn(new BusResult<>(false,
                                                                                                                   globalPacket0x17));

        when(communicationsModule.requestDM21(any(ResultsListener.class), eq(0x21)))
                                                                                       .thenReturn(new BusResult<>(false,
                                                                                                                   globalPacket0x21));

        runTest();

        verify(communicationsModule).setJ1939(j1939);
        verify(communicationsModule).requestDM21(any(), eq(0x00));
        verify(communicationsModule).requestDM21(any(), eq(0x17));
        verify(communicationsModule).requestDM21(any(), eq(0x21));
        verify(communicationsModule).requestDM21(any());

        verify(mockListener).addOutcome(1,
                                        11,
                                        FAIL,
                                        "6.1.11.2.a - Engine #1 (0) reported distance with MIL on (SP 3069) is not zero");
        verify(mockListener).addOutcome(1,
                                        11,
                                        FAIL,
                                        "6.1.11.4.a - Engine #1 (0) reported distance with MIL on (SP 3069) is not zero");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    /**
     * Test method for {@link Part01Step11Controller#run()}.
     * Test three module responding:<br>
     * <br>
     * <p>
     * <b style="color:red">Module Responses:</b>
     * <table style="border-collapse: collapse;border-spacing: 0px;border:1px solid #ddd;">
     * <col width="25%";/>
     * <col width="45%";/>
     * <col width="30%";/>
     *
     * <thead>
     * <th colspan="1" style="text-align:center;border-bottom:2px solid #ddd;padding: 4px;word-wrap:break-word">Module
     * Address</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">Global
     * Response</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">DS
     * Response</th>
     *
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;border-bottom:1px solid #ddd;padding:3px;word-wrap:break-word">0x00</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">good DM21 response</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">good DM21 response</td>
     * </tr>
     * <tr>
     * <td style="text-align:center;border-bottom:1px solid #ddd;padding: 3px;word-wrap:break-word">0x17</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid #ddd;padding:
     * 3px;word-wrap:break-word">good DM21 response; <br>
     * non-zero km while MIL active</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid #ddd;padding:
     * 3px;word-wrap:break-word">good DM21 response; <br>
     * non-zero km while MIL active</td>
     * </tr>
     * <tr>
     * <td style="text-align:center;padding: 3px;word-wrap:break-word">0x21</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM21
     * response</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM21
     * response</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.11.2.b", description = "Fail if any ECU reports time with MIL on (SP 3295) is not zero (if supported)."),
            @TestItem(verifies = "6.1.11.4.b", description = "Fail if any ECU reports time with MIL on (SP 3295) is not zero (if supported).") })
    public void testKmWhileMILIsActivated() {
        dataRepository.putObdModule(new OBDModuleInformation(0x00));
        dataRepository.putObdModule(new OBDModuleInformation(0x17));
        dataRepository.putObdModule(new OBDModuleInformation(0x21));

        List<DM21DiagnosticReadinessPacket> packets = new ArrayList<>();

        DM21DiagnosticReadinessPacket packet0x00 = create(0x00, 0, 0, 0, 0, 0);
        when(communicationsModule.requestDM21(any(ResultsListener.class), eq(0x00)))
                                                                                       .thenReturn(new BusResult<>(false,
                                                                                                                   packet0x00));
        packets.add(packet0x00);

        DM21DiagnosticReadinessPacket packet0x17 = create(0x17, 0, 10, 0, 0, 0);
        when(communicationsModule.requestDM21(any(ResultsListener.class), eq(0x17)))
                                                                                       .thenReturn(new BusResult<>(false,
                                                                                                                   packet0x17));
        packets.add(packet0x17);

        DM21DiagnosticReadinessPacket packet0x21 = DM21DiagnosticReadinessPacket.create(0x21,
                                                                                        0,
                                                                                        0,
                                                                                        0,
                                                                                        0,
                                                                                        0);
        when(communicationsModule.requestDM21(any(ResultsListener.class), eq(0x21)))
                                                                                       .thenReturn(new BusResult<>(false,
                                                                                                                   packet0x21));
        packets.add(packet0x21);
        when(communicationsModule.requestDM21(any(ResultsListener.class)))
                                                                             .thenReturn(new RequestResult<>(false,
                                                                                                             packets,
                                                                                                             List.of()));

        runTest();

        verify(communicationsModule).setJ1939(j1939);
        verify(communicationsModule).requestDM21(any());
        verify(communicationsModule).requestDM21(any(), eq(0x00));
        verify(communicationsModule).requestDM21(any(), eq(0x17));
        verify(communicationsModule).requestDM21(any(), eq(0x21));

        verify(mockListener).addOutcome(1,
                                        11,
                                        FAIL,
                                        "6.1.11.2.b - Instrument Cluster #1 (23) reported distance SCC (SP 3294) is not zero");
        verify(mockListener).addOutcome(1,
                                        11,
                                        FAIL,
                                        "6.1.11.4.b - Instrument Cluster #1 (23) reported distance SCC (SP 3294) is not zero");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    /**
     * Test method for {@link Part01Step11Controller#run()}.
     * Test three module responding:<br>
     * <br>
     * <p>
     * <b style="color:red">Module Responses:</b>
     * <table style="border-collapse: collapse;border-spacing: 0px;border:1px solid #ddd;">
     * <col width="25%";/>
     * <col width="45%";/>
     * <col width="30%";/>
     *
     * <thead>
     * <th colspan="1" style="text-align:center;border-bottom:2px solid #ddd;padding: 4px;word-wrap:break-word">Module
     * Address</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">Global
     * Response</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">DS
     * Response</th>
     *
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;border-bottom:1px solid #ddd;padding:3px;word-wrap:break-word">0x00</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">good DM21 response</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">good DM21 response</td>
     * </tr>
     * <tr>
     * <td style="text-align:center;border-bottom:1px solid #ddd;padding: 3px;word-wrap:break-word">0x17</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid #ddd;padding:
     * 3px;word-wrap:break-word">good DM21 response; <br>
     * with non-zero min w/ MIL on</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid #ddd;padding:
     * 3px;word-wrap:break-word">good DM21 response; <br>
     * with non-zer0 min w/ MIL on</td>
     * </tr>
     * <tr>
     * <td style="text-align:center;padding: 3px;word-wrap:break-word">0x21</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM21
     * response</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM21
     * response</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.11.2.c", description = "Fail if any ECU reports time with MIL on (SP 3295) is not zero (if supported)."),
            @TestItem(verifies = "6.1.11.4.c", description = "Fail if any ECU reports time with MIL on (SP 3295) is not zero (if supported).") })
    public void testMinutesWithMilOnFailure() {
        dataRepository.putObdModule(new OBDModuleInformation(0x00));
        dataRepository.putObdModule(new OBDModuleInformation(0x17));
        dataRepository.putObdModule(new OBDModuleInformation(0x21));

        List<DM21DiagnosticReadinessPacket> packets = new ArrayList<>();

        DM21DiagnosticReadinessPacket packet0x00 = create(0x00, 0, 0, 0, 0, 0);
        packets.add(packet0x00);
        when(communicationsModule.requestDM21(any(), eq(0x00)))
                                                                  .thenReturn(new BusResult<>(false, packet0x00));

        DM21DiagnosticReadinessPacket packet0x17 = create(0x17, 0, 0, 0, 20, 0);
        packets.add(packet0x17);
        when(communicationsModule.requestDM21(any(), eq(0x17)))
                                                                  .thenReturn(new BusResult<>(false, packet0x17));

        DM21DiagnosticReadinessPacket packet0x21 = create(0x21, 0, 0, 0, 0, 0);
        packets.add(packet0x21);
        when(communicationsModule.requestDM21(any(), eq(0x21)))
                                                                  .thenReturn(new BusResult<>(false, packet0x21));
        when(communicationsModule.requestDM21(any()))
                                                        .thenReturn(new RequestResult<>(false, packets, List.of()));

        runTest();

        verify(communicationsModule).setJ1939(j1939);
        verify(communicationsModule).requestDM21(any(ResultsListener.class));
        verify(communicationsModule).requestDM21(any(ResultsListener.class), eq(0x00));
        verify(communicationsModule).requestDM21(any(ResultsListener.class), eq(0x17));
        verify(communicationsModule).requestDM21(any(ResultsListener.class), eq(0x21));

        verify(mockListener).addOutcome(1,
                                        11,
                                        FAIL,
                                        "6.1.11.2.c - Instrument Cluster #1 (23) reported time with MIL on (SP 3295) is not zero");
        verify(mockListener).addOutcome(1,
                                        11,
                                        FAIL,
                                        "6.1.11.4.c - Instrument Cluster #1 (23) reported time with MIL on (SP 3295) is not zero");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());

    }

    /**
     * Test method for {@link Part01Step11Controller#run()}.
     * Test three module responding:<br>
     * <br>
     * <p>
     * <b style="color:red">Module Responses:</b>
     * <table style="border-collapse: collapse;border-spacing: 0px;border:1px solid #ddd;">
     * <col width="25%";/>
     * <col width="45%";/>
     * <col width="30%";/>
     *
     * <thead>
     * <th colspan="1" style="text-align:center;border-bottom:2px solid #ddd;padding: 4px;word-wrap:break-word">Module
     * Address</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">Global
     * Response</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">DS
     * Response</th>
     *
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;border-bottom:1px solid #ddd;padding:3px;word-wrap:break-word">0x00</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">good DM21 response;<br>
     * with non-zero min while MIL active</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">good DM21 response;<br>
     * with non-zero min while MIL active</td>
     * </tr>
     * <tr>
     * <td style="text-align:center;border-bottom:1px solid #ddd;padding: 3px;word-wrap:break-word">0x17</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid #ddd;padding:
     * 3px;word-wrap:break-word">good DM21 response</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid #ddd;padding:
     * 3px;word-wrap:break-word">good DM21 response</td>
     * </tr>
     * <tr>
     * <td style="text-align:center;padding: 3px;word-wrap:break-word">0x21</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM21
     * response</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM21
     * response</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.11.2.d", description = "Fail if any ECU reports time SCC (SP 3296) > 1 minute (if supported)."),
            @TestItem(verifies = "6.1.11.4.d", description = "Fail if any ECU reports time SCC (SP 3296) > 1 minute (if supported).") })
    public void testMinutesWhileMILIsActivated() {
        dataRepository.putObdModule(new OBDModuleInformation(0x00));
        dataRepository.putObdModule(new OBDModuleInformation(0x17));
        dataRepository.putObdModule(new OBDModuleInformation(0x21));

        List<DM21DiagnosticReadinessPacket> packets = new ArrayList<>();

        DM21DiagnosticReadinessPacket packet0x00 = create(0x00, 0, 0, 0, 0, 25);
        when(communicationsModule.requestDM21(any(), eq(0x00)))
                                                                  .thenReturn(new BusResult<>(false, packet0x00));
        packets.add(packet0x00);

        DM21DiagnosticReadinessPacket packet0x17 = create(0x17, 0, 0, 0, 0, 0);
        when(communicationsModule.requestDM21(any(), eq(0x17)))
                                                                  .thenReturn(new BusResult<>(false, packet0x17));
        packets.add(packet0x17);

        DM21DiagnosticReadinessPacket packet0x21 = create(0x21, 0, 0, 0, 0, 0);
        when(communicationsModule.requestDM21(any(), eq(0x21)))
                                                                  .thenReturn(new BusResult<>(false, packet0x21));
        packets.add(packet0x21);

        when(communicationsModule.requestDM21(any()))
                                                        .thenReturn(new RequestResult<>(false, packets, List.of()));


        runTest();

        verify(communicationsModule).setJ1939(j1939);
        verify(communicationsModule).requestDM21(any(), eq(0x00));
        verify(communicationsModule).requestDM21(any(), eq(0x17));
        verify(communicationsModule).requestDM21(any(), eq(0x21));
        verify(communicationsModule).requestDM21(any());

        verify(mockListener).addOutcome(1,
                                        11,
                                        FAIL,
                                        "6.1.11.2.d - Engine #1 (0) reported time SCC (SP 3296) > 1 minute");
        verify(mockListener).addOutcome(1,
                                        11,
                                        FAIL,
                                        "6.1.11.4.d - Engine #1 (0) reported time SCC (SP 3296) > 1 minute");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

}
