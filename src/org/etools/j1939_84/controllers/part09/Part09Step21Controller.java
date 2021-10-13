/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part09;

import static java.lang.String.format;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

import net.soliddesign.j1939tools.modules.CommunicationsModule;
import net.soliddesign.j1939tools.modules.DateTimeModule;

;

/**
 * 6.9.21 DM5: Diagnostic Readiness 1
 */
public class Part09Step21Controller extends StepController {
    private static final int PART_NUMBER = 9;
    private static final int STEP_NUMBER = 21;
    private static final int TOTAL_STEPS = 0;

    Part09Step21Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule());
    }

    Part09Step21Controller(Executor executor,
                           BannerModule bannerModule,
                           DateTimeModule dateTimeModule,
                           DataRepository dataRepository,
                           EngineSpeedModule engineSpeedModule,
                           VehicleInformationModule vehicleInformationModule,
                           CommunicationsModule communicationsModule) {
        super(executor,
              bannerModule,
              dateTimeModule,
              dataRepository,
              engineSpeedModule,
              vehicleInformationModule,
              communicationsModule,
              PART_NUMBER,
              STEP_NUMBER,
              TOTAL_STEPS);
    }

    @Override
    protected void run() throws Throwable {
        // 6.9.21.1.a. Global DM5 [(send Request (PGN 59904) for PGN 65230 (SPNs 1218-1219)]).
        getCommunicationsModule().requestDM5(getListener())
                                    .getPackets()
                                    .stream()
                                    .peek(this::save)
                                    .filter(p -> getDataRepository().isObdModule(p.getSourceAddress()))
                                    // 6.9.21.2.a. Fail if any OBD ECU reports > 0 active DTCs or > 0 previously active
                                    // DTCs.
                                    .forEach(p -> {
                                        if (p.getPreviouslyActiveCodeCount() != (byte) 0xFF
                                                && p.getPreviouslyActiveCodeCount() > 0) {
                                            addFailure(format("6.9.21.2.a - OBD ECU %s reported > 0 previously active DTCs count",
                                                              p.getModuleName()));
                                        }
                                        if (p.getActiveCodeCount() != (byte) 0xFF && p.getActiveCodeCount() > 0) {
                                            addFailure(format("6.9.21.2.a - OBD ECU %s reported > 0 active DTCs count",
                                                              p.getModuleName()));
                                        }
                                    });
    }

}
