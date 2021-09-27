/*
 * Copyright (c) 2020. Electronic Tools Institute
 */
package org.etools.j1939_84.controllers.part01;

import static org.etools.j1939_84.modules.CommunicationsModule.getCompositeSystems;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket.Response;
import org.etools.j1939_84.bus.j1939.packets.AddressClaimPacket;
import org.etools.j1939_84.bus.j1939.packets.DM5DiagnosticReadinessPacket;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.model.RequestResult;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.CommunicationsModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * 6.1.3 DM5: Diagnostic Readiness 1
 */
public class Part01Step03Controller extends StepController {
    private static final int PART_NUMBER = 1;
    private static final int STEP_NUMBER = 3;
    private static final int TOTAL_STEPS = 0;

    Part01Step03Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(),
             new EngineSpeedModule(),
             new BannerModule(),
             new VehicleInformationModule(),
             new CommunicationsModule(),
             dataRepository,
             DateTimeModule.getInstance());
    }

    Part01Step03Controller(Executor executor,
                           EngineSpeedModule engineSpeedModule,
                           BannerModule bannerModule,
                           VehicleInformationModule vehicleInformationModule,
                           CommunicationsModule communicationsModule,
                           DataRepository dataRepository,
                           DateTimeModule dateTimeModule) {
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
        // 6.1.3.1.a. Global5 DM5 (send Request (PGN 59904) for PGN 65230 (SPN 1220)).
        RequestResult<DM5DiagnosticReadinessPacket> response = getCommunicationsModule().requestDM5(getListener());

        // 6.1.3.1.b. Fail if any ECU responds with a NACK (for PGN 65230).
        boolean nacked = response.getAcks().stream().anyMatch(packet -> packet.getResponse() == Response.NACK);
        if (nacked) {
            addFailure("6.1.3.2.b - The request for DM5 was NACK'ed");
        }

        List<DM5DiagnosticReadinessPacket> parsedPackets = response.getPackets();

        if (!parsedPackets.isEmpty()) {
            getListener().onResult("");
            getListener().onResult("Vehicle Composite of DM5:");
            getCompositeSystems(parsedPackets, true).forEach(s -> getListener().onResult(s.toString()));
        }

        // 6.1.3.1.b. Create “'OBD ECU”' list (comprised of all ECUs that indicate 0x13, 0x14, 0x22, or 0x23 for
        // OBD compliance) for use later in the test as the “OBD ECUs.”.
        response.getPackets()
                .stream()
                .filter(DM5DiagnosticReadinessPacket::isObd)
                .forEach(p -> {
                    OBDModuleInformation info = new OBDModuleInformation(p.getSourceAddress(),
                                                                         getAddressClaimFunction(p));
                    info.set(p, 1);
                    getDataRepository().putObdModule(info);
                });

        // 6.1.3.2.a. Fail if no ECU reports as an OBD ECU.
        if (getDataRepository().getObdModules().size() < 1) {
            addFailure("6.1.3.2.a - No ECU reported as an OBD ECU");
        }

        // 6.1.3.3.a. Warn if more than one ECU responds with a value for OBD compliance where the values are not
        // identical (e.g., if one ECU reports 0x13 and another reports 0x22, if one reports 0x13 and another reports
        // 0x11).
        long distinctCount = response.getPackets()
                                     .stream()
                                     .map(DM5DiagnosticReadinessPacket::getOBDCompliance)
                                     .filter(c -> c != (byte) 255 && c != (byte) 5) // Non-OBD values
                                     .distinct()
                                     .count();

        if (distinctCount > 1) {
            // All the values should be the same
            addWarning("6.1.3.3.a - An ECU responded with a value for OBD Compliance that was not identical to other ECUs");
        }
    }

    private Integer getAddressClaimFunction(DM5DiagnosticReadinessPacket p) {
        return getDataRepository().getVehicleInformation()
                                  .getAddressClaim()
                                  .getPackets()
                                  .stream()
                                  .filter(a -> a.getSourceAddress() == p.getSourceAddress())
                                  .map(AddressClaimPacket::getFunctionId)
                                  .findFirst()
                                  .orElse(-1);
    }

}
