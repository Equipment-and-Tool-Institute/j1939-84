/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part02;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939tools.j1939.packets.DM20MonitorPerformanceRatioPacket;
import org.etools.j1939tools.j1939.packets.ParsedPacket;
import org.etools.j1939tools.j1939.packets.PerformanceRatio;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;

/**
 * 6.2.4 DM20: Monitor performance ratio
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
             new CommunicationsModule(),
             dataRepository,
             DateTimeModule.getInstance());
    }

    Part02Step04Controller(Executor executor,
                           EngineSpeedModule engineSpeedModule,
                           BannerModule bannerModule,
                           VehicleInformationModule vehicleInformationModule,
                           CommunicationsModule communicationsModule,
                           DataRepository dataRepository,
                           DateTimeModule dateTimeModule) {
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
        // 6.2.4.1.a. Global DM20 (send Request (PGN 59904) for PGN 49664 (SPNs 3048-3049, 3066-3068)).
        var globalPackets = getCommunicationsModule().requestDM20(getListener()).getPackets();

        // 6.2.4.2.a. Fail if any ECU reports different SPNs as supported for data than in part 1.
        globalPackets.stream()
                     .filter(p -> isObdModule(p.getSourceAddress()))
                     .filter(packet -> {
                         var part1SPNs = collectSPNs(getPart1Ratios(packet.getSourceAddress()));
                         var part2SPNs = collectSPNs(packet.getRatios());
                         return !part1SPNs.equals(part2SPNs);
                     })
                     .map(ParsedPacket::getModuleName)
                     .forEach(moduleName -> {
                         addFailure("6.2.4.2.a - ECU " + moduleName
                                 + " reported different SPNs as supported for data than in part 1");
                     });

        // 6.2.4.2.b. Fail if any denominator does not match denominator recorded in part 1.
        globalPackets.stream()
                     .filter(p -> isObdModule(p.getSourceAddress()))
                     .filter(packet -> {
                         var part1Dems = collectDenominators(getPart1Ratios(packet.getSourceAddress()));
                         var part2Dems = collectDenominators(packet.getRatios());
                         return !part1Dems.equals(part2Dems);
                     })
                     .map(ParsedPacket::getModuleName)
                     .forEach(moduleName -> {
                         addFailure("6.2.4.2.b - ECU " + moduleName
                                 + " reported a denominator that does not match denominator recorded in part 1");
                     });

        // 6.2.4.2.c. Fail if any ECU does not report a value for ignition cycle that is one cycle greater
        // than the value reported by that ECU in part 1.
        globalPackets.stream()
                     .filter(p -> isObdModule(p.getSourceAddress()))
                     .filter(p -> p.getIgnitionCycles() != getPart1IgnitionCycles(p.getSourceAddress()) + 1)
                     .map(ParsedPacket::getModuleName)
                     .forEach(moduleName -> {
                         addFailure("6.2.4.2.c - " + moduleName
                                 + " reported value for ignition cycle is not one cycle greater than the value reported in part 1");
                     });

        // 6.2.4.3.a. DS DM20 to ECUs that responded to global DM20 in part 1.
        var dsResults = getDataRepository().getObdModuleAddresses()
                                           .stream()
                                           .map(a -> getCommunicationsModule().requestDM20(getListener(), a))
                                           .collect(toList());

        // 6.2.4.4.a. Fail if any difference compared to data received during global request in 6.2.4.1.
        compareRequestPackets(globalPackets, filterPackets(dsResults), "6.2.4.4.a");

        // 6.2.4.4.b. Fail if NACK not received from OBD ECUs that did not respond to global query
        checkForNACKsGlobal(globalPackets, filterAcks(dsResults), "6.2.4.4.b");

    }

    private static List<Integer> collectDenominators(List<PerformanceRatio> ratios) {
        return ratios.stream().map(PerformanceRatio::getDenominator).sorted().collect(toList());
    }

    private static List<Integer> collectSPNs(List<PerformanceRatio> ratios) {
        return ratios.stream().map(PerformanceRatio::getSpn).sorted().collect(toList());
    }

    private int getPart1IgnitionCycles(int address) {
        var dm20 = getPart1DM20(address);
        return dm20 == null ? -1 : dm20.getIgnitionCycles();
    }

    private List<PerformanceRatio> getPart1Ratios(int address) {
        var dm20 = getPart1DM20(address);
        return dm20 == null ? List.of() : dm20.getRatios();
    }

    private DM20MonitorPerformanceRatioPacket getPart1DM20(int address) {
        return get(DM20MonitorPerformanceRatioPacket.class, address, 1);
    }
}
