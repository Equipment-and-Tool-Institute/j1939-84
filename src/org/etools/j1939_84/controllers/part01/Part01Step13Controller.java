/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part01;

import static org.etools.j1939tools.modules.CommunicationsModule.getCompositeSystems;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939tools.bus.RequestResult;
import org.etools.j1939tools.j1939.packets.DM5DiagnosticReadinessPacket;
import org.etools.j1939tools.j1939.packets.DiagnosticReadinessPacket;
import org.etools.j1939tools.j1939.packets.MonitoredSystem;
import org.etools.j1939tools.j1939.packets.MonitoredSystemStatus;
import org.etools.j1939tools.j1939.packets.ParsedPacket;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;

/**
 * 6.1.13 DM5: Diagnostic Readiness 1: Monitor Readiness
 */
public class Part01Step13Controller extends StepController {

    private static final int PART_NUMBER = 1;
    private static final int STEP_NUMBER = 13;
    private static final int TOTAL_STEPS = 0;

    private final SectionA6Validator sectionA6Validator;

    Part01Step13Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(),
             new EngineSpeedModule(),
             new BannerModule(),
             new VehicleInformationModule(),
             new CommunicationsModule(),
             dataRepository,
             new SectionA6Validator(dataRepository, PART_NUMBER, STEP_NUMBER),
             DateTimeModule.getInstance());
    }

    Part01Step13Controller(Executor executor,
                           EngineSpeedModule engineSpeedModule,
                           BannerModule bannerModule,
                           VehicleInformationModule vehicleInformationModule,
                           CommunicationsModule communicationsModule,
                           DataRepository dataRepository,
                           SectionA6Validator sectionA6Validator,
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
        this.sectionA6Validator = sectionA6Validator;
    }

    @Override
    protected void run() throws Throwable {

        // 6.1.13.1.a. Global DM5 (send Request (PGN 59904) for PGN 65230 (SPNs 1218-1223)).
        RequestResult<DM5DiagnosticReadinessPacket> response = getCommunicationsModule().requestDM5(getListener());
        List<DM5DiagnosticReadinessPacket> obdGlobalPackets = response.getPackets()
                                                                      .stream()
                                                                      .filter(DM5DiagnosticReadinessPacket::isObd)
                                                                      .collect(Collectors.toList());
        if (!obdGlobalPackets.isEmpty()) {
            // 6.1.13.1.b. Display monitor readiness composite value in log for OBD ECU replies only.
            getListener().onResult("");
            getListener().onResult("Vehicle Composite of DM5:");
            getListener().onResult(getCompositeSystems(obdGlobalPackets, true)
                                                                              .stream()
                                                                              .sorted()
                                                                              .map(MonitoredSystem::toString)
                                                                              .collect(Collectors.toList()));
            getListener().onResult("");
        } else {
            addFailure("6.1.13.1.a - Global DM5 request did not receive any response packets");
        }

        // 6.1.13.2.a. Fail/warn per section A.6, Criteria for Readiness 1 Evaluation
        sectionA6Validator.verify(getListener(), "6.1.13.2.a", response);

        // 6.1.13.2.b. Fail if any OBD ECU reports active DTCs count not = 0.
        obdGlobalPackets.stream()
                        .filter(p -> p.getActiveCodeCount() != (byte) 0xFF && p.getActiveCodeCount() != 0)
                        .map(ParsedPacket::getModuleName)
                        .forEach(moduleName -> {
                            addFailure("6.1.13.2.b - OBD ECU " + moduleName + " reported active DTC count not = 0");
                        });

        // 6.1.13.2.b. Fail if any OBD ECU reports previously active DTCs count not = 0.
        obdGlobalPackets.stream()
                        .filter(p -> p.getPreviouslyActiveCodeCount() != (byte) 0xFF
                                && p.getPreviouslyActiveCodeCount() != 0)
                        .map(ParsedPacket::getModuleName)
                        .forEach(moduleName -> {
                            addFailure("6.1.13.2.b - OBD ECU " + moduleName
                                    + " reported previously active DTC count not = 0");
                        });

        // 6.1.13.2.c. Fail if no OBD ECU provides DM5 with readiness bits showing monitor support.
        boolean isEnabled = obdGlobalPackets.stream()
                                            .map(DiagnosticReadinessPacket::getMonitoredSystems)
                                            .flatMap(Collection::stream)
                                            .map(MonitoredSystem::getStatus)
                                            .anyMatch(MonitoredSystemStatus::isEnabled);
        if (!isEnabled) {
            addFailure("6.1.13.2.c - No OBD ECU provided DM5 with readiness bits showing monitor support");
        }

        // 6.1.13.3.a. DS DM5 to each OBD ECU.
        var dsPackets = getDataRepository().getObdModuleAddresses()
                                           .stream()
                                           .map(a -> getCommunicationsModule().requestDM5(getListener(), a))
                                           .collect(Collectors.toList());

        // 6.1.13.4.a. Fail if any difference compared to data received during global request.
        compareRequestPackets(obdGlobalPackets, filterPackets(dsPackets), "6.1.13.4.a");

        // 6.1.13.4.b. Fail if NACK not received from OBD ECUs that did not respond to global query.
        checkForNACKsGlobal(obdGlobalPackets, filterAcks(dsPackets), "6.1.13.4.b.");
    }
}
