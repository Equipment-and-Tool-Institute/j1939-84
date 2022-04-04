/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part12;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.SectionA5Verifier;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

import net.soliddesign.j1939tools.modules.CommunicationsModule;
import net.soliddesign.j1939tools.modules.DateTimeModule;

/**
 * 6.12.9 DM20: Monitor Performance Ratio
 */
public class Part12Step09Controller extends StepController {
    private static final int PART_NUMBER = 12;
    private static final int STEP_NUMBER = 9;
    private static final int TOTAL_STEPS = 0;
    private final SectionA5Verifier verifier;

    Part12Step09Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule(),
             new SectionA5Verifier(PART_NUMBER, STEP_NUMBER));
    }

    Part12Step09Controller(Executor executor,
                           BannerModule bannerModule,
                           DateTimeModule dateTimeModule,
                           DataRepository dataRepository,
                           EngineSpeedModule engineSpeedModule,
                           VehicleInformationModule vehicleInformationModule,
                           CommunicationsModule communicationsModule,
                           SectionA5Verifier verifier) {
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
        this.verifier = verifier;
    }

    @Override
    protected void run() throws Throwable {
        verifier.setJ1939(getJ1939());
        // 6.12.9 DM20: Monitor Performance Ratio // NEW Test
        // 6.12.9.1 Actions
        // a. DS DM20 [send Request (PG 59904) for PG 49664 (SPs 3048-3049, 3066-3068)] to ECUs that reported DM20 data
        // earlier in Part 11.
        // b. If no response [transport protocol RTS or NACK(Busy) in 220 ms], then retry DS DM20 request to the OBD
        // ECU. [Do not attempt retry for NACKs that indicate not supported].
        // c. Record responses for use in part 12 test 11 (formerly 12.9.2).
        // 6.12.9.2 Fail Criteria
        // a. Fail if retry was required to obtain DM20 response.
        // b. Fail if any response indicates that the general denominator (SP 3049) is greater by more than 1 when
        // compared to the general denominator received in Part 11 test 5.
        // c. Fail if NACK received from OBD ECUs that previously provided a DM20 message.
        // 6.12.9.3 Warn Criteria
        // a. Warn if any response indicates an individual numerator or denominator that is greater than the
        // corresponding values received in Part 11 test 8.
        // b. Warn if any ECU response shows:
        // i. any monitor denominator greater than the general denominator;
        // ii. general denominator greater than the ignition cycle counter (SP 3048); or
        // iii. if any numerator greater than the ignition cycle counter.
        // c. Compare all values to values recorded in part 11 test 8.
        // i. Warn if any value (numerator, denominator, or ignition cycle counter) is less than their corresponding
        // value in part 11 test.
        // d. If more than one ECU reports DM20 data, warn if general denominators or ignition cycle counts do not match
        // from all ECUs.
    }

}
