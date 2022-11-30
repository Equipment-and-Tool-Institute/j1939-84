/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part12;

import static org.etools.j1939tools.j1939.packets.AcknowledgmentPacket.Response.ACK;
import static org.etools.j1939tools.j1939.packets.AcknowledgmentPacket.Response.NACK;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.SectionA5Verifier;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939tools.j1939.packets.DM11ClearActiveDTCsPacket;
import org.etools.j1939tools.j1939.packets.ParsedPacket;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;

/**
 * 6.12.10 DM11: Diagnostic Data Clear/Reset for Active DTCs
 */
public class Part12Step10Controller extends StepController {
    private static final int PART_NUMBER = 12;
    private static final int STEP_NUMBER = 10;
    private static final int TOTAL_STEPS = 0;
    private final SectionA5Verifier verifier;

    Part12Step10Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule(),
             new SectionA5Verifier(true, PART_NUMBER, STEP_NUMBER));
    }

    Part12Step10Controller(Executor executor,
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
        // 6.12.10.1.a. DS DM11 [send Request (PGN 59904) for PGN 65235] to each OBD ECU.
        var dsPackets = getDataRepository().getObdModuleAddresses()
                                           .stream()
                                           .map(a -> getCommunicationsModule().requestDM11(getListener(), a))
                                           .flatMap(Collection::stream)
                                           .collect(Collectors.toList());

        // 6.12.10.1.b. Wait 5 seconds before checking for erased data.
        pause("Step 6.12.10.1.b - Waiting %1$d seconds before checking for erased data", 5);

        // 6.12.10.2.a. Fail if any OBD ECU does not respond with a NACK.
        checkForNACKsDS(List.of(), dsPackets, "6.12.10.2.a");

        // 6.12.10.2.b. Check diagnostic information as described in Section A.5 and fail if any ECU partially erases
        // diagnostic information (pass if it erases either all or none).
        // 6.12.10.2.c. For systems with multiple ECUs, fail if one OBD ECU or more than one OBD ECU erases diagnostic
        // information and one or more other OBD ECUs do not erase diagnostic information.
        verifier.verifyDataNotPartialErased(getListener(), "6.12.10.2.b", "6.12.10.2.c", false);

        // 6.12.10.3.a. Global DM11.
        var globalPackets = getCommunicationsModule().requestDM11(getListener());

        var obdPackets = globalPackets.stream()
                                      .filter(p -> isObdModule(p.getSourceAddress()))
                                      .collect(Collectors.toList());

        // 6.12.10.3.b. Wait 5 seconds before checking for erased data.
        pause("Step 6.12.10.3.b - Waiting %1$d seconds before checking for erased data", 5);

        // 6.12.10.4.a. Fail if any OBD ECU responds with a NACK.
        obdPackets.stream()
                  .map(p -> new DM11ClearActiveDTCsPacket(p.getPacket()))
                  .filter(p -> p.getResponse() == NACK)
                  .map(ParsedPacket::getModuleName)
                  .forEach(moduleName -> {
                      addFailure("6.12.10.4.a - " + moduleName + " responded with a NACK");
                  });

        // 6.12.10.4.b. Info if any OBD ECU responds with an ACK.
        obdPackets.stream()
                  .map(p -> new DM11ClearActiveDTCsPacket(p.getPacket()))
                  .filter(p -> p.getResponse() == ACK)
                  .map(ParsedPacket::getModuleName)
                  .forEach(moduleName -> {
                      addInfo("6.12.10.4.b - " + moduleName + " responded with a ACK");
                  });

        // 6.12.10.4.c. Check diagnostic information and fail if any ECU partially erases diagnostic information
        // (pass if it erases either all or none).
        verifier.verifyDataNotPartialErased(getListener(), "6.12.10.4.c", "6.12.10.4.c", true);
    }

}
