/**
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part1;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.etools.j1939_84.bus.j1939.packets.DM25ExpandedFreezeFrame;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.*;

/**
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 * <p>
 * The controller for 6.1.23 DM31: DTC to Lamp Association
 */

public class Step24Controller extends StepController {

    private static final int PART_NUMBER = 1;
    private static final int STEP_NUMBER = 24;
    private static final int TOTAL_STEPS = 0;

    private final DataRepository dataRepository;

    private final DTCModule dtcModule;

    Step24Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(),
             new EngineSpeedModule(),
             new BannerModule(),
             new VehicleInformationModule(),
             new DTCModule(),
             dataRepository,
                DateTimeModule.getInstance());
    }

    Step24Controller(Executor executor,
                     EngineSpeedModule engineSpeedModule,
                     BannerModule bannerModule,
                     VehicleInformationModule vehicleInformationModule,
                     DTCModule dtcModule,
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
        this.dtcModule = dtcModule;
        this.dataRepository = dataRepository;
    }

    @Override
    protected void run() throws Throwable {

        dtcModule.setJ1939(getJ1939());

        // 6.1.24.1 Actions:
        // a.. DS DM25 (send Request (PGN 59904) for PGN 64951 (SPNs 3300,
        // 1214-1215)) to each OBD ECU that responded to DS DM24 with supported
        // freeze frame SPNs.

        // First get the correct OBD Modules per requirements
        List<Integer> obdModuleAddresses = dataRepository.getObdModules().stream()
                .filter(module -> !module.getFreezeFrameSpns().isEmpty())
                .map(m -> m.getSourceAddress())
                .collect(Collectors.toList());

        // Get the responses from each of the modules required
        List<DM25ExpandedFreezeFrame> dm25Packets = obdModuleAddresses.stream()
                .flatMap(address -> dtcModule.requestDM25(getListener(), address).getPacket().stream())
                .flatMap(e -> e.left.stream())
                // filter invalid DM25 out
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
                .collect(Collectors.toList());

        // 6.1.24.2 Fail criteria:
        // a. Fail if any OBD ECU provides freeze frame data other than no
        // freeze frame data stored [i.e., bytes 1-5= 0x00 and
        // bytes 6-8 = 0xFF]
        if (!dm25Packets.isEmpty()) {
            // Verify the no data & DTC didn't cause freeze frame
            addFailure(
                    "6.1.24.2.a. Fail if any OBD ECU provides freeze frame data other than no freeze frame data stored [i.e., bytes 1-5= 0x00 and bytes 6-8 = 0xFF]");
        }
    }
}
