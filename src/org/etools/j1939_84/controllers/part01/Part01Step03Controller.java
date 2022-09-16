/*
 * Copyright (c) 2020. Electronic Tools Institute
 */
package org.etools.j1939_84.controllers.part01;

import static org.etools.j1939tools.modules.CommunicationsModule.getCompositeSystems;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939tools.bus.RequestResult;
import org.etools.j1939tools.j1939.model.FuelType;
import org.etools.j1939tools.j1939.packets.AcknowledgmentPacket.Response;
import org.etools.j1939tools.j1939.packets.AddressClaimPacket;
import org.etools.j1939tools.j1939.packets.DM5DiagnosticReadinessPacket;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;

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

        // 6.1.3.2.b. Fail if any ECU responds with a NACK (for PGN 65230).
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

        // 6.1.3.1.b. Create “OBD ECU” list (comprised of all ECUs that indicate OBD compliance values other than 0, 5,
        // FBh, FCh, FDh, FEh, or FFh that indicate 13h, 14h, 22h, or 23h for OBD compliance) for use later in the test
        // as the “OBD ECUs.”
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

        // 6.1.3.2.c. Fail if a BEV vehicle response provides a value other than 1Bh (27) for OBD compliance.
        if (getFuelType() == FuelType.BATT_ELEC) {
            parsedPackets.stream().forEach(p -> {
                if (p.getOBDCompliance() != 0x1B) {
                    addFailure("6.1.3.2.c - BEV vehicle reports ODB Compliance other than 1Bh (27)");
                }
            });
        }
        // 6.1.3.2.d. Fail if any response from a function 0 device provides OBD Compliance values of 0, 5, FBh, FCh,
        // FDh, FEh, or FFh.
        parsedPackets.stream().forEach(p -> {
            if (getDataRepository().getFunctionZeroAddress() == p.getSourceAddress() && !p.isObd()) {
                addFailure("6.1.3.2.d - Fail if any response from a function 0 device provides OBD Compliance values of 0, 5, FBh, FCh, FDh, FEh, or FFh");
            }
        });

        // 6.1.3.2.e. Fail if a US/CARB vehicle does not provide OBD Compliance values of 13h, 14h, 22h, or 23h.
        var usCarbCompliance = List.of(0x13, 0x14, 0x22, 0x23);
        if (getDataRepository().getVehicleInformation().isUsCarb()
                && parsedPackets.stream().noneMatch(p -> usCarbCompliance.contains((int) p.getOBDCompliance()))) {
            addFailure("6.1.3.2.e - US/CARB vehicle does not provide OBD Compliance values of 13h, 14h, 22h, or 23h");
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

        // 6.1.3.3.b Warn if any response received from a non-OBD ECU provides OBD Compliance values of 0, FBh, FCh,
        // FDh, FEh, or FFh.
        Collection<Integer> invalidNonObdCompliance = List.of(0, 0xFB, 0xFC, 0xFD, 0xFE, 0xFF);
        parsedPackets.stream().forEach(p -> {
            if (!p.isObd() && invalidNonObdCompliance.contains((int) p.getOBDCompliance())) {
                addWarning(String.format("6.1.3.3.b - Response received from a non-OBD ECU provided OBD Compliance values of %Xh",
                                         p.getOBDCompliance()));
            }
        });

        // 6.1.3.3.c Info for DM5 replies from non-OBD ECUs.
        parsedPackets.stream()
                     .filter(p -> !p.isObd())
                     .forEach(p -> {
                         addInfo(String.format("6.1.3.3.c - Response received from a non-OBD ECU provided OBD Compliance values of %Xh",
                                               p.getOBDCompliance()));
                     });
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
