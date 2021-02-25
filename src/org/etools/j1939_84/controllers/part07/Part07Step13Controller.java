/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part07;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

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
 * 6.7.13 DM20: Monitor Performance Ratio
 */
public class Part07Step13Controller extends StepController {
    private static final int PART_NUMBER = 7;
    private static final int STEP_NUMBER = 13;
    private static final int TOTAL_STEPS = 0;

    Part07Step13Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part07Step13Controller(Executor executor,
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
        // 6.7.13.1.a. DS DM20 [(send Request (PGN 59904) for PGN 49664 (SPN 3048)]) to ECU(s) that responded in part 5
        // with DM20 data.
        var dsResults = getDataRepository().getObdModules()
                                           .stream()
                                           .filter(m -> m.getIgnitionCycleCounterValue() != -1)
                                           .map(OBDModuleInformation::getSourceAddress)
                                           .map(a -> getDiagnosticMessageModule().requestDM20(getListener(), a))
                                           .collect(Collectors.toList());
        var packets = filterPackets(dsResults);

        // 6.7.13.2.a. Fail if ignition cycle counter (SPN 3048) for any ECU has incremented by other than 3 cycles from
        // part 5.
        packets.stream()
               .filter(p -> p.getIgnitionCycles() != getIgnCycles(p.getSourceAddress()) + 3)
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> addFailure("6.7.13.2.a - Ignition cycle counter for " + moduleName
                       + " has incremented by other than 3 cycles from part 5"));
    }

    private int getIgnCycles(int address) {
        OBDModuleInformation module = getDataRepository().getObdModule(address);
        return module == null ? -1 : module.getIgnitionCycleCounterValue();
    }

}
