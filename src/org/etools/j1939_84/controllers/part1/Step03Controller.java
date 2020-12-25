/*
 * Copyright (c) 2020. Electronic Tools Institute
 */
package org.etools.j1939_84.controllers.part1;

import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket.Response;
import org.etools.j1939_84.bus.j1939.packets.DM5DiagnosticReadinessPacket;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.model.RequestResult;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DiagnosticReadinessModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

public class Step03Controller extends StepController {
    private static final int PART_NUMBER = 1;
    private static final int STEP_NUMBER = 3;
    private static final int TOTAL_STEPS = 1;

    private final DataRepository dataRepository;

    private final DiagnosticReadinessModule diagnosticReadinessModule;

    Step03Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(),
                new EngineSpeedModule(),
                new BannerModule(),
                new VehicleInformationModule(),
                new DiagnosticReadinessModule(),
                dataRepository);
    }

    Step03Controller(Executor executor,
            EngineSpeedModule engineSpeedModule,
            BannerModule bannerModule,
            VehicleInformationModule vehicleInformationModule,
            DiagnosticReadinessModule diagnosticReadinessModule,
            DataRepository dataRepository) {
        super(executor,
                engineSpeedModule,
                bannerModule,
                vehicleInformationModule,
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
                    info.setMonitoredSystems(new HashSet<>(p.getMonitoredSystems()));
                    dataRepository.putObdModule(p.getSourceAddress(), info);
                });

        Collection<OBDModuleInformation> modules = dataRepository.getObdModules();
        if (modules.isEmpty()) {
            addFailure(1, 3, "6.1.3.2.a - There needs to be at least one OBD Module");
        } else {
            dataRepository.getVehicleInformation()
                    .getAddressClaim()
                    .getPackets()
                    .forEach(p -> {
                        OBDModuleInformation moduleInformation = dataRepository.getObdModule(p.getSourceAddress());
                        if (moduleInformation != null) {
                            moduleInformation.setFunction(p.getFunctionId());
                            dataRepository.putObdModule(p.getSourceAddress(), moduleInformation);
                        }
                    });
        }

        long distinctCount = new HashSet<>(modules).size();
        if (distinctCount > 1) {
            // All the values should be the same
            addWarning(1,
                    3,
                    "6.1.3.3.a - An ECU responded with a value for OBD Compliance that was not identical to other ECUs");
        }
    }

}
