/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part2;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.etools.j1939_84.bus.j1939.Lookup;
import org.etools.j1939_84.bus.j1939.packets.DM19CalibrationInformationPacket;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 * <p>
 * The controller for DM19: Calibration information
 */

public class Part02Step05Controller extends StepController {

    private static final int PART_NUMBER = 2;
    private static final int STEP_NUMBER = 5;
    private static final int TOTAL_STEPS = 0;

    private final DataRepository dataRepository;

    Part02Step05Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(),
             new EngineSpeedModule(),
             new BannerModule(),
             new VehicleInformationModule(),
             dataRepository,
             DateTimeModule.getInstance());
    }

    Part02Step05Controller(Executor executor,
                           EngineSpeedModule engineSpeedModule,
                           BannerModule bannerModule,
                           VehicleInformationModule vehicleInformationModule,
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
        this.dataRepository = dataRepository;
    }

    @Override
    protected void run() throws Throwable {
        //        6.2.5 DM19: Calibration information
        //        6.2.5.1 Actions:
        //          a. DS DM19 (send Request (PGN 59904) for PGN 54016 (SPNs 1634-1635)) to all ECUs
        //             that responded to global DM19 in part 1.
        dataRepository.getObdModules()
                .stream()
                .filter(module -> !module.getCalibrationInformation().isEmpty())
                .forEach(moduleInfo -> {
                    int sourceAddress = moduleInfo.getSourceAddress();
                    getVehicleInformationModule()
                            .reportCalibrationInformation(getListener(), sourceAddress)
                            .getPacket()
                            .ifPresent(p -> {
                                Optional<DM19CalibrationInformationPacket> left = p.left;
                                if (left.isPresent() && !Objects.equals(left.get().getCalibrationInformation(),
                                                                        moduleInfo.getCalibrationInformation())) {
                                    addFailure(
                                            "6.2.5.2.a - " + Lookup.getAddressName(sourceAddress) + " reported CAL IDs/CVNs with different values/quantity than those reported in Part 1 data");
                                }
                            });
                });
    }
}
