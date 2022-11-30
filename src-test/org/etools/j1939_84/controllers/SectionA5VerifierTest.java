/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.modules.TestDateTimeModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939_84.utils.AbstractControllerTest;
import org.etools.j1939tools.j1939.J1939;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * The unit test for {@link SectionA5Verifier}
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 */
@RunWith(MockitoJUnitRunner.class)
public class SectionA5VerifierTest extends AbstractControllerTest {

    private static final int PART_NUMBER = 1;
    private static final int STEP_NUMBER = 2;
    private static final String SECTION = "6.1.2.3.a";

    private DataRepository dataRepository;

    private SectionA5Verifier instance;

    @Mock
    private J1939 j1939;

    private TestResultsListener listener;

    @Mock
    private ResultsListener mockListener;

    @Mock
    private SectionA5MessageVerifier sectionA5MessageVerifier;

    @Mock
    private SectionA5NoxGhgVerifier sectionA5NoxGhgVerifier;

    @Before
    public void setUp() {
        DateTimeModule.setInstance(new TestDateTimeModule());

        dataRepository = DataRepository.newInstance();
        listener = new TestResultsListener(mockListener);

        instance = new SectionA5Verifier(dataRepository,
                                         sectionA5MessageVerifier,
                                         sectionA5NoxGhgVerifier,
                                         new CommunicationsModule(),
                                         new VehicleInformationModule(),
                                         PART_NUMBER,
                                         STEP_NUMBER);
    }

    @After
    public void tearDown() {
        DateTimeModule.setInstance(null);
        verifyNoMoreInteractions(mockListener, sectionA5MessageVerifier);
    }

    @Test
    public void testSetJ1939() {
        instance.setJ1939(j1939);
        verify(sectionA5MessageVerifier).setJ1939(j1939);
    }

    @Test
    public void testVerifyDataErasedHappyPathNoFailures() {
        dataRepository.putObdModule(new OBDModuleInformation(0));

        when(sectionA5MessageVerifier.checkDM6(listener, SECTION, 0, true)).thenReturn(true);
        when(sectionA5MessageVerifier.checkDM12(listener, SECTION, 0, true)).thenReturn(true);
        when(sectionA5MessageVerifier.checkDM23(listener, SECTION, 0, true)).thenReturn(true);
        when(sectionA5MessageVerifier.checkDM29(listener, SECTION, 0, true)).thenReturn(true);
        when(sectionA5MessageVerifier.checkDM5(listener, SECTION, 0, true)).thenReturn(true);
        when(sectionA5MessageVerifier.checkDM25(listener, SECTION, 0, true)).thenReturn(true);
        when(sectionA5MessageVerifier.checkDM31(listener, SECTION, 0, true)).thenReturn(true);
        when(sectionA5MessageVerifier.checkDM21(listener, SECTION, 0, true)).thenReturn(true);
        when(sectionA5MessageVerifier.checkDM26(listener, SECTION, 0, true)).thenReturn(true);
        when(sectionA5MessageVerifier.checkTestResults(listener, SECTION, 0, true)).thenReturn(true);

        instance.verifyDataErased(listener, SECTION);

        verify(sectionA5MessageVerifier).checkDM6(listener, SECTION, 0, true);
        verify(sectionA5MessageVerifier).checkDM12(listener, SECTION, 0, true);
        verify(sectionA5MessageVerifier).checkDM23(listener, SECTION, 0, true);
        verify(sectionA5MessageVerifier).checkDM29(listener, SECTION, 0, true);
        verify(sectionA5MessageVerifier).checkDM5(listener, SECTION, 0, true);
        verify(sectionA5MessageVerifier).checkDM25(listener, SECTION, 0, true);
        verify(sectionA5MessageVerifier).checkDM31(listener, SECTION, 0, true);
        verify(sectionA5MessageVerifier).checkDM21(listener, SECTION, 0, true);
        verify(sectionA5MessageVerifier).checkDM26(listener, SECTION, 0, true);
        verify(sectionA5MessageVerifier).checkTestResults(listener, SECTION, 0, true);

        assertEquals(List.of(), listener.getOutcomes());
    }

