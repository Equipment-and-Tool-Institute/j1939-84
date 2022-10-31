/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part01;

import java.util.Collection;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939tools.bus.BusResult;
import org.etools.j1939tools.bus.RequestResult;
import org.etools.j1939tools.j1939.packets.DM24SPNSupportPacket;
import org.etools.j1939tools.j1939.packets.ParsedPacket;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;

/**
 * 6.1.24 DM25: Expanded freeze frame
 */
public class Part01Step24Controller extends StepController {

    private static final int PART_NUMBER = 1;
    private static final int STEP_NUMBER = 24;
    private static final int TOTAL_STEPS = 0;

    Part01Step24Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(),
             new EngineSpeedModule(),
             new BannerModule(),
             new VehicleInformationModule(),
             new CommunicationsModule(),
             dataRepository,
             DateTimeModule.getInstance());
    }

    Part01Step24Controller(Executor executor,
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

    @Override
    protected void run() throws Throwable {

        // 6.1.24.1.a. DS DM25 (send Request (PGN 59904) for PGN 64951 (SPNs 3300,
        // 1214-1215)) to each OBD ECU that responded to DS DM24 with supported
        // freeze frame SPNs.
        //
        // 6.1.24.2.a. Fail if any OBD ECU provides freeze frame data other than no
        // freeze frame data stored [i.e., bytes 1-5= 0x00 and bytes 6-8 = 0xFF]
        getDataRepository().getObdModules()
                           .stream()
                           .filter(module -> !module.getFreezeFrameSPNs().isEmpty())
                           .map(OBDModuleInformation::getSourceAddress)
                           .map(address -> getCommunicationsModule().requestDM25(getListener(),
                                                                                 address,
                                                                                 get(DM24SPNSupportPacket.class,
                                                                                     address,
                                                                                     1)))
                           .map(BusResult::requestResult)
                           .map(RequestResult::getPackets)
                           .flatMap(Collection::stream)
                           .filter(p -> isObdModule(p.getSourceAddress()))
                           .filter(packet -> !packet.getFreezeFrames().isEmpty())
                           .map(ParsedPacket::getModuleName)
                           .forEach(moduleName -> {
                               addFailure("6.1.24.2.a - " + moduleName
                                       + " provided freeze frame data other than no freeze frame data stored");
                           });
    }
}
