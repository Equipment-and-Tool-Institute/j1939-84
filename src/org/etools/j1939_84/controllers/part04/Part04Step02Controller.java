/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part04;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.bus.j1939.packets.LampStatus.ON;
import static org.etools.j1939_84.controllers.ResultsListener.MessageType.QUESTION;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.etools.j1939_84.bus.j1939.BusResult;
import org.etools.j1939_84.bus.j1939.Lookup;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939_84.bus.j1939.packets.DM12MILOnEmissionDTCPacket;
import org.etools.j1939_84.bus.j1939.packets.DM6PendingEmissionDTCPacket;
import org.etools.j1939_84.bus.j1939.packets.DiagnosticTroubleCode;
import org.etools.j1939_84.bus.j1939.packets.DiagnosticTroubleCodePacket;
import org.etools.j1939_84.bus.j1939.packets.ParsedPacket;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.model.RequestResult;
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
        boolean foundDTC = false;
        while (!foundDTC) {
            // 6.4.2.1.a. Global DM12 ([send Request (PGN 59904) for PGN 65236 (SPN 1213-1215, 1706, and 3038)])
            //   to retrieve confirmed and active DTCs.

            // 6.4.2.1.a.i. Repeat request no more frequently than once per second until one or more ECUs
            //   reports a confirmed and active DTC.

            attempts++;
            updateProgress("Step 4.2. Requesting DM12 Attempt " + attempts);

            getListener().onResult(NL + "Attempt " + attempts);
            globalPackets = getDiagnosticMessageModule().requestDM12(getListener()).getPackets();

            foundDTC = globalPackets.stream()
                    .map(DiagnosticTroubleCodePacket::getDtcs)
                    .map(dtcs -> !dtcs.isEmpty())
                    .findFirst()
                    .orElse(false);

            if (!foundDTC) {
                if (attempts == 5 * 60) {
                    // 6.4.2.1.a.ii. Time-out after every 5 minutes
                    // and ask user “‘yes/no”’ to continue if there is still no confirmed and active DTC;
                    // fail if user says “'no”' and no ECU reports a confirmed and active DTC.

                    // This will throw an exception if the user chooses 'no'
                    displayInstructionAndWait("No module has reported a confirmed and active DTC." + NL +
                                                      "Do you wish to continue?",
                                              "No Confirmed and Active DTCs Found",
                                              QUESTION);
                    attempts = 0;
                } else {
                    getDateTimeModule().pauseFor(1000);
                }
            }
        }

        //Save the DTCs per module
        globalPackets.stream()
                .filter(p -> getDataRepository().isObdModule(p.getSourceAddress()))
                .filter(p -> !p.getDtcs().isEmpty())
                .forEach(p -> {
                    var moduleInfo = getDataRepository().getObdModule(p.getSourceAddress());
                    moduleInfo.set(p);
                    getDataRepository().putObdModule(moduleInfo);
                });

        // 6.4.2.2.a. Fail if no ECU reports MIL on. See Section A.8 for allowed values.
        boolean isMILOn = globalPackets.stream().anyMatch(p -> p.getMalfunctionIndicatorLampStatus() == ON);
        if (!isMILOn) {
            addFailure("6.4.2.2.a - No ECU reported MIL on");
        }

        // 6.4.2.2.b. Fail if DM12 DTC(s) is (are) not the same SPN+FMI(s) as DM6 pending DTC in part 3.
        for (OBDModuleInformation moduleInfo : getDataRepository().getObdModules()) {
            int moduleAddress = moduleInfo.getSourceAddress();
            String moduleName = Lookup.getAddressName(moduleAddress);
            var dm6 = moduleInfo.get(DM6PendingEmissionDTCPacket.class);
            var existingDTCs = toString(dm6 == null ? List.of() : dm6.getDtcs());

            List<DM12MILOnEmissionDTCPacket> packets = globalPackets.stream()
                    .filter(p -> p.getSourceAddress() == moduleAddress)
                    .collect(Collectors.toList());
            for (DM12MILOnEmissionDTCPacket packet : packets) {
                var packetDTCs = toString(packet.getDtcs());
                if (!packetDTCs.equals(existingDTCs)) {
                    addFailure("6.4.2.2.b - " + moduleName + " reported DM12 DTC(s) different than DM6 pending DTC(s) in part 3");
                    break;
                }
            }
        }

        // 6.4.2.3.a. Warn if any ECU reports > 1 confirmed and active DTC.
        globalPackets.stream()
                .filter(p -> p.getDtcs().size() > 1)
                .map(ParsedPacket::getSourceAddress)
                .map(Lookup::getAddressName)
                .forEach(moduleName -> addWarning("6.4.2.3.a - " + moduleName + " reported > 1 confirmed and active DTC"));

        // 6.4.2.3.b. Warn if more than one ECU reports a confirmed and active DTC.
        long modulesWithFaults = globalPackets.stream()
                .filter(p -> !p.getDtcs().isEmpty())
                .count();
        if (modulesWithFaults > 1) {
            addWarning("6.4.2.3.b - More than one ECU reported a confirmed and active DTC");
        }

        List<Integer> obdModuleAddresses = getDataRepository().getObdModuleAddresses();

        // 6.4.2.4.a. DS DM12 to each OBD ECU.
        List<RequestResult<DM12MILOnEmissionDTCPacket>> dsResults = obdModuleAddresses.stream()
                .map(address -> getDiagnosticMessageModule().requestDM12(getListener(), address))
                .map(BusResult::requestResult)
                .collect(Collectors.toList());

        // 6.4.2.5.a. Fail if any difference compared to data received from global request.
        List<DM12MILOnEmissionDTCPacket> dsPackets = filterRequestResultPackets(dsResults);
        compareRequestPackets(globalPackets, dsPackets, "6.4.2.5.a");

        // 6.4.2.5.b. Fail if NACK not received from OBD ECUs that did not respond to global query.
        List<AcknowledgmentPacket> dsAcks = filterRequestResultAcks(dsResults);
        checkForNACKs(globalPackets, dsAcks, obdModuleAddresses, "6.4.2.5.b");
    }

    private static String toString(List<DiagnosticTroubleCode> dtcs) {
        return dtcs.stream()
                .map(d -> d.getSuspectParameterNumber() + ":" + d.getFailureModeIndicator())
                .sorted()
                .collect(Collectors.joining(","));
    }

}