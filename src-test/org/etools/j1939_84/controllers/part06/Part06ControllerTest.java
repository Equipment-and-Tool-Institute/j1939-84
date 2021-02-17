/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part06;

import org.etools.j1939_84.controllers.AbstractPartControllerTest;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.controllers.part01.Part01Controller;
import org.etools.j1939_84.modules.DateTimeModule;
import org.junit.Before;

public class Part06ControllerTest extends AbstractPartControllerTest {

    @Before
    public void setUp() {
        DateTimeModule.setInstance(null);

        this.partNumber = 6;
        listener = new TestResultsListener(mockListener);
        instance = new Part06Controller(executor,
                                        bannerModule,
                                        DateTimeModule.getInstance(),
                                        DataRepository.newInstance(),
                                        engineSpeedModule,
                                        vehicleInformationModule,
                                        diagnosticMessageModule,
                                        step01Controller,
                                        step02Controller,
                                        step03Controller,
                                        step04Controller,
                                        step05Controller,
                                        step06Controller,
                                        step07Controller,
                                        step08Controller,
                                        step09Controller,
                                        step10Controller,
                                        step11Controller);
    }

}
