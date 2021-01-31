/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part1;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.modules.DiagnosticMessageModule.getCompositeSystems;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.etools.j1939_84.bus.j1939.BusResult;
import org.etools.j1939_84.bus.j1939.Lookup;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939_84.bus.j1939.packets.DM5DiagnosticReadinessPacket;
import org.etools.j1939_84.bus.j1939.packets.MonitoredSystem;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.model.RequestResult;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticMessageModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 * <p>
 * The controller for 6.1.13 DM5: Diagnostic Readiness 1: Monitor Readiness
 */
public class Part01Step13Controller extends StepController {

    private static final int PART_NUMBER = 1;
    private static final int STEP_NUMBER = 13;
    private static final int TOTAL_STEPS = 0;

    private final DataRepository dataRepository;

    private final SectionA6Validator sectionA6Validator;

    Part01Step13Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(),
             new EngineSpeedModule(),
             new BannerModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule(),
             dataRepository,
             new SectionA6Validator(dataRepository),
             DateTimeModule.getInstance());
    }

    Part01Step13Controller(Executor executor,
                           EngineSpeedModule engineSpeedModule,
                           BannerModule bannerModule,
                           VehicleInformationModule vehicleInformationModule,
                           DiagnosticMessageModule diagnosticMessageModule,
                           DataRepository dataRepository,
                           SectionA6Validator sectionA6Validator,
                           DateTimeModule dateTimeModule) {
        super(executor,
              engineSpeedModule,
              bannerModule,
              vehicleInformationModule,
              diagnosticMessageModule,
              dateTimeModule,
              PART_NUMBER,
              STEP_NUMBER,
              TOTAL_STEPS);
        this.dataRepository = dataRepository;
        this.sectionA6Validator = sectionA6Validator;
    }

    @Override
    protected void run() throws Throwable {

        // 6.1.13.1.a. Global DM5 (send Request (PGN 59904) for PGN 65230 (SPNs 1218-1223)).
        RequestResult<DM5DiagnosticReadinessPacket> response = getDiagnosticMessageModule().requestDM5(getListener());
        List<DM5DiagnosticReadinessPacket> obdGlobalPackets = response.getPackets().stream()
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
        sectionA6Validator.verify(getListener(), getPartNumber(), getStepNumber(), response);

        // 6.1.13.2.b. Fail if any OBD ECU reports active/previously active fault DTCs count not = 0/0.
        obdGlobalPackets.stream()
                .filter(p -> p.getActiveCodeCount() != (byte) 0xFF)
                .filter(p -> p.getPreviouslyActiveCodeCount() != (byte) 0xFF)
                .forEach(packet -> {
                    byte acc = packet.getActiveCodeCount();
                    byte pacc = packet.getPreviouslyActiveCodeCount();
                    if (acc != 0 || pacc != 0) {
                        String moduleName = Lookup.getAddressName(packet.getSourceAddress());
                        String msg = "";
                        msg += "6.1.13.2.b - OBD ECU " + moduleName + " reported active/previously active fault DTCs count not = 0/0" + NL;
                        msg += "  Reported active fault count = " + acc + NL;
                        msg += "  Reported previously active fault count = " + pacc;
                        addFailure(msg);
                    }
                });

        // 6.1.13.2.c. Fail if no OBD ECU provides DM5 with readiness bits showing monitor support.
        List<DM5DiagnosticReadinessPacket> dm5PacketsShowingMonitorSupport = obdGlobalPackets.stream()
                .filter(packet -> packet.getMonitoredSystems()
                        .stream()
                        .anyMatch(system -> system.getStatus().isEnabled()))
                .collect(Collectors.toList());
        if (dm5PacketsShowingMonitorSupport.isEmpty()) {
            addFailure("6.1.13.2.c - No OBD ECU provided DM5 with readiness bits showing monitor support");
        }

        // 6.1.13.2.d. Warn if any individual required monitor, except Continuous
        // Component Monitoring (CCM) is supported by more than one OBD ECU.
        // Get the list of duplicate composite systems
        reportDuplicateCompositeSystems(obdGlobalPackets,"6.1.13.2.d");

        List<Integer> obdAddresses = dataRepository.getObdModuleAddresses();
        // 6.1.13.3.a. DS DM5 to each OBD ECU.
        List<BusResult<DM5DiagnosticReadinessPacket>> destinationSpecificPackets = obdAddresses
                .stream()
                .map(address -> getDiagnosticMessageModule().requestDM5(getListener(), address))
                .collect(Collectors.toList());

        // 6.1.13.4.a. Fail if any difference compared to data received during global request.
        List<DM5DiagnosticReadinessPacket> dsDM5s = filterPackets(destinationSpecificPackets);
        compareRequestPackets(obdGlobalPackets, dsDM5s, "6.1.13.4.a");

        // 6.1.13.4.a. Fail if any difference compared to data received during global request.
        List<AcknowledgmentPacket> dsAcks = filterAcks(destinationSpecificPackets);
        checkForNACKs(obdGlobalPackets, dsAcks, dataRepository.getObdModuleAddresses(), "6.1.13.4.b.");
    }
}
