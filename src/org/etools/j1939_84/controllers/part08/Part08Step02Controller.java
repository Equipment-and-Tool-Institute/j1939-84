/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part08;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.controllers.QuestionListener.AnswerType.CANCEL;
import static org.etools.j1939_84.controllers.QuestionListener.AnswerType.NO;
import static org.etools.j1939_84.controllers.ResultsListener.MessageType.QUESTION;
import static org.etools.j1939tools.j1939.packets.LampStatus.ON;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.QuestionListener;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939tools.bus.BusResult;
import org.etools.j1939tools.j1939.packets.DM12MILOnEmissionDTCPacket;
import org.etools.j1939tools.j1939.packets.DiagnosticTroubleCodePacket;
import org.etools.j1939tools.j1939.packets.ParsedPacket;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;

;

/**
 * 6.8.2 DM12: Emissions Related Active DTCs
 */
public class Part08Step02Controller extends StepController {
    private static final int PART_NUMBER = 8;
    private static final int STEP_NUMBER = 2;
    private static final int TOTAL_STEPS = 0;

    Part08Step02Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule());
    }

    Part08Step02Controller(Executor executor,
                           BannerModule bannerModule,
                           DateTimeModule dateTimeModule,
                           DataRepository dataRepository,
                           EngineSpeedModule engineSpeedModule,
                           VehicleInformationModule vehicleInformationModule,
                           CommunicationsModule communicationsModule) {
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
        int attempts = 0;
        List<DM12MILOnEmissionDTCPacket> globalPackets = List.of();
        int faultBImplants = getDataRepository().getVehicleInformation().getNumberOfFaultBImplants();
        int foundDTCCount = 0;
        AtomicBoolean userCancel = new AtomicBoolean(false);
        while (foundDTCCount < faultBImplants && !userCancel.get()) {
            // 6.8.2.1.a. Global DM12 [(send Request (PGN 59904) for PGN 65236 (SPNs 1213-1215, 1706, and 3038)]).
            // 6.8.2.1.b. Repeat request no more frequently than once per second while the number of returned
            // DM12 (MIL on) DTCs is less than the total number of Fault B DTCs.

            attempts++;
            updateProgress("Step 6.8.2.1.a - Requesting DM12 Attempt " + attempts);

            getListener().onResult(NL + "Attempt " + attempts);
            globalPackets = getCommunicationsModule().requestDM12(getListener()).getPackets();

            foundDTCCount = globalPackets.stream().mapToInt(p -> p.getDtcs().size()).sum();

            if (foundDTCCount < faultBImplants && !userCancel.get()) {
                if (attempts == 5 * 60) {
                    // 6.8.2.1.b.i. Time-out after 5 minutes and ask user yes/no to continue
                    // if there are fewer active Fault B DTCs are reported than the expected
                    // total number of Fault B DTCs.
                    // 6.8.2.1.b.ii. Fail if user says “no” and fewer active Fault B DTCs are
                    // reported than the expected total number of Fault B DTCs

                    QuestionListener questionListener = answer -> {
                        if (answer == CANCEL || answer == NO) {
                            addFailure("6.8.2.1.b.ii - User says 'no' and fewer active Fault B DTCs are reported than the expected total number of Fault B DTCs.");
                            userCancel.set(true);
                        }
                    };

                    String msg = "Fewer active Fault B DTCs have been reported than the expected total number of Fault B DTCs." + NL + "Do you wish to continue?";
                    getListener().onUrgentMessage(msg,
                                                  "Fewer Than Expected Active DTCs Found",
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

        // 6.8.2.2.a. Info if any ECU reports > 1 active DTC.
        globalPackets.stream()
                     .filter(p -> p.getDtcs().size() > 1)
                     .map(ParsedPacket::getModuleName)
                     .forEach(moduleName -> {
                         addInfo("6.8.2.2.a - " + moduleName + " reported > 1 active DTC");
                     });

        // 6.8.2.2.b. Info if more than one ECU reports an active DTC.
        long modulesWithFaults = globalPackets.stream().filter(DiagnosticTroubleCodePacket::hasDTCs).count();
        if (modulesWithFaults > 1) {
            addInfo("6.8.2.2.b - More than one ECU reported an active DTC");
        }

        // 6.8.2.3.a. DS DM12 to each OBD ECU.
        var dsResults = getDataRepository().getObdModuleAddresses()
                                           .stream()
                                           .map(a -> getCommunicationsModule().requestDM12(getListener(), a))
                                           .map(BusResult::requestResult)
                                           .collect(Collectors.toList());

        List<DM12MILOnEmissionDTCPacket> dsPackets = filterRequestResultPackets(dsResults);

        // 6.8.2.4.a. Fail if any difference compared to data received with global request.
        compareRequestPackets(globalPackets, dsPackets, "6.8.2.4.a");

        // 6.8.2.4.c. Fail if NACK not received from OBD ECUs that did not respond to global query.
        checkForNACKsGlobal(globalPackets, filterRequestResultAcks(dsResults), "6.8.2.4.c");

        // 6.8.2.4.b. Fail if no ECU reports MIL on. See Section A.8 for allowed values.
        boolean isMILOn = globalPackets.stream().anyMatch(p -> p.getMalfunctionIndicatorLampStatus() == ON);
        if (!isMILOn) {
            addFailure("6.8.2.4.b - No ECU reported MIL on");
        }

        // 6.8.2.5.a Fail, if any ECU reporting any active (DM12) DTC does not also report MIL status as MIL on.
        dsPackets.stream()
                 .filter(DiagnosticTroubleCodePacket::hasDTCs)
                 .filter(p -> p.getMalfunctionIndicatorLampStatus() != ON)
                 .map(ParsedPacket::getModuleName)
                 .forEach(moduleName -> {
                     addFailure("6.8.2.5.a - " + moduleName + " reported an active DTC and did not report MIL on");
                 });

        // 6.8.2.5.b. Warn if an ECU not reporting an active DTC reports MIL on.
        dsPackets.stream()
                 .filter(p -> !p.hasDTCs())
                 .filter(p -> p.getMalfunctionIndicatorLampStatus() == ON)
                 .map(ParsedPacket::getModuleName)
                 .forEach(moduleName -> {
                     addWarning("6.8.2.5.b - " + moduleName + " did not report an active DTC and did report MIL on");
                 });
    }

}
