/**
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part1;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.etools.j1939_84.bus.j1939.packets.DM6PendingEmissionDTCPacket;
import org.etools.j1939_84.bus.j1939.packets.LampStatus;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.model.RequestResult;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DTCModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 * <p>
 * The controller for 6.1.17 DM6: Emission related pending DTCs
 */

public class Step17Controller extends StepController {

    private static final int PART_NUMBER = 1;

    private static final int STEP_NUMBER = 17;

    private static final int TOTAL_STEPS = 1;

    private final DataRepository dataRepository;

    private final DTCModule dtcModule;

    Step17Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(),
             new EngineSpeedModule(),
             new BannerModule(),
             new VehicleInformationModule(),
             new DTCModule(),
             dataRepository);
    }

    Step17Controller(Executor executor,
                     EngineSpeedModule engineSpeedModule,
                     BannerModule bannerModule,
                     VehicleInformationModule vehicleInformationModule,
                     DTCModule dtcModule,
                     DataRepository dataRepository) {
        super(executor,
              engineSpeedModule,
              bannerModule,
              vehicleInformationModule,
              PART_NUMBER,
              STEP_NUMBER,
              TOTAL_STEPS);
        this.dtcModule = dtcModule;
        this.dataRepository = dataRepository;
    }

    @Override
    protected void run() throws Throwable {

        dtcModule.setJ1939(getJ1939());

        // 6.1.17.1 Actions:
        // a. Global DM6 (send Request (PGN 59904) for PGN 65227 (SPNs
        // 1213-1215, 3038, 1706)).
        RequestResult<DM6PendingEmissionDTCPacket> globalResponse = dtcModule.requestDM6(getListener());

        globalResponse.getPackets().forEach(packet -> {
            // 6.1.17.2 Fail criteria:
            // a. Fail if any ECU reports pending DTCs
            if (!packet.getDtcs().isEmpty()) {
                addFailure("6.1.17.2.a - Fail if any ECU reports pending DTCs");
            }
            // b. Fail if any ECU does not report MIL off.
            if (packet.getMalfunctionIndicatorLampStatus() != LampStatus.OFF) {
                addFailure("6.1.17.2.b - Fail if any ECU does not report MIL off");
            }
        });
        // c. Fail if no OBD ECU provides DM6.
        if (globalResponse.getPackets().isEmpty()) {
            addFailure("6.1.17.2.c - Fail if no OBD ECU provides DM6");

        }
        // 6.1.17.3 Actions2:
        // a. DS DM6 to each OBD ECU.
        List<DM6PendingEmissionDTCPacket> destinationSpecificPackets = new ArrayList<>();
        dataRepository.getObdModuleAddresses().forEach(address -> {
            destinationSpecificPackets.addAll(dtcModule.requestDM6(getListener(), address).getPackets());
        });
        if (destinationSpecificPackets.isEmpty()) {
            addWarning("6.1.17.3.a Destination Specific DM6 requests to OBD modules did not return any responses");
        }
        // 6.1.17.4 Fail criteria2:
        // a. Fail if any difference compared to data received during global
        // request.
        List<DM6PendingEmissionDTCPacket> differentPackets = globalResponse.getPackets().stream()
                .filter(packet -> {
                    return !destinationSpecificPackets.contains(packet);
                }).collect(Collectors.toList());
        if (!differentPackets.isEmpty()) {
            addFailure("6.1.17.4.a Fail if any difference compared to data received during global request");
        }
        // b. Fail if NACK not received from OBD ECUs that did not respond to
        // global query.
        List<DM6PendingEmissionDTCPacket> nacksRemovedPackets = differentPackets.stream().filter(packet -> {
            return globalResponse.getAcks().stream()
                    .anyMatch(ack -> ack.getSourceAddress() != packet.getSourceAddress());
        }).collect(Collectors.toList());
        if (!nacksRemovedPackets.isEmpty()) {
            addFailure("6.1.17.4.b Fail if NACK not received from OBD ECUs that did not respond to global query");
        }
    }
}
