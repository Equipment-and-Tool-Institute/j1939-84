/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part01;

import static org.etools.j1939_84.bus.j1939.packets.LampStatus.OFF;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.etools.j1939_84.bus.j1939.packets.ParsedPacket;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticMessageModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 * <p>
 * The controller for 6.1.19 DM23: Emission Related Previously Active DTCs
 */
public class Part01Step19Controller extends StepController {

    private static final int PART_NUMBER = 1;
    private static final int STEP_NUMBER = 19;
    private static final int TOTAL_STEPS = 0;

    Part01Step19Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(),
             new EngineSpeedModule(),
             new BannerModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule(),
             dataRepository,
             DateTimeModule.getInstance());
    }

    Part01Step19Controller(Executor executor,
                           EngineSpeedModule engineSpeedModule,
                           BannerModule bannerModule,
                           VehicleInformationModule vehicleInformationModule,
                           DiagnosticMessageModule diagnosticMessageModule,
                           DataRepository dataRepository,
                           DateTimeModule dateTimeModule) {
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
        // 6.1.19.1.a. Global DM23 for PGN 65236
        var globalPackets = getDiagnosticMessageModule().requestDM23(getListener()).getPackets();

        // 6.1.19.2.a. Fail if any ECU reports previously active DTCs.
        globalPackets.stream()
                .filter(p -> !p.getDtcs().isEmpty())
                .map(ParsedPacket::getModuleName)
                .forEach(moduleName -> addFailure("6.1.19.2.a - " + moduleName + " reported previously active DTCs"));

        // 6.1.19.2.b. Fail if any ECU does not report MIL off.
        globalPackets.stream()
                .filter(p -> p.getMalfunctionIndicatorLampStatus() != OFF)
                .map(ParsedPacket::getModuleName)
                .forEach(moduleName -> addFailure("6.1.19.2.b - " + moduleName + " did not report MIL off"));

        boolean obdModuleResponded = globalPackets.stream()
                .anyMatch(p -> getDataRepository().isObdModule(p.getSourceAddress()));
        if (!obdModuleResponded) {
            // 6.1.19.2.c. Fail if no OBD ECU provides DM23.
            addFailure("6.1.19.2.c - No OBD ECU provided DM23");
        }

        List<Integer> obdModuleAddresses = getDataRepository().getObdModuleAddresses();

        // 6.1.19.3.a. DS DM23 to all OBD ECUs.
        var dsResults = obdModuleAddresses
                .stream().map(address -> getDiagnosticMessageModule().requestDM23(getListener(), address))
                .collect(Collectors.toList());

        // 6.1.19.4.a. Fail if any difference compared to data received during global request.
        compareRequestPackets(globalPackets, filterPackets(dsResults), "6.1.19.4.a");

        // 6.1.19.4.b. Fail if NACK not received from OBD ECUs that did not respond to global query.
        checkForNACKs(globalPackets, filterAcks(dsResults), obdModuleAddresses, "6.1.19.4.b");
    }
}
