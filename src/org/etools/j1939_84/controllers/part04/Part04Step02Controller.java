/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part04;

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
import org.etools.j1939tools.j1939.packets.DM6PendingEmissionDTCPacket;
import org.etools.j1939tools.j1939.packets.DiagnosticTroubleCode;
import org.etools.j1939tools.j1939.packets.ParsedPacket;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;

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
             new CommunicationsModule());
    }

    Part04Step02Controller(Executor executor,
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
        int faultAImplants = getDataRepository().getVehicleInformation().getNumberOfFaultAImplants();
        int foundDTCCount = -1; //send global DM12 query at least once
        AtomicBoolean userCancel = new AtomicBoolean(false);
        while (foundDTCCount < faultAImplants && !userCancel.get()) {
            // 6.4.2.1.a. Global DM12 ([send Request (PGN 59904) for PGN 65236 (SPN 1213-1215, 1706, and 3038)])
            // to retrieve confirmed and active DTCs.

            // 6.4.2.1.a.i. Repeat request no more frequently than once per second until one or more ECUs
            // reports a confirmed and active DTC.

            attempts++;
            updateProgress("Step 6.4.2.1.a - Requesting DM12 Attempt " + attempts);

            getListener().onResult(NL + "Attempt " + attempts);
            globalPackets = getCommunicationsModule().requestDM12(getListener()).getPackets();

            foundDTCCount = globalPackets.stream().mapToInt(p -> p.getDtcs().size()).sum();

            if (foundDTCCount < faultAImplants && !userCancel.get()) {
                if (attempts == 5 * 60) {
                    // 6.4.2.1.a.ii Time-out after every 5 minutes and ask user “yes/no” to continue if the
                    // number of returned DM12 (MIL on) DTCs is less than the total number of Fault A DTCs;
                    // and fail if user says “no” and fewer DM12 DTCs were reported than the expected total
                    // number of Fault A DTCs.
                    QuestionListener questionListener = answer -> {
                        if (answer == CANCEL || answer == NO) {
                            addFailure("6.4.2.1.a.ii - User said 'no' and fewer DM12 DTCs were reported than the expected total number of Fault A DTCs");
                           userCancel.set(true);
                        }
                    };

                    String msg = "Fewer confirmed and active DTCs have been reported than the expected total of Fault A DTCs." + NL + "Do you wish to continue?";
                    getListener().onUrgentMessage(msg,
                                                  "Fewer Than Expected Confirmed and Active DTCs Found",
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

        // 6.4.2.2.b Fail if all the DM6 pending DTCs observed in part 3, are not observed in the MIL on DM12 DTCs.
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

        // 6.4.2.3.b. Info if more than one ECU reports a confirmed and active DTC.
        long modulesWithFaults = globalPackets.stream()
                                              .filter(p -> !p.getDtcs().isEmpty())
                                              .count();
        if (modulesWithFaults > 1) {
            addInfo("6.4.2.3.b - More than one ECU reported a confirmed and active DTC");
        }

        // 6.4.2.4.a. DS DM12 to each OBD ECU.
        var dsResults = getDataRepository().getObdModuleAddresses()
                                           .stream()
                                           .map(a -> getCommunicationsModule().requestDM12(getListener(), a))
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
