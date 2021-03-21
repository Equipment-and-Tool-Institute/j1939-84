/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part08;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticMessageModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * 6.8.15 DM26: Diagnostic Readiness 3
 */
public class Part08Step15Controller extends StepController {
    private static final int PART_NUMBER = 8;
    private static final int STEP_NUMBER = 15;
    private static final int TOTAL_STEPS = 0;

    Part08Step15Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part08Step15Controller(Executor executor,
                           BannerModule bannerModule,
                           DateTimeModule dateTimeModule,
                           DataRepository dataRepository,
                           EngineSpeedModule engineSpeedModule,
                           VehicleInformationModule vehicleInformationModule,
                           DiagnosticMessageModule diagnosticMessageModule) {
        super(executor,
              bannerModule,
              dateTimeModule,
              dataRepository,
              engineSpeedModule,
              vehicleInformationModule,
              diagnosticMessageModule,
              PART_NUMBER,
              STEP_NUMBER,
              TOTAL_STEPS);
    }

    @Override
    protected void run() throws Throwable {
        // 6.8.15.1.a. DS DM26 ([send Request (PGN 59904) for PGN 64952 (SPN 3302)]) to each OBD ECU.
        var results = getDataRepository().getObdModuleAddresses()
                                         .stream()
                                         .map(a -> getDiagnosticMessageModule().requestDM26(getListener(), a))
                                         .collect(Collectors.toList());

        var packets = filterRequestResultPackets(results);

        // 6.8.15.1.b. Record all values of provided for number of warm-ups since code clear (SPN 3302).
        packets.forEach(this::save);

        // 6.8.15.2.a. Fail if NACK not received from OBD ECUs that did not provide DM26 message.
        checkForNACKsDS(packets, filterRequestResultAcks(results), "6.8.15.2.a");
    }

}
