/*
 * Copyright (c) 2020. Electronic Tools Institute
 */
package org.etools.j1939_84.controllers.part02;

import static org.etools.j1939tools.j1939.Lookup.getAddressName;
import static org.etools.j1939tools.modules.CommunicationsModule.getCompositeSystems;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.controllers.part01.SectionA6Validator;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939tools.bus.BusResult;
import org.etools.j1939tools.bus.RequestResult;
import org.etools.j1939tools.j1939.packets.CompositeMonitoredSystem;
import org.etools.j1939tools.j1939.packets.DM5DiagnosticReadinessPacket;
import org.etools.j1939tools.j1939.packets.MonitoredSystem;
import org.etools.j1939tools.j1939.packets.ParsedPacket;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;

/**
 * 6.2.2 DM5: Diagnostic Readiness 1
 */
public class Part02Step02Controller extends StepController {
    private static final int PART_NUMBER = 2;
    private static final int STEP_NUMBER = 2;
    private static final int TOTAL_STEPS = 0;

    private final SectionA6Validator sectionA6Validator;

    Part02Step02Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(),
             new EngineSpeedModule(),
             new BannerModule(),
             new VehicleInformationModule(),
             new CommunicationsModule(),
             DateTimeModule.getInstance(),
             new SectionA6Validator(dataRepository, PART_NUMBER, STEP_NUMBER),
             dataRepository);
    }

    Part02Step02Controller(Executor executor,
                           EngineSpeedModule engineSpeedModule,
                           BannerModule bannerModule,
                           VehicleInformationModule vehicleInformationModule,
                           CommunicationsModule communicationsModule,
                           DateTimeModule dateTimeModule,
                           SectionA6Validator sectionA6Validator,
                           DataRepository dataRepository) {
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
        // 6.2.2.1.a. Global DM5 (send Request (PGN 59904) for PGN 65230 (SPNs 1218-1223)).
        RequestResult<DM5DiagnosticReadinessPacket> globalDM5Result = getCommunicationsModule().requestDM5(
                                                                                                              getListener());
        List<DM5DiagnosticReadinessPacket> globalDM5Packets = globalDM5Result.getPackets();
        List<DM5DiagnosticReadinessPacket> obdGlobalPackets = globalDM5Packets
                                                                              .stream()
                                                                              .filter(DM5DiagnosticReadinessPacket::isObd)
                                                                              .collect(Collectors.toList());
        if (!obdGlobalPackets.isEmpty()) {
            // 6.2.2.1.b. Display monitor readiness composite value in log for OBD ECU replies only.
            List<CompositeMonitoredSystem> systems = getCompositeSystems(obdGlobalPackets, true);
            getListener().onResult("");
            getListener().onResult("Vehicle Composite of DM5:");
            getListener().onResult(systems.stream()
                                          .sorted()
                                          .map(MonitoredSystem::toString)
                                          .collect(Collectors.toList()));
            getListener().onResult("");
        } else {
            addFailure("6.2.2.1.a - Global DM5 request did not receive any response packets");
        }
        // 6.2.2.2.a. Fail/warn per the section A.6 Criteria for Readiness 1 Evaluation.27
        sectionA6Validator.verify(getListener(), "6.2.2.2.a", globalDM5Result,true);

        // 6.2.2.2.b. Fail if any OBD ECU reports active DTC count not = 0.
        obdGlobalPackets.stream()
                        .filter(p -> p.getActiveCodeCount() != (byte) 0xFF && p.getActiveCodeCount() != 0)
                        .map(ParsedPacket::getModuleName)
                        .forEach(moduleName -> addFailure("6.2.2.2.b - OBD ECU " + moduleName
                                + " reported active DTC count not = 0"));

        // 6.2.2.2.b. Fail if any OBD ECU reports previously active fault DTC count not = 0.
        obdGlobalPackets.stream()
                        .filter(p -> p.getPreviouslyActiveCodeCount() != (byte) 0xFF
                                && p.getPreviouslyActiveCodeCount() != 0)
                        .map(ParsedPacket::getModuleName)
                        .forEach(moduleName -> addFailure("6.2.2.2.b - OBD ECU " + moduleName
                                + " reported previously active DTC count not = 0"));

        // 6.2.2.3.a. DS DM5 to each OBD ECU.
        List<DM5DiagnosticReadinessPacket> destinationSpecificPackets = new ArrayList<>();
        getDataRepository().getObdModuleAddresses()
                           .forEach(address -> {
                               BusResult<DM5DiagnosticReadinessPacket> busResult = getCommunicationsModule()
                                                                                                               .requestDM5(getListener(),
                                                                                                                           address);
                               busResult.getPacket()
                                        .ifPresentOrElse(packet -> {
                                            // No requirements around the destination specific acks
                                            // so, the acks are not needed
                                            packet.left.ifPresent(destinationSpecificPackets::add);
                                        },
                                                         () -> addWarning("6.2.2.3.a - OBD ECU "
                                                                 + getAddressName(address)
                                                                 + " did not return a response to a destination specific DM5 request"));
                           });

        // 6.2.2.4.a. Fail if any difference compared to data received during global request.
        for (DM5DiagnosticReadinessPacket responsePacket : globalDM5Packets) {
            DM5DiagnosticReadinessPacket dsPacketResponse = null;
            for (DM5DiagnosticReadinessPacket dsPacket : destinationSpecificPackets) {
                //FIXME - need document updated @Joe
                save(dsPacket);
                if (dsPacket.getSourceAddress() == responsePacket.getSourceAddress()) {
                    dsPacketResponse = dsPacket;
                    break;
                }
            }
            if (dsPacketResponse != null && !dsPacketResponse.equals(responsePacket)) {
                addFailure("6.2.2.4.a - Difference compared to data received during global request");
                break;
            }
        }
    }
}
