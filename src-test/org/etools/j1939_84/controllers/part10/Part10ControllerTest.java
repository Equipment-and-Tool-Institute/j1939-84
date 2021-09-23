/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part10;

import org.etools.j1939_84.controllers.AbstractPartControllerTest;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.modules.DateTimeModule;
import org.junit.Before;

public class Part10ControllerTest extends AbstractPartControllerTest {

    @Override
    @Before
    public void setUp() {
        DateTimeModule.setInstance(null);

        partNumber = 10;
        listener = new TestResultsListener(mockListener);
        instance = new Part10Controller(executor,
                                        bannerModule,
                                        DateTimeModule.getInstance(),
                                        DataRepository.newInstance(),
                                        engineSpeedModule,
                                        vehicleInformationModule,
                                        communicationsModule,
                                        step01Controller,
                                        step02Controller,
                                        step03Controller,
                                        step04Controller,
                                        step05Controller);
    }

}
