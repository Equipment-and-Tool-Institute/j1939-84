/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part1;

import static org.etools.j1939_84.bus.j1939.Lookup.getAddressName;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.etools.j1939_84.bus.j1939.packets.DM12MILOnEmissionDTCPacket;
import org.etools.j1939_84.bus.j1939.packets.LampStatus;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.model.RequestResult;
import org.etools.j1939_84.modules.*;

/**
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 * The controller for 6.1.18 DM12: Emissions related active DTCs
 */

public class Step18Controller extends StepController {

    private static final int PART_NUMBER = 1;
    private static final int STEP_NUMBER = 18;
    private static final int TOTAL_STEPS = 0;

    private final DataRepository dataRepository;

    private final DTCModule dtcModule;

    Step18Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(),
                new EngineSpeedModule(),
                new BannerModule(),
                new VehicleInformationModule(),
                new DTCModule(),
                dataRepository,
                DateTimeModule.getInstance());
    }

    Step18Controller(Executor executor,
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

        // 6.1.18.1.a. Global DM12 for PGN 65236
        RequestResult<DM12MILOnEmissionDTCPacket> globalResponse = dtcModule.requestDM12(getListener(), true);

        globalResponse.getPackets().forEach(packet -> {
            // 6.1.18.2.a. Fail if any ECU reports active DTCs.
            if (!packet.getDtcs().isEmpty()) {
                addFailure("6.1.18.2.a - An ECU reported active DTCs");
            }
            // 6.1.18.2.b. Fail if any ECU does not report MIL off.
            if (packet.getMalfunctionIndicatorLampStatus() != LampStatus.OFF) {
                addFailure("6.1.18.2.b - An ECU did not report MIL off");
            }
        });

        // 6.1.18.2.c. Fail if no OBD ECU provides DM12.
        if (globalResponse.getPackets().isEmpty()) {
            addFailure("6.1.18.2.c - No OBD ECU provided DM12");
        }

        // 6.1.18.3.a. DS DM12 to all OBD ECUs.
        List<DM12MILOnEmissionDTCPacket> destinationSpecificPackets = new ArrayList<>();
        dataRepository.getObdModuleAddresses().forEach(address -> dtcModule.requestDM12(getListener(), true, address)
                .getPacket()
                .ifPresentOrElse((packet) -> {
                    // No requirements around the destination specific acks so, the acks are not needed
                    packet.left.ifPresent(destinationSpecificPackets::add);
                }, () -> addWarning("6.1.18.3 OBD module " + getAddressName(address)
                        + " did not return a response to a destination specific request")));
        if (destinationSpecificPackets.isEmpty()) {
            addWarning("6.1.18.3.a Destination Specific DM12 requests to OBD modules did not return any responses");
        }

        // 6.1.18.4.a. Fail if any difference compared to data received during global request.
        List<DM12MILOnEmissionDTCPacket> differentPackets = globalResponse
                .getPackets()
                .stream()
                .filter(packet -> !destinationSpecificPackets.contains(packet))
                .collect(Collectors.toList());
        if (!differentPackets.isEmpty()) {
            addFailure("6.1.18.4.a Difference compared to data received during global request");
        }

        // 6.1.18.4.b. Fail if NACK not received from OBD ECUs that did not respond to global query.
        List<DM12MILOnEmissionDTCPacket> nacksRemovedPackets = differentPackets.stream()
                .filter(packet -> globalResponse.getAcks().stream()
                        .anyMatch(ack -> ack.getSourceAddress() != packet.getSourceAddress()))
                .collect(Collectors.toList());
        if (!nacksRemovedPackets.isEmpty()) {
            addFailure("6.1.18.4.b NACK not received from OBD ECUs that did not respond to global query");
        }
    }
}