    @Test
    public void testVerifyDataErasedWithFailure() {
        dataRepository.putObdModule(new OBDModuleInformation(0));

        when(sectionA5MessageVerifier.checkDM6(listener, SECTION, 0, true)).thenReturn(false);
        when(sectionA5MessageVerifier.checkDM12(listener, SECTION, 0, true)).thenReturn(false);
        when(sectionA5MessageVerifier.checkDM23(listener, SECTION, 0, true)).thenReturn(false);
        when(sectionA5MessageVerifier.checkDM29(listener, SECTION, 0, true)).thenReturn(false);
        when(sectionA5MessageVerifier.checkDM5(listener, SECTION, 0, true)).thenReturn(false);
        when(sectionA5MessageVerifier.checkDM25(listener, SECTION, 0, true)).thenReturn(false);
        when(sectionA5MessageVerifier.checkDM31(listener, SECTION, 0, true)).thenReturn(false);
        when(sectionA5MessageVerifier.checkDM21(listener, SECTION, 0, true)).thenReturn(false);
        when(sectionA5MessageVerifier.checkDM26(listener, SECTION, 0, true)).thenReturn(false);
        when(sectionA5MessageVerifier.checkTestResults(listener, SECTION, 0, true)).thenReturn(false);

        instance.verifyDataErased(listener, SECTION);

        verify(sectionA5MessageVerifier).checkDM6(listener, SECTION, 0, true);
        verify(sectionA5MessageVerifier).checkDM12(listener, SECTION, 0, true);
        verify(sectionA5MessageVerifier).checkDM23(listener, SECTION, 0, true);
        verify(sectionA5MessageVerifier).checkDM29(listener, SECTION, 0, true);
        verify(sectionA5MessageVerifier).checkDM5(listener, SECTION, 0, true);
        verify(sectionA5MessageVerifier).checkDM25(listener, SECTION, 0, true);
        verify(sectionA5MessageVerifier).checkDM31(listener, SECTION, 0, true);
        verify(sectionA5MessageVerifier).checkDM21(listener, SECTION, 0, true);
        verify(sectionA5MessageVerifier).checkDM26(listener, SECTION, 0, true);
        verify(sectionA5MessageVerifier).checkTestResults(listener, SECTION, 0, true);

        assertEquals(NL + SECTION + " - Checking for erased diagnostic information" + NL, listener.getResults());
    }

    @Test
    public void testVerifyDataNotErasedHappyPathNoFailures() {
        dataRepository.putObdModule(new OBDModuleInformation(0));

        when(sectionA5MessageVerifier.checkDM6(listener, SECTION, 0, false)).thenReturn(true);
        when(sectionA5MessageVerifier.checkDM12(listener, SECTION, 0, false)).thenReturn(true);
        when(sectionA5MessageVerifier.checkDM23(listener, SECTION, 0, false)).thenReturn(true);
        when(sectionA5MessageVerifier.checkDM29(listener, SECTION, 0, false)).thenReturn(true);
        when(sectionA5MessageVerifier.checkDM5(listener, SECTION, 0, false)).thenReturn(true);
        when(sectionA5MessageVerifier.checkDM25(listener, SECTION, 0, false)).thenReturn(true);
        when(sectionA5MessageVerifier.checkDM31(listener, SECTION, 0, false)).thenReturn(true);
        when(sectionA5MessageVerifier.checkDM21(listener, SECTION, 0, false)).thenReturn(true);
        when(sectionA5MessageVerifier.checkDM26(listener, SECTION, 0, false)).thenReturn(true);
        when(sectionA5MessageVerifier.checkTestResults(listener, SECTION, 0, false)).thenReturn(true);
        when(sectionA5MessageVerifier.checkDM20(listener, SECTION, 0)).thenReturn(true);
        when(sectionA5MessageVerifier.checkDM28(listener, SECTION, 0)).thenReturn(true);
        when(sectionA5MessageVerifier.checkDM33(listener, SECTION, 0)).thenReturn(true);
        when(sectionA5MessageVerifier.checkEngineRunTime(listener, SECTION, 0)).thenReturn(true);
        when(sectionA5MessageVerifier.checkEngineIdleTime(listener, SECTION, 0)).thenReturn(true);

        instance.verifyDataNotErased(listener, SECTION);

        verify(sectionA5MessageVerifier).checkDM6(listener, SECTION, 0, false);
        verify(sectionA5MessageVerifier).checkDM12(listener, SECTION, 0, false);
        verify(sectionA5MessageVerifier).checkDM23(listener, SECTION, 0, false);
        verify(sectionA5MessageVerifier).checkDM29(listener, SECTION, 0, false);
        verify(sectionA5MessageVerifier).checkDM5(listener, SECTION, 0, false);
        verify(sectionA5MessageVerifier).checkDM25(listener, SECTION, 0, false);
        verify(sectionA5MessageVerifier).checkDM31(listener, SECTION, 0, false);
        verify(sectionA5MessageVerifier).checkDM21(listener, SECTION, 0, false);
        verify(sectionA5MessageVerifier).checkDM26(listener, SECTION, 0, false);
        verify(sectionA5MessageVerifier).checkTestResults(listener, SECTION, 0, false);
        verify(sectionA5MessageVerifier).checkDM20(listener, SECTION, 0);
        verify(sectionA5MessageVerifier).checkDM28(listener, SECTION, 0);
        verify(sectionA5MessageVerifier).checkDM33(listener, SECTION, 0);
        verify(sectionA5MessageVerifier).checkEngineRunTime(listener, SECTION, 0);
        verify(sectionA5MessageVerifier).checkEngineIdleTime(listener, SECTION, 0);

        assertEquals(List.of(), listener.getOutcomes());
    }

