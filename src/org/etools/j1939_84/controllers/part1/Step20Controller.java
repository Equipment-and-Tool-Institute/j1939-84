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

import org.etools.j1939_84.bus.j1939.packets.DM28PermanentEmissionDTCPacket;
import org.etools.j1939_84.bus.j1939.packets.LampStatus;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.model.PartResultFactory;
import org.etools.j1939_84.model.RequestResult;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DTCModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 *
 *         The controller for 6.1.20 DM28: Permanent DTCs DTCs
 */

public class Step20Controller extends StepController {

    private static final int PART_NUMBER = 1;

    private static final int STEP_NUMBER = 20;

    private static final int TOTAL_STEPS = 1;

    private final DataRepository dataRepository;

    private final DTCModule dtcModule;

    Step20Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(), new EngineSpeedModule(), new BannerModule(),
                new VehicleInformationModule(), new PartResultFactory(),
                new DTCModule(), dataRepository);
    }

    Step20Controller(Executor executor, EngineSpeedModule engineSpeedModule,
            BannerModule bannerModule,
            VehicleInformationModule vehicleInformationModule, PartResultFactory partResultFactory,
            DTCModule dtcModule, DataRepository dataRepository) {
        super(executor, engineSpeedModule, bannerModule, vehicleInformationModule, partResultFactory,
                PART_NUMBER, STEP_NUMBER, TOTAL_STEPS);
        this.dtcModule = dtcModule;
        this.dataRepository = dataRepository;
    }

    @Override
    protected void run() throws Throwable {

        dtcModule.setJ1939(getJ1939());

        // 6.1.20.1 Actions:
        // a. Global DM28 (send Request (PGN 59904) for PGN 64896 (SPNs
        // 1213-1215, 3038, 1706)).
        RequestResult<DM28PermanentEmissionDTCPacket> globalResponse = dtcModule.requestDM28(getListener(), true);

        globalResponse.getPackets().forEach(packet -> {
            // 6.1.20.2 Fail criteria:
            // a. Fail if any ECU reports a permanent DTC
            if (!packet.getDtcs().isEmpty()) {
                addFailure("6.1.20.2.a - Fail if any ECU reports active DTCs");
            }
            // b. Fail if any ECU does not report MIL off
            if (packet.getMalfunctionIndicatorLampStatus() != LampStatus.OFF) {
                addFailure("6.1.20.2.b - Fail if any ECU does not report MIL off");
            }
        });
        // c. Fail if no OBD ECU provides DM28
        if (globalResponse.getPackets().isEmpty()) {
            addFailure("6.1.20.2.c - Fail if no OBD ECU provides DM28");

        }
        // 6.1.20.3 Actions2:
        // a. DS DM28 to each OBD ECU.
        List<DM28PermanentEmissionDTCPacket> destinationSpecificPackets = new ArrayList<>();
        dataRepository.getObdModuleAddresses().forEach(address -> {
            dtcModule.requestDM28(getListener(), true, address)
                    .getPacket()
                    .ifPresentOrElse((packet) -> {
                        // No requirements around the destination specific acks
                        // so, the acks are not needed
                        if (packet.left.isPresent()) {
                            destinationSpecificPackets.add(packet.left.get());
                        }
                    }, () -> {
                        addWarning("6.1.20.3 OBD module " + getAddressName(address)
                                + " did not return a response to a destination specific request");
                    });
        });
        if (destinationSpecificPackets.isEmpty()) {
            addWarning("6.1.20.3.a Destination Specific DM28 requests to OBD modules did not return any responses");
        }
        // 6.1.20.4 Fail criteria2:
        // a. Fail if any difference compared to data received during global
        // request.
        List<DM28PermanentEmissionDTCPacket> differentPackets = globalResponse.getPackets().stream()
                .filter(packet -> {
                    return !destinationSpecificPackets.contains(packet);
                }).collect(Collectors.toList());
        if (!differentPackets.isEmpty()) {
            addFailure("6.1.20.4.a Fail if any difference compared to data received during global request");
        }
        // b. Fail if NACK not received from OBD ECUs that did not respond to
        // global query.
        List<DM28PermanentEmissionDTCPacket> nacksRemovedPackets = differentPackets.stream().filter(packet -> {
            return globalResponse.getAcks().stream()
                    .anyMatch(ack -> ack.getSourceAddress() != packet.getSourceAddress());
        }).collect(Collectors.toList());
        if (!nacksRemovedPackets.isEmpty()) {
            addFailure("6.1.20.4.b Fail if NACK not received from OBD ECUs that did not respond to global query");
        }
    }
}
