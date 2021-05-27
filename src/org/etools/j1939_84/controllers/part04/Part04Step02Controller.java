/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part04;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.bus.j1939.packets.LampStatus.ON;
import static org.etools.j1939_84.controllers.QuestionListener.AnswerType.CANCEL;
import static org.etools.j1939_84.controllers.QuestionListener.AnswerType.NO;
import static org.etools.j1939_84.controllers.ResultsListener.MessageType.QUESTION;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.etools.j1939_84.bus.j1939.BusResult;
import org.etools.j1939_84.bus.j1939.packets.DM12MILOnEmissionDTCPacket;
import org.etools.j1939_84.bus.j1939.packets.DM6PendingEmissionDTCPacket;
import org.etools.j1939_84.bus.j1939.packets.DiagnosticTroubleCode;
import org.etools.j1939_84.bus.j1939.packets.ParsedPacket;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.QuestionListener;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticMessageModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * 6.4.2 DM12: Emissions Related Active DTCs
 */
public class Part04Step02Controller extends StepController {
    private static final int PART_NUMBER = 4;
    private static final int STEP_NUMBER = 2;
    private static final int TOTAL_STEPS = 0;

    Part04Step02Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part04Step02Controller(Executor executor,
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
        int attempts = 0;
        List<DM12MILOnEmissionDTCPacket> globalPackets = List.of();
        AtomicBoolean foundDTC = new AtomicBoolean(false);
        while (!foundDTC.get()) {
            // 6.4.2.1.a. Global DM12 ([send Request (PGN 59904) for PGN 65236 (SPN 1213-1215, 1706, and 3038)])
            // to retrieve confirmed and active DTCs.

            // 6.4.2.1.a.i. Repeat request no more frequently than once per second until one or more ECUs
            // reports a confirmed and active DTC.

            attempts++;
            updateProgress("Step 6.4.2.1.a - Requesting DM12 Attempt " + attempts);

            getListener().onResult(NL + "Attempt " + attempts);
            globalPackets = getDiagnosticMessageModule().requestDM12(getListener()).getPackets();

            foundDTC.set(globalPackets.stream().anyMatch(p -> !p.getDtcs().isEmpty()));

            if (!foundDTC.get()) {
                if (attempts == 5 * 60) {
                    // 6.4.2.1.a.ii. Time-out after every 5 minutes
                    // and ask user “‘yes/no”’ to continue if there is still no confirmed and active DTC;
                    // fail if user says “'no”' and no ECU reports a confirmed and active DTC.

                    QuestionListener questionListener = answer -> {
                        if (answer == CANCEL || answer == NO) {
                            addFailure("6.4.2.1.a.ii - User said 'no' and no ECU reported a confirmed and active DTC");
                            foundDTC.set(true);
                        }
                    };

                    String msg = "No ECU has reported a confirmed and active DTC." + NL + "Do you wish to continue?";
                    getListener().onUrgentMessage(msg,
                                                  "No Confirmed and Active DTCs Found",
                                                  QUESTION,
                                                  questionListener);
                    attempts = 0;
                } else {
                    getDateTimeModule().pauseFor(1000);
                }
            }
        }

        // Save the DTCs per module
        globalPackets.forEach(this::save);

        // 6.4.2.2.a. Fail if no ECU reports MIL on. See Section A.8 for allowed values.
        boolean isMILOn = globalPackets.stream().anyMatch(p -> p.getMalfunctionIndicatorLampStatus() == ON);
        if (!isMILOn) {
            addFailure("6.4.2.2.a - No ECU reported MIL on");
        }

        // 6.4.2.2.b. Fail if DM12 DTC(s) is (are) not the same SPN+FMI(s) as DM6 pending DTC in part 3.
        globalPackets.stream()
                     .filter(p -> isObdModule(p.getSourceAddress()))
                     .filter(p -> !p.getDtcs().containsAll(getDTCs(p.getSourceAddress())))
                     .map(ParsedPacket::getModuleName)
                     .forEach(moduleName -> {
                         addFailure("6.4.2.2.b - " + moduleName
                                 + " reported DM12 DTC(s) different than DM6 pending DTC(s) in part 3");
                     });

        // 6.4.2.3.a. Warn if any ECU reports > 1 confirmed and active DTC.
        globalPackets.stream()
                     .filter(p -> p.getDtcs().size() > 1)
                     .map(ParsedPacket::getModuleName)
                     .forEach(moduleName -> {
                         addWarning("6.4.2.3.a - " + moduleName
                                 + " reported > 1 confirmed and active DTC");
                     });

        // 6.4.2.3.b. Warn if more than one ECU reports a confirmed and active DTC.
        long modulesWithFaults = globalPackets.stream()
                                              .filter(p -> !p.getDtcs().isEmpty())
                                              .count();
        if (modulesWithFaults > 1) {
            addWarning("6.4.2.3.b - More than one ECU reported a confirmed and active DTC");
        }

        // 6.4.2.4.a. DS DM12 to each OBD ECU.
        var dsResults = getDataRepository().getObdModuleAddresses()
                                           .stream()
                                           .map(a -> getDiagnosticMessageModule().requestDM12(getListener(), a))
                                           .map(BusResult::requestResult)
                                           .collect(Collectors.toList());

        // 6.4.2.5.a. Fail if any difference compared to data received from global request.
        compareRequestPackets(globalPackets, filterRequestResultPackets(dsResults), "6.4.2.5.a");

        // 6.4.2.5.b. Fail if NACK not received from OBD ECUs that did not respond to global query.
        checkForNACKsGlobal(globalPackets, filterRequestResultAcks(dsResults), "6.4.2.5.b");
    }

    private List<DiagnosticTroubleCode> getDTCs(int moduleAddress) {
        return getDTCs(DM6PendingEmissionDTCPacket.class, moduleAddress, 3);
    }

}
