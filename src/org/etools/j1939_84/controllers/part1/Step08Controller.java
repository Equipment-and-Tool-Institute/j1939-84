/**
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part1;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.etools.j1939_84.bus.j1939.packets.DM20MonitorPerformanceRatioPacket;
import org.etools.j1939_84.controllers.Controller;
import org.etools.j1939_84.model.FuelType;
import org.etools.j1939_84.model.IgnitionType;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.model.PartResultFactory;
import org.etools.j1939_84.model.VehicleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticReadinessModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

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

    @Override
    protected void run() throws Throwable {
        diagnosticReadinessModule.setJ1939(getJ1939());

        // 6.1.8.1.a. Global DM20 (send Request (PGN 59904) for PGN 49664
        List<DM20MonitorPerformanceRatioPacket> globalDM20s = diagnosticReadinessModule.getDM20Packets(getListener(),
                true);
        if (globalDM20s.isEmpty()) {
            getListener().onResult("DM20 is not supported");
            return;
        }

        // 6.1.8.1.a.i. Create list of ECU address
        List<Integer> ecuAddresses = new ArrayList<>();
        for (DM20MonitorPerformanceRatioPacket packet : globalDM20s) {
            int sourceAddress = packet.getSourceAddress();
            OBDModuleInformation info = dataRepository.getObdModule(sourceAddress);
            if (info != null) {
                ecuAddresses.add(sourceAddress);
            }
        }

        VehicleInformation vehicleInfo = dataRepository.getVehicleInformation();
        DiagnosticReadinessModule.getRatios(globalDM20s);
        vehicleInfo.getFuelType();

        // create set of all SPNs (as integers) from all DM20s
        Set<Integer> dm20Spns = globalDM20s.stream()
                .flatMap(dm20 -> dm20.getRatios().stream())
                .map(ratio -> ratio.getSpn())
                .collect(Collectors.toSet());

        if (FuelType.DSL == vehicleInfo.getFuelType()) {
            // List of SPNs per Fuel type that is checked with the SPNs Stored in stream
            // FIXME Last three SPNs for the NOx Catalyst Bank 1 or NOx Adsorber Don't all
            // have to be in there.
            // It can be any combination of the three.
            int SPNd[] = { 5322, 5318, 3058, 3064, 5321, 3055 };
            if (!IntStream.of(SPNd).allMatch(spn -> dm20Spns.contains(spn))) {
                addFailure(1, 8, "6.1.8.2.a - minimum expected SPNs for Diesel fuel type are not supported.");
            } else {
                // TODO Send message that the proper SPNs were passed
                getListener().onResult("All minimum SPNs found for Diesel Fuel Type.");
            }
        } else {

            // FIXME add warning Unknown fuel type.
            addFailure(1, 8, "6.1.8.2.a - Fuel Type not supported in Monitor Performance Ratio Evaluation.");
        }
        if (IgnitionType.SPARK != null) {
            // TODO Add the Outlet Oxygen Sensor Banks in Table A-3-2 (pg.111) with
            // non-integer variables
            int SPNsi[] = { 3054, 3058, 3306, 3053, 3050, 3051, 3055, 3056, 3057 };
            if (!IntStream.of(SPNsi).allMatch(spn -> dm20Spns.contains(spn))) {
                addFailure(1, 8, "6.1.8.2.a - minimum expected SPNs for Spark Ignition are not supported.");
            } else {
                // TODO Send message that the proper SPNs were passed
                getListener().onResult("All minimum SPNs found for Spark Ignition.");
            }
        } else {

            // FIXME add warning Unknown fuel type.
            addFailure(1, 8, "6.1.8.2.a - Ignition Type not supported in Monitor Performance Ratio Evaluation.");
        }
    }
}