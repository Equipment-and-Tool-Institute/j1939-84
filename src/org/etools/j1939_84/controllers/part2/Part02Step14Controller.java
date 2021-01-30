/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part2;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.etools.j1939_84.bus.j1939.Lookup;
import org.etools.j1939_84.bus.j1939.packets.ParsedPacket;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DTCModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * 6.2.14 DM25: Expanded freeze frame
 */
public class Part02Step14Controller extends StepController {

    private static final int PART_NUMBER = 2;
    private static final int STEP_NUMBER = 14;
    private static final int TOTAL_STEPS = 0;

    private final DataRepository dataRepository;
    private final DTCModule dtcModule;

    Part02Step14Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(),
             new EngineSpeedModule(),
             new BannerModule(),
             new VehicleInformationModule(),
             dataRepository,
             DateTimeModule.getInstance(),
             new DTCModule());
    }

    Part02Step14Controller(Executor executor,
                           EngineSpeedModule engineSpeedModule,
                           BannerModule bannerModule,
                           VehicleInformationModule vehicleInformationModule,
                           DataRepository dataRepository,
                           DateTimeModule dateTimeModule,
                           DTCModule dtcModule) {
        super(executor,
              engineSpeedModule,
              bannerModule,
              vehicleInformationModule,
              dateTimeModule,
              PART_NUMBER,
              STEP_NUMBER,
              TOTAL_STEPS);
        this.dataRepository = dataRepository;
        this.dtcModule = dtcModule;
    }

    @Override
    protected void run() throws Throwable {
        dtcModule.setJ1939(getJ1939());

        // 6.2.14.1.a. DS DM25 (send Request (PGN 59904) for PGN 64951 (SPNs 3300, 1214-1215))
        // to each OBD ECU that responded to global DM24 with supported freeze frame SPNs in part 1.
        //
        // 6.2.14.2.a. Fail if any OBD ECU provides freeze frame data other than
        // bytes 1-5= 0x00 and bytes 6-8 = 0xFF (No freeze frame data available).
        dataRepository.getObdModules()
                .stream()
                .filter(module -> !module.getFreezeFrameSpns().isEmpty())
                .map(OBDModuleInformation::getSourceAddress)
                .flatMap(address -> dtcModule.requestDM25(getListener(), address).getPacket().stream())
                .flatMap(e -> e.left.stream())
                .filter(p -> dataRepository.isObdModule(p.getSourceAddress()))
                .collect(Collectors.toList())
                .stream()
                .filter(packet -> {
                    byte[] bytes = packet.getPacket().getBytes();
                    return bytes[0] != 0x00
                            || bytes[1] != 0x00
                            || bytes[2] != 0x00
                            || bytes[3] != 0x00
                            || bytes[4] != 0x00
                            || bytes[5] != (byte) 0xFF
                            || bytes[6] != (byte) 0xFF
                            || bytes[7] != (byte) 0xFF;
                })
                .map(ParsedPacket::getSourceAddress)
                .map(Lookup::getAddressName)
                .forEach(moduleName -> addFailure("6.2.14.2.a - " + moduleName + " provided freeze frame data other than no freeze frame data stored"));
    }
}
