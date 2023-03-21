/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part09;

import java.util.Optional;
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
import org.etools.j1939tools.j1939.packets.AcknowledgmentPacket.Response;
import org.etools.j1939tools.j1939.packets.DM11ClearActiveDTCsPacket;
import org.etools.j1939tools.j1939.packets.DM12MILOnEmissionDTCPacket;
import org.etools.j1939tools.j1939.packets.LampStatus;
import org.etools.j1939tools.j1939.packets.ParsedPacket;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;

/**
 * 6.9.8 DM11: Diagnostic Data Clear/Reset for Active DTCs
 */
public class Part09Step08Controller extends StepController {
    private static final int PART_NUMBER = 9;
    private static final int STEP_NUMBER = 8;
    private static final int TOTAL_STEPS = 0;

    private final SectionA5Verifier verifier;

    Part09Step08Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule(),
             new SectionA5Verifier(PART_NUMBER, STEP_NUMBER));
    }

    Part09Step08Controller(Executor executor,
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

        // 6.9.8.1.a DS DM11 [(send Request (PGN 59904) for PGN 65235]) to each OBD ECU without DM12 active MIL on DTC,
        // based on the list created in step 6.9.2.1.
        getDataRepository().getObdModules()
                           .stream()
                           .map(OBDModuleInformation::getSourceAddress)
                           .filter(a -> Optional.ofNullable(get(DM12MILOnEmissionDTCPacket.class,
                                                                a,
                                                                9))
                                                .map(p -> !p.getMalfunctionIndicatorLampStatus()
                                                            .isActive())
                                                .orElse(true))
                           .forEach(a -> {
                               getCommunicationsModule().requestDM11(getListener(), a);
                           });

        // 6.9.8.1.b Wait 5 seconds before checking for erased data.
        pause("Step 6.9.8.1.b - Waiting %1$d seconds before checking for erased data", 5);

        // 6.9.8.2.a Fail if any ECU partially erases diagnostic information (pass if it erases either all or none).
        // 6.9.8.2.b Fail if one or more than one ECU erases diagnostic information and one or more other ECUs do not
        // erase diagnostic information. See Section A.5.
        verifier.verifyDataNotPartialErased(getListener(), "6.9.8.2.a", "6.9.8.2.b", false);

        // 6.9.8.3.a DS DM11 to each OBD ECU with DM12 active MIL on DTC, based on the list created in step 6.9.2.1.
        getDataRepository().getObdModules()
                           .stream()
                           .map(OBDModuleInformation::getSourceAddress)
                           .filter(a -> Optional.ofNullable(get(DM12MILOnEmissionDTCPacket.class,
                                                                a,
                                                                8))
                                                .map(p -> p.getMalfunctionIndicatorLampStatus()
                                                           .isActive())
                                                .orElse(false))
                           .flatMap(a -> {
                               return getCommunicationsModule().requestDM11(getListener(), a).stream();
                           })
                           .filter(a -> {
                               return a.getResponse() == Response.ACK;
                           })
                           .forEach(a -> {
                               // 6.9.8.4.a Fail if ACK received in response OBD ECU to DS DM11 query.
                               addFailure("6.9.8.4.a - ACK received from " + a.getModuleName()
                                       + " in response OBD ECU to DS DM11 query");
                           });

        // 6.9.8.3.b Wait 5 seconds before checking for erased data.
        pause("Step 6.9.8.3.b - Waiting %1$d seconds before checking for erased data", 5);

        // 6.9.8.4.b Fail if any ECU partially erases diagnostic information (pass if it erases either all or none).
        // 6.9.8.4.c For systems with multiple ECU’s, fail if one ECU or more than one ECU erases diagnostic information
        // and one or more other ECUs do not erase diagnostic information.
        verifier.verifyDataNotPartialErased(getListener(), "6.9.8.4.b", "6.9.8.4.c", false);

        // 6.9.8.5.a Global DM11 ([send Request (PGN 59904) for PGN 65235]).
        var packets = getCommunicationsModule().requestDM11(getListener());

        // 6.9.8.5.b Wait 5 seconds before checking for erased data.
        pause("Step 6.9.8.5.b - Waiting %1$d seconds before checking for erased data", 5);

        // 6.9.8.6.a Fail if any OBD ECU provides a NACK to the global DM11 request.
        packets.stream()
               .map(p -> new DM11ClearActiveDTCsPacket(p.getPacket()))
               .filter(p -> isObdModule(p.getSourceAddress()))
               .filter(p -> p.getResponse() == AcknowledgmentPacket.Response.NACK)
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addFailure("6.9.8.6.a - " + moduleName + " provided a NACK to the global DM11 request");
               });

        // 6.9.8.6.b Warn if any OBD ECU provides an ACK to the global DM11 request.
        packets.stream()
               .map(p -> new DM11ClearActiveDTCsPacket(p.getPacket()))
               .filter(p -> isObdModule(p.getSourceAddress()))
               .filter(p -> p.getResponse() == AcknowledgmentPacket.Response.ACK)
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addInfo("6.9.8.6.b - " + moduleName + " provided an ACK to the global DM11 request");
               });

        // 6.9.8.6.c Fail if any diagnostic information was not erased from any OBD ECUs.
        verifier.verifyDataErased(getListener(), "6.9.8.6.c");
    }

}
