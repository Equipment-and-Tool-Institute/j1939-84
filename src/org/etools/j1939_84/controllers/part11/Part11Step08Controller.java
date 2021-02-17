/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part11;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticMessageModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * 6.11.8 DM20: Monitor Performance Ratio
 */
public class Part11Step08Controller extends StepController {
    private static final int PART_NUMBER = 11;
    private static final int STEP_NUMBER = 8;
    private static final int TOTAL_STEPS = 0;

    Part11Step08Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part11Step08Controller(Executor executor,
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
        // 6.11.8.1.a. DS DM20 [(send Request (PGN 59904) for PGN 49664 (SPNs 3048-3049, 3066-3068)]) to ECUs that reported DM20 data earlier in this part.
        // 6.11.8.1.b. If no response [(transport protocol RTS or NACK(Busy) in 220 ms]), then retry DS DM20 request to the OBD ECU. ([Do not attempt retry for NACKs that indicate not supported]).
        // 6.11.8.2.a. Fail if retry was required to obtain DM20 response.
        // 6.11.8.2.b. Fail if any response indicates that the general denominator (SPN 3049) has not incremented by one from value earlier in part 9.
        // 6.11.8.2.c. Fail if NACK received from OBD ECUs that previously provided a DM20 message.
        // 6.11.8.3.a. Warn if any response indicates denominator for SCR, EGR, NOx sensor, boost, and fuel system have not incremented by one.
        // 6.11.8.3.b. Warn if any ECU response shows:
        // 6.11.8.3.b.i. any monitor denominator greater than the general denominator;
        // 6.11.8.3.b.ii. general denominator greater than the ignition cycle counter (SPN 3048); or
        // 6.11.8.3.b.iii. if any numerator greater than the ignition cycle counter.
        // 6.11.8.3.c. Compare all values to values recorded in part 1.
        // 6.11.8.3.c.i. Warn if any value (numerator, denominator, or ignition cycle counter) is less than their corresponding value in part 1.
        // 6.11.8.3.d. If more than one ECU reports DM20 data, warn if general denominators or ignition cycle counts do not match from all ECUs.
    }

}