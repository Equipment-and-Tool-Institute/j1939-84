/**
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part1;

import static org.etools.j1939_84.bus.j1939.Lookup.getAddressName;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.etools.j1939_84.bus.Packet;
import org.etools.j1939_84.bus.j1939.Lookup;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket.Response;
import org.etools.j1939_84.bus.j1939.packets.DM29DtcCounts;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.model.RequestResult;
import org.etools.j1939_84.modules.*;

/**
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 * <p>
 * The controller for 6.1.22 DM29: Regulated DTC counts
 */

public class Step22Controller extends StepController {

    private static final int PART_NUMBER = 1;
    private static final int STEP_NUMBER = 22;
    private static final int TOTAL_STEPS = 0;

    private final DataRepository dataRepository;

    private final DTCModule dtcModule;

    Step22Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(),
             new EngineSpeedModule(),
             new BannerModule(),
             new VehicleInformationModule(),
             new DTCModule(),
             dataRepository,
                DateTimeModule.getInstance());
    }

    Step22Controller(Executor executor,
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

        List<Integer> obdModuleAddresses = dataRepository.getObdModuleAddresses();

        // 6.1.22.1 Actions:
        // a. Global DM29 (send Request (PGN 59904) for PGN 40448 (SPNs
        // 4104-4108)).
        RequestResult<DM29DtcCounts> globalResponse = dtcModule.requestDM29(getListener());

        List<DM29DtcCounts> globalPackets = globalResponse.getPackets();
        globalPackets.forEach(dm29 -> {
            Packet packet = dm29.getPacket();

            // 6.1.22.2 Fail criteria:
            // a. For ECUs that support DM27, fail if any ECU does not report
            // pending/all pending/MIL on/previous MIL on/permanent = 0/0/0/0/0
            if (dm29.isDM27Supported() && (packet.get(0) != 0
                    || packet.get(1) != 0
                    || packet.get(2) != 0
                    || packet.get(3) != 0)) {
                addFailure("6.1.22.2.a - For ECUs that support DM27, fail if any ECU does "
                                   + "not report pending/all pending/MIL on/previous MIL on/permanent = 0/0/0/0/0");
            }

            // b. For ECUs that do not support DM27, fail if any ECU does not
            // report pending/all pending/MIL on/previous MIL on/permanent =
            // 0/0xFF/0/0/0.
            if (!dm29.isDM27Supported() && (packet.get(0) != 0
                    || packet.get(2) != 0
                    || packet.get(3) != 0)) {
                addFailure("6.1.22.2.b - For ECUs that do not support DM27, fail if any ECU does "
                                   + "not report pending/all pending/MIL on/previous MIL on/permanent = 0/0xFF/0/0/0");
            }

            // c. For non-OBD ECUs, fail if any ECU reports pending, MIL-on,
            // previously MIL-on or permanent DTC count greater than 0
            if (!obdModuleAddresses.contains(dm29.getSourceAddress())
                    && (packet.get(0) > 0
                    || packet.get(1) > 0
                    || packet.get(2) > 0
                    || packet.get(3) > 0)) {
                addFailure("6.1.22.2.c - For non-OBD ECUs, fail if any ECU reports pending, MIL-on, "
                                   + "previously MIL-on or permanent DTC count greater than 0");

            }
        });

        // d. Fail if no OBD ECU provides DM29.
        Optional<Integer> obdAddresses = globalPackets
                .stream()
                .map(p -> p.getSourceAddress())
                .filter(a -> obdModuleAddresses.contains(a))
                .findAny();
        if (!obdAddresses.isPresent()) {
            addFailure("6.1.22.2.c - Fail if no OBD ECU provides DM29");
        }

        // 6.1.22.3 Actions:
        // a. DS DM29 to each OBD ECU.
        List<DM29DtcCounts> destinationSpecificPackets = new ArrayList<>();
        List<AcknowledgmentPacket> destinationSpecificAcks = new ArrayList<>();
        obdModuleAddresses.forEach(address -> {
            dtcModule.requestDM29(getListener(), address)
                    .getPacket()
                    .ifPresentOrElse((packet) -> {
                        // No requirements around the destination specific acks
                        // so, the acks are not needed
                        if (packet.left.isPresent()) {
                            destinationSpecificPackets.add(packet.left.get());
                        }
                        if (packet.right.isPresent()) {
                            destinationSpecificAcks.add(packet.right.get());
                        }
                    }, () -> {
                        addWarning("6.1.22.3 - OBD module " + getAddressName(address)
                                           + " did not return a response to a destination specific request");
                    });
        });
        if (destinationSpecificPackets.isEmpty()) {
            addWarning(
                    "6.1.22.3.a - Destination Specific DM29 requests to OBD modules did not return any responses");
        }

        // 6.1.22.4 Fail criteria:
        // a. Fail if any difference compared to data received during global
        // request.
        List<DM29DtcCounts> differentPackets = globalPackets.stream()
                .filter(packet -> {
                    return !destinationSpecificPackets.contains(packet);
                }).collect(Collectors.toList());
        if (!differentPackets.isEmpty()) {
            addFailure("6.1.22.4.a - Fail if any difference compared to data received during global request");
        }

        // b. Fail if NACK not received from OBD ECUs that did not respond to
        // global query.
        List<Integer> responseAddresses = globalPackets
                .stream()
                .map(p -> p.getSourceAddress())
                .collect(Collectors.toList());

        obdModuleAddresses.removeAll(responseAddresses);

        List<Integer> nackAddresses = destinationSpecificAcks.stream()
                .map(p -> p)
                .filter(ack -> ack.getResponse() == Response.NACK)
                .map(ack -> ack.getSourceAddress())
                .collect(Collectors.toList());
        obdModuleAddresses.removeAll(nackAddresses);
        obdModuleAddresses.forEach(address -> {
            addFailure(
                    "6.1.22.4.b - Fail if NACK not received from OBD ECUs that did not respond to global query - OBD module "
                            + Lookup.getAddressName(address) + " did not return a response");
        });
    }
}
