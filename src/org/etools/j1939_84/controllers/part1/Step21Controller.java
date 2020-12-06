/**
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part1;

import static org.etools.j1939_84.bus.j1939.Lookup.getAddressName;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.etools.j1939_84.bus.j1939.packets.DM27AllPendingDTCsPacket;
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
 * The controller for 6.1.21 DM27: All Pending DTCs
 */

public class Step21Controller extends StepController {

    private static final int PART_NUMBER = 1;

    private static final int STEP_NUMBER = 21;

    private static final int TOTAL_STEPS = 1;

    private final DataRepository dataRepository;

    private final DTCModule dtcModule;

    Step21Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(),
             new EngineSpeedModule(),
             new BannerModule(),
             new VehicleInformationModule(),
             new DTCModule(),
             dataRepository);
    }

    Step21Controller(Executor executor,
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

        // 6.1.21.1 Actions:
        // a. Global DM27 (send Request (PGN 59904) for PGN 64898 (SPNs
        // 1213-1215, 3038, 1706)).
        RequestResult<DM27AllPendingDTCsPacket> globalResponse = dtcModule.requestDM27(getListener(), true);

        List<Integer> obdModuleAddresses = dataRepository.getObdModuleAddresses();
        globalResponse.getPackets().forEach(packet -> {

            // 6.1.21.2 Fail criteria: (if supported)
            // a. Fail if any OBD ECU reports an all pending DTC.
            if (!packet.getDtcs().isEmpty() && obdModuleAddresses.contains(packet.getSourceAddress())) {
                addFailure("6.1.21.2.a - Fail if any OBD ECU reports an all pending DTC");
            }
            // b. Fail if any ECU does not report MIL off
            if (packet.getMalfunctionIndicatorLampStatus() != LampStatus.OFF) {
                addFailure("6.1.21.2.b - Fail if any ECU does not report MIL off");
            }
        });

        // 6.1.21.3 Actions2:
        // a. DS DM27 to each OBD ECU.
        List<DM27AllPendingDTCsPacket> destinationSpecificPackets = new ArrayList<>();
        obdModuleAddresses.forEach(address -> {
            dtcModule.requestDM27(getListener(), true, address)
                    .getPacket()
                    .ifPresentOrElse((packet) -> {
                        // No requirements around the destination specific acks
                        // so, the acks are not needed
                        if (packet.left.isPresent()) {
                            destinationSpecificPackets.add(packet.left.get());
                        }
                    }, () -> {
                        addWarning("6.1.21.3 OBD module " + getAddressName(address)
                                           + " did not return a response to a destination specific request");
                    });
        });

        // 6.1.21.4 Fail criteria2:
        // a. Fail if any difference compared to data received during global
        // request.
        List<DM27AllPendingDTCsPacket> differentPackets = globalResponse.getPackets().stream()
                .filter(packet -> {
                    return !destinationSpecificPackets.contains(packet);
                }).collect(Collectors.toList());
        if (!differentPackets.isEmpty()) {
            addFailure("6.1.21.4.a Fail if any difference compared to data received during global request");
        }
        // b. Fail if NACK not received from OBD ECUs that did not respond to
        // global query.
        List<DM27AllPendingDTCsPacket> nacksRemovedPackets = differentPackets.stream().filter(packet -> {
            return globalResponse.getAcks().stream()
                    .anyMatch(ack -> ack.getSourceAddress() != packet.getSourceAddress());
        }).collect(Collectors.toList());
        if (!nacksRemovedPackets.isEmpty()) {
            addFailure("6.1.21.4.b Fail if NACK not received from OBD ECUs that did not respond to global query");
        }
    }
}
