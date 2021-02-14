/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part03;

import static org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket.Response.NACK;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.etools.j1939_84.bus.j1939.BusResult;
import org.etools.j1939_84.bus.j1939.Lookup;
import org.etools.j1939_84.bus.j1939.packets.DM24SPNSupportPacket;
import org.etools.j1939_84.bus.j1939.packets.DM25ExpandedFreezeFrame;
import org.etools.j1939_84.bus.j1939.packets.FreezeFrame;
import org.etools.j1939_84.bus.j1939.packets.ParsedPacket;
import org.etools.j1939_84.bus.j1939.packets.SupportedSPN;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.FreezeFrameDataTranslator;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.controllers.TableA2ValueValidator;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticMessageModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * 6.3.13 DM25: Expanded freeze frame
 */
public class Part03Step13Controller extends StepController {

    private static final int PART_NUMBER = 3;
    private static final int STEP_NUMBER = 13;
    private static final int TOTAL_STEPS = 0;

    private final FreezeFrameDataTranslator translator;
    private final TableA2ValueValidator validator;

    Part03Step13Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule(),
             new FreezeFrameDataTranslator(),
             new TableA2ValueValidator(PART_NUMBER, STEP_NUMBER));
    }

    Part03Step13Controller(Executor executor,
                           BannerModule bannerModule,
                           DateTimeModule dateTimeModule,
                           DataRepository dataRepository,
                           EngineSpeedModule engineSpeedModule,
                           VehicleInformationModule vehicleInformationModule,
                           DiagnosticMessageModule diagnosticMessageModule,
                           FreezeFrameDataTranslator translator,
                           TableA2ValueValidator validator) {
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
        this.translator = translator;
        this.validator = validator;
    }

    @Override
    protected void run() throws Throwable {
        // 6.3.13.1.a. DS DM25 (send Request (PGN 59904) for PGN 64951 (SPNs 3300, 1214-1215)) to each OBD ECU.
        var responses = getDataRepository().getObdModuleAddresses()
                .stream()
                .map(address -> getDiagnosticMessageModule().requestDM25(getListener(), address))
                .map(BusResult::requestResult)
                .collect(Collectors.toList());

        // 6.3.13.1.b. If no response (transport protocol RTS or NACK(Busy) in 220 ms), then retry DS DM25 request to the OBD ECU.
        // [Do not attempt retry for NACKS that indicate not supported]
        var responseAddresses = filterRequestResultPackets(responses)
                .stream()
                .map(ParsedPacket::getSourceAddress)
                .collect(Collectors.toSet());

        var nackAddresses = filterRequestResultAcks(responses)
                .stream()
                .filter(r -> r.getResponse() == NACK)
                .map(ParsedPacket::getSourceAddress)
                .collect(Collectors.toSet());

        List<Integer> missingAddresses = getDataRepository().getObdModuleAddresses();
        missingAddresses.removeAll(responseAddresses);
        missingAddresses.removeAll(nackAddresses);

        missingAddresses.stream()
                .map(address -> getDiagnosticMessageModule().requestDM25(getListener(), address))
                .map(BusResult::requestResult)
                .forEach(responses::add);

        // 6.3.13.2.a. Fail if retry was required to obtain DM25 response.
        missingAddresses.stream()
                .map(Lookup::getAddressName)
                .forEach(moduleName -> addFailure("6.3.13.2.a - Retry was required to obtain DM25 response from " + moduleName));

        List<DM25ExpandedFreezeFrame> packets = filterRequestResultPackets(responses);

        // 6.3.13.1.c. Translate and print in log file all received freeze frame data with data labels assuming data
        // received in order expected by DM24 response for visual check by test log reviewer.
        for (OBDModuleInformation moduleInformation : getDataRepository().getObdModules()) {
            DM24SPNSupportPacket dm24 = moduleInformation.getDm24();
            if (dm24 == null) {
                continue;
            }

            List<SupportedSPN> supportedSPNs = dm24.getFreezeFrameSPNsInOrder();
            int moduleAddress = moduleInformation.getSourceAddress();
            String moduleName = Lookup.getAddressName(moduleAddress);

            packets.stream()
                    .filter(p -> p.getSourceAddress() == moduleAddress)
                    .findFirst()
                    .ifPresent(p -> {
                        for (FreezeFrame freezeFrame : p.getFreezeFrames()) {
                            freezeFrame.setSPNs(translator.getFreezeFrameSPNs(freezeFrame, supportedSPNs));
                            getListener().onResult("6.3.13.1.c - Received from " + moduleName + ": ");
                            getListener().onResult(freezeFrame.toString());
                            getListener().onResult("");

                            // 6.3.13.2.e. Fail/warn per section A.2, Criteria for Freeze Frame Evaluation.
                            validator.reportWarnings(freezeFrame, getListener(), "6.3.13.2.e");
                        }
                    });
        }

        // 6.3.13.2.b. Fail if no ECU has freeze frame data to report.
        boolean freezeFrameReceived = packets.stream().anyMatch(p -> !p.getFreezeFrames().isEmpty());
        if (!freezeFrameReceived) {
            addFailure("6.3.13.2.b - No ECU has freeze frame data to report");
        }

        // 6.3.13.2.c. Fail if received data does not match expected number of bytes based on DM24 supported SPN list for that ECU.
        for (DM25ExpandedFreezeFrame dm25 : packets) {
            String moduleName = dm25.getModuleName();
            int expectedLength = getDataRepository().getObdModule(dm25.getSourceAddress())
                    .getFreezeFrameSpns()
                    .stream()
                    .mapToInt(SupportedSPN::getLength)
                    .sum();
            for (FreezeFrame freezeFrame : dm25.getFreezeFrames()) {
                int actualLength = freezeFrame.getSpnData().length;
                if (expectedLength != actualLength) {
                    addFailure(
                            "6.3.13.2.c - Received data (" + actualLength + ") does not match expected number of bytes (" + expectedLength + ") based on DM24 supported SPN list for " + moduleName);
                }
            }
        }

        // 6.3.13.2.d. Fail if freeze frame data does not include the same SPN+FMI as DM6 pending DTC earlier in this part.
        for (DM25ExpandedFreezeFrame dm25 : packets) {
            var pendingDTCs = getDataRepository().getObdModule(dm25.getSourceAddress())
                    .getEmissionDTCs()
                    .stream()
                    .map(dtc -> dtc.getSuspectParameterNumber() + ":" + dtc.getFailureModeIndicator())
                    .collect(Collectors.toList());
            dm25.getFreezeFrames()
                    .stream()
                    .map(FreezeFrame::getDtc)
                    .map(dtc -> dtc.getSuspectParameterNumber() + ":" + dtc.getFailureModeIndicator())
                    .forEach(pendingDTCs::remove);

            if (!pendingDTCs.isEmpty()) {
                addFailure("6.3.13.2.d - Freeze frame data from " + dm25.getModuleName() + " does not include the same SPN+FMI as DM6 Pending DTC earlier in this part.");
            }
        }

        // 6.3.13.2.f. Warn if more than 1 freeze frame data set is included in the response.
        packets.stream()
                .filter(p -> p.getFreezeFrames().size() > 1)
                .map(ParsedPacket::getModuleName)
                .forEach(moduleName -> addWarning(
                        "6.3.13.2.e - More than 1 freeze frame data set is included in the response from " + moduleName));

        // 6.3.13.2.g. Fail if NACK not received from OBD ECUs that did not provide DM25 response to query.
        checkForNACKsFromObdModules(packets, filterRequestResultAcks(responses), "6.3.13.2.g");
    }

}
