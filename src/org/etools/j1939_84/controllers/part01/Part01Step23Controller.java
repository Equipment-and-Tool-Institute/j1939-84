/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part01;

import static org.etools.j1939_84.bus.j1939.packets.LampStatus.OFF;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.etools.j1939_84.bus.j1939.packets.DM31DtcToLampAssociation;
import org.etools.j1939_84.bus.j1939.packets.DTCLampStatus;
import org.etools.j1939_84.bus.j1939.packets.ParsedPacket;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.CommunicationsModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * 6.1.23 DM31: DTC to Lamp Association
 */
public class Part01Step23Controller extends StepController {

    private static final int PART_NUMBER = 1;
    private static final int STEP_NUMBER = 23;
    private static final int TOTAL_STEPS = 0;

    Part01Step23Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new EngineSpeedModule(),
             new BannerModule(),
             new VehicleInformationModule(),
             new CommunicationsModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance());
    }

    Part01Step23Controller(Executor executor,
                           EngineSpeedModule engineSpeedModule,
                           BannerModule bannerModule,
                           VehicleInformationModule vehicleInformationModule,
                           CommunicationsModule communicationsModule,
                           DateTimeModule dateTimeModule,
                           DataRepository dataRepository) {
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
        // 6.1.23.1.a. Global DM31 (send Request (PGN 59904) for PGN 41728 (SPNs 1214-1215, 4113, 4117)).
        // 6.1.23.2.a. Fail if any received ECU response does not report MIL off.
        getCommunicationsModule().requestDM31(getListener())
                                    .getPackets()
                                    .stream()
                                    .filter(Part01Step23Controller::isMilNotOff)
                                    .map(ParsedPacket::getModuleName)
                                    .forEach(moduleName -> {
                                        addFailure("6.1.23.2.a - ECU " + moduleName + " did not report MIL off");
                                    });
    }

    private static boolean isMilNotOff(DM31DtcToLampAssociation packet) {
        return packet.getDtcLampStatuses()
                     .stream()
                     .map(DTCLampStatus::getMalfunctionIndicatorLampStatus)
                     .anyMatch(lampStatus -> lampStatus != OFF);
    }
}
