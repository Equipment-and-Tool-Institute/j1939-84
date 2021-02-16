/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part01;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939_84.model.Outcome.WARN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.etools.j1939_84.bus.Packet;
import org.etools.j1939_84.bus.j1939.packets.DM5DiagnosticReadinessPacket;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.RequestResult;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * The unit test for {@link SectionA6Validator}
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 */
@RunWith(MockitoJUnitRunner.class)
public class SectionA6ValidatorTest {

    private static final int PART_NUMBER = 1;
    private static final String SECTION_A6_VALIDATOR = "Section A6 Validator";

    private static final int STEP_NUMBER = 2;

    @Mock
    private DataRepository dataRepository;

    private SectionA6Validator instance;

    private TestResultsListener listener;

    @Mock
    private ResultsListener mockListener;

    @Mock
    private TableA6Validator tableA6Validator;

    @Before
    public void setUp() {
        listener = new TestResultsListener(mockListener);
        instance = new SectionA6Validator(dataRepository, tableA6Validator);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(dataRepository, mockListener, tableA6Validator);
    }

    @Test
    public void testMoreFails() {

        ArrayList<Integer> obdModuleAddresses = new ArrayList<>() {
            {
                add(0x00);
                add(0x17);
                add(0x21);
            }
        };
        when(dataRepository.getObdModuleAddresses()).thenReturn(obdModuleAddresses);

        List<DM5DiagnosticReadinessPacket> dm5Packets = new ArrayList<>() {
            {
                add(new DM5DiagnosticReadinessPacket(Packet.create(DM5DiagnosticReadinessPacket.PGN, 0x00, 0x11, 0x22,
                                                                   0x14, 0x88, 0x00, 0x00, 0x00, 0x00)));
                add(new DM5DiagnosticReadinessPacket(Packet.create(DM5DiagnosticReadinessPacket.PGN, 0x17, 0x01, 0x14,
                                                                   0x22, 0x00, 0x00, 0x00, 0x00, 0x00)));
                add(new DM5DiagnosticReadinessPacket(Packet.create(DM5DiagnosticReadinessPacket.PGN, 0x21, 0x10, 0x23,
                                                                   0x13, 0x00, 0x00, 0x00, 0x00, 0x00)));
            }
        };
        RequestResult<DM5DiagnosticReadinessPacket> response = new RequestResult<>(false,
                                                                                   dm5Packets, Collections.emptyList());

        when(tableA6Validator.verify(any(), any(), eq(PART_NUMBER), eq(STEP_NUMBER))).thenReturn(true);

        assertFalse(instance.verify(listener, PART_NUMBER, STEP_NUMBER, response));

        verify(dataRepository).getObdModuleAddresses();

        StringBuilder expectedMessage1dPacket0 = new StringBuilder(SECTION_A6_VALIDATOR + NL);
        expectedMessage1dPacket0.append(" Step 1.d - A response does not report 0 for reserved bits")
                .append(NL)
                .append("(SPN 1221 byte 4 bits 4 and 8, SPN 1222 byte 6 bits 6-8, and")
                .append(NL)
                .append("SPN 1223 byte 8 bits 6-8)");
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, expectedMessage1dPacket0.toString());

        verify(tableA6Validator, times(3)).verify(any(), any(), eq(PART_NUMBER), eq(STEP_NUMBER));

