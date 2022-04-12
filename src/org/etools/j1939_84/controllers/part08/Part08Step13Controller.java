/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part08;

import static org.etools.j1939tools.j1939.packets.AcknowledgmentPacket.Response.NACK;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.SectionA5Verifier;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939tools.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;

/**
 * 6.8.13 DM3: Diagnostic Data Clear/Reset for Previously Active DTCs
 */
public class Part08Step13Controller extends StepController {
    private static final int PART_NUMBER = 8;
    private static final int STEP_NUMBER = 13;
    private static final int TOTAL_STEPS = 0;

    private final SectionA5Verifier verifier;

    Part08Step13Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule(),
             new SectionA5Verifier(PART_NUMBER, STEP_NUMBER));
    }

    Part08Step13Controller(Executor executor,
                           BannerModule bannerModule,
                           DateTimeModule dateTimeModule,
                           DataRepository dataRepository,
                           EngineSpeedModule engineSpeedModule,
                           VehicleInformationModule vehicleInformationModule,
                           CommunicationsModule communicationsModule,
                           SectionA5Verifier verifier) {
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
        this.verifier = verifier;
    }

    @Override
    protected void run() throws Throwable {
        verifier.setJ1939(getJ1939());

        // 6.8.13.1.a. DS DM3 [(send Request (PGN 59904) for PGN 65228]) to each OBD ECU.
        for (OBDModuleInformation obdInfo : getDataRepository().getObdModules()) {

            // 6.8.13.2.a. Fail if any ECU does not NACK
            boolean nacked = getCommunicationsModule().requestDM3(getListener(), obdInfo.getSourceAddress())
                                                      .stream()
                                                      .map(AcknowledgmentPacket::getResponse)
                                                      .anyMatch(r -> r == NACK);
            if (!nacked) {
                addFailure("6.8.13.2.a - " + obdInfo.getModuleName() + " did not NACK");
            }
        }

        // 6.8.13.1.b. Wait 5 seconds before checking for erased information.
        pause("Step 6.8.13.1.b - Waiting %1$d seconds before checking for erased information", 5);

        // 6.8.13.2.a. Fail if any diagnostic information erased.
        verifier.verifyDataNotErased(getListener(), "6.8.13.2.a");

        // 6.8.13.3.a. Global DM3.
        getCommunicationsModule().requestDM3(getListener());

        // 6.8.13.3.b. Wait 5 seconds before checking for erased information.
        pause("Step 6.8.13.3.b - Waiting %1$d seconds before checking for erased information", 5);

        // 6.8.13.4.a. Fail if any OBD ECU erases any diagnostic information.
        verifier.verifyDataNotErased(getListener(), "6.8.13.4.a");
    }

}