    @Test
    public void testVerifyDataNotErasedWithFailures() {
        dataRepository.putObdModule(new OBDModuleInformation(0));

        when(sectionA5MessageVerifier.checkDM6(listener, SECTION, 0, false)).thenReturn(false);
        when(sectionA5MessageVerifier.checkDM12(listener, SECTION, 0, false)).thenReturn(false);
        when(sectionA5MessageVerifier.checkDM23(listener, SECTION, 0, false)).thenReturn(false);
        when(sectionA5MessageVerifier.checkDM29(listener, SECTION, 0, false)).thenReturn(false);
        when(sectionA5MessageVerifier.checkDM5(listener, SECTION, 0, false)).thenReturn(false);
        when(sectionA5MessageVerifier.checkDM25(listener, SECTION, 0, false)).thenReturn(false);
        when(sectionA5MessageVerifier.checkDM31(listener, SECTION, 0, false)).thenReturn(false);
        when(sectionA5MessageVerifier.checkDM21(listener, SECTION, 0, false)).thenReturn(false);
        when(sectionA5MessageVerifier.checkDM26(listener, SECTION, 0, false)).thenReturn(false);
        when(sectionA5MessageVerifier.checkTestResults(listener, SECTION, 0, false)).thenReturn(false);
        when(sectionA5MessageVerifier.checkDM20(listener, SECTION, 0)).thenReturn(false);
        when(sectionA5MessageVerifier.checkDM28(listener, SECTION, 0)).thenReturn(false);
        when(sectionA5MessageVerifier.checkDM33(listener, SECTION, 0)).thenReturn(false);
        when(sectionA5MessageVerifier.checkEngineRunTime(listener, SECTION, 0)).thenReturn(false);
        when(sectionA5MessageVerifier.checkEngineIdleTime(listener, SECTION, 0)).thenReturn(false);

        instance.verifyDataNotErased(listener, SECTION);

        verify(sectionA5MessageVerifier).checkDM6(listener, SECTION, 0, false);
        verify(sectionA5MessageVerifier).checkDM12(listener, SECTION, 0, false);
        verify(sectionA5MessageVerifier).checkDM23(listener, SECTION, 0, false);
        verify(sectionA5MessageVerifier).checkDM29(listener, SECTION, 0, false);
        verify(sectionA5MessageVerifier).checkDM5(listener, SECTION, 0, false);
        verify(sectionA5MessageVerifier).checkDM25(listener, SECTION, 0, false);
        verify(sectionA5MessageVerifier).checkDM31(listener, SECTION, 0, false);
        verify(sectionA5MessageVerifier).checkDM21(listener, SECTION, 0, false);
        verify(sectionA5MessageVerifier).checkDM26(listener, SECTION, 0, false);
        verify(sectionA5MessageVerifier).checkTestResults(listener, SECTION, 0, false);
        verify(sectionA5MessageVerifier).checkDM20(listener, SECTION, 0);
        verify(sectionA5MessageVerifier).checkDM28(listener, SECTION, 0);
        verify(sectionA5MessageVerifier).checkDM33(listener, SECTION, 0);
        verify(sectionA5MessageVerifier).checkEngineRunTime(listener, SECTION, 0);
        verify(sectionA5MessageVerifier).checkEngineIdleTime(listener, SECTION, 0);

        assertEquals(NL + SECTION + " - Checking for erased diagnostic information" + NL, listener.getResults());
    }

