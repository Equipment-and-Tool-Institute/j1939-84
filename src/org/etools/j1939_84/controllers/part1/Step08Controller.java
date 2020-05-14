/**
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part1;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.etools.j1939_84.bus.j1939.packets.DM20MonitorPerformanceRatioPacket;
import org.etools.j1939_84.controllers.Controller;
import org.etools.j1939_84.model.FuelType;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.model.PartResultFactory;
import org.etools.j1939_84.model.VehicleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticReadinessModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 *
 * @author Garrison Garland (garrison@soliddesign.net)
 *
 */

public class Step08Controller extends Controller {

    private final DataRepository dataRepository;

    private final DiagnosticReadinessModule diagnosticReadinessModule;

    Step08Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(), new EngineSpeedModule(), new BannerModule(),
                new DateTimeModule(), new VehicleInformationModule(), new PartResultFactory(),
                new DiagnosticReadinessModule(), dataRepository);
    }

    Step08Controller(Executor executor, EngineSpeedModule engineSpeedModule,
            BannerModule bannerModule, DateTimeModule dateTimeModule,
            VehicleInformationModule vehicleInformationModule, PartResultFactory partResultFactory,
            DiagnosticReadinessModule diagnosticReadinessModule, DataRepository dataRepository) {
        super(executor, engineSpeedModule, bannerModule, dateTimeModule, vehicleInformationModule, partResultFactory);
        this.diagnosticReadinessModule = diagnosticReadinessModule;
        this.dataRepository = dataRepository;
    }

    @Override
    public String getDisplayName() {
        return "Part 1 Step 8";
    }

    @Override
    protected int getTotalSteps() {
        return 1;
    }

    /**
     *
     * 6.1.8 DM20: Monitor Performance Ratio
     *
     * 6.1.8.1 Actions:
     *
     * a. Global DM20 (send Request (PGN 59904) for PGN 49664 (SPNs 3048, 3049,
     * 3066-3068).
     *
     * i. Create list by ECU address of all data for use later in the test.
     *
     * 6.1.8.2 Fail criteria:
     *
     * a. Fail if minimum expected SPNs are not supported (in the aggregate response
     * for the vehicle) per section A.4, Criteria for Monitor Performance Ratio
     * Evaluation.
     *
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
            if (obdModule == null) {
                obdModule = new OBDModuleInformation(sourceAddress);
            }
            obdModule.setPerformanceRatios(packet.getRatios());
        }

        // Gather all the spn of performance ratio from the vehicle
        Set<Integer> dm20Spns = globalDM20s.stream()
                .flatMap(dm20 -> dm20.getRatios().stream())
                .map(ratio -> ratio.getSpn())
                .collect(Collectors.toSet());

        verifyMinimumExpectedSpnSupported(dm20Spns);
    }

    /**
     * This method verifies that the minimum expected SPNs are supported per the
     * "Criteria for Monitor Performance Ratio Evaluation" section A.4 of J1939_84
     *
     * @param dm20Spns
     *
     */
    private void verifyMinimumExpectedSpnSupported(Set<Integer> dm20Spns) {
        VehicleInformation vehicleInfo = dataRepository.getVehicleInformation();
        FuelType fuelType = vehicleInfo.getFuelType();

        if (fuelType.isCompressionIgnition()) {

            int SPNa[] = { 5322, 5318, 3058, 3064, 5321, 3055 };
            int SPNn[] = { 4792, 5308, 4364 };

            if ((!IntStream.of(SPNa).allMatch(spn -> dm20Spns.contains(spn)))
                    && (!IntStream.of(SPNn).anyMatch(spn -> dm20Spns.contains(spn)))) {
                addFailure(1, 8, "6.1.8.2.a - minimum expected SPNs for compression ignition are not supported.");
            }
        } else if (fuelType.isSparkIgnition()) {
            // TODO Add the Outlet Oxygen Sensor Banks in Table A-3-2 (pg.111) with
            // non-integer variables i.e. New1, New2 at the end of the table.
            int SPNsi[] = { 3054, 3058, 3306, 3053, 3050, 3051, 3055, 3056, 3057 };
            if (!IntStream.of(SPNsi).allMatch(spn -> dm20Spns.contains(spn))) {
                addFailure(1, 8, "6.1.8.2.a - minimum expected SPNs for spark ignition are not supported.");
            }
        } else {
            getLogger().info("Ignition Type not supported in Monitor Performance Ratio Evaluation.");
        }
    }
}