/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part02;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.etools.j1939_84.bus.j1939.BusResult;
import org.etools.j1939_84.bus.j1939.packets.DM19CalibrationInformationPacket;
import org.etools.j1939_84.bus.j1939.packets.DM19CalibrationInformationPacket.CalibrationInformation;
import org.etools.j1939_84.bus.j1939.packets.ParsedPacket;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.model.RequestResult;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.CommunicationsModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * 6.2.5 DM19: Calibration information
 */

public class Part02Step05Controller extends StepController {

    private static final int PART_NUMBER = 2;
    private static final int STEP_NUMBER = 5;
    private static final int TOTAL_STEPS = 0;

    Part02Step05Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(),
             new EngineSpeedModule(),
             new BannerModule(),
             new VehicleInformationModule(),
             dataRepository,
             DateTimeModule.getInstance(),
             new CommunicationsModule());
    }

    Part02Step05Controller(Executor executor,
                           EngineSpeedModule engineSpeedModule,
                           BannerModule bannerModule,
                           VehicleInformationModule vehicleInformationModule,
                           DataRepository dataRepository,
                           DateTimeModule dateTimeModule,
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
        // 6.2.5.1.a. DS DM19 (send Request (PGN 59904) for PGN 54016 (SPNs 1634-1635)) to all ECUs
        // that responded to global DM19 in part 1.
        // 6.2.5.2.a Fail if any ECU reports a different number of CAL ID and CVNs or different CAL ID and CVN values
        // than was provided by the ECU in part 1.
        getDataRepository().getObdModuleAddresses()
                           .stream()
                           .filter(a -> !getCalibrationInfo(a).isEmpty())
                           .map(a -> getVehicleInformationModule().requestDM19(getListener(), a))
                           .map(BusResult::requestResult)
                           .map(RequestResult::getPackets)
                           .flatMap(Collection::stream)
                           .filter(p -> !Objects.equals(p.getCalibrationInformation(),
                                                        getCalibrationInfo(p.getSourceAddress())))
                           .map(ParsedPacket::getModuleName)
                           .forEach(moduleName -> {
                               addFailure("6.2.5.2.a - " + moduleName
                                       + " reported CAL IDs/CVNs with different values/quantity than those reported in Part 1 data");
                           });
    }

    private List<CalibrationInformation> getCalibrationInfo(int address) {
        var dm19 = get(DM19CalibrationInformationPacket.class, address, 1);
        return dm19 == null ? List.of() : dm19.getCalibrationInformation();
    }
}
