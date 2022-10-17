/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part07;

import static org.etools.j1939tools.j1939.packets.AcknowledgmentPacket.Response.BUSY;
import static org.etools.j1939tools.j1939.packets.AcknowledgmentPacket.Response.DENIED;
import static org.etools.j1939tools.j1939.packets.AcknowledgmentPacket.Response.NACK;

import java.util.ArrayList;
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
import org.etools.j1939tools.j1939.Lookup;
import org.etools.j1939tools.j1939.packets.ParsedPacket;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;

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
             new CommunicationsModule(),
             new SectionA5Verifier(PART_NUMBER, STEP_NUMBER));
    }

    Part07Step16Controller(Executor executor,
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

        // 6.7.16.1.a. Global DM3 [(send Request (PGN 59904) for PGN 65228]).
        getCommunicationsModule().requestDM3(getListener());

        // 6.7.16.1.b. Wait 5 seconds before checking for erased information.
        pause("Step 6.7.16.1.b - Waiting %1$d seconds before checking for erased information", 5L);

        // 6.7.16.2.a. Fail if any OBD ECU erases any diagnostic information as discussed in Section A.5.
        verifier.verifyDataNotErased(getListener(), "6.7.16.2.a");

        // 6.7.16.3.a. DS DM3 to each OBD ECU.
        var dsPackets = getDataRepository().getObdModuleAddresses()
                                           .stream()
                                           .map(a -> getCommunicationsModule().requestDM3(getListener(), a))
                                           .flatMap(Collection::stream)
                                           .collect(Collectors.toList());

        // 6.7.16.3.b. Wait 5 seconds before checking for erased information.
        pause("Step 6.7.16.3.b - Waiting %1$d seconds before checking for erased information", 5L);

        // 6.7.16.4.a. Fail if any ECU does not NACK with control byte = 1 or 2 or 3.
        List<Integer> addresses1 = new ArrayList<>(getDataRepository().getObdModuleAddresses());
        dsPackets.stream()
                 .filter(a2 -> a2.getResponse() == NACK || a2.getResponse() == BUSY || a2.getResponse() == DENIED)
                 .map(ParsedPacket::getSourceAddress)
                 .forEach(addresses1::remove);

        addresses1.stream()
                  .distinct()
                  .sorted()
                  .map(Lookup::getAddressName)
                  .map(moduleName1 -> "6.7.16.4.a" + " - OBD ECU " + moduleName1
                          + " did not provide a NACK for the DS query")
                  .forEach(this::addFailure);

        // 6.7.16.4.b. Fail if any OBD ECU erases any diagnostic information. See Section A.5 for more information.
        verifier.verifyDataNotErased(getListener(), "6.7.16.4.b");

        // 6.7.2.16.4.c Warn if any OBD ECU NACKs with control byte = 3
        dsPackets.stream()
                 .filter(a1 -> a1.getResponse() == BUSY)
                 .map(ParsedPacket::getSourceAddress)
                 .distinct()
                 .sorted()
                 .map(Lookup::getAddressName)
                 .map(moduleName -> "6.7.16.4.c" + " - OBD ECU " + moduleName
                         + " did provide a NACK with control byte = 3 for the DS query")
                 .forEach(this::addWarning);
    }
}
