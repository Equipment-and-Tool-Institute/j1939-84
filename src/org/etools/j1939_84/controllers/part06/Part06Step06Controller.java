/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part06;

import static net.soliddesign.j1939tools.j1939.packets.LampStatus.ON;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

import net.soliddesign.j1939tools.j1939.packets.DiagnosticTroubleCodePacket;
import net.soliddesign.j1939tools.j1939.packets.ParsedPacket;
import net.soliddesign.j1939tools.modules.CommunicationsModule;
import net.soliddesign.j1939tools.modules.DateTimeModule;

/**
 * 6.6.6. DM23: Emission Related Previously Active DTCs
 */
public class Part06Step06Controller extends StepController {
    private static final int PART_NUMBER = 6;
    private static final int STEP_NUMBER = 6;
    private static final int TOTAL_STEPS = 0;

    Part06Step06Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule());
    }

    Part06Step06Controller(Executor executor,
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
        // 6.6.6.1.a DS DM23 [(send Request (PGN 59904) for PGN 64949 (SPNs 1213-1215, 3038, 1706)]) to each OBD ECU.
        var dsResults = getDataRepository().getObdModuleAddresses()
                                           .stream()
                                           .map(a -> getCommunicationsModule().requestDM23(getListener(), a))
                                           .collect(Collectors.toList());

        var packets = filterPackets(dsResults);
        packets.forEach(this::save);

        // 6.6.6.2.a. Fail if any OBD ECU reports a previously active DTC.
        packets.stream()
               .filter(p -> !p.getDtcs().isEmpty())
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> addFailure("6.6.6.2.a - " + moduleName + " reported an previously active DTC"));

        // 6.6.6.2.b. Fail if no OBD ECU reports MIL on.
        boolean noMil = packets.stream()
                               .map(DiagnosticTroubleCodePacket::getMalfunctionIndicatorLampStatus)
                               .noneMatch(mil -> mil == ON);
        if (noMil) {
            addFailure("6.6.6.2.b - No OBD ECU reported MIL on");
        }

        // 6.6.6.2.c. Fail if NACK not received from OBD ECUs that did not provide a DM23 message.
        checkForNACKsDS(packets, filterAcks(dsResults), "6.6.6.2.c");
    }

}