        String expectedMessages = FAIL.toString() + ": " + expectedMessage1dPacket0;
        assertEquals(expectedMessages, listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());
    }

    @Test
    public void testNoObdResponse() {

        ArrayList<Integer> obdModuleAddresses = new ArrayList<>() {
            {
                add(0x00);
                add(0x17);
                add(0x21);
            }
        };
        when(dataRepository.getObdModuleAddresses()).thenReturn(obdModuleAddresses);

        RequestResult<DM5DiagnosticReadinessPacket> response = new RequestResult<>(false,
                                                                                   Collections.emptyList(),
                                                                                   Collections.emptyList());

        assertFalse(instance.verify(listener, PART_NUMBER, STEP_NUMBER, response));

        verify(dataRepository).getObdModuleAddresses();

        StringBuilder expectedMessage1a = new StringBuilder(SECTION_A6_VALIDATOR + NL);
        expectedMessage1a.append(
                " Step 1.a - No response from an OBD ECU (ECUs that indicate 0x13, 0x14, 0x22, or 0x23 for OBD compliance)")
                .append(NL)
                .append("   ECU with source address :  0 did not return a response")
                .append(NL)
                .append("   ECU with source address :  23 did not return a response")
                .append(NL)
                .append("   ECU with source address :  33 did not return a response");
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, expectedMessage1a.toString());

        String expectedMessage = FAIL.toString() + ": " + expectedMessage1a;
        assertEquals(expectedMessage, listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());
    }

    @Test
    public void testSupportedSystem() {
        List<DM5DiagnosticReadinessPacket> dm5Packets = new ArrayList<>() {
            {
                add(new DM5DiagnosticReadinessPacket(Packet.create(DM5DiagnosticReadinessPacket.PGN, 0x00, 0x11, 0x22,
                                                                   0x22, 0xEE, 0x00, 0x00, 0x00, 0x00)));
                add(new DM5DiagnosticReadinessPacket(Packet.create(DM5DiagnosticReadinessPacket.PGN, 0x17, 0x01, 0x14,
                                                                   0x14, 0xEE, 0x00, 0x00, 0x00, 0x00)));
                add(new DM5DiagnosticReadinessPacket(Packet.create(DM5DiagnosticReadinessPacket.PGN, 0x21, 0x10, 0x23,
                                                                   0x23, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF)));
                add(new DM5DiagnosticReadinessPacket(Packet.create(DM5DiagnosticReadinessPacket.PGN, 0x22, 0x10, 0x23,
                                                                   0x23, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF)));
            }
        };

        RequestResult<DM5DiagnosticReadinessPacket> response = new RequestResult<>(false,
                                                                                   dm5Packets, Collections.emptyList());
        ArrayList<Integer> obdModuleAddresses = new ArrayList<>() {
            {
                add(0x00);
                add(0x17);
                add(0x21);
                add(0x22);
            }
        };
        when(dataRepository.getObdModuleAddresses()).thenReturn(obdModuleAddresses);

        when(tableA6Validator.verify(any(), any(), eq(PART_NUMBER), eq(STEP_NUMBER))).thenReturn(true);

        assertFalse(instance.verify(listener, PART_NUMBER, STEP_NUMBER, response));

        verify(dataRepository, times(1)).getObdModuleAddresses();

        StringBuilder expectedMessage1d = new StringBuilder(SECTION_A6_VALIDATOR + NL);
        expectedMessage1d.append(" Step 1.d - A response does not report 0 for reserved bits")
                .append(NL)
                .append("(SPN 1221 byte 4 bits 4 and 8, SPN 1222 byte 6 bits 6-8, and")
                .append(NL)
                .append("SPN 1223 byte 8 bits 6-8)");
        verify(mockListener, times(4)).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, expectedMessage1d.toString());

        StringBuilder expectedMessage2d = new StringBuilder(SECTION_A6_VALIDATOR + NL);
        expectedMessage2d.append(" Step 2.d An individual required monitor is supported by more than one OBD ECU");
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, WARN, expectedMessage2d.toString());

        verify(tableA6Validator, times(4)).verify(any(), any(), eq(PART_NUMBER), eq(STEP_NUMBER));

        String expectedMessage = "" +
                FAIL.toString() + ": " + expectedMessage1d + NL +
                FAIL.toString() + ": " + expectedMessage1d + NL +
                FAIL.toString() + ": " + expectedMessage1d + NL +
                FAIL.toString() + ": " + expectedMessage1d + NL +
                WARN.toString() + ": " + expectedMessage2d;
        assertEquals(expectedMessage, listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());
    }

    @Test
    public void testVerify() {

        ArrayList<Integer> obdModuleAddresses = new ArrayList<>() {
            {
                add(0x00);
                add(0x17);
                add(0x21);
            }
        };
        when(dataRepository.getObdModuleAddresses()).thenReturn(obdModuleAddresses);

        List<DM5DiagnosticReadinessPacket> dm5Packets = new ArrayList<>() {
            {
                add(new DM5DiagnosticReadinessPacket(Packet.create(DM5DiagnosticReadinessPacket.PGN, 0x00, 0x11, 0x22,
                                                                   0x22, 0x00, 0x00, 0x00, 0x00, 0x00)));
                add(new DM5DiagnosticReadinessPacket(Packet.create(DM5DiagnosticReadinessPacket.PGN, 0x17, 0x01, 0x14,
                                                                   0x14, 0x77, 0xFF, 0xE0, 0xFF, 0xE0)));
                add(new DM5DiagnosticReadinessPacket(Packet.create(DM5DiagnosticReadinessPacket.PGN, 0x21, 0x10, 0x23,
                                                                   0x23, 0x00, 0x00, 0x00, 0x00, 0x00)));
            }
        };
        RequestResult<DM5DiagnosticReadinessPacket> response = new RequestResult<>(false,
                                                                                   dm5Packets,
                                                                                   Collections.emptyList());

        when(tableA6Validator.verify(any(), any(), eq(PART_NUMBER), eq(STEP_NUMBER))).thenReturn(true);

        assertTrue(instance.verify(listener, PART_NUMBER, STEP_NUMBER, response));

        verify(dataRepository).getObdModuleAddresses();

        verify(tableA6Validator, times(3)).verify(any(), any(), eq(PART_NUMBER), eq(STEP_NUMBER));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());
    }

    @Test
    public void testVerifyError() {

        List<DM5DiagnosticReadinessPacket> dm5Packets = new ArrayList<>() {
            {
                add(new DM5DiagnosticReadinessPacket(Packet.create(DM5DiagnosticReadinessPacket.PGN, 0x00, 0x11, 0x22,
                                                                   0x13, 0x44, 0x55, 0x66, 0x77, 0x88)));
                add(new DM5DiagnosticReadinessPacket(Packet.create(DM5DiagnosticReadinessPacket.PGN, 0x17, 0x01, 0x02,
                                                                   0x14, 0x04, 0x05, 0x06, 0x07, 0x08)));
                add(new DM5DiagnosticReadinessPacket(Packet.create(DM5DiagnosticReadinessPacket.PGN, 0x21, 0x10, 0x20,
                                                                   0x00, 0x40, 0x50, 0x60, 0x70, 0x80)));
            }
        };

        RequestResult<DM5DiagnosticReadinessPacket> response = new RequestResult<>(false,
                                                                                   dm5Packets, Collections.emptyList());
        ArrayList<Integer> obdModuleAddresses = new ArrayList<>() {
            {
                add(0x00);
                add(0x17);
                add(0x21);
            }
        };
        when(dataRepository.getObdModuleAddresses()).thenReturn(obdModuleAddresses);

        assertFalse(instance.verify(listener, PART_NUMBER, STEP_NUMBER, response));

        verify(dataRepository).getObdModuleAddresses();

        StringBuilder expectedMessage1a = new StringBuilder(SECTION_A6_VALIDATOR + NL);
        expectedMessage1a.append(
                " Step 1.a - No response from an OBD ECU (ECUs that indicate 0x13, 0x14, 0x22, or 0x23 for OBD compliance)")
                .append(NL);
        expectedMessage1a.append("   ECU with source address :  33 did not return a response");
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL,
                                        expectedMessage1a.toString());

        StringBuilder expectedMessage1b = new StringBuilder(
                SECTION_A6_VALIDATOR + NL);
        expectedMessage1b.append("Step 1.b - A response does not report supported and")
                .append(NL)
                .append(" complete for comprehensive components support and status (SPN")
                .append(NL)
                .append(" 1221, byte 4, bit 3 = 1 and bit 7 = 0), except when all the")
                .append(NL)
                .append(" bits in SPNs 1221, 1222, and 1223 are sent as 0 as defined in")
                .append(NL)
                .append(" SAE J1939-73 paragraph 5.7.5");
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL,
                                        expectedMessage1b.toString());

        StringBuilder expectedMessage1c = new StringBuilder(
                SECTION_A6_VALIDATOR + NL);
        expectedMessage1c.append(" Step 1.c - A response does not report 0 = ‘complete/not")
                .append(NL)
                .append("supported’ for the status bit for every unsupported monitors")
                .append(NL)
                .append("(i.e., any of the support bits in SPN 1221, byte 4 bits 1-3,")
                .append(NL)
                .append("1222 byte 5 bits 1-8, or 1222 byte 6 bits 1-5 that report 0")
                .append(NL)
                .append("also report 0 in the corresponding status bit in SPN 1221 and")
                .append(NL)
                .append("1223");
        verify(mockListener, times(2)).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL,
                                                  expectedMessage1c.toString());

        String expectedMessage2c = SECTION_A6_VALIDATOR + NL +
                " Step 2.c - Composite vehicle readiness does not meet any of the criteria in Table A-6";
        verify(mockListener, times(2)).addOutcome(PART_NUMBER, STEP_NUMBER,
                                                  FAIL,
                                                  expectedMessage2c);

        StringBuilder expectedMessage2d = new StringBuilder(SECTION_A6_VALIDATOR + NL);
        expectedMessage2d.append(" Step 2.d An individual required monitor is supported by more than one OBD ECU");
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, WARN, expectedMessage2d.toString());

        StringBuilder expectedMessage3b = new StringBuilder(SECTION_A6_VALIDATOR + NL);
        expectedMessage3b.append(" Step 3.b [byte 4] failed all binary zeros or all binary ones check")
                .append(NL)
                .append(" Step 3.b [byte 5] failed all binary zeros or all binary ones check")
                .append(NL)
                .append(" Step 3.b [byte 6] failed all binary zeros or all binary ones check")
                .append(NL)
                .append(" Step 3.b [byte 7] failed all binary zeros or all binary ones check")
                .append(NL)
                .append(" Step 3.b [byte 8] failed all binary zeros or all binary ones check")
                .append(NL);
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, WARN,
                                        expectedMessage3b.toString());

        verify(tableA6Validator, times(2)).verify(any(), any(), eq(PART_NUMBER), eq(STEP_NUMBER));

        String expectedMessage =
                FAIL.toString() + ": " + expectedMessage1a + NL +
                        FAIL.toString() + ": " + expectedMessage1c + NL +
                        FAIL.toString() + ": " + expectedMessage1b + NL +
                        FAIL.toString() + ": " + expectedMessage1c + NL +
                        WARN.toString() + ": " + expectedMessage2d + NL +
                        WARN.toString() + ": " + expectedMessage3b;
        assertEquals(expectedMessage, listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());
    }

}
