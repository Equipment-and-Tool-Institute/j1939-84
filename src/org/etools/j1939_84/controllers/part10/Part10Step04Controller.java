/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part10;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import net.soliddesign.j1939tools.j1939.packets.DiagnosticTroubleCodePacket;
import net.soliddesign.j1939tools.j1939.packets.ParsedPacket;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import net.soliddesign.j1939tools.modules.DateTimeModule;
import net.soliddesign.j1939tools.modules.CommunicationsModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;;

/**
 * 6.10.4 DM28: Permanent DTCs
 */
public class Part10Step04Controller extends StepController {
    private static final int PART_NUMBER = 10;
    private static final int STEP_NUMBER = 4;
    private static final int TOTAL_STEPS = 0;

    Part10Step04Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule());
    }

    Part10Step04Controller(Executor executor,
                           BannerModule bannerModule,
                           DateTimeModule dateTimeModule,
                           DataRepository dataRepository,
                           EngineSpeedModule engineSpeedModule,
                           VehicleInformationModule vehicleInformationModule,
                           CommunicationsModule communicationsModule) {
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
    }

    @Override
    protected void run() throws Throwable {
        // 6.10.4.1.a. DS DM28 [send Request (PGN 59904) for PGN 64896 (SPNs 1213-1215, 1706, and 3038)] to each OBD ECU
        var dsResults = getDataRepository().getObdModuleAddresses()
                                           .stream()
                                           .map(a -> getCommunicationsModule().requestDM28(getListener(), a))
                                           .collect(Collectors.toList());

        var dsPackets = filterPackets(dsResults);
        dsPackets.forEach(this::save);

        // 6.10.4.2.b. Fail if any ECU does not report MIL off. See Section A.8 for allowed values.
        dsPackets.stream()
                 .filter(p -> isNotOff(p.getMalfunctionIndicatorLampStatus()))
                 .map(ParsedPacket::getModuleName)
                 .forEach(moduleName -> {
                     addFailure("6.10.4.2.b. - ECU " + moduleName + " did not report MIL 'off'");
                 });

        // 6.10.4.2.a. Fail if no ECU reports a permanent DTC.
        boolean noDTCs = dsPackets.stream().noneMatch(DiagnosticTroubleCodePacket::hasDTCs);
        if (noDTCs) {
            addFailure("6.10.4.2.a - No ECU reported a permanent DTC");
        }

        // 6.10.4.2.c. Fail if NACK not received from OBD ECUs that did not provide a DM28 message.
        checkForNACKsDS(dsPackets, filterAcks(dsResults), "6.10.4.2.c");
    }
}
