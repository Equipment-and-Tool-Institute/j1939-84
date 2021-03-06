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

import org.etools.j1939_84.bus.j1939.packets.PerformanceRatio;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.model.FuelType;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticMessageModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

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
             new DiagnosticMessageModule(),
             dataRepository,
             DateTimeModule.getInstance());
    }

    Part01Step08Controller(Executor executor,
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

    private static String spnToString(List<Integer> spns) {
        return spns.stream().map(i -> "" + i).sorted().collect(Collectors.joining(", "));
    }

    @Override
    protected void run() throws Throwable {

        // 6.1.8.1.a. Global DM20 (send Request (PGN 59904) for PGN 49664
        var globalDM20s = getDiagnosticMessageModule().requestDM20(getListener()).getPackets();

        // 6.1.8.1.a.i. Create list of ECU address
        globalDM20s.forEach(this::save);

        // 6.1.8.2.a. Fail if minimum expected SPNs are not supported (in the aggregate response for the vehicle)
        // per section A.4, Criteria for Monitor Performance Ratio Evaluation.
        Set<Integer> dm20Spns = globalDM20s.stream()
                                           .flatMap(dm20 -> dm20.getRatios().stream())
                                           .map(PerformanceRatio::getSpn)
                                           .collect(Collectors.toSet());

        boolean failure = false;

        String msg = "6.1.8.2.a - Minimum expected SPNs are not supported.";

        if (getFuelType().isCompressionIgnition()) {
            List<Integer> SPNa = new ArrayList<>(List.of(5322, 5318, 3058, 3064, 5321, 3055));
            SPNa.removeAll(dm20Spns);
            if (!SPNa.isEmpty()) {
                msg += " Not Supported SPNs: " + spnToString(SPNa);
                failure = true;
            }

            List<Integer> SPNn = new ArrayList<>(List.of(4792, 5308, 4364));
            SPNn.removeAll(dm20Spns);
            if (SPNn.size() == 3) {
                msg += " None of these SPNs are supported: " + spnToString(SPNn);
                failure = true;
            }
        } else if (getFuelType().isSparkIgnition()) {
            // TODO Add the Outlet Oxygen Sensor Banks in Table A-3-2 (pg.111)
            // with non-integer variables i.e. New1, New2 at the end of the table.
            List<Integer> SPNsi = new ArrayList<>(List.of(3054, 3058, 3306, 3053, 3050, 3051, 3055, 3056, 3057));
            SPNsi.removeAll(dm20Spns);
            if (!SPNsi.isEmpty()) {
                msg += " Not Supported SPNs: " + spnToString(SPNsi);
                failure = true;
            }
        }
        if (failure) {
            addFailure(msg);
        }
    }


    private FuelType getFuelType() {
        return getDataRepository().getVehicleInformation().getFuelType();
    }
}
