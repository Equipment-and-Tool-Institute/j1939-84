/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part02;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

import net.soliddesign.j1939tools.bus.BusResult;
import net.soliddesign.j1939tools.j1939.packets.ParsedPacket;
import net.soliddesign.j1939tools.j1939.packets.SupportedSPN;
import net.soliddesign.j1939tools.modules.CommunicationsModule;
import net.soliddesign.j1939tools.modules.DateTimeModule;

/**
 * 6.2.3 DM24: SPN support
 */
public class Part02Step03Controller extends StepController {

    private static final int PART_NUMBER = 2;
    private static final int STEP_NUMBER = 3;
    private static final int TOTAL_STEPS = 0;

    Part02Step03Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(),
             new EngineSpeedModule(),
             new BannerModule(),
             new VehicleInformationModule(),
             new CommunicationsModule(),
             dataRepository,
             DateTimeModule.getInstance());
    }

    Part02Step03Controller(Executor executor,
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
        // 6.2.3.1.a. DS DM24 (send Request (PGN 59904) for PGN 64950 (SPNs 3297, 4100-4103)) to each OBD ECU.
        var packets = getDataRepository().getObdModuleAddresses()
                                         .stream()
                                         .map(a -> getCommunicationsModule().requestDM24(getListener(), a))
                                         .flatMap(BusResult::toPacketStream)
                                         .collect(Collectors.toList());

        // 6.2.3.2.a. Fail if the message data received differs from that provided in part 6.1.4
        packets.stream()
               .filter(p -> !getPart1SPNs(p.getSourceAddress()).equals(p.getSupportedSpns()))
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addFailure("6.2.3.2.a - Message data received from " + moduleName
                           + " differs from that provided in part 6.1.4");
               });
    }

    private List<SupportedSPN> getPart1SPNs(int sourceAddress) {
        OBDModuleInformation info = getDataRepository().getObdModule(sourceAddress);
        return info == null ? List.of() : info.getSupportedSPNs();
    }
}
