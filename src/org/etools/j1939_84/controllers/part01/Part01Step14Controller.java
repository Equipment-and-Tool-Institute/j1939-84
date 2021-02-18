/*
  Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part01;

import static org.etools.j1939_84.modules.DiagnosticMessageModule.getCompositeSystems;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.etools.j1939_84.bus.j1939.Lookup;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939_84.bus.j1939.packets.CompositeSystem;
import org.etools.j1939_84.bus.j1939.packets.DM26TripDiagnosticReadinessPacket;
import org.etools.j1939_84.bus.j1939.packets.MonitoredSystem;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.model.RequestResult;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DiagnosticMessageModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 * <p>
 * The controller for 6.1.14 DM26: Diagnostic readiness 3
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
        RequestResult<DM26TripDiagnosticReadinessPacket> globalResponse = getDiagnosticMessageModule().requestDM26(getListener());

        if (!globalResponse.getPackets().isEmpty()) {
            List<DM26TripDiagnosticReadinessPacket> obdModulePackets = globalResponse.getPackets().stream()
                    .filter(p -> getDataRepository().isObdModule(p.getSourceAddress()))
                    .peek(p -> {
                        // 6.1.14.1.a.i. Create list by ECU address of all data and current status for use later in the test.
                        OBDModuleInformation moduleInformation = getDataRepository().getObdModule(p.getSourceAddress());
                        moduleInformation.set(p);
                        getDataRepository().putObdModule(moduleInformation);
                    })
                    .collect(Collectors.toList());

            // 6.1.14.1.b. Display monitor readiness composite value in log for OBD ECU replies only.
            getListener().onResult("");
            getListener().onResult("Vehicle Composite of DM26:");
            getListener().onResult(getCompositeSystems(obdModulePackets, false).stream()
                                           .sorted()
                                           .map(MonitoredSystem::toString)
                                           .collect(Collectors.toList()));
            getListener().onResult("");

            obdModulePackets.stream()
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
                                if (dm26System.getId() == CompositeSystem.COMPREHENSIVE_COMPONENT) {
                                    // 6.1.14.2.c. Fail if any response from an ECU indicating support for CCM monitor
                                    // in DM5 report '0=monitor disabled for rest of this cycle or not supported'
                                    // in SPN 3303 bit 3.
                                    addFailure("6.1.14.2.c - " + moduleName + " response indicates support for monitor " + systemName + " in DM5 but is reported as not enabled by DM26 response");
                                } else {
                                    // 6.1.14.2.a. Fail if any response for any monitor supported in
                                    // DM5 by a given ECU is reported as '0=monitor complete
                                    // this cycle or not supported' in SPN 3303 bits 1-4 and
                                    // SPN 3305 [except comprehensive components monitor
                                    // (CCM)].
                                    addFailure("6.1.14.2.a - " + moduleName + " response for a monitor " + systemName + " in DM5 is reported as supported and is reported as not enabled by DM26 response");
                                }
                            } else if (dm26SystemEnabled && !dm5SystemEnabled) {
                                // 6.1.14.2.b. Fail if any response for each monitor not
                                // supported in DM5 by a given ECU is not also
                                // reported in DM26 as '0=monitor complete this
                                // cycle or not supported' in SPN 3303 bits 5-7 and
                                // '0=monitor disabled for rest of this cycle or not
                                // supported' in SPN 3303 bits 1-2 and SPN 3304.20
                                addFailure(
                                        "6.1.14.2.b - " + moduleName + " response for a monitor " + systemName + " in DM5 is reported as not supported and is reported as enabled by DM26 response");
                            }
                        }
                    });

            // 6.1.14.2.d. Fail if any response indicates number of warm-ups since code clear (WU-SCC) (SPN 3302) is not zero.
            globalResponse.getPackets().stream()
                    .filter(packet -> packet.getWarmUpsSinceClear() != 0)
                    .map(packet -> Lookup.getAddressName(packet.getSourceAddress()))
                    .forEach(moduleName -> addFailure("6.1.14.2.d - " + moduleName + " response indicates number of warm-ups since code clear is not zero"));

            // 6.1.14.2.e. Fail if any response indicates time since engine start (SPN 3301) is not zero.
            globalResponse.getPackets().stream()
                    .filter(packet -> packet.getTimeSinceEngineStart() != 0)
                    .map(packet -> Lookup.getAddressName(packet.getSourceAddress()))
                    .forEach(moduleName -> addFailure("6.1.14.2.e - " + moduleName + " response indicates time since engine start is not zero"));

            // 6.1.14.3.a. Warn if any individual required monitor, except Continuous
            // Component Monitoring (CCM) is supported by more than one OBD ECU.
            // Get the list of duplicate composite systems
            reportDuplicateCompositeSystems(globalResponse.getPackets(), "6.1.14.3.a");

            // 6.1.14.4.a. DS DM26 to each OBD ECU.
            List<Integer> obdModuleAddresses = getDataRepository().getObdModuleAddresses();
            List<DM26TripDiagnosticReadinessPacket> destinationSpecificPackets = new ArrayList<>();
            List<AcknowledgmentPacket> dsAcks = new ArrayList<>();
            obdModuleAddresses.forEach(address -> {
                RequestResult<DM26TripDiagnosticReadinessPacket> result = getDiagnosticMessageModule().requestDM26(getListener(), address);
                destinationSpecificPackets.addAll(result.getPackets());
                dsAcks.addAll(result.getAcks());
            });

            // 6.1.14.5.a. Fail if any difference compared to data received during global request.
            compareRequestPackets(globalResponse.getPackets(), destinationSpecificPackets, "6.1.14.5.a");

            // 6.1.14.5.b. Fail if NACK not received from OBD ECUs that did not respond to global query.
            checkForNACKs(globalResponse.getPackets(), dsAcks, obdModuleAddresses, "6.1.14.5.b");

        } else {
            // 6.1.14.2.f. Fail if no OBD ECU provides DM26.
            addFailure("6.1.14.2.f - No OBD ECU provided DM26");
        }
    }

    private MonitoredSystem getDM5System(CompositeSystem systemId, int address) {
        return getDataRepository().getObdModule(address)
                .getMonitoredSystems()
                .stream()
                .filter(s -> s.getId() == systemId)
                .findFirst()
                .orElse(null);
    }
}
