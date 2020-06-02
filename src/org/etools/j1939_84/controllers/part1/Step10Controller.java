/**
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part1;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.etools.j1939_84.controllers.Controller;
import org.etools.j1939_84.model.PartResultFactory;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 *
 *         6.1.10 DM11: Diagnostic Data Clear/Reset for Active DTCs
 *
 */
public class Step10Controller extends Controller {

    private final DataRepository dataRepository;

    Step10Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(), new EngineSpeedModule(), new BannerModule(),
                new DateTimeModule(), new VehicleInformationModule(), new PartResultFactory(),
                dataRepository);
    }

    protected Step10Controller(Executor executor, EngineSpeedModule engineSpeedModule, BannerModule bannerModule,
            DateTimeModule dateTimeModule, VehicleInformationModule vehicleInformationModule,
            PartResultFactory partResultFactory, DataRepository dataRepository) {
        super(executor, engineSpeedModule, bannerModule, dateTimeModule, vehicleInformationModule, partResultFactory);
        this.dataRepository = dataRepository;
    }

    @Override
    public String getDisplayName() {
        return "Part 1 Step 10";
    }

    @Override
    protected int getTotalSteps() {
        return 1;
    }

    @Override
    protected void run() throws Throwable {
        /**
         * 6.1.10 DM11: Diagnostic Data Clear/Reset for Active DTCs
         *
         * 6.1.10.1 Actions:
         *
         * a. Global DM11 (send Request (PGN 59904) for PGN 65235).
         *
         * b. Record all ACK/NACK/BUSY/Access Denied responses (for PGN 65235) in the
         * log.
         * use DTCModule.reportDM11: you will need to create the object and all that
         * before using it.
         *
         * c. Allow 5 s to elapse before proceeding with test step 6.1.9.2.
         * going to use something like this: dateTimeModule.pauseFor(5*60*1000);;
         * 6.1.10.2 Fail criteria:
         *
         * a. Fail if NACK received from any HD OBD ECU.
         * from the dataRepo grab the obdModule addresses
         *
         * b. Fail if any diagnostic information in any ECU is not reset or starts out
         * with unexpected values.
         *
         * information is defined in section A.5, Diagnostic Information Definition.15
         *
         * 6.1.10.3 Warn criteria:
         *
         * a. Warn if ACK received from any HD OBD ECU.16
         */

    }
}
