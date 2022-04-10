/*
 * Copyright (c) 2022 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part12;

import static org.etools.j1939tools.j1939.packets.AcknowledgmentPacket.Response.NACK;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939tools.bus.BusResult;
import org.etools.j1939tools.j1939.Lookup;
import org.etools.j1939tools.j1939.packets.DM20MonitorPerformanceRatioPacket;
import org.etools.j1939tools.j1939.packets.ParsedPacket;
import org.etools.j1939tools.j1939.packets.PerformanceRatio;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;

/**
 * 6.12.9 DM20: Monitor Performance Ratio
 */
public class Part12Step09Controller extends StepController {
    private static final int PART_NUMBER = 12;
    private static final int STEP_NUMBER = 9;
    private static final int TOTAL_STEPS = 0;

    Part12Step09Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule());
    }

    Part12Step09Controller(Executor executor,
                           BannerModule bannerModule,
                           DateTimeModule dateTimeModule,
                           DataRepository dataRepository,
                           EngineSpeedModule engineSpeedModule,
                           VehicleInformationModule vehicleInformationModule,
                           CommunicationsModule communicationsModule) {
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

        // 6.12.9.1 Actions
        // 6.12.9.1.a. DS DM20 [send Request (PG 59904) for PG 49664 (SPs 3048-3049, 3066-3068)]
        // to ECUs that reported DM20 data earlier in Part 11.
        List<Integer> addresses = getDataRepository().getObdModuleAddresses()
                                                     .stream()
                                                     .filter(a -> getPart11DM20(a) != null)
                                                     .collect(Collectors.toList());

        var dsResults = addresses.stream()
                                 .map(a -> getCommunicationsModule().requestDM20(getListener(), a))
                                 .collect(Collectors.toList());

        // 6.12.9.1.b. If no response [transport protocol RTS or NACK(Busy) in 220 ms],
        // then retry DS DM20 request to the OBD ECU.
        // [Do not attempt retry for NACKs that indicate not supported].
        List<Integer> missingAddresses = determineNoResponseAddresses(dsResults, addresses);
        missingAddresses.stream()
                        .map(address -> getCommunicationsModule().requestDM20(getListener(), address))
                        .forEach(dsResults::add);

        // 6.12.9.1.c. Record responses for use in part 12 test 11.
        dsResults.stream().map(BusResult::requestResult).flatMap(r -> r.getPackets().stream()).forEach(this::save);

        // 6.12.9.2.a. Fail if retry was required to obtain DM20 response.
        missingAddresses.stream()
                        .map(Lookup::getAddressName)
                        .forEach(moduleName -> {
                            addFailure("6.12.9.2.a - Retry was required to obtain DM20 response from " + moduleName);
                        });

        var packets = filterPackets(dsResults);

        // 6.12.9.2.b. Fail if any response indicates that the general denominator (SP 3049) is greater by more than 1
        // when compared to the general denominator received in Part 11 test 5.
        packets.stream()
               .filter(p -> p.getOBDConditionsCount() > getPart11GeneralDenominator(p.getSourceAddress()) + 1)
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addFailure("6.12.9.2.b - " + moduleName
                           + " response indicates that the general denominator (SP 3049) is greater by more than 1 when compared to the general denominator received in Part 11 test 5");
               });

        // 6.12.9.2.c. Fail if NACK received from OBD ECUs that previously provided a DM20 message.
        filterAcks(dsResults).stream().forEach(ack -> {
            addFailure("6.12.9.2.c - NACK received from OBD ECUs that previously provided a DM20 message");
        });

        // 6.12.9.3.a. Warn if any response indicates an individual numerator that is greater than the
        // corresponding values received in Part 11 test 8.
        for (DM20MonitorPerformanceRatioPacket packet : packets) {
            packet.getRatios()
                  .stream()
                  .filter(r -> r.getNumerator() > getPart11RatioNumerator(r))
                  .map(PerformanceRatio::getName)
                  .forEach(ratioName -> {
                      addWarning("6.12.9.3.a - " + packet.getModuleName() + " numerator for monitor "
                              + ratioName + " is greater than the corresponding value in part 11 test");
                  });
        }

        // 6.12.9.3.a. Warn if any response indicates an individual denominator that is greater than the
        // corresponding values received in Part 11 test 8.
        for (DM20MonitorPerformanceRatioPacket packet : packets) {
            packet.getRatios()
                  .stream()
                  .filter(r -> r.getDenominator() > getPart11RatioDenominator(r))
                  .map(PerformanceRatio::getName)
                  .forEach(ratioName -> {
                      addWarning("6.12.9.3.a - " + packet.getModuleName() + " denominator for monitor "
                              + ratioName + " is greater than the corresponding value in part 11 test");
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

        // 6.12.9.3.b.ii. Warn if any ECU response shows:
        // general denominator greater than the ignition cycle counter (SPN 3048);
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
        // 6.12.9.3.c.i. Warn if any numerator is less than their corresponding value in part 11.
        for (DM20MonitorPerformanceRatioPacket packet : packets) {
            packet.getRatios()
                  .stream()
                  .filter(r -> r.getNumerator() < getPart11RatioNumerator(r))
                  .map(PerformanceRatio::getName)
                  .forEach(ratioName -> {
                      addWarning("6.12.9.3.c.i - " + packet.getModuleName() + " numerator for monitor "
                              + ratioName + " is less than the corresponding value in part 11 test");
                  });
        }

        // 6.12.9.3.c.i. Warn if any denominator is less than their corresponding value in part 11.
        for (DM20MonitorPerformanceRatioPacket packet : packets) {
            packet.getRatios()
                  .stream()
                  .filter(r -> r.getDenominator() < getPart11RatioDenominator(r))
                  .map(PerformanceRatio::getName)
                  .forEach(ratioName -> {
                      addWarning("6.12.9.3.c.i - " + packet.getModuleName() + " denominator for monitor "
                              + ratioName + " is less than the corresponding value in part 11 test");
                  });
        }

        // 6.12.9.3.c.i. Warn if any value ignition cycle counter is less than their corresponding value in part 11.
        packets.stream()
               .filter(p -> p.getIgnitionCycles() < getPart11IgnitionCycles(p.getSourceAddress()))
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addWarning("6.12.9.3.c.i - " + moduleName
                           + " ignition cycle counter is less than the corresponding value in in part 11 test");
               });

        // 6.12.9.3.d. If more than one ECU reports DM20 data,
        // warn if general denominators do not match from all ECUs.
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
                       "6.12.9.3.d - More than one ECU reported DM20 data and general denominators do not match from all ECUs");
        }

        // 6.12.9.3.d. If more than one ECU reports DM20 data, warn if ignition cycle counts do not match from all ECUs.
        var maxIgn = packets.stream().mapToInt(DM20MonitorPerformanceRatioPacket::getIgnitionCycles).max().orElse(0);
        var minIgn = packets.stream().mapToInt(DM20MonitorPerformanceRatioPacket::getIgnitionCycles).min().orElse(0);
        if (minIgn != maxIgn) {
            addWarning(
                       "6.12.9.3.d - More than one ECU reported DM20 data and ignition cycle counts do not match from all ECUs");
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

    private int getPart11GeneralDenominator(int address) {
        var dm20 = getPart11DM20(address);
        return dm20 == null ? -1 : dm20.getOBDConditionsCount();
    }

    private int getPart11RatioNumerator(PerformanceRatio ratio) {
        var dm20 = getPart11DM20(ratio.getSourceAddress());
        return dm20 == null ? -1 : dm20.getRatio(ratio.getId()).map(PerformanceRatio::getNumerator).orElse(-1);
    }

    private int getPart11RatioDenominator(PerformanceRatio ratio) {
        var dm20 = getPart11DM20(ratio.getSourceAddress());
        return dm20 == null ? -1 : dm20.getRatio(ratio.getId()).map(PerformanceRatio::getDenominator).orElse(-1);
    }

    private int getPart11IgnitionCycles(int address) {
        var dm20 = getPart11DM20(address);
        return dm20 == null ? -1 : dm20.getIgnitionCycles();
    }

}
