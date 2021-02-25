/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part07;

import static org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket.Response.NACK;

import java.util.Collection;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.controllers.part01.SectionA5Verifier;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticMessageModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * 6.7.16 DM3: Diagnostic Data Clear/Reset for Previously Active DTCs
 */
public class Part07Step16Controller extends StepController {
    private static final int PART_NUMBER = 7;
    private static final int STEP_NUMBER = 16;
    private static final int TOTAL_STEPS = 0;

    private final SectionA5Verifier verifier;

    Part07Step16Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule(),
             new SectionA5Verifier(PART_NUMBER, STEP_NUMBER));
    }

    Part07Step16Controller(Executor executor,
                           BannerModule bannerModule,
                           DateTimeModule dateTimeModule,
                           DataRepository dataRepository,
                           EngineSpeedModule engineSpeedModule,
                           VehicleInformationModule vehicleInformationModule,
                           DiagnosticMessageModule diagnosticMessageModule,
                           SectionA5Verifier verifier) {
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
        this.verifier = verifier;
    }

    @Override
    protected void run() throws Throwable {
        verifier.setJ1939(getJ1939());

        // 6.7.16.1.a. Global DM3 [(send Request (PGN 59904) for PGN 65228]).
        getDiagnosticMessageModule().requestDM3(getListener());

        // 6.7.16.1.b. Wait 5 seconds before checking for erased information.
        pause("Step 6.7.16.1.b Waiting %1$d seconds", 5L);

        // 6.7.16.2.a. Fail if any OBD ECU erases any diagnostic information as discussed in Section A.5.
        verifier.verifyDataNotErased(getListener(), "6.7.16.2.a");

        // 6.7.16.3.a. DS DM3 to each OBD ECU.
        var dsPackets = getDataRepository().getObdModules()
                                           .stream()
                                           .map(OBDModuleInformation::getSourceAddress)
                                           .map(a -> getDiagnosticMessageModule().requestDM3(getListener(), a))
                                           .flatMap(Collection::stream)
                                           .collect(Collectors.toList());

        // 6.7.16.3.b. Wait 5 seconds before checking for erased information.
        pause("Step 6.7.16.3.b Waiting %1$d seconds", 5L);

        // 6.7.16.4.a. Fail if any ECU does not NACK, or if any OBD ECU erases any diagnostic information. See Section
        // A.5 for more information.
        dsPackets.stream()
                 .peek(acknowledgmentPacket -> verifier.verifyDataNotErased(getListener(), "6.7.16.4.a"))
                 .filter(p -> {
                     AcknowledgmentPacket.Response r = p.getResponse();
                     return r != NACK;
                 })
                 .forEach(packet -> {
                     AcknowledgmentPacket.Response response = packet.getResponse();
                     if (response != NACK) {
                         addFailure("6.7.16.4.a - " + packet.getModuleName()
                                 + " did not NACK the DS DM3 request");
                     }
                 });
    }
}
