/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part02;

import java.util.Collection;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939tools.j1939.packets.DM56EngineFamilyPacket;
import org.etools.j1939tools.j1939.packets.ParsedPacket;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;

/**
 * 6.2.6 DM56: Model year and certification engine family
 */
public class Part02Step06Controller extends StepController {

    private static final int PART_NUMBER = 2;
    private static final int STEP_NUMBER = 6;
    private static final int TOTAL_STEPS = 0;

    Part02Step06Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(),
             new EngineSpeedModule(),
             new BannerModule(),
             new VehicleInformationModule(),
             dataRepository,
             DateTimeModule.getInstance(),
             new CommunicationsModule());
    }

    Part02Step06Controller(Executor executor,
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
    protected void run() {

        // 6.2.6.1.a. DS DM56 (send Request (PGN 59904) for PGN 64711 (SPNs 5844 and 5845)) to each OBD ECU.
        var packets = getDataRepository().getObdModuleAddresses()
                                         .stream()
                                         .map(a -> getCommunicationsModule().requestDM56(getListener(), a))
                                         .flatMap(Collection::stream)
                                         .collect(Collectors.toList());

        // 6.2.6.2.a. Fail if any difference is found when compared to data received during part 1
        packets.stream()
               .filter(p -> !p.equals(getDM56(p.getSourceAddress())))
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addFailure("6.2.6.2.a - " + moduleName
                           + " reported difference when compared to data received during part 1");
               });
    }

    private DM56EngineFamilyPacket getDM56(int address) {
        return get(DM56EngineFamilyPacket.class, address, 1);
    }

}
