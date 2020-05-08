/**
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part1;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.etools.j1939_84.bus.j1939.packets.DM2PreviouslyActiveDTC;
import org.etools.j1939_84.controllers.Controller;
import org.etools.j1939_84.model.PartResultFactory;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DTCModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * @author mmschaefer
 *
 */
public class Step16Controller extends Controller {

    private final DTCModule dtcModule;

    Step16Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(), new EngineSpeedModule(), new BannerModule(),
                new DateTimeModule(), new VehicleInformationModule(), new DTCModule(), new PartResultFactory());
    }

    protected Step16Controller(Executor executor, EngineSpeedModule engineSpeedModule, BannerModule bannerModule,
            DateTimeModule dateTimeModule, VehicleInformationModule vehicleInformationModule, DTCModule dtcmodule,
            PartResultFactory partResultFactory) {
        super(executor, engineSpeedModule, bannerModule, dateTimeModule, vehicleInformationModule, partResultFactory);
        dtcModule = dtcmodule;
        // TODO Auto-generated constructor stub
    }

    @Override
    public String getDisplayName() {
        // TODO Auto-generated method stub
        return "Part 1 Step 16";
    }

    @Override
    protected int getTotalSteps() {
        // TODO Auto-generated method stub
        return 1;
    }

    @Override
    protected void run() throws Throwable {
        /**
         * 6.1.16.1 Actions:
         *
         * a. Global DM2 (send Request (PGN 59904) for PGN 65227 (SPNs 1213-1215, 3038,
         * 1706)).
         * globalDM 2 =
         *
         * 6.1.16.2 Fail criteria (if supported):
         *
         * a. Fail if any OBD ECU reports a previously active DTC.
         *
         * b. Fail if any OBD ECU does not report MIL off.
         *
         * c. Fail if any non-OBD ECU does not report MIL off or not supported.
         *
         * 6.1.16.3 Actions2:
         *
         * a. DS DM2 to each OBD ECU.
         *
         * 6.1.16.4 Fail criteria2 (if supported):
         *
         * a. Fail if any responses differ from global responses.
         *
         * a. Fail if NACK not received from OBD ECUs that did not respond to global
         * query.
         */

        // 6.1.16.1.a. Global DM2 (send Request (PGN 59904) for PGN 65227
        List<DM2PreviouslyActiveDTC> globalDM2s = dtcModule.requestDM2(listener).stream()
                .filter(p -> p instanceof DM2PreviouslyActiveDTC)
                .map(p -> (DM2PreviouslyActiveDTC) p)
                .collect(Collectors.toList());
        globalDM2s.get(0).getDtcs();
        globalDM2s.get(0).getMalfunctionIndicatorLampStatus();
        // LampStatus.OFF;
        globalDM2s.get(0).getProtectLampStatus();

    }

}
