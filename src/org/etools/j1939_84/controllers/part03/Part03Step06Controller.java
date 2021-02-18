/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part03;

import static org.etools.j1939_84.bus.j1939.packets.LampStatus.ALTERNATE_OFF;
import static org.etools.j1939_84.bus.j1939.packets.LampStatus.NOT_SUPPORTED;
import static org.etools.j1939_84.bus.j1939.packets.LampStatus.OFF;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.etools.j1939_84.bus.j1939.packets.DM1ActiveDTCsPacket;
import org.etools.j1939_84.bus.j1939.packets.LampStatus;
import org.etools.j1939_84.bus.j1939.packets.ParsedPacket;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticMessageModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * 6.3.6 DM1: Active diagnostic trouble codes (DTCs)
 */
public class Part03Step06Controller extends StepController {

    private static final int PART_NUMBER = 3;
    private static final int STEP_NUMBER = 6;
    private static final int TOTAL_STEPS = 0;

    Part03Step06Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part03Step06Controller(Executor executor,
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
        // 6.3.6.1.a Receive DM1 broadcast info (PGN 65226 (SPNs 1213-1215, 1706, and 3038)).
        List<DM1ActiveDTCsPacket> packets = getDiagnosticMessageModule().readDM1(getListener());

        // 6.3.6.2.a Fail if no OBD ECU supports DM1.
        boolean noObdDM1s = packets.stream().noneMatch(p -> getDataRepository().isObdModule(p.getSourceAddress()));
        if (noObdDM1s) {
            addFailure("6.3.6.2.a - No OBD ECU supports DM1");
        }

        // 6.3.6.2.b Fail if any OBD ECU reports an active DTC.
        packets.stream()
                .filter(p -> getDataRepository().isObdModule(p.getSourceAddress()))
                .filter(p -> !p.getDtcs().isEmpty())
                .map(ParsedPacket::getModuleName)
                .forEach(moduleName -> addFailure("6.3.6.2.b - " + moduleName + " reported an active DTC"));

        // 6.3.6.2.c Fail if any OBD ECU does not report MIL off. See section A.8 for allowed values.
        packets.stream()
                .filter(p -> getDataRepository().isObdModule(p.getSourceAddress()))
                .filter(p -> {
                    LampStatus mil = p.getMalfunctionIndicatorLampStatus();
                    return mil != OFF && mil != ALTERNATE_OFF;
                })
                .map(ParsedPacket::getModuleName)
                .forEach(moduleName -> addFailure("6.3.6.2.c - " + moduleName + " did not report MIL 'off'"));

        // 6.3.6.2.d Fail if any non-OBD ECU does not report MIL off or not supported.
        packets.stream()
                .filter(p -> !getDataRepository().isObdModule(p.getSourceAddress()))
                .filter(p -> {
                    LampStatus mil = p.getMalfunctionIndicatorLampStatus();
                    return mil != OFF && mil != NOT_SUPPORTED;
                })
                .map(ParsedPacket::getModuleName)
                .forEach(moduleName -> addFailure("6.3.6.2.d - Non-OBD module " + moduleName + " did not report MIL off or not supported"));
    }

}
