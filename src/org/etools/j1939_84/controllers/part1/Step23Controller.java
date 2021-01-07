/**
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part1;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.etools.j1939_84.bus.j1939.packets.DM31DtcToLampAssociation;
import org.etools.j1939_84.bus.j1939.packets.DTCLampStatus;
import org.etools.j1939_84.bus.j1939.packets.LampStatus;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.model.RequestResult;
import org.etools.j1939_84.modules.*;

/**
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 * <p>
 * The controller for 6.1.23 DM31: DTC to Lamp Association
 */

public class Step23Controller extends StepController {

    private static final int PART_NUMBER = 1;
    private static final int STEP_NUMBER = 23;
    private static final int TOTAL_STEPS = 0;

    private final DTCModule dtcModule;

    Step23Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new EngineSpeedModule(),
             new BannerModule(),
             new VehicleInformationModule(),
             new DTCModule(), DateTimeModule.getInstance());
    }

    Step23Controller(Executor executor,
                     EngineSpeedModule engineSpeedModule,
                     BannerModule bannerModule,
                     VehicleInformationModule vehicleInformationModule,
                     DTCModule dtcModule,
                     DateTimeModule dateTimeModule) {
        super(executor,
              engineSpeedModule,
              bannerModule,
              vehicleInformationModule,
              dateTimeModule,
              PART_NUMBER,
              STEP_NUMBER,
              TOTAL_STEPS);
        this.dtcModule = dtcModule;
    }

    @Override
    protected void run() throws Throwable {

        dtcModule.setJ1939(getJ1939());

        // 6.1.23.1 Actions:
        // a. Global DM31 (send Request (PGN 59904) for PGN 41728 (SPNs
        // 1214-1215, 4113, 4117)).
        RequestResult<DM31DtcToLampAssociation> globalResponse = dtcModule.requestDM31(getListener());

        List<DTCLampStatus> milOnPackets = globalResponse.getPackets().stream()
                .flatMap(p -> p.getDtcLampStatuses().stream())
                .filter(s -> s.getMalfunctionIndicatorLampStatus() != LampStatus.OFF)
                .collect(Collectors.toList());
        // 6.1.23.2 Fail criteria (if supported):
        // a. Fail if any received ECU response does not report MIL off.
        if (!milOnPackets.isEmpty()) {
            addFailure("6.1.23.2 - a. Fail if any received ECU response does not report MIL off");
        }
    }
}
