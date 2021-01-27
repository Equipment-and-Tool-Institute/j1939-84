/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part1;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.etools.j1939_84.bus.j1939.packets.DM20MonitorPerformanceRatioPacket;
import org.etools.j1939_84.bus.j1939.packets.PerformanceRatio;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.model.FuelType;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticReadinessModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * @author Garrison Garland (garrison@soliddesign.net)
 */
public class Part01Step08Controller extends StepController {

    private static final int PART_NUMBER = 1;
    private static final int STEP_NUMBER = 8;
    private static final int TOTAL_STEPS = 0;

    private final DataRepository dataRepository;

    private final DiagnosticReadinessModule diagnosticReadinessModule;

    Part01Step08Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(),
             new EngineSpeedModule(),
             new BannerModule(),
             new VehicleInformationModule(),
             new DiagnosticReadinessModule(),
             dataRepository,
             DateTimeModule.getInstance());
    }

    Part01Step08Controller(Executor executor,
                           EngineSpeedModule engineSpeedModule,
                           BannerModule bannerModule,
                           VehicleInformationModule vehicleInformationModule,
                           DiagnosticReadinessModule diagnosticReadinessModule,
                           DataRepository dataRepository,
                           DateTimeModule dateTimeModule) {
        super(executor,
              engineSpeedModule,
              bannerModule,
              vehicleInformationModule,
              dateTimeModule,
              PART_NUMBER,
              STEP_NUMBER,
              TOTAL_STEPS);
        this.diagnosticReadinessModule = diagnosticReadinessModule;
        this.dataRepository = dataRepository;
    }

    /**
     * 6.1.8 DM20: Monitor Performance Ratio
     * <p>
     * 6.1.8.1 Actions:
     * <p>
     * a. Global DM20 (send Request (PGN 59904) for PGN 49664 (SPNs 3048, 3049,
     * 3066-3068).
     * <p>
     * i. Create list by ECU address of all data for use later in the test.
     * <p>
     * 6.1.8.2 Fail criteria:
     * <p>
     * a. Fail if minimum expected SPNs are not supported (in the aggregate
     * response for the vehicle) per section A.4, Criteria for Monitor
     * Performance Ratio Evaluation.
     */

    @Override
    protected void run() throws Throwable {
        // 6.1.8 DM20: Monitor Performance Ratio
        diagnosticReadinessModule.setJ1939(getJ1939());

        // 6.1.8.1.a. Global DM20 (send Request (PGN 59904) for PGN 49664
        List<DM20MonitorPerformanceRatioPacket> globalDM20s = diagnosticReadinessModule.getDM20Packets(getListener(),
                                                                                                       true);

        // 6.1.8.1 Actions:
        // 6.1.8.1.a.i. Create list of ECU address
        for (DM20MonitorPerformanceRatioPacket packet : globalDM20s) {
            int sourceAddress = packet.getSourceAddress();
            // Save performance ratio on the obdModule for each ECU
            OBDModuleInformation obdModule = dataRepository.getObdModule(sourceAddress);
            if (obdModule != null) {
                obdModule.setPerformanceRatios(packet.getRatios());
                dataRepository.putObdModule(sourceAddress, obdModule);
            }
        }

        // Gather all the spn of performance ratio from the vehicle
        Set<Integer> dm20Spns = globalDM20s.stream()
                .flatMap(dm20 -> dm20.getRatios().stream())
                .map(PerformanceRatio::getSpn)
                .collect(Collectors.toSet());

        verifyMinimumExpectedSpnSupported(dm20Spns);
    }

    /**
     * This method verifies that the minimum expected SPNs are supported per the
     * "Criteria for Monitor Performance Ratio Evaluation" section A.4 of J1939_84
     */
    private void verifyMinimumExpectedSpnSupported(Set<Integer> dm20Spns) {
        FuelType fuelType = dataRepository.getVehicleInformation().getFuelType();
        boolean failure = false;

        String msg = "6.1.8.2.a - minimum expected SPNs are not supported.";

        if (fuelType.isCompressionIgnition()) {
            List<Integer> SPNa = new ArrayList<>(List.of(5322, 5318, 3058, 3064, 5321, 3055));
            SPNa.removeAll(dm20Spns);
            if (!SPNa.isEmpty()) {
                msg += " Not Supported SPNs: " + toString(SPNa);
                failure = true;
            }

            List<Integer> SPNn = new ArrayList<>(List.of(4792, 5308, 4364));
            SPNn.removeAll(dm20Spns);
            if (SPNn.size() == 3) {
                msg += " None of these SPNs are supported: " + toString(SPNn);
                failure = true;
            }
        } else if (fuelType.isSparkIgnition()) {
            // TODO Add the Outlet Oxygen Sensor Banks in Table A-3-2 (pg.111)
            // with non-integer variables i.e. New1, New2 at the end of the table.
            List<Integer> SPNsi = new ArrayList<>(List.of(3054, 3058, 3306, 3053, 3050, 3051, 3055, 3056, 3057));
            SPNsi.removeAll(dm20Spns);
            if (!SPNsi.isEmpty()) {
                msg += " Not Supported SPNs: " + toString(SPNsi);
                failure = true;
            }
        } else {
            getLogger().info("Ignition Type not supported in Monitor Performance Ratio Evaluation.");
        }
        if (failure) {
            addFailure(msg);
        }
    }

    private static String toString(List<Integer> spns) {
        return spns.stream().map(i -> "" + i).sorted().collect(Collectors.joining(", "));
    }
}