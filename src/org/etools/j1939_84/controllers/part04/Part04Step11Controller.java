/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part04;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.etools.j1939_84.bus.j1939.packets.ParsedPacket;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticMessageModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * 6.4.11 DM20: Monitor Performance Ratio
 */
public class Part04Step11Controller extends StepController {
    private static final int PART_NUMBER = 4;
    private static final int STEP_NUMBER = 11;
    private static final int TOTAL_STEPS = 0;

    Part04Step11Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part04Step11Controller(Executor executor,
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
        for (OBDModuleInformation obdModuleInformation : getDataRepository().getObdModules()) {
            int ignCycles = obdModuleInformation.getIgnitionCycleCounterValue();
            if (!obdModuleInformation.getPerformanceRatios().isEmpty()) {
                // 6.4.11.1.a DS DM20 [(send Request (PGN 59904) for PGN 49664 (SPN 3048)]) to ECU(s) that responded in
                // part 1 with DM20 data.
                // 6.4.11.2.a Fail if ignition cycle counter (SPN 3048) for any ECU has not incremented by one compared
                // to value recorded at end of part 3.
                getDiagnosticMessageModule().requestDM20(getListener(), obdModuleInformation.getSourceAddress())
                                            .requestResult()
                                            .getPackets()
                                            .stream()
                                            .filter(p -> p.getIgnitionCycles() != ignCycles + 1)
                                            .map(ParsedPacket::getModuleName)
                                            .forEach(moduleName -> addFailure("6.4.11.2.a - Ignition cycle counter (SPN 3048) from "
                                                    + moduleName +
                                                    " has not incremented by one compared to the value recorded at the end of part 3"));
            }
        }

    }

}
