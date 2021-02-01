/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part2;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DiagnosticMessageModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * 6.2.16 DM34: NTE status
 */
public class Part02Step16Controller extends StepController {

    private static final int PART_NUMBER = 2;
    private static final int STEP_NUMBER = 16;
    private static final int TOTAL_STEPS = 0;

    private final DataRepository dataRepository;
    private final DiagnosticMessageModule diagnosticMessageModule;

    Part02Step16Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(),
             new EngineSpeedModule(),
             new BannerModule(),
             new VehicleInformationModule(),
             dataRepository,
             DateTimeModule.getInstance(),
             new DiagnosticMessageModule());
    }

    Part02Step16Controller(Executor executor,
                           EngineSpeedModule engineSpeedModule,
                           BannerModule bannerModule,
                           VehicleInformationModule vehicleInformationModule,
                           DataRepository dataRepository,
                           DateTimeModule dateTimeModule,
                           DiagnosticMessageModule diagnosticMessageModule) {
        super(executor,
              engineSpeedModule,
              bannerModule,
              vehicleInformationModule,
              new DiagnosticMessageModule(), dateTimeModule,
              PART_NUMBER,
              STEP_NUMBER,
              TOTAL_STEPS);
        this.dataRepository = dataRepository;
        this.diagnosticMessageModule = diagnosticMessageModule;
    }

    @Override
    protected void run() throws Throwable {
        // 6.2.16.1.a. Global DM34 (send Request (PGN 59904) for PGN 40960 (SPNs 4127-4132)).
        // 6.2.16.2.a. Fail if no ECU responds, unless the user selected SI technology.
        // 6.2.16.2.b. Fail if any ECU response is not = 0b00 (Outside Control Area) for NOx and PM control areas (byte 1 bits 7-8, byte 2 bits 7-8).
        // 6.2.16.2.c. Fail if any ECU response is not = 0b00 (Outside Area) or 0b11 (not available) for NOx/PM carve-out/deficiency areas (byte 1 bits 5-6 and byte 2 bits 5-6).
        // 6.2.16.2.d. Fail if any ECU response is not = 0b11 for byte 1 bits 1-2 and for byte 2 bits 1-2.
        // 6.2.16.2.e. Fail if any reserved bytes 3-8 are not = 0xFF.
        // 6.2.16.3.a. DS DM34 to each OBD ECU which responded to the DM34 global request in step 1.
        // 6.2.16.4.a. Fail if any difference compared to data received from global request.
        // 6.2.16.4.b. Fail if NACK received from OBD ECUs that responded to the global query in part 1.
    }
}
