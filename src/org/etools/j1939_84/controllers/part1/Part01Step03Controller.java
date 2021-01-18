/*
 * Copyright (c) 2020. Electronic Tools Institute
 */
package org.etools.j1939_84.controllers.part1;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket.Response;
import org.etools.j1939_84.bus.j1939.packets.AddressClaimPacket;
import org.etools.j1939_84.bus.j1939.packets.DM5DiagnosticReadinessPacket;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.model.RequestResult;
import org.etools.j1939_84.modules.*;

public class Part01Step03Controller extends StepController {
    private static final int PART_NUMBER = 1;
    private static final int STEP_NUMBER = 3;
    private static final int TOTAL_STEPS = 0;

    private final DataRepository dataRepository;

    private final DiagnosticReadinessModule diagnosticReadinessModule;

    Part01Step03Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(),
                new EngineSpeedModule(),
                new BannerModule(),
                new VehicleInformationModule(),
                new DiagnosticReadinessModule(),
                dataRepository,
                DateTimeModule.getInstance());
    }

    Part01Step03Controller(Executor executor,
                           EngineSpeedModule engineSpeedModule,
                           BannerModule bannerModule,
                           VehicleInformationModule vehicleInformationModule,
                           DiagnosticReadinessModule diagnosticReadinessModule,
                           DataRepository dataRepository, DateTimeModule dateTimeModule) {
        super(executor,
                engineSpeedModule,
                bannerModule,
                vehicleInformationModule,
                dateTimeModule,
                PART_NUMBER,
                STEP_NUMBER,
                TOTAL_STEPS);
        this.diagnosticReadinessModule = diagnosticReadinessModule;
        this.dataRepository = dataRepository;
    }

    @Override
    protected void run() throws Throwable {
        diagnosticReadinessModule.setJ1939(getJ1939());

        RequestResult<DM5DiagnosticReadinessPacket> response = diagnosticReadinessModule
                .requestDM5Packets(getListener(), true);
        boolean nacked = response.getAcks().stream().anyMatch(packet -> packet.getResponse() == Response.NACK);
        if (nacked) {
            addFailure(1, 3, "6.1.3.2.b - The request for DM5 was NACK'ed");
        }

        response.getPackets().stream()
                .filter(DM5DiagnosticReadinessPacket::isObd)
                .forEach(p -> {
                    OBDModuleInformation info = new OBDModuleInformation(p.getSourceAddress());
                    info.setObdCompliance(p.getOBDCompliance());
                    info.setMonitoredSystems(p.getMonitoredSystems());
                    int function = dataRepository.getVehicleInformation()
                            .getAddressClaim()
                            .getPackets()
                            .stream()
                            .filter(a -> a.getSourceAddress() == p.getSourceAddress())
                            .map(AddressClaimPacket::getFunctionId)
                            .findFirst()
                            .orElse(-1);
                    info.setFunction(function);
                    dataRepository.putObdModule(p.getSourceAddress(), info);
                });

        if (dataRepository.getObdModules().size() < 1) {
            addFailure(1, 3, "6.1.3.2.a - There needs to be at least one OBD Module");
        }

        long distinctCount = response.getPackets()
                .stream()
                .map(DM5DiagnosticReadinessPacket::getOBDCompliance)
                .filter(c -> c != (byte) 255 && c != (byte) 5) //Non-OBD values
                .distinct()
                .count();

        if (distinctCount > 1) {
            // All the values should be the same
            addWarning(1,
                    3,
                    "6.1.3.3.a - An ECU responded with a value for OBD Compliance that was not identical to other ECUs");
        }
    }

}
