/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part01;

import static org.etools.j1939_84.modules.DiagnosticMessageModule.getCompositeSystems;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.etools.j1939_84.bus.j1939.Lookup;
import org.etools.j1939_84.bus.j1939.packets.CompositeSystem;
import org.etools.j1939_84.bus.j1939.packets.DM5DiagnosticReadinessPacket;
import org.etools.j1939_84.bus.j1939.packets.MonitoredSystem;
import org.etools.j1939_84.bus.j1939.packets.ParsedPacket;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticMessageModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * 6.1.14 DM26: Diagnostic readiness 3
 */

public class Part01Step14Controller extends StepController {

    private static final int PART_NUMBER = 1;
    private static final int STEP_NUMBER = 14;
    private static final int TOTAL_STEPS = 0;

    Part01Step14Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(),
             new EngineSpeedModule(),
             new BannerModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule(),
             dataRepository,
             DateTimeModule.getInstance());
    }

    Part01Step14Controller(Executor executor,
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

        // 6.1.14.1.a. Global DM26 (send Request (PGN 59904) for PGN 64952 (SPNs 3301-3305)).
        var globalPackets = getDiagnosticMessageModule().requestDM26(getListener())
                                                        .getPackets()
                                                        .stream()
                                                        .filter(p -> isObdModule(p.getSourceAddress()))
                                                        .collect(Collectors.toList());

        // 6.1.14.2.e. Fail if no OBD ECU provides DM26.
        if (globalPackets.isEmpty()) {
            addFailure("6.1.14.2.e - No OBD ECU provided DM26");
        } else {
            // 6.1.14.1.a.i. Create list by ECU address of all data and current status for use later in the test.
            globalPackets.forEach(this::save);

            // 6.1.14.1.b. Display monitor readiness composite value in log for OBD ECU replies only.
            getListener().onResult("");
            getListener().onResult("Vehicle Composite of DM26:");
            getCompositeSystems(globalPackets, false).stream()
                                                     .sorted()
                                                     .map(MonitoredSystem::toString)
                                                     .forEach(s -> getListener().onResult(s));
            getListener().onResult("");
        }

        globalPackets.stream()
                     .flatMap(p -> p.getMonitoredSystems().stream())
                     .sorted()
                     .forEach(dm26System -> {
                         int address = dm26System.getSourceAddress();
                         String moduleName = Lookup.getAddressName(address);
                         String systemName = dm26System.getName().trim();
                         boolean dm26SystemEnabled = dm26System.getStatus().isEnabled();

                         MonitoredSystem dm5System = getDM5System(dm26System.getId(), address);

                         if (dm5System != null) {
                             boolean dm5SystemEnabled = dm5System.getStatus().isEnabled();
                             if (!dm26SystemEnabled && dm5SystemEnabled) {
                                 if (dm26System.getId() != CompositeSystem.COMPREHENSIVE_COMPONENT) {
                                     // 6.1.14.2.a. Fail if any response for any monitor supported in
                                     // DM5 by a given ECU is reported as '0=monitor complete
                                     // this cycle or not supported' in SPN 3303 bits 1-4 and
                                     // SPN 3305 [except comprehensive components monitor
                                     // (CCM)].
                                     addFailure("6.1.14.2.a - " + moduleName + " response for a monitor "
                                             + systemName
                                             + " in DM5 is reported as supported and is reported as not enabled by DM26 response");
                                 }
                             } else if (dm26SystemEnabled && !dm5SystemEnabled) {
                                 // 6.1.14.2.b. Fail if any response for each monitor not
                                 // supported in DM5 by a given ECU is not also
                                 // reported in DM26 as '0=monitor complete this
                                 // cycle or not supported' in SPN 3303 bits 5-7 and
                                 // '0=monitor disabled for rest of this cycle or not
                                 // supported' in SPN 3303 bits 1-2 and SPN 3304.
                                 addFailure("6.1.14.2.b - " + moduleName + " response for a monitor "
                                                    + systemName
                                                    + " in DM5 is reported as not supported and is reported as enabled by DM26 response");
                             }
                         }
                     });

        // 6.1.14.2.c. Fail if any response indicates number of warm-ups since code clear (SPN 3302) is not zero.
        globalPackets.stream()
                     .filter(packet -> packet.getWarmUpsSinceClear() != 0)
                     .map(ParsedPacket::getModuleName)
                     .forEach(moduleName -> {
                         addFailure("6.1.14.2.c - " + moduleName
                                 + " response indicates number of warm-ups since code clear is not zero");
                     });

        // 6.1.14.2.d. Fail if any response indicates time since engine start (SPN 3301) is not zero.
        globalPackets.stream()
                     .filter(packet -> packet.getTimeSinceEngineStart() != 0)
                     .map(ParsedPacket::getModuleName)
                     .forEach(moduleName -> {
                         addFailure("6.1.14.2.d - " + moduleName
                                 + " response indicates time since engine start is not zero");
                     });

        // 6.1.14.3.a. Warn if any individual required monitor, except Continuous
        // Component Monitoring (CCM) is supported by more than one OBD ECU.
        // Get the list of duplicate composite systems
        reportDuplicateCompositeSystems(globalPackets, "6.1.14.3.a");

        // 6.1.14.4.a. DS DM26 to each OBD ECU.
        var dsResults = getDataRepository().getObdModuleAddresses()
                                           .stream()
                                           .map(a -> getDiagnosticMessageModule().requestDM26(getListener(), a))
                                           .collect(Collectors.toList());

        // 6.1.14.5.a. Fail if any difference compared to data received during global request.
        compareRequestPackets(globalPackets, filterRequestResultPackets(dsResults), "6.1.14.5.a");

        // 6.1.14.5.b. Fail if NACK not received from OBD ECUs that did not respond to global query.
        checkForNACKsGlobal(globalPackets, filterRequestResultAcks(dsResults), "6.1.14.5.b");
    }

    private MonitoredSystem getDM5System(CompositeSystem systemId, int address) {
        var dm5 = get(DM5DiagnosticReadinessPacket.class, address, 1);
        return dm5 == null ? null
                : dm5.getMonitoredSystems()
                     .stream()
                     .filter(s -> s.getId() == systemId)
                     .findFirst()
                     .orElse(null);
    }
}
