/*
 * Copyright (c) 2020. Electronic Tools Institute
 */
package org.etools.j1939_84.controllers.part01;

import static org.etools.j1939_84.modules.DiagnosticMessageModule.getCompositeSystems;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket.Response;
import org.etools.j1939_84.bus.j1939.packets.AddressClaimPacket;
import org.etools.j1939_84.bus.j1939.packets.CompositeMonitoredSystem;
import org.etools.j1939_84.bus.j1939.packets.DM5DiagnosticReadinessPacket;
import org.etools.j1939_84.bus.j1939.packets.MonitoredSystem;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.model.RequestResult;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticMessageModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

public class Part01Step03Controller extends StepController {
    private static final int PART_NUMBER = 1;
    private static final int STEP_NUMBER = 3;
    private static final int TOTAL_STEPS = 0;

    Part01Step03Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(),
             new EngineSpeedModule(),
             new BannerModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule(),
             dataRepository,
             DateTimeModule.getInstance());
    }

    Part01Step03Controller(Executor executor,
                           EngineSpeedModule engineSpeedModule,
                           BannerModule bannerModule,
                           VehicleInformationModule vehicleInformationModule,
                           DiagnosticMessageModule diagnosticMessageModule,
                           DataRepository dataRepository,
                           DateTimeModule dateTimeModule) {
        super(executor,
              bannerModule,
              dateTimeModule,
              dataRepository,
              engineSpeedModule,
              vehicleInformationModule,
              diagnosticMessageModule,
              PART_NUMBER,
              STEP_NUMBER,
              TOTAL_STEPS);
    }

    @Override
    protected void run() throws Throwable {

        RequestResult<DM5DiagnosticReadinessPacket> response = getDiagnosticMessageModule().requestDM5(getListener());
        boolean nacked = response.getAcks().stream().anyMatch(packet -> packet.getResponse() == Response.NACK);
        if (nacked) {
            addFailure("6.1.3.2.b - The request for DM5 was NACK'ed");
        }

        List<DM5DiagnosticReadinessPacket> parsedPackets = response.getPackets();

        if (!parsedPackets.isEmpty()) {
            getListener().onResult("");
            getListener().onResult("Vehicle Composite of DM5:");
            List<CompositeMonitoredSystem> systems = getCompositeSystems(parsedPackets, true);
            getListener().onResult(systems.stream().map(MonitoredSystem::toString).collect(Collectors.toList()));
        }

        response.getPackets()
                .stream()
                .filter(DM5DiagnosticReadinessPacket::isObd)
                .forEach(p -> {
                    OBDModuleInformation info = new OBDModuleInformation(p.getSourceAddress());
                    info.setObdCompliance(p.getOBDCompliance());
                    info.setMonitoredSystems(p.getMonitoredSystems());
                    int function = getDataRepository().getVehicleInformation()
                                                      .getAddressClaim()
                                                      .getPackets()
                                                      .stream()
                                                      .filter(a -> a.getSourceAddress() == p.getSourceAddress())
                                                      .map(AddressClaimPacket::getFunctionId)
                                                      .findFirst()
                                                      .orElse(-1);
                    info.setFunction(function);
                    getDataRepository().putObdModule(info);
                });

        if (getDataRepository().getObdModules().size() < 1) {
            addFailure("6.1.3.2.a - There needs to be at least one OBD Module");
        }

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

}
