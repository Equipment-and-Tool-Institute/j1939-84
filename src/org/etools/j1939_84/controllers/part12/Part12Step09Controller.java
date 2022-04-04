/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part12;

import static net.soliddesign.j1939tools.j1939.packets.AcknowledgmentPacket.Response.NACK;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.SectionA5Verifier;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

import net.soliddesign.j1939tools.bus.BusResult;
import net.soliddesign.j1939tools.j1939.Lookup;
import net.soliddesign.j1939tools.j1939.packets.DM20MonitorPerformanceRatioPacket;
import net.soliddesign.j1939tools.j1939.packets.ParsedPacket;
import net.soliddesign.j1939tools.j1939.packets.PerformanceRatio;
import net.soliddesign.j1939tools.modules.CommunicationsModule;
import net.soliddesign.j1939tools.modules.DateTimeModule;

/**
 * 6.12.9 DM20: Monitor Performance Ratio
 */
public class Part12Step09Controller extends StepController {
    private static final int PART_NUMBER = 12;
    private static final int STEP_NUMBER = 9;
    private static final int TOTAL_STEPS = 0;
    private static final Collection<Integer> CI_SPNS = Set.of(3055, // Fuel System
                                                              3058, // EGR
                                                              4364, // SCR
                                                              4792, // SCR
                                                              5308, // SCR
                                                              5318, // NOx Sensor
                                                              5321 // Boost
    );

    private static final Collection<Integer> SI_SPNS = Set.of(3050, // Catalyst
                                                              3055, // Fuel System
                                                              3056, // O2 Sensor
                                                              3058, // EGR
                                                              5318, // NOx Sensor
                                                              5321 // Boost
    );

    private final SectionA5Verifier verifier;

