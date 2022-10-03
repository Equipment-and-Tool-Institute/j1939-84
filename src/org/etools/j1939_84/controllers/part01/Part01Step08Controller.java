/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part01;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939tools.j1939.packets.PerformanceRatio;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;

/**
 * 6.1.8 DM20: Monitor Performance Ratio
 */
public class Part01Step08Controller extends StepController {

    private static final int PART_NUMBER = 1;
    private static final int STEP_NUMBER = 8;
    private static final int TOTAL_STEPS = 0;

    Part01Step08Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(),
             new EngineSpeedModule(),
             new BannerModule(),
             new VehicleInformationModule(),
             new CommunicationsModule(),
             dataRepository,
             DateTimeModule.getInstance());
    }

    Part01Step08Controller(Executor executor,
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

    private static String spToString(List<Integer> sps) {
        return sps.stream().sorted().map(i -> "" + i).collect(Collectors.joining(", "));
    }

    @Override
    protected void run() throws Throwable {

        // 6.1.8.1.a. Global DM20 (send Request (PGN 59904) for PGN 49664
        var globalDM20s = getCommunicationsModule().requestDM20(getListener()).getPackets();

        // 6.1.8.1.a.i. Create list of ECU address
        globalDM20s.forEach(this::save);

        // 6.1.8.2.a. Fail if minimum expected SPs are not supported (in the aggregate response for the vehicle)
        // per section A.4. When a numerator and denominator are provided as FFFF(h) and FFFF(h), the monitor identified
        // in the label SP shall be considered to be unsupported.
        Set<Integer> dm20Sps = globalDM20s.stream()
                                          .flatMap(dm20 -> dm20.getRatios().stream())
                                          .filter(p -> {
                                              return p.getNumerator() != 0xFFFF ||
                                                      p.getDenominator() != 0xFFFF;
                                          })
                                          .map(PerformanceRatio::getSpn)
                                          .collect(Collectors.toSet());

        boolean failure = false;

        String msg = "6.1.8.2.a - Minimum expected SPs are not supported.";

        if (getFuelType().isCompressionIgnition()) {
            List<Integer> SPs = new ArrayList<>(List.of(5322, 5318, 3058, 3064, 5321, 3055));
            SPs.removeAll(dm20Sps);
            if (!SPs.isEmpty()) {
                msg += " Not Supported SPs: " + spToString(SPs);
                failure = true;
            }

            List<Integer> SPNn = new ArrayList<>(List.of(4792, 5308, 4364));
            SPNn.removeAll(dm20Sps);
            if (SPNn.size() == 3) {
                msg += " None of these SPs are supported: " + spToString(SPNn);
                failure = true;
            }
        } else if (getFuelType().isSparkIgnition()) {
            List<Integer> SPNsi = new ArrayList<>(List.of(3054,
                                                          3058,
                                                          3306,
                                                          3053,
                                                          3050,
                                                          3051,
                                                          3055,
                                                          3056,
                                                          3057,
                                                          21227,
                                                          21228));
            SPNsi.removeAll(dm20Sps);
            if (!SPNsi.isEmpty()) {
                msg += " Not Supported SPs: " + spToString(SPNsi);
                failure = true;
            }
        }
        if (failure) {
            addFailure(msg);
        }
    }
}
