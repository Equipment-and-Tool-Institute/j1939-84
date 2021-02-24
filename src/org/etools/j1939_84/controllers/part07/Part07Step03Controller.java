/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part07;

import static org.etools.j1939_84.bus.j1939.packets.LampStatus.ALTERNATE_OFF;
import static org.etools.j1939_84.bus.j1939.packets.LampStatus.NOT_SUPPORTED;
import static org.etools.j1939_84.bus.j1939.packets.LampStatus.OFF;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.etools.j1939_84.bus.j1939.packets.DM12MILOnEmissionDTCPacket;
import org.etools.j1939_84.bus.j1939.packets.DM23PreviouslyMILOnEmissionDTCPacket;
import org.etools.j1939_84.bus.j1939.packets.DiagnosticTroubleCode;
import org.etools.j1939_84.bus.j1939.packets.LampStatus;
import org.etools.j1939_84.bus.j1939.packets.ParsedPacket;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticMessageModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * 6.7.3 DM2: Previously Active Diagnostic Trouble Codes (DTCs)
 */
public class Part07Step03Controller extends StepController {
    private static final int PART_NUMBER = 7;
    private static final int STEP_NUMBER = 3;
    private static final int TOTAL_STEPS = 0;

    Part07Step03Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part07Step03Controller(Executor executor,
                           BannerModule bannerModule,
                           DateTimeModule dateTimeModule,
                           DataRepository dataRepository,
                           EngineSpeedModule engineSpeedModule,
                           VehicleInformationModule vehicleInformationModule,
                           DiagnosticMessageModule diagnosticMessageModule) {
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
        // 6.7.3.1.a Global DM2 [(send Request (PGN 59904) for PGN 65227 (SPNs 1213-1215, 1706, and 3038)]).
        var globalPackets = getDiagnosticMessageModule().requestDM2(getListener()).getPackets();

        // 6.7.3.2.a (if supported) Fail if no OBD ECU reports any previously active DTC(s).
        boolean noDTCs = globalPackets.stream()
                                      .filter(p -> isObdModule(p.getSourceAddress()))
                                      .allMatch(p -> p.getDtcs().isEmpty());
        if (noDTCs) {
            addFailure("6.7.3.2.a - No OBD ECU reported previously active DTC(s)");
        }

        // Save the responses from each OBD Module
        globalPackets.forEach(this::save);

        // 6.7.3.2.b (if supported) Fail if any OBD ECU reports a fewer previously active DTCs than in DM23 response
        // earlier in this part.
        globalPackets.stream()
                     .filter(p -> isObdModule(p.getSourceAddress()))
                     .filter(p -> p.getDtcs()
                                   .size() < getDTCs(DM23PreviouslyMILOnEmissionDTCPacket.class,
                                                     p.getSourceAddress()).size())
                     .map(ParsedPacket::getModuleName)
                     .forEach(moduleName -> addFailure("6.7.3.2.b - " + moduleName
                             + " reported fewer previously active DTCs than in DM23 response earlier in this part"));

        // 6.7.3.2.c (if supported) Fail if any OBD ECU fails to provide its DTC from its DM12 response in part 6
        // as a previously active DTC in its DM2 response.
        globalPackets.stream()
                     .filter(p -> isObdModule(p.getSourceAddress()))
                     .forEach(p -> {
                         var dm2DTCs = p.getDtcs();
                         for (DiagnosticTroubleCode dtc : getDTCs(DM12MILOnEmissionDTCPacket.class,
                                                                  p.getSourceAddress())) {
                             if (!listContainsDTC(dm2DTCs, dtc)) {
                                 int spn = dtc.getSuspectParameterNumber();
                                 int fmi = dtc.getFailureModeIndicator();
                                 addFailure("6.7.3.2.c - " + p.getModuleName() + " DM2 response does not include SPN = "
                                         + spn + ", FMI = " + fmi + " in the previous DM12 response");
                             }
                         }
                     });

        // 6.7.3.2.d (if supported) Fail if any OBD ECU does not report MIL off. See Section A.8 for allowed values.
        globalPackets.stream()
                     .filter(p -> isObdModule(p.getSourceAddress()))
                     .filter(p -> {
                         LampStatus mil = p.getMalfunctionIndicatorLampStatus();
                         return mil != OFF && mil != ALTERNATE_OFF;
                     })
                     .map(ParsedPacket::getModuleName)
                     .forEach(moduleName -> addFailure("6.7.3.2.d - " + moduleName + " did not report MIL 'off'"));

        // 6.7.3.2.e Fail if any non-OBD ECU does not report MIL off or not supported.
        globalPackets.stream()
                     .filter(p -> !isObdModule(p.getSourceAddress()))
                     .filter(p -> {
                         LampStatus mil = p.getMalfunctionIndicatorLampStatus();
                         return mil != OFF && mil != NOT_SUPPORTED;
                     })
                     .map(ParsedPacket::getModuleName)
                     .forEach(moduleName -> addFailure("6.7.3.2.e - " + moduleName
                             + " did not report MIL off or not supported"));

        // 6.7.3.3.a DS DM2 to each OBD ECU.
        var dsResults = getDataRepository().getObdModuleAddresses()
                                           .stream()
                                           .map(a -> getDiagnosticMessageModule().requestDM2(getListener(), a))
                                           .collect(Collectors.toList());

        // 6.7.3.4.a (if supported) Fail if any difference compared to data received for global request.
        compareRequestPackets(globalPackets, filterPackets(dsResults), "6.7.3.4.a");

        // 6.7.3.4.b (if supported) Fail if NACK not received from OBD ECUs that did not respond to global query.
        checkForNACKsGlobal(globalPackets, filterAcks(dsResults), "6.7.3.4.b");
    }

}
