/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part10;

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
 * 6.10.4 DM28: Permanent DTCs
 */
public class Part10Step04Controller extends StepController {
    private static final int PART_NUMBER = 10;
    private static final int STEP_NUMBER = 4;
    private static final int TOTAL_STEPS = 0;

    Part10Step04Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part10Step04Controller(Executor executor,
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
        // 6.10.4.1.a. DS DM28 [(send Request (PGN 59904) for PGN 64896 (SPNs 1213-1215, 1706, and 3038))] to each OBD
        // ECU.
        var dsResults = getDataRepository().getObdModuleAddresses()
                                           .stream()
                                           .map(a -> getDiagnosticMessageModule().requestDM28(getListener(), a))
                                           .collect(Collectors.toList());

        var dsPackets = filterPackets(dsResults).stream()
                                                // Save DM28
                                                .peek(this::save)
                                                .peek(p -> {
                                                    // 6.10.4.2.b. Fail if any ECU does not report MIL off.
                                                    // See Section A.8 for allowed values.
                                                    if (isNotOff(p.getMalfunctionIndicatorLampStatus())) {
                                                        addFailure("6.10.4.2.b. - ECU " + p.getModuleName()
                                                                + "did not report MIL 'off'");
                                                    }
                                                })
                                                .collect(Collectors.toList());

        // 6.10.4.2.a. Fail if no ECU reports a permanent DTC.
        if (dsPackets.isEmpty()) {
            addFailure("6.10.4.2.a - No ECU reported a permanent DTC");
        }

        // 6.10.4.2.c. Fail if NACK not received from OBD ECUs that did not provide a DM28 message.
        checkForNACKsDS(filterPackets(dsResults), filterAcks(dsResults), "6.10.4.2.c");
    }
}