    Part12Step09Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule(),
             new SectionA5Verifier(PART_NUMBER, STEP_NUMBER));
    }

    Part12Step09Controller(Executor executor,
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

        // 6.12.9.1 Actions
        // c. Record responses for use in part 12 test 11 (formerly 12.9.2).
        // 6.12.9.3 Warn Criteria
        // a. Warn if any response indicates an individual numerator or denominator that is greater than the
        // corresponding values received in Part 11 test 8.
        // b. Warn if any ECU response shows:
        // d. If more than one ECU reports DM20 data, warn if general denominators or ignition cycle counts do not match
        // from all ECUs.

        verifier.setJ1939(getJ1939());
        // 6.12.9.1 Actions
        // a. DS DM20 [send Request (PG 59904) for PG 49664 (SPs 3048-3049, 3066-3068)] to ECUs that reported DM20 data
        // earlier in Part 11.
        List<Integer> addresses = getDataRepository().getObdModuleAddresses()
                                                     .stream()
                                                     .filter(a -> getPart11DM20(a) != null)
                                                     .collect(Collectors.toList());

        var dsResults = addresses.stream()
                                 .map(a -> getCommunicationsModule().requestDM20(getListener(), a))
                                 .collect(Collectors.toList());

        // 6.12.9.1 Actions
        // b. If no response [transport protocol RTS or NACK(Busy) in 220 ms], then retry DS DM20 request to the OBD
        // ECU. [Do not attempt retry for NACKs that indicate not supported].
        List<Integer> missingAddresses = determineNoResponseAddresses(dsResults, addresses);
        missingAddresses.stream()
                        .map(address -> getCommunicationsModule().requestDM20(getListener(), address))
                        .forEach(dsResults::add);

        // 6.12.9.1 Actions
        // c. Record responses for use in part 12 test 11 (formerly 12.9.2).

        // 6.12.9.2 Fail Criteria
        // a. Fail if retry was required to obtain DM20 response.
        missingAddresses.stream()
                        .map(Lookup::getAddressName)
                        .forEach(moduleName -> {
                            addFailure("6.11.8.2.a - Retry was required to obtain DM20 response from " + moduleName);
                        });

        var packets = filterPackets(dsResults);

        // 6.11.8.2.b. Fail if any response indicates that the general denominator (SPN 3049) has not incremented
        // by one from value earlier in part 9.
        // 6.12.9.2.b. Fail if any response indicates that the general denominator (SP 3049) is greater by more than 1
        // when compared to the general denominator received in Part 11 test 5.

        packets.stream()
               .filter(p -> p.getOBDConditionsCount() != getPart9GeneralDenominator(p.getSourceAddress()) + 1)
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addFailure("6.11.8.2.b - " + moduleName
                           + " response indicates the general denominator has not incremented by one from the value earlier in part 9");
               });

        // 6.12.9.2 Fail Criteria
        // c. Fail if NACK received from OBD ECUs that previously provided a DM20 message.
        checkForNACKsDS(packets, filterAcks(dsResults), "6.12.9.2.c", addresses);

        // 6.12.9.3 Warn Criteria
        // a. Warn if any response indicates an individual numerator or denominator that is greater than the
        // corresponding values received in Part 11 test 8.

        // 6.11.8.3.a. Warn if any response indicates denominator for
        // SCR, EGR, NOx sensor, boost, and fuel system
        // have not incremented by one.
        for (DM20MonitorPerformanceRatioPacket packet : packets) {
            packet.getRatios()
                  .stream()
                  .filter(PerformanceRatio::isSupported)
                  .filter(this::isRatioOfInterest)
                  .filter(r -> r.getDenominator() != getPart9RatioDenominator(r) + 1)
                  .map(PerformanceRatio::getName)
                  .forEach(ratioName -> {
                      addWarning("6.11.8.3.a - " + packet.getModuleName()
                              + " response indicates denominator for monitor " + ratioName
                              + " has not incremented by one");
                  });
        }

        // 6.12.9.3.b.i - Warn if any ECU response shows:
        // i. any monitor denominator greater than the general denominator;
        for (DM20MonitorPerformanceRatioPacket packet : packets) {
            packet.getRatios()
                  .stream()
                  .filter(PerformanceRatio::isSupported)
                  .filter(r -> r.getDenominator() > packet.getOBDConditionsCount())
                  .map(PerformanceRatio::getName)
                  .forEach(ratioName -> {
                      addWarning("6.12.9.3.b.i - " + packet.getModuleName() + " response shows denominator for monitor "
                              + ratioName + " is greater than the general denominator");
                  });
        }

        // 6.12.9.3.b.ii. Warn if any ECU response shows: general denominator greater than the ignition cycle counter
        // (SPN 3048);
        packets.stream()
               .filter(p -> p.getOBDConditionsCount() > p.getIgnitionCycles())
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addWarning("6.12.9.3.b.ii - " + moduleName
                           + " response shows general denominator greater than the ignition cycle counter");
               });

        // 6.12.9.3.b.iii. Warn if any ECU response shows: if any numerator greater than the ignition cycle counter.
        for (DM20MonitorPerformanceRatioPacket packet : packets) {
            packet.getRatios()
                  .stream()
                  .filter(PerformanceRatio::isSupported)
                  .filter(r -> r.getNumerator() > packet.getIgnitionCycles())
                  .map(PerformanceRatio::getName)
                  .forEach(ratioName -> {
                      addWarning("6.12.9.3.b.iii - " + packet.getModuleName() + " response shows numerator for monitor "
                              + ratioName + " is greater than the ignition cycle counter");
                  });
        }

        // c. Compare all values to values recorded in part 11 test 8.
        // i. Warn if any value (numerator, denominator, or ignition cycle counter) is less than their corresponding
        // value in part 11 test.

        // 6.11.8.3.c. Compare all values to values recorded in part 1.
        // 6.11.8.3.c.i. Warn if any numerator is less than their corresponding value in part 1.
        for (DM20MonitorPerformanceRatioPacket packet : packets) {
            packet.getRatios()
                  .stream()
                  .filter(PerformanceRatio::isSupported)
                  .filter(r -> r.getNumerator() < getPart1RatioNumerator(r))
                  .map(PerformanceRatio::getName)
                  .forEach(ratioName -> {
                      addWarning("6.11.8.3.c.i - " + packet.getModuleName() + " numerator for monitor "
                              + ratioName + " is less than the corresponding value in part1");
                  });
        }

        // 6.11.8.3.c.i. Warn if any denominator is less than their corresponding value in part 1.
        for (DM20MonitorPerformanceRatioPacket packet : packets) {
            packet.getRatios()
                  .stream()
                  .filter(PerformanceRatio::isSupported)
                  .filter(r -> r.getDenominator() < getPart1RatioDenominator(r))
                  .map(PerformanceRatio::getName)
                  .forEach(ratioName -> {
                      addWarning("6.11.8.3.c.i - " + packet.getModuleName() + " denominator for monitor "
                              + ratioName + " is less than the corresponding value in part1");
                  });
        }

        // 6.12.9.3 Warn Criteria

        // 6.11.8.3.c.i. Warn if any value ignition cycle counter is less than their corresponding value in part 1.
        packets.stream()
               .filter(p -> p.getIgnitionCycles() < getPart1IgnitionCycles(p.getSourceAddress()))
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addWarning("6.11.8.3.c.i - " + moduleName
                           + " ignition cycle counter is less than the corresponding value in part1");
               });

        // 6.11.8.3.d. If more than one ECU reports DM20 data, warn if general denominators do not match from all ECUs.
        var maxDem = packets.stream()
                            .mapToInt(DM20MonitorPerformanceRatioPacket::getOBDConditionsCount)
                            .max()
                            .orElse(0);
        var minDem = packets.stream()
                            .mapToInt(DM20MonitorPerformanceRatioPacket::getOBDConditionsCount)
                            .min()
                            .orElse(0);
        if (minDem != maxDem) {
            addWarning(
                       "6.11.8.3.d - More than one ECU reported DM20 data and general denominators do not match from all ECUs");
        }

        // 6.11.8.3.d. If more than one ECU reports DM20 data, warn if ignition cycle counts do not match from all ECUs.
        var maxIgn = packets.stream().mapToInt(DM20MonitorPerformanceRatioPacket::getIgnitionCycles).max().orElse(0);
        var minIgn = packets.stream().mapToInt(DM20MonitorPerformanceRatioPacket::getIgnitionCycles).min().orElse(0);
        if (minIgn != maxIgn) {
            addWarning(
                       "6.11.8.3.d - More than one ECU reported DM20 data and ignition cycle counts do not match from all ECUs");
        }
    }

    private static List<Integer>
            determineNoResponseAddresses(List<BusResult<DM20MonitorPerformanceRatioPacket>> dsResults,
                                         List<Integer> addresses) {
        var responseAddresses = filterPackets(dsResults)
                                                        .stream()
                                                        .map(ParsedPacket::getSourceAddress)
                                                        .collect(Collectors.toSet());

        var nackAddresses = filterAcks(dsResults)
                                                 .stream()
                                                 .filter(r -> r.getResponse() == NACK)
                                                 .map(ParsedPacket::getSourceAddress)
                                                 .collect(Collectors.toSet());

        List<Integer> missingAddresses = new ArrayList<>(addresses);
        missingAddresses.removeAll(responseAddresses);
        missingAddresses.removeAll(nackAddresses);
        return missingAddresses;
    }

    private DM20MonitorPerformanceRatioPacket getPart11DM20(int address) {
        return get(DM20MonitorPerformanceRatioPacket.class, address, 11);
    }

    private DM20MonitorPerformanceRatioPacket getPart1DM20(int address) {
        return get(DM20MonitorPerformanceRatioPacket.class, address, 1);
    }

    private DM20MonitorPerformanceRatioPacket getPart9DM20(int address) {
        return get(DM20MonitorPerformanceRatioPacket.class, address, 9);
    }

    private int getPart9GeneralDenominator(int address) {
        var dm20 = getPart9DM20(address);
        return dm20 == null ? -1 : dm20.getOBDConditionsCount();
    }

    private int getPart9RatioDenominator(PerformanceRatio ratio) {
        var dm20 = getPart9DM20(ratio.getSourceAddress());
        return dm20 == null ? -1 : dm20.getRatio(ratio.getId()).map(PerformanceRatio::getDenominator).orElse(-1);
    }

    private int getPart1RatioNumerator(PerformanceRatio ratio) {
        var dm20 = getPart1DM20(ratio.getSourceAddress());
        return dm20 == null ? -1 : dm20.getRatio(ratio.getId()).map(PerformanceRatio::getNumerator).orElse(-1);
    }

    private int getPart1RatioDenominator(PerformanceRatio ratio) {
        var dm20 = getPart1DM20(ratio.getSourceAddress());
        return dm20 == null ? -1 : dm20.getRatio(ratio.getId()).map(PerformanceRatio::getDenominator).orElse(-1);
    }

    private int getPart1IgnitionCycles(int address) {
        var dm20 = getPart1DM20(address);
        return dm20 == null ? -1 : dm20.getIgnitionCycles();
    }

    private boolean isRatioOfInterest(PerformanceRatio ratio) {
        if (getFuelType().isSparkIgnition()) {
            return SI_SPNS.contains(ratio.getSpn());
        } else if (getFuelType().isCompressionIgnition()) {
            return CI_SPNS.contains(ratio.getSpn());
        } else {
            return false;
        }
    }

}
