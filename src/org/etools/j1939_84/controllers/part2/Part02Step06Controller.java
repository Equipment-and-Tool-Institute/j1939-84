/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part2;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.etools.j1939_84.bus.j1939.Lookup;
import org.etools.j1939_84.bus.j1939.packets.DM56EngineFamilyPacket;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * DM56: Model year and certification engine family
 */
public class Part02Step06Controller extends StepController {

    private static final int PART_NUMBER = 2;
    private static final int STEP_NUMBER = 6;
    private static final int TOTAL_STEPS = 0;

    private final DataRepository dataRepository;

    Part02Step06Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(),
             new EngineSpeedModule(),
             new BannerModule(),
             new VehicleInformationModule(),
             dataRepository,
             DateTimeModule.getInstance());
    }

    Part02Step06Controller(Executor executor,
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
        // 6.2.6.1.a. DS DM56 (send Request (PGN 59904) for PGN 64711 (SPNs 5844 and 5845)) to each OBD ECU.
        for (int address : dataRepository.getObdModuleAddresses()) {
            String moduleName = Lookup.getAddressName(address);
            getListener().onResult("");
            List<DM56EngineFamilyPacket> packets = getVehicleInformationModule().requestDM56(getListener(), address);

            // 6.2.6.2.a. Fail if any difference is found when compared to data received during part 1
            var obdModuleInfo = dataRepository.getObdModule(address);
            for (DM56EngineFamilyPacket packet : packets) {
                if (obdModuleInfo == null || !packet.getModelYearField().equals(obdModuleInfo.getModelYear())) {
                    addFailure("6.2.6.2.a - " + moduleName + " reported different Model Year when compared to data received in part 1");
                    break;
                }
            }

            for (DM56EngineFamilyPacket packet : packets) {
                if (obdModuleInfo == null || !packet.getFamilyName().equals(obdModuleInfo.getEngineFamilyName())) {
                    addFailure("6.2.6.2.a - " + moduleName + " reported different Engine Family Name when compared to data received in part 1");
                    break;
                }
            }
        }
    }

}