    @Test
    public void testVerifyDataNotPartialErasedHappyPathNoFailuresAllNotErased() {
        dataRepository.putObdModule(new OBDModuleInformation(0));
        dataRepository.putObdModule(new OBDModuleInformation(1));

        when(sectionA5MessageVerifier.checkDM6(listener, SECTION, 0, false)).thenReturn(true);
        when(sectionA5MessageVerifier.checkDM12(listener, SECTION, 0, false)).thenReturn(true);
        when(sectionA5MessageVerifier.checkDM23(listener, SECTION, 0, false)).thenReturn(true);
        when(sectionA5MessageVerifier.checkDM29(listener, SECTION, 0, false)).thenReturn(true);
        when(sectionA5MessageVerifier.checkDM5(listener, SECTION, 0, false)).thenReturn(true);
        when(sectionA5MessageVerifier.checkDM25(listener, SECTION, 0, false)).thenReturn(true);
        when(sectionA5MessageVerifier.checkDM31(listener, SECTION, 0, false)).thenReturn(true);
        when(sectionA5MessageVerifier.checkDM21(listener, SECTION, 0, false)).thenReturn(true);
        when(sectionA5MessageVerifier.checkDM26(listener, SECTION, 0, false)).thenReturn(true);
        when(sectionA5MessageVerifier.checkTestResults(listener, SECTION, 0, false)).thenReturn(true);
        when(sectionA5MessageVerifier.checkDM20(listener, SECTION, 0)).thenReturn(true);
        when(sectionA5MessageVerifier.checkDM28(listener, SECTION, 0)).thenReturn(true);
        when(sectionA5MessageVerifier.checkDM33(listener, SECTION, 0)).thenReturn(true);
        when(sectionA5MessageVerifier.checkEngineRunTime(listener, SECTION, 0)).thenReturn(true);
        when(sectionA5MessageVerifier.checkEngineIdleTime(listener, SECTION, 0)).thenReturn(true);

        when(sectionA5MessageVerifier.checkDM6(listener, SECTION, 1, false)).thenReturn(true);
        when(sectionA5MessageVerifier.checkDM12(listener, SECTION, 1, false)).thenReturn(true);
        when(sectionA5MessageVerifier.checkDM23(listener, SECTION, 1, false)).thenReturn(true);
        when(sectionA5MessageVerifier.checkDM29(listener, SECTION, 1, false)).thenReturn(true);
        when(sectionA5MessageVerifier.checkDM5(listener, SECTION, 1, false)).thenReturn(true);
        when(sectionA5MessageVerifier.checkDM25(listener, SECTION, 1, false)).thenReturn(true);
        when(sectionA5MessageVerifier.checkDM31(listener, SECTION, 1, false)).thenReturn(true);
        when(sectionA5MessageVerifier.checkDM21(listener, SECTION, 1, false)).thenReturn(true);
        when(sectionA5MessageVerifier.checkDM26(listener, SECTION, 1, false)).thenReturn(true);
        when(sectionA5MessageVerifier.checkTestResults(listener, SECTION, 1, false)).thenReturn(true);
        when(sectionA5MessageVerifier.checkDM20(listener, SECTION, 1)).thenReturn(true);
        when(sectionA5MessageVerifier.checkDM28(listener, SECTION, 1)).thenReturn(true);
        when(sectionA5MessageVerifier.checkDM33(listener, SECTION, 1)).thenReturn(true);
        when(sectionA5MessageVerifier.checkEngineRunTime(listener, SECTION, 1)).thenReturn(true);
        when(sectionA5MessageVerifier.checkEngineIdleTime(listener, SECTION, 1)).thenReturn(true);

        instance.verifyDataNotPartialErased(listener, SECTION, "Section2", false);

        verify(sectionA5MessageVerifier).checkDM6(listener, SECTION, 0, false);
        verify(sectionA5MessageVerifier).checkDM12(listener, SECTION, 0, false);
        verify(sectionA5MessageVerifier).checkDM23(listener, SECTION, 0, false);
        verify(sectionA5MessageVerifier).checkDM29(listener, SECTION, 0, false);
        verify(sectionA5MessageVerifier).checkDM5(listener, SECTION, 0, false);
        verify(sectionA5MessageVerifier).checkDM25(listener, SECTION, 0, false);
        verify(sectionA5MessageVerifier).checkDM31(listener, SECTION, 0, false);
        verify(sectionA5MessageVerifier).checkDM21(listener, SECTION, 0, false);
        verify(sectionA5MessageVerifier).checkDM26(listener, SECTION, 0, false);
        verify(sectionA5MessageVerifier).checkTestResults(listener, SECTION, 0, false);
        verify(sectionA5MessageVerifier).checkDM20(listener, SECTION, 0);
        verify(sectionA5MessageVerifier).checkDM28(listener, SECTION, 0);
        verify(sectionA5MessageVerifier).checkDM33(listener, SECTION, 0);
        verify(sectionA5MessageVerifier).checkEngineRunTime(listener, SECTION, 0);
        verify(sectionA5MessageVerifier).checkEngineIdleTime(listener, SECTION, 0);

        verify(sectionA5MessageVerifier).checkDM6(listener, SECTION, 1, false);
        verify(sectionA5MessageVerifier).checkDM12(listener, SECTION, 1, false);
        verify(sectionA5MessageVerifier).checkDM23(listener, SECTION, 1, false);
        verify(sectionA5MessageVerifier).checkDM29(listener, SECTION, 1, false);
        verify(sectionA5MessageVerifier).checkDM5(listener, SECTION, 1, false);
        verify(sectionA5MessageVerifier).checkDM25(listener, SECTION, 1, false);
        verify(sectionA5MessageVerifier).checkDM31(listener, SECTION, 1, false);
        verify(sectionA5MessageVerifier).checkDM21(listener, SECTION, 1, false);
        verify(sectionA5MessageVerifier).checkDM26(listener, SECTION, 1, false);
        verify(sectionA5MessageVerifier).checkTestResults(listener, SECTION, 1, false);
        verify(sectionA5MessageVerifier).checkDM20(listener, SECTION, 1);
        verify(sectionA5MessageVerifier).checkDM28(listener, SECTION, 1);
        verify(sectionA5MessageVerifier).checkDM33(listener, SECTION, 1);
        verify(sectionA5MessageVerifier).checkEngineRunTime(listener, SECTION, 1);
        verify(sectionA5MessageVerifier).checkEngineIdleTime(listener, SECTION, 1);

        assertEquals(List.of(), listener.getOutcomes());
        assertEquals(NL + SECTION + " - Checking for erased diagnostic information" + NL, listener.getResults());
    }

