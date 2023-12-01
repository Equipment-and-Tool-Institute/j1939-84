/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part02;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939tools.bus.BusResult;
import org.etools.j1939tools.bus.RequestResult;
import org.etools.j1939tools.j1939.Lookup;
import org.etools.j1939tools.j1939.packets.AcknowledgmentPacket.Response;
import org.etools.j1939tools.j1939.packets.ComponentIdentificationPacket;
import org.etools.j1939tools.j1939.packets.GenericPacket;
import org.etools.j1939tools.j1939.packets.ParsedPacket;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;

/**
 * 6.2.7 Component ID: Make, Model, Serial Number Support
 */
public class Part02Step07Controller extends StepController {

    private static final int PART_NUMBER = 2;
    private static final int STEP_NUMBER = 7;
    private static final int TOTAL_STEPS = 0;

    Part02Step07Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             dataRepository,
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule());
    }

    Part02Step07Controller(Executor executor,
                           BannerModule bannerModule,
                           DateTimeModule dateTimeModule,
                           DataRepository dataRepository,
                           EngineSpeedModule engineSpeedModule,
                           VehicleInformationModule vehicleInformationModule,
                           CommunicationsModule communicationsModule) {
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
        List<Integer> function0Addresses = getDataRepository().getObdModules()
                                                              .stream()
                                                              .filter(m -> m.getFunction() == 0)
                                                              .map(OBDModuleInformation::getSourceAddress)
                                                              .toList();

        // 6.2.7.1.a Destination Specific (DS) Component ID request (PGN 59904) for PGN 65259 (SPNs 586, 587, and 588)
        // to each OBD ECU.
        Map<Integer, BusResult<ComponentIdentificationPacket>> dsPackets = getDataRepository().getObdModuleAddresses()
                                                                                              .stream()
                                                                                              .collect(Collectors.toMap(b -> b,
                                                                                                                        b -> requestComponentId(b),
                                                                                                                        (a,
                                                                                                                         b) -> b,
                                                                                                                        () -> new LinkedHashMap<>()));

        /**
         * <pre>
         * 6.2.7.2 Fail Criteria
         * a. Fail if any OBD ECU NACKs (control byte = 1) its DS request, where the ECU supported PG 65259 in Part 1
         * b. Info if any OBD ECU NACKs (control byte = 2) its DS request. (2, access denied, conditions not correct -
         * Engine running.
         * c. Fail if there is any difference between the part 2 response and the part 1 response from an OBD ECU, as PG
         * 65259 data is defined to be static values.
         * d. Fail if no Function 0 device supports PG 65259 with the engine running.
         */

        // 6.2.7.2.a Fail if any OBD ECU NACKs (control byte = 1) its DS request, where the ECU supported PG 65259 in
        // Part 1
        for (int address : getDataRepository().getObdModuleAddresses()) {
            var response = dsPackets.get(address);
            var part1Packet = getPart1Packet(address);
            if (part1Packet != null) {
                // implied
                if (response == null || response.getPacket().isEmpty()) {
                    addFailure("6.2.7.2 - " + Lookup.getAddressName(address)
                            + " did not respond with PGN 65259 with the engine running");
                } else {
                    var packet = response.getPacket().get();
                    if (packet.right.filter(ack -> ack.getResponse() == Response.NACK).isPresent()) {
                        addFailure("6.2.7.2.a - " + Lookup.getAddressName(address)
                                + " NACK (control byte = 1) PGN 65259 with the engine running");
                    } else if (packet.right.filter(ack -> ack.getResponse() == Response.DENIED).isPresent()) {
                        addInfo("6.2.7.2.b - " + Lookup.getAddressName(address)
                                + " NACK (control byte = 2) PGN 65259 with the engine running");
                    }
                }
            }
        }

        // 6.2.7.2.c Fail if there is any difference between the part 2 response and the part 1 response, as PGN 65259
        // data is defined to be static values.
        dsPackets.values()
                 .stream()
                 .flatMap(r -> r.getPacket().stream())
                 .map(e -> (GenericPacket) e.resolve())
                 .filter(p -> !p.equals(getPart1Packet(p.getSourceAddress())))
                 .map(ParsedPacket::getModuleName)
                 .forEach(moduleName -> {
                     addFailure("6.2.7.2.c - " + moduleName
                             + " reported difference between the part2 response and the part 1 response");
                 });

        // 6.2.7.2.d Fail if no Function 0 device supports PG 65259 with the engine running.
        if (!dsPackets.keySet().stream().anyMatch(function0Addresses::contains)) {
            addFailure("6.2.7.2.d - No Function 0 device supports PG 65259 with the engine running");
        }

        // 6.2.7.3.a. Global Request for Component ID request (PGN 59904) for PGN 65259 (SPNs 586, 587, and 588)
        // 6.2.7.3.b. Display each positive return in the log.
        var globalPackets = requestComponentIds().getPackets();
        var function0GlobalPackets = globalPackets.stream().filter(t -> function0Addresses.contains(t.getSourceAddress())).toList();

        // 6.2.7.4.a. Fail if there is no positive response from function 0. (Global request not supported or timed out)
        if (function0GlobalPackets.isEmpty()) {
            addFailure("6.2.7.4.a - There is no positive response from function 0. (Global request not supported or timed out.)");
        }

        // 6.2.7.4.b. Fail if the global response does not match the destination specific response from function 0.
        for (var r : function0GlobalPackets) {
            var function0DSPacket = dsPackets.get(r.getSourceAddress());
            if (function0DSPacket == null || !r.equals(function0DSPacket.getPacket().map(e -> (GenericPacket) e.resolve()).orElse(null))) {
                addFailure("6.2.7.4.b - Global response does not match the destination specific response from "
                        + Lookup.getAddressName(r.getSourceAddress()));
            }
        }

        // 6.2.7.5 Warn Criteria2 for OBD ECUs Other than Function 0, Warn if Component ID not supported for the
        // global query in 6.2.7.3 with engine running.
        for (int address : getDataRepository().getObdModuleAddresses()) {
            if (function0Addresses.contains(address)) {
                continue;
            }

            var globalResponse = globalPackets.stream().anyMatch(p -> p.getSourceAddress() == address);
            if (!globalResponse) {
                addWarning("6.2.7.5.a - " + Lookup.getAddressName(address)
                        + " did not support PGN 65259 with the engine running");
            }
        }
    }

    private RequestResult<ComponentIdentificationPacket> requestComponentIds() {
        return request(ComponentIdentificationPacket.PGN);
    }

    private ComponentIdentificationPacket getPart1Packet(int address) {
        return get(ComponentIdentificationPacket.class, address, 1);
    }

    private BusResult<ComponentIdentificationPacket> requestComponentId(int address) {
        return request(ComponentIdentificationPacket.PGN, address);
    }
}
