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
import org.etools.j1939_84.bus.j1939.packets.DM28PermanentEmissionDTCPacket;
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
 * The controller for 6.1.20 DM28: Permanent DTCs DTCs
 */

public class Step20Controller extends StepController {

    private static final int PART_NUMBER = 1;

    private static final int STEP_NUMBER = 20;

    private static final int TOTAL_STEPS = 1;

    private final DataRepository dataRepository;

    private final DTCModule dtcModule;

    Step20Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(),
                new EngineSpeedModule(),
                new BannerModule(),
                new VehicleInformationModule(),
                new DTCModule(),
                dataRepository);
    }

    Step20Controller(Executor executor,
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

        // 6.1.20.1.a. Global DM28 for PGN 64896
        RequestResult<DM28PermanentEmissionDTCPacket> globalResponse = dtcModule.requestDM28(getListener(), true);

        globalResponse.getPackets().forEach(packet -> {
            // 6.1.20.2.a. Fail if any ECU reports a permanent DTC
            if (!packet.getDtcs().isEmpty()) {
                addFailure("6.1.20.2.a - An ECU reported permanent DTCs");
            }
            // 6.1.20.2.b. Fail if any ECU does not report MIL off
            if (packet.getMalfunctionIndicatorLampStatus() != LampStatus.OFF) {
                addFailure("6.1.20.2.b - An ECU did not report MIL off");
            }
        });

        // 6.1.20.2.c. Fail if no OBD ECU provides DM28
        if (globalResponse.getPackets().isEmpty()) {
            addFailure("6.1.20.2.c - No OBD ECU provided a DM28");
        }

        // 6.1.20.3.a. DS DM28 to each OBD ECU.
        List<DM28PermanentEmissionDTCPacket> destinationSpecificPackets = new ArrayList<>();
        dataRepository.getObdModuleAddresses().forEach(address ->
                dtcModule.requestDM28(getListener(), true, address)
                        .getPacket()
                        .ifPresentOrElse((packet) -> {
                            // No requirements around the destination specific ack so, the acks are not needed
                            packet.left.ifPresent(destinationSpecificPackets::add);
                        }, () -> addWarning("6.1.20.3 OBD module " + getAddressName(address)
                                + " did not return a response to a destination specific request")));
        if (destinationSpecificPackets.isEmpty()) {
            addWarning("6.1.20.3.a Destination Specific DM28 requests to OBD modules did not return any responses");
        }


        // 6.1.20.4.a. Fail if any difference compared to data received during global request.
        List<DM28PermanentEmissionDTCPacket> differentPackets = globalResponse
                .getPackets()
                .stream()
                .filter(packet -> !destinationSpecificPackets.contains(packet))
                .collect(Collectors.toList());
        if (!differentPackets.isEmpty()) {
            addFailure("6.1.20.4.a Difference compared to data received during global request");
        }

        // 6.1.20.4.b. Fail if NACK not received from OBD ECUs that did not respond to global query.
        List<DM28PermanentEmissionDTCPacket> nacksRemovedPackets = differentPackets.stream()
                .filter(packet -> globalResponse.getAcks()
                        .stream()
                        .anyMatch(ack -> ack.getSourceAddress() != packet.getSourceAddress()))
                .collect(Collectors.toList());
        if (!nacksRemovedPackets.isEmpty()) {
            addFailure("6.1.20.4.b NACK not received from OBD ECUs that did not respond to global query");
        }
    }
}