    @Test
    public void testVerifyDataNotPartialErasedHappyPathNoFailuresAllErased() {
        dataRepository.putObdModule(new OBDModuleInformation(0));
        dataRepository.putObdModule(new OBDModuleInformation(1));

        when(sectionA5MessageVerifier.checkDM6(listener, SECTION, 0, false)).thenReturn(false);
        when(sectionA5MessageVerifier.checkDM12(listener, SECTION, 0, false)).thenReturn(false);
        when(sectionA5MessageVerifier.checkDM23(listener, SECTION, 0, false)).thenReturn(false);
        when(sectionA5MessageVerifier.checkDM29(listener, SECTION, 0, false)).thenReturn(false);
        when(sectionA5MessageVerifier.checkDM5(listener, SECTION, 0, false)).thenReturn(false);
        when(sectionA5MessageVerifier.checkDM25(listener, SECTION, 0, false)).thenReturn(false);
        when(sectionA5MessageVerifier.checkDM31(listener, SECTION, 0, false)).thenReturn(false);
        when(sectionA5MessageVerifier.checkDM21(listener, SECTION, 0, false)).thenReturn(false);
        when(sectionA5MessageVerifier.checkDM26(listener, SECTION, 0, false)).thenReturn(false);
        when(sectionA5MessageVerifier.checkTestResults(listener, SECTION, 0, false)).thenReturn(false);
        when(sectionA5MessageVerifier.checkDM20(listener, SECTION, 0)).thenReturn(false);
        when(sectionA5MessageVerifier.checkDM28(listener, SECTION, 0)).thenReturn(false);
        when(sectionA5MessageVerifier.checkDM33(listener, SECTION, 0)).thenReturn(false);
        when(sectionA5MessageVerifier.checkEngineRunTime(listener, SECTION, 0)).thenReturn(false);
        when(sectionA5MessageVerifier.checkEngineIdleTime(listener, SECTION, 0)).thenReturn(false);

        when(sectionA5MessageVerifier.checkDM6(listener, SECTION, 1, false)).thenReturn(false);
        when(sectionA5MessageVerifier.checkDM12(listener, SECTION, 1, false)).thenReturn(false);
        when(sectionA5MessageVerifier.checkDM23(listener, SECTION, 1, false)).thenReturn(false);
        when(sectionA5MessageVerifier.checkDM29(listener, SECTION, 1, false)).thenReturn(false);
        when(sectionA5MessageVerifier.checkDM5(listener, SECTION, 1, false)).thenReturn(false);
        when(sectionA5MessageVerifier.checkDM25(listener, SECTION, 1, false)).thenReturn(false);
        when(sectionA5MessageVerifier.checkDM31(listener, SECTION, 1, false)).thenReturn(false);
        when(sectionA5MessageVerifier.checkDM21(listener, SECTION, 1, false)).thenReturn(false);
        when(sectionA5MessageVerifier.checkDM26(listener, SECTION, 1, false)).thenReturn(false);
        when(sectionA5MessageVerifier.checkTestResults(listener, SECTION, 1, false)).thenReturn(false);
        when(sectionA5MessageVerifier.checkDM20(listener, SECTION, 1)).thenReturn(false);
        when(sectionA5MessageVerifier.checkDM28(listener, SECTION, 1)).thenReturn(false);
        when(sectionA5MessageVerifier.checkDM33(listener, SECTION, 1)).thenReturn(false);
        when(sectionA5MessageVerifier.checkEngineRunTime(listener, SECTION, 1)).thenReturn(false);
        when(sectionA5MessageVerifier.checkEngineIdleTime(listener, SECTION, 1)).thenReturn(false);

        instance.verifyDataNotPartialErased(listener, SECTION, "Section2", false);

        verify(sectionA5MessageVerifier).checkDM6(listener, SECTION, 0, false);
        verify(sectionA5MessageVerifier).checkDM12(listener, SECTION, 0, false);
        verify(sectionA5MessageVerifier).checkDM23(listener, SECTION, 0, false);
        verify(sectionA5MessageVerifier).checkDM29(listener, SECTION, 0, false);
        verify(sectionA5MessageVerifier).checkDM5(listener, SECTION, 0, false);
        verify(sectionA5MessageVerifier).checkDM25(listener, SECTION, 0, false);
        verify(sectionA5MessageVerifier).checkDM31(listener, SECTION, 0, false);
        verify(sectionA5MessageVerifier).checkDM21(listener, SECTION, 0, false);
        verify(sectionA5MessageVerifier).checkDM26(listener, SECTION, 0, false);
        verify(sectionA5MessageVerifier).checkTestResults(listener, SECTION, 0, false);
        verify(sectionA5MessageVerifier).checkDM20(listener, SECTION, 0);
        verify(sectionA5MessageVerifier).checkDM28(listener, SECTION, 0);
        verify(sectionA5MessageVerifier).checkDM33(listener, SECTION, 0);
        verify(sectionA5MessageVerifier).checkEngineRunTime(listener, SECTION, 0);
        verify(sectionA5MessageVerifier).checkEngineIdleTime(listener, SECTION, 0);

        verify(sectionA5MessageVerifier).checkDM6(listener, SECTION, 1, false);
        verify(sectionA5MessageVerifier).checkDM12(listener, SECTION, 1, false);
        verify(sectionA5MessageVerifier).checkDM23(listener, SECTION, 1, false);
        verify(sectionA5MessageVerifier).checkDM29(listener, SECTION, 1, false);
        verify(sectionA5MessageVerifier).checkDM5(listener, SECTION, 1, false);
        verify(sectionA5MessageVerifier).checkDM25(listener, SECTION, 1, false);
        verify(sectionA5MessageVerifier).checkDM31(listener, SECTION, 1, false);
        verify(sectionA5MessageVerifier).checkDM21(listener, SECTION, 1, false);
        verify(sectionA5MessageVerifier).checkDM26(listener, SECTION, 1, false);
        verify(sectionA5MessageVerifier).checkTestResults(listener, SECTION, 1, false);
        verify(sectionA5MessageVerifier).checkDM20(listener, SECTION, 1);
        verify(sectionA5MessageVerifier).checkDM28(listener, SECTION, 1);
        verify(sectionA5MessageVerifier).checkDM33(listener, SECTION, 1);
        verify(sectionA5MessageVerifier).checkEngineRunTime(listener, SECTION, 1);
        verify(sectionA5MessageVerifier).checkEngineIdleTime(listener, SECTION, 1);

        assertEquals(List.of(), listener.getOutcomes());
        assertEquals(NL + SECTION + " - Checking for erased diagnostic information" + NL, listener.getResults());
    }

