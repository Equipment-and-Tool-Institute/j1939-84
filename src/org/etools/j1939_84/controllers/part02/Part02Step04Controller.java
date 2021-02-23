/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part02;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.etools.j1939_84.bus.j1939.BusResult;
import org.etools.j1939_84.bus.j1939.Lookup;
import org.etools.j1939_84.bus.j1939.packets.DM20MonitorPerformanceRatioPacket;
import org.etools.j1939_84.bus.j1939.packets.PerformanceRatio;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticMessageModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * @author Matt Gumbel (matt@soliddesign.net)
 *         <p>
 *         The controller for DM20: Monitor performance ratio
 */
public class Part02Step04Controller extends StepController {

    private static final int PART_NUMBER = 2;
    private static final int STEP_NUMBER = 4;
    private static final int TOTAL_STEPS = 0;

    Part02Step04Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(),
             new EngineSpeedModule(),
             new BannerModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule(),
             dataRepository,
             DateTimeModule.getInstance());
    }

    Part02Step04Controller(Executor executor,
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
        // 6.2.4.1.a. Global DM20 (send Request (PGN 59904) for PGN 49664 (SPNs 3048-3049, 3066-3068)).
        var globalResult = getDiagnosticMessageModule().requestDM20(getListener());
        List<DM20MonitorPerformanceRatioPacket> globalPackets = globalResult.getPackets();

        globalPackets.forEach(packet -> {
            int moduleAddress = packet.getSourceAddress();
            String moduleName = Lookup.getAddressName(moduleAddress);

            OBDModuleInformation obdModuleInformation = getDataRepository().getObdModule(moduleAddress);
            if (obdModuleInformation != null) {

                // 6.2.4.2.a. Fail if any ECU reports different SPNs as supported for data than in part 1.
                var part1Ratios = obdModuleInformation.getPerformanceRatios();
                var part2Ratios = packet.getRatios();

                var part1SPNs = part1Ratios.stream()
                                           .sorted()
                                           .map(PerformanceRatio::getSpn)
                                           .collect(Collectors.toList());
                var part2SPNs = part2Ratios.stream()
                                           .sorted()
                                           .map(PerformanceRatio::getSpn)
                                           .collect(Collectors.toList());
                if (!part1SPNs.equals(part2SPNs)) {
                    addFailure("6.2.4.2.a - ECU " + moduleName
                            + " reported different SPNs as supported for data than in part 1");
                }

                // 6.2.4.2.b. Fail if any denominator does not match denominator recorded in part 1.
                var part1Dems = part1Ratios.stream()
                                           .sorted()
                                           .map(PerformanceRatio::getDenominator)
                                           .collect(Collectors.toList());
                var part2Dems = part2Ratios.stream()
                                           .sorted()
                                           .map(PerformanceRatio::getDenominator)
                                           .collect(Collectors.toList());
                if (!part1Dems.equals(part2Dems)) {
                    addFailure("6.2.4.2.b - ECU " + moduleName
                            + " reported a denominator that does not match denominator recorded in part 1");
                }

                // 6.2.4.2.c. Fail if any ECU does not report a value for ignition cycle that is one cycle greater
                // than the value reported by that ECU in part 1.
                int part1Ign = obdModuleInformation.getIgnitionCycleCounterValue();
                int expectedIgn = part1Ign + 1;
                int part2Ign = packet.getIgnitionCycles();

                if (part2Ign != expectedIgn) {
                    String message = "6.2.4.2.a - ECU " + moduleName + " reported ignition cycle is invalid.  " +
                            "Expected " + expectedIgn + " but was " + part2Ign;
                    addFailure(message);
                }
            } else {
                getLogger().log(Level.INFO, "Unable to find module for " + moduleAddress);
            }
        });

        // 6.2.4.3.a. DS DM20 to ECUs that responded to global DM20 in part 1.
        List<BusResult<DM20MonitorPerformanceRatioPacket>> dsResults = getDataRepository().getObdModuleAddresses()
                                                                                          .stream()
                                                                                          .sorted()
                                                                                          .map(address -> getDiagnosticMessageModule().requestDM20(getListener(),
                                                                                                                                                   address))
                                                                                          .collect(Collectors.toList());

        // 6.2.4.4.a. Fail if any difference compared to data received during global request in 6.2.4.1.
        compareRequestPackets(globalPackets, filterPackets(dsResults), "6.2.4.4.a");

        // 6.2.4.4.b. Fail if NACK not received from OBD ECUs that did not respond to global query
        checkForNACKs(globalPackets,
                      filterAcks(dsResults),
                      "6.2.4.4.b");

    }
}
