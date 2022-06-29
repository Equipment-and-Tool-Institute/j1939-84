/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part02;

import static org.etools.j1939tools.j1939.Lookup.getAddressName;

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
import org.etools.j1939tools.bus.BusResult;
import org.etools.j1939tools.j1939.packets.ComponentIdentificationPacket;
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
        // 6.2.7.1.a Destination Specific (DS) Component ID request (PGN 59904) for PGN 65259 (SPNs 586, 587, and 588)
        // to each OBD ECU.
        var dsPackets = getDataRepository().getObdModuleAddresses()
                                           .stream()
                                           .map(this::requestComponentId)
                                           .flatMap(BusResult::toPacketStream)
                                           .collect(Collectors.toList());

        // 6.2.7.2.a Fail if any device does not support PGN 65259 with the engine running that supported PGN 65259 with
        // the engine off in part 1.
        for (int address : getDataRepository().getObdModuleAddresses()) {
            var packet = getPart1Packet(address);
            if (packet != null) {
                boolean hasResponse = dsPackets.stream().anyMatch(p -> p.getSourceAddress() == address);
                if (!hasResponse) {
                    addFailure("6.2.7.2.a - " + getAddressName(address)
                            + " did not support PGN 65259 with the engine running");
                }
            }
        }

        // 6.2.7.2.b Fail if there is any difference between the part 2 response and the part 1 response, as PGN 65259
        // data is defined to be static values.
        dsPackets.stream()
                 .filter(p -> !p.equals(getPart1Packet(p.getSourceAddress())))
                 .map(ParsedPacket::getModuleName)
                 .forEach(moduleName -> {
                     addFailure("6.2.7.2.b - " + moduleName
                             + " reported difference between the part2 response and the part 1 response");
                 });

        // 6.2.7.3.a. Global Request for Component ID request (PGN 59904) for PGN 65259 (SPNs 586, 587, and 588)
        // 6.2.7.3.b. Display each positive return in the log.
        var globalPackets = requestComponentIds();

        // 6.2.7.4.a. Fail if there is no positive response from function 0. (Global request not supported or timed out)
        int function0Address = getDataRepository().getObdModules()
                                                  .stream()
                                                  .filter(m -> m.getFunction() == 0)
                                                  .map(OBDModuleInformation::getSourceAddress)
                                                  .findFirst()
                                                  .orElse(-1);

        var function0GlobalPacket = globalPackets.stream()
                                                 .filter(p -> p.getSourceAddress() == function0Address)
                                                 .findFirst()
                                                 .orElse(null);
        if (function0GlobalPacket == null) {
            addFailure("6.2.7.4.a - There is no positive response from " + getAddressName(function0Address));
        } else {
            // 6.2.7.4.b. Fail if the global response does not match the destination specific response from function 0.
            var function0DSPacket = dsPackets.stream()
                                             .filter(p -> p.getSourceAddress() == function0Address)
                                             .findFirst()
                                             .orElse(null);
            if (!function0GlobalPacket.equals(function0DSPacket)) {
                addFailure("6.2.7.4.b - Global response does not match the destination specific response from "
                        + getAddressName(function0Address));
            }
        }

        // 6.2.7.5 Warn Criteria2 for OBD ECUs Other than Function 0, Warn if Component ID not supported for the
        // global query in 6.2.7.3 with engine running.
        for (int address : getDataRepository().getObdModuleAddresses()) {
            if (address == function0Address) {
                continue;
            }

            var globalResponse = globalPackets.stream().anyMatch(p -> p.getSourceAddress() == address);
            if (!globalResponse) {
                addWarning("6.2.7.5.a - " + getAddressName(address)
                        + " did not support PGN 65259 with the engine running");
            }
        }
    }

    private List<ComponentIdentificationPacket> requestComponentIds() {
        return request(ComponentIdentificationPacket.PGN)
                                                           .toPacketStream()
                                                           .map(p -> new ComponentIdentificationPacket(p.getPacket()))
                                                           .collect(Collectors.toList());
    }

    private List<ComponentIdentificationPacket> requestComponentIds(int address) {
        return requestComponentId(address)
                                          .toPacketStream()
                                          .map(p -> new ComponentIdentificationPacket(p.getPacket()))
                                          .collect(Collectors.toList());
    }

    private ComponentIdentificationPacket getPart1Packet(int address) {
        return get(ComponentIdentificationPacket.class, address, 1);
    }

    private BusResult<ComponentIdentificationPacket> requestComponentId(int address) {
        return request(ComponentIdentificationPacket.PGN, address);
    }
}
