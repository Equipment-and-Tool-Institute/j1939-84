/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part04;

import static org.etools.j1939tools.j1939.packets.AcknowledgmentPacket.Response.BUSY;
import static org.etools.j1939tools.j1939.packets.AcknowledgmentPacket.Response.DENIED;
import static org.etools.j1939tools.j1939.packets.AcknowledgmentPacket.Response.NACK;

import java.util.Collection;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.SectionA5Verifier;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939tools.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939tools.j1939.packets.ParsedPacket;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;

/**
 * 6.4.13 DM3: Diagnostic Data Clear/Reset for Previously Active DTCs
 */
public class Part04Step13Controller extends StepController {
    private static final int PART_NUMBER = 4;
    private static final int STEP_NUMBER = 13;
    private static final int TOTAL_STEPS = 0;

    private final SectionA5Verifier verifier;

    Part04Step13Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule(),
             new SectionA5Verifier(false, PART_NUMBER, STEP_NUMBER));
    }

    Part04Step13Controller(Executor executor,
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

        // 6.4.13.1.a. DS DM3 [(send Request (PGN 59904) for PGN 65228)] to each OBD ECU
        var dsPackets = getDataRepository().getObdModules()
                                           .stream()
                                           .map(OBDModuleInformation::getSourceAddress)
                                           .map(a -> getCommunicationsModule().requestDM3(getListener(), a))
                                           .flatMap(Collection::stream)
                                           .collect(Collectors.toList());

        // 6.4.13.1.b. Wait 5 seconds before checking for erased information.
        pause("Step 6.4.13.1.b - Waiting %1$d seconds before checking for erased information", 5L);

        // 6.4.13.2.a. Fail if any OBD ECU does not NACK with control byte = 1 or 2 or 3,
        // 1 - Negative Acknowledgment (NACK)
        // 2 - Access Denied
        // 3 - Cannot Respond
        dsPackets.stream()
                 .filter(p -> {
                     AcknowledgmentPacket.Response r = p.getResponse();
                     return r != NACK && r != DENIED && r != BUSY;
                 })
                 .map(ParsedPacket::getModuleName)
                 .forEach(moduleName -> {
                     addFailure("6.4.13.2.a - " + moduleName
                             + " did not NACK with control byte 1 or 2 or 3");
                 });

        // 6.4.13.2.b. Warn if any OBD ECU NACKs with control byte = 3.
        dsPackets.stream()
                 .filter(p -> p.getResponse() == BUSY)
                 .map(ParsedPacket::getModuleName)
                 .forEach(moduleName -> {
                     addWarning("6.4.13.2.b - " + moduleName + " NACKs with control = 3");
                 });

        // 6.4.13.2.c. Fail if any ECU erases any diagnostic information. See Section A.5 for more information.
        verifier.verifyDataNotErased(getListener(), "6.4.13.2.c");

        // 6.4.13.3.a. Global DM3
        getCommunicationsModule().requestDM3(getListener());

        // 6.4.13.3.b. Wait 5 seconds before checking for erased information.
        pause("Step 6.4.13.3.b - Waiting %1$d seconds before checking for erased information", 5L);

        // 6.4.13.4.a. Fail if any OBD ECU erases OBD diagnostic information.
        verifier.verifyDataNotErased(getListener(), "6.4.13.4.a");
    }

}
