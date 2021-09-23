/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part02;

import org.etools.j1939_84.controllers.AbstractPartControllerTest;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.modules.DateTimeModule;
import org.junit.Before;

public class Part02ControllerTest extends AbstractPartControllerTest {

    @Override
    @Before
    public void setUp() {
        DateTimeModule.setInstance(null);

        partNumber = 2;
        listener = new TestResultsListener(mockListener);
        instance = new Part02Controller(executor,
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
                                        step05Controller,
                                        step06Controller,
                                        step07Controller,
                                        step08Controller,
                                        step09Controller,
                                        step10Controller,
                                        step11Controller,
                                        step12Controller,
                                        step13Controller,
                                        step14Controller,
                                        step15Controller,
                                        step16Controller,
                                        step17Controller,
                                        step18Controller);
    }

}