    @Test
    public void testVerifyDataNotPartialErasedFailureForMixedSingleModule() {
        dataRepository.putObdModule(new OBDModuleInformation(0));

        when(sectionA5MessageVerifier.checkDM6(listener, SECTION, 0, false)).thenReturn(false);
        when(sectionA5MessageVerifier.checkDM12(listener, SECTION, 0, false)).thenReturn(true);
        when(sectionA5MessageVerifier.checkDM23(listener, SECTION, 0, false)).thenReturn(true);
        when(sectionA5MessageVerifier.checkDM29(listener, SECTION, 0, false)).thenReturn(true);
        when(sectionA5MessageVerifier.checkDM5(listener, SECTION, 0, false)).thenReturn(true);
        when(sectionA5MessageVerifier.checkDM25(listener, SECTION, 0, false)).thenReturn(true);
        when(sectionA5MessageVerifier.checkDM31(listener, SECTION, 0, false)).thenReturn(true);
        when(sectionA5MessageVerifier.checkDM21(listener, SECTION, 0, false)).thenReturn(true);
        when(sectionA5MessageVerifier.checkDM26(listener, SECTION, 0, false)).thenReturn(true);
        when(sectionA5MessageVerifier.checkTestResults(listener, SECTION, 0, false)).thenReturn(true);
        when(sectionA5MessageVerifier.checkDM20(listener, SECTION, 0)).thenReturn(true);
        when(sectionA5MessageVerifier.checkDM28(listener, SECTION, 0)).thenReturn(true);
        when(sectionA5MessageVerifier.checkDM33(listener, SECTION, 0)).thenReturn(true);
        when(sectionA5MessageVerifier.checkEngineRunTime(listener, SECTION, 0)).thenReturn(true);
        when(sectionA5MessageVerifier.checkEngineIdleTime(listener, SECTION, 0)).thenReturn(true);

        instance.verifyDataNotPartialErased(listener, SECTION, "Section2", false);

        verify(sectionA5MessageVerifier).checkDM6(listener, SECTION, 0, false);
        verify(sectionA5MessageVerifier).checkDM12(listener, SECTION, 0, false);
        verify(sectionA5MessageVerifier).checkDM23(listener, SECTION, 0, false);
        verify(sectionA5MessageVerifier).checkDM29(listener, SECTION, 0, false);
        verify(sectionA5MessageVerifier).checkDM5(listener, SECTION, 0, false);
        verify(sectionA5MessageVerifier).checkDM25(listener, SECTION, 0, false);
        verify(sectionA5MessageVerifier).checkDM31(listener, SECTION, 0, false);
        verify(sectionA5MessageVerifier).checkDM21(listener, SECTION, 0, false);
        verify(sectionA5MessageVerifier).checkDM26(listener, SECTION, 0, false);
        verify(sectionA5MessageVerifier).checkTestResults(listener, SECTION, 0, false);
        verify(sectionA5MessageVerifier).checkDM20(listener, SECTION, 0);
        verify(sectionA5MessageVerifier).checkDM28(listener, SECTION, 0);
        verify(sectionA5MessageVerifier).checkDM33(listener, SECTION, 0);
        verify(sectionA5MessageVerifier).checkEngineRunTime(listener, SECTION, 0);
        verify(sectionA5MessageVerifier).checkEngineIdleTime(listener, SECTION, 0);

        assertEquals(NL + SECTION + " - Checking for erased diagnostic information" + NL, listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.2.3.a - Engine #1 (0) partially erased diagnostic information");
    }

    @Test
    public void testVerifyDataNotPartialErasedMixedModuleFailure() {
        dataRepository.putObdModule(new OBDModuleInformation(0));
        dataRepository.putObdModule(new OBDModuleInformation(1));

        when(sectionA5MessageVerifier.checkDM6(listener, SECTION, 0, false)).thenReturn(false);
        when(sectionA5MessageVerifier.checkDM12(listener, SECTION, 0, false)).thenReturn(false);
        when(sectionA5MessageVerifier.checkDM23(listener, SECTION, 0, false)).thenReturn(false);
        when(sectionA5MessageVerifier.checkDM29(listener, SECTION, 0, false)).thenReturn(false);
        when(sectionA5MessageVerifier.checkDM5(listener, SECTION, 0, false)).thenReturn(false);
        when(sectionA5MessageVerifier.checkDM25(listener, SECTION, 0, false)).thenReturn(false);
        when(sectionA5MessageVerifier.checkDM31(listener, SECTION, 0, false)).thenReturn(false);
        when(sectionA5MessageVerifier.checkDM21(listener, SECTION, 0, false)).thenReturn(false);
        when(sectionA5MessageVerifier.checkDM26(listener, SECTION, 0, false)).thenReturn(false);
        when(sectionA5MessageVerifier.checkTestResults(listener, SECTION, 0, false)).thenReturn(false);
        when(sectionA5MessageVerifier.checkDM20(listener, SECTION, 0)).thenReturn(false);
        when(sectionA5MessageVerifier.checkDM28(listener, SECTION, 0)).thenReturn(false);
        when(sectionA5MessageVerifier.checkDM33(listener, SECTION, 0)).thenReturn(false);
        when(sectionA5MessageVerifier.checkEngineRunTime(listener, SECTION, 0)).thenReturn(false);
        when(sectionA5MessageVerifier.checkEngineIdleTime(listener, SECTION, 0)).thenReturn(false);

        when(sectionA5MessageVerifier.checkDM6(listener, SECTION, 1, false)).thenReturn(true);
        when(sectionA5MessageVerifier.checkDM12(listener, SECTION, 1, false)).thenReturn(true);
        when(sectionA5MessageVerifier.checkDM23(listener, SECTION, 1, false)).thenReturn(true);
        when(sectionA5MessageVerifier.checkDM29(listener, SECTION, 1, false)).thenReturn(true);
        when(sectionA5MessageVerifier.checkDM5(listener, SECTION, 1, false)).thenReturn(true);
        when(sectionA5MessageVerifier.checkDM25(listener, SECTION, 1, false)).thenReturn(true);
        when(sectionA5MessageVerifier.checkDM31(listener, SECTION, 1, false)).thenReturn(true);
        when(sectionA5MessageVerifier.checkDM21(listener, SECTION, 1, false)).thenReturn(true);
        when(sectionA5MessageVerifier.checkDM26(listener, SECTION, 1, false)).thenReturn(true);
        when(sectionA5MessageVerifier.checkTestResults(listener, SECTION, 1, false)).thenReturn(true);
        when(sectionA5MessageVerifier.checkDM20(listener, SECTION, 1)).thenReturn(true);
        when(sectionA5MessageVerifier.checkDM28(listener, SECTION, 1)).thenReturn(true);
        when(sectionA5MessageVerifier.checkDM33(listener, SECTION, 1)).thenReturn(true);
        when(sectionA5MessageVerifier.checkEngineRunTime(listener, SECTION, 1)).thenReturn(true);
        when(sectionA5MessageVerifier.checkEngineIdleTime(listener, SECTION, 1)).thenReturn(true);

        instance.verifyDataNotPartialErased(listener, SECTION, "Section2", false);

        verify(sectionA5MessageVerifier).checkDM6(listener, SECTION, 0, false);
        verify(sectionA5MessageVerifier).checkDM12(listener, SECTION, 0, false);
        verify(sectionA5MessageVerifier).checkDM23(listener, SECTION, 0, false);
        verify(sectionA5MessageVerifier).checkDM29(listener, SECTION, 0, false);
        verify(sectionA5MessageVerifier).checkDM5(listener, SECTION, 0, false);
        verify(sectionA5MessageVerifier).checkDM25(listener, SECTION, 0, false);
        verify(sectionA5MessageVerifier).checkDM31(listener, SECTION, 0, false);
        verify(sectionA5MessageVerifier).checkDM21(listener, SECTION, 0, false);
        verify(sectionA5MessageVerifier).checkDM26(listener, SECTION, 0, false);
        verify(sectionA5MessageVerifier).checkTestResults(listener, SECTION, 0, false);
        verify(sectionA5MessageVerifier).checkDM20(listener, SECTION, 0);
        verify(sectionA5MessageVerifier).checkDM28(listener, SECTION, 0);
        verify(sectionA5MessageVerifier).checkDM33(listener, SECTION, 0);
        verify(sectionA5MessageVerifier).checkEngineRunTime(listener, SECTION, 0);
        verify(sectionA5MessageVerifier).checkEngineIdleTime(listener, SECTION, 0);

        verify(sectionA5MessageVerifier).checkDM6(listener, SECTION, 1, false);
        verify(sectionA5MessageVerifier).checkDM12(listener, SECTION, 1, false);
        verify(sectionA5MessageVerifier).checkDM23(listener, SECTION, 1, false);
        verify(sectionA5MessageVerifier).checkDM29(listener, SECTION, 1, false);
        verify(sectionA5MessageVerifier).checkDM5(listener, SECTION, 1, false);
        verify(sectionA5MessageVerifier).checkDM25(listener, SECTION, 1, false);
        verify(sectionA5MessageVerifier).checkDM31(listener, SECTION, 1, false);
        verify(sectionA5MessageVerifier).checkDM21(listener, SECTION, 1, false);
        verify(sectionA5MessageVerifier).checkDM26(listener, SECTION, 1, false);
        verify(sectionA5MessageVerifier).checkTestResults(listener, SECTION, 1, false);
        verify(sectionA5MessageVerifier).checkDM20(listener, SECTION, 1);
        verify(sectionA5MessageVerifier).checkDM28(listener, SECTION, 1);
        verify(sectionA5MessageVerifier).checkDM33(listener, SECTION, 1);
        verify(sectionA5MessageVerifier).checkEngineRunTime(listener, SECTION, 1);
        verify(sectionA5MessageVerifier).checkEngineIdleTime(listener, SECTION, 1);

        assertEquals(NL + SECTION + " - Checking for erased diagnostic information" + NL, listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "Section2 - One or more than one ECU erased diagnostic information and one or more other ECUs did not erase diagnostic information");
    }
}